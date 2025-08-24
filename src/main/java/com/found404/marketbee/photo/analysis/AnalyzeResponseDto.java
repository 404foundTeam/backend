    package com.found404.marketbee.photo.analysis;

    import lombok.*;

    import java.util.List;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public class AnalyzeResponseDto {
        private String resultId;
        private String pictureId;
        private String summary;
        private double mean;
        private double stddev;
        private double focusScore;
        private double exposureScore;
        private String guideText;

        private String quality;        // GOOD / FAIR / RETAKE
        private int qualityScore;      // 0~100
        private List<String> issues;   // 감지된 문제 요약
        private String retryPrompt;    // 재촬영 지시문(짧고 실행가능)
        private int width;             // 선택: 이미지 가로
        private int height;            // 선택: 이미지 세로
    }
