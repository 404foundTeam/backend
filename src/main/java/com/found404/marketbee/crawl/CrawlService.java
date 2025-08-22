package com.found404.marketbee.crawl;

import com.found404.marketbee.rating.Rating;
import com.found404.marketbee.rating.RatingRepository;
import com.found404.marketbee.review.ReviewRepository;
import com.found404.marketbee.reviewAnalysis.ReviewAnalysisService;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CrawlService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlService.class);
    private final CrawlStatusRepository crawlStatusRepository;
    private final ReviewRepository reviewRepository;
    private final RatingRepository ratingRepository;
    private final CrawlService self;
    private final EntityManager em;
    private final ReviewAnalysisService reviewAnalysisService;

    @Autowired
    public CrawlService(CrawlStatusRepository crawlStatusRepository, ReviewRepository reviewRepository,
                        RatingRepository ratingRepository, @Lazy CrawlService self, EntityManager em, ReviewAnalysisService reviewAnalysisService) {
        this.crawlStatusRepository = crawlStatusRepository;
        this.reviewRepository = reviewRepository;
        this.ratingRepository = ratingRepository;
        this.self = self;
        this.em = em;
        this.reviewAnalysisService = reviewAnalysisService;
    }

    public CrawlResult triggerCrawling(String placeName) {
        YearMonth targetMonth = YearMonth.from(LocalDate.now().minusMonths(1));
        Optional<CrawlStatus> status = crawlStatusRepository.findByPlaceName(placeName);

        if (status.isPresent() && status.get().getLastCrawled() != null && status.get().getLastCrawled().equals(targetMonth)) {
            logger.info("[{}] 은(는) 이미 최신 데이터를 가지고 있습니다.", placeName);
            return CrawlResult.SKIPPED;
        }

        boolean isSuccess = executeCrawlingScript(placeName);
        if (isSuccess) {
            try {
                self.updateCrawlStatus(placeName, targetMonth);
                self.deleteOldReviews(placeName);
                self.updateMonthlyAverageRatings(placeName);
                self.updateGptAnalysis(placeName);
                return CrawlResult.SUCCESS;
            } catch (Exception e) {
                logger.error("[{}] 크롤링 후처리 작업 중 심각한 오류 발생", placeName, e);
                return CrawlResult.FAILED;
            }
        } else {
            logger.error("[{}] 크롤링 프로세스가 실패했습니다.", placeName);
            return CrawlResult.FAILED;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateCrawlStatus(String placeName, YearMonth targetMonth) {
        CrawlStatus status = crawlStatusRepository.findByPlaceName(placeName)
                .orElse(new CrawlStatus(placeName, null));
        status.updateLastCrawled(targetMonth);
        crawlStatusRepository.save(status);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteOldReviews(String placeName) {
        LocalDate cutOffDate = LocalDate.now().withDayOfMonth(1).minusMonths(6);
        long deletedCount = reviewRepository.deleteByPlaceNameAndReviewDateBefore(placeName, cutOffDate);
        logger.info(">>>>>> [2단계] 오래된 리뷰 {}건 삭제.", deletedCount);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMonthlyAverageRatings(String placeName) {
        ratingRepository.deleteByPlaceName(placeName);
        em.flush();
        em.clear();

        List<Object[]> rawStats = reviewRepository.findMonthlyAverageRatingsByPlaceName(placeName);
        if (rawStats.isEmpty()) {
            logger.warn(">>>>>> [3단계] 리뷰 데이터가 없어 통계를 생성하지 않습니다.");
            return;
        }

        List<Rating> newStats = rawStats.stream().map(row -> {
            YearMonth month = YearMonth.parse((String) row[0], DateTimeFormatter.ofPattern("yyyy-MM"));
            Number avgRatingNumber = (Number) row[1];
            BigDecimal avgRatingBigDecimal = BigDecimal.valueOf(avgRatingNumber.doubleValue()).setScale(2, RoundingMode.HALF_UP);
            return Rating.builder().placeName(placeName).ratingMonth(month).averageRating(avgRatingBigDecimal).build();
        }).collect(Collectors.toList());
        ratingRepository.saveAll(newStats);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateGptAnalysis(String placeName) {
        reviewAnalysisService.updateGptAnalysis(placeName);
    }

    private boolean executeCrawlingScript(String placeName) {
        logger.info("[START] Crawling process for: {}", placeName);
        try {
            String projectRootPath = System.getProperty("user.dir");
            String scriptPath = projectRootPath + File.separator + "src" + File.separator + "main" + File.separator + "python" + File.separator + "ReviewCrawler.py";
            String pythonExecutable = projectRootPath + File.separator + "src" + File.separator + "main" + File.separator
                    + "python" + File.separator + ".venv" + File.separator + "Scripts" + File.separator + "python.exe";
            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, scriptPath, placeName);
            processBuilder.environment().put("PYTHONIOENCODING", "UTF-8");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug("[Python] {}", line);
                }
            }
            boolean finished = process.waitFor(20, TimeUnit.MINUTES);
            if (finished && process.exitValue() == 0) {
                logger.info("[{}] 크롤링 및 분석 작업이 성공적으로 완료되었습니다.", placeName);
                return true;
            } else {
                logger.error("[{}] 크롤링 작업 실패 또는 타임아웃. Exit code: {}", placeName, finished ? process.exitValue() : "Timeout");
                return false;
            }
        } catch (Exception e) {
            logger.error("[{}] 크롤링 스크립트 실행 중 예외 발생", placeName, e);
            return false;
        }
    }
}