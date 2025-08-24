package com.found404.marketbee.photo.analysis;

import com.found404.marketbee.photo.upload.Photo;
import com.found404.marketbee.photo.upload.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PhotoAnalyzeService {

    private final PhotoRepository photoRepository;

    public AnalyzeResponseDto analyze(AnalyzeRequestDto req) throws Exception {
        String path = resolvePath(req);
        BufferedImage img = loadImage(path);
        if (img == null) throw new IllegalArgumentException("이미지 로딩 실패: " + path);

        int w = img.getWidth(), h = img.getHeight();

        Stats stats = computeStats(img);
        double focus = estimateFocus(img);
        double exposure = estimateExposure(stats.mean, stats.stddev);

        QualityResult q = assessQuality(img, stats.mean, stats.stddev, focus, exposure);
        String summary = makeSummary(stats.mean, stats.stddev, focus, exposure);
        String guide   = buildGuide(stats.mean, stats.stddev, focus, exposure);

        return AnalyzeResponseDto.builder()
                .resultId(UUID.randomUUID().toString())
                .pictureId(req.getPictureId())
                .summary(summary)
                .mean(stats.mean)
                .stddev(stats.stddev)
                .focusScore(focus)
                .exposureScore(exposure)
                .guideText(guide)
                .quality(q.label)
                .qualityScore(q.score)
                .issues(q.issues)
                .retryPrompt(q.retryPrompt)
                .width(w)
                .height(h)
                .build();
    }

    private BufferedImage loadImage(String src) throws Exception {
        if (src.startsWith("http://") || src.startsWith("https://")) return ImageIO.read(new URL(src));
        return ImageIO.read(new File(src));
    }

    private String resolvePath(AnalyzeRequestDto req) {
        if (req.getFileUrl() != null && !req.getFileUrl().isBlank()) return toFsPathOrUrl(req.getFileUrl());
        if (req.getPictureId() == null || req.getPictureId().isBlank())
            throw new IllegalArgumentException("pictureId 또는 fileUrl 중 하나는 필요합니다.");
        Optional<Photo> opt = photoRepository.findByPictureId(req.getPictureId());
        Photo photo = opt.orElseThrow(() -> new IllegalArgumentException("pictureId를 찾을 수 없습니다: " + req.getPictureId()));
        String fileUrl = photo.getFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) throw new IllegalArgumentException("해당 pictureId에 파일 경로가 비어있습니다.");
        return toFsPathOrUrl(fileUrl);
    }

    private String toFsPathOrUrl(String input) {
        String s = input.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) return s;
        if (s.startsWith("file:")) return Paths.get(URI.create(s)).toString();
        if (s.startsWith("/uploads/")) return Paths.get(System.getProperty("user.dir"), s.substring(1)).toString();
        if (!s.startsWith("/") && !s.matches("^[A-Za-z]:\\\\.*")) return Paths.get(System.getProperty("user.dir"), s).toString();
        return s;
    }

    private static class Stats {
        final double mean, stddev;
        Stats(double m, double s) { this.mean = m; this.stddev = s; }
    }

    private Stats computeStats(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        long sum = 0, sumSq = 0;
        int[] rgb = new int[w*h];
        img.getRGB(0,0,w,h,rgb,0,w);
        for (int p : rgb) {
            int r=(p>>16)&0xff, g=(p>>8)&0xff, b=p&0xff;
            int y = (int)Math.round(0.2126*r + 0.7152*g + 0.0722*b);
            sum += y; sumSq += (long) y * y;
        }
        double count = (double) w * h;
        double mean = sum / count;
        double variance = (sumSq / count) - (mean * mean);
        double stddev = Math.sqrt(Math.max(variance, 0));
        return new Stats(mean, stddev);
    }

    private double estimateFocus(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        double acc = 0; int cnt = 0;
        for (int y=1; y<h-1; y++) for (int x=1; x<w-1; x++) {
            int c = luminance(img.getRGB(x,y));
            int left = luminance(img.getRGB(x-1,y));
            int right = luminance(img.getRGB(x+1,y));
            int up = luminance(img.getRGB(x,y-1));
            int down = luminance(img.getRGB(x,y+1));
            int lap = (4*c) - left - right - up - down;
            acc += (double) lap * lap;
            cnt++;
        }
        return Math.sqrt(acc / Math.max(cnt,1));
    }

    private int luminance(int p) {
        int r=(p>>16)&0xff, g=(p>>8)&0xff, b=p&0xff;
        return (int)Math.round(0.2126*r + 0.7152*g + 0.0722*b);
    }

    private double estimateExposure(double mean, double stddev) {
        double centerScore = Math.max(0, 1.0 - (Math.abs(mean - 128.0)/128.0));
        double contrastScore = Math.min(1.0, stddev / 64.0);
        return (0.7 * centerScore) + (0.3 * contrastScore);
    }

    private String makeSummary(double mean, double stddev, double focus, double exposure) {
        String sharp = focus >= 20 ? "선명함" : (focus >= 12 ? "보통" : "다소 흐림");
        String expo  = exposure >= 0.75 ? "적정 노출" : (exposure >= 0.45 ? "약간 치우침" : "노출 불균형");
        return String.format("선명도: %s, 노출: %s (평균 %.1f, 표준편차 %.1f)", sharp, expo, mean, stddev);
    }

    private String buildGuide(double mean, double stddev, double focus, double exposure) {
        StringBuilder sb = new StringBuilder();
        if (focus < 12) sb.append("사진이 흐릿합니다. 빛을 충분히 확보하고 단단히 고정해 촬영하세요.\n");
        if (exposure < 0.45) {
            if (mean < 100) sb.append("전체가 어둡습니다. 조명을 더 켜거나 창가에서 촬영하세요.\n");
            else if (mean > 160) sb.append("과노출입니다. 직사광선을 피하고 노출을 낮추세요.\n");
            else sb.append("대비가 낮습니다. 배경/각도를 조정해 분리를 확보하세요.\n");
        }
        if (stddev < 25) sb.append("밝기 대비가 낮습니다. 단색 배경을 사용해 피사체를 강조하세요.\n");
        if (sb.length()==0) sb.append("촬영 상태 양호합니다. 같은 구도로 2~3장 더 촬영해 베스트 컷을 선택하세요.\n");
        return sb.toString().trim();
    }

    private static class QualityResult {
        final String label;
        final int score;
        final List<String> issues;
        final String retryPrompt;
        QualityResult(String l, int s, List<String> i, String r) { label = l; score = s; issues = i; retryPrompt = r; }
    }

    private Map<String, Double> extremeRatios(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int total = Math.max(0, (w-2)*(h-2));
        int lows = 0, highs = 0;
        for (int y=1; y<h-1; y++) for (int x=1; x<w-1; x++) {
            int yv = luminance(img.getRGB(x,y));
            if (yv <= 10) lows++; else if (yv >= 245) highs++;
        }
        double lowRatio = total > 0 ? (double)lows/total : 0.0;
        double highRatio = total > 0 ? (double)highs/total : 0.0;
        return Map.of("low", lowRatio, "high", highRatio);
    }

    private QualityResult assessQuality(BufferedImage img, double mean, double stddev, double focus, double exposure) {
        int w = img.getWidth(), h = img.getHeight();
        int minDim = Math.min(w, h);
        Map<String, Double> ex = extremeRatios(img);
        double lowR = ex.get("low");
        double highR = ex.get("high");

        List<String> issues = new ArrayList<>();
        int score = 100;

        if (focus < 8) { issues.add("사진이 매우 흐림"); score -= 55; }
        else if (focus < 12) { issues.add("사진이 다소 흐림"); score -= 35; }

        if (exposure < 0.45) { issues.add("노출 불균형"); score -= 25; }
        if (mean < 95) { issues.add("전체적으로 어두움"); score -= 15; }
        if (mean > 165) { issues.add("전체적으로 너무 밝음"); score -= 15; }

        if (stddev < 22) { issues.add("대비 낮음"); score -= 15; }

        if (lowR > 0.25) { issues.add("어두운 영역 손실(클리핑)"); score -= 15; }
        if (highR > 0.25) { issues.add("빛 번짐/하이라이트 손실"); score -= 15; }

        if (minDim < 600) { issues.add("해상도/프레임이 너무 작음"); score -= 25; }

        score = Math.max(0, Math.min(100, score));
        String label = score >= 80 ? "GOOD" : (score >= 60 ? "FAIR" : "RETAKE");
        String retry = buildRetryPrompt(label, issues);
        return new QualityResult(label, score, issues, retry);
    }

    private String buildRetryPrompt(String label, List<String> issues) {
        StringBuilder sb = new StringBuilder();
        if ("RETAKE".equals(label)) sb.append("재촬영이 필요해요.\n");
        else if ("FAIR".equals(label)) sb.append("촬영은 가능하지만 한 번 더 찍으면 더 좋아요.\n");

        if (issues.stream().anyMatch(s -> s.contains("어두움")))
            sb.append("• 조명을 더 켜거나 창가로 이동하세요.\n");
        if (issues.stream().anyMatch(s -> s.contains("너무 밝음")) || issues.stream().anyMatch(s -> s.contains("손실")))
            sb.append("• 직사광선을 피하고 노출을 한 단계 낮춰 찍으세요.\n");
        if (issues.stream().anyMatch(s -> s.contains("흐림")))
            sb.append("• 휴대폰을 테이블/삼각대에 고정하고 연속으로 3장 찍으세요.\n");
        if (issues.stream().anyMatch(s -> s.contains("대비 낮음")))
            sb.append("• 배경은 단색으로, 피사체와 50~70cm 거리에서 촬영하세요.\n");
        if (issues.stream().anyMatch(s -> s.contains("작음")))
            sb.append("• 피사체가 화면의 60~70%를 차지하도록 더 가까이 찍으세요.\n");

        if (sb.length() == 0) sb.append("• 같은 구도에서 2~3장 더 촬영해 가장 선명한 사진을 선택하세요.\n");
        return sb.toString().trim();
    }
}
