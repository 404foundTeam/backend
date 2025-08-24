package com.found404.marketbee.photo.analysis;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalyzeRequestDto {
    private String pictureId; // 업로드 때 생성된 식별자
    private String fileUrl;   // 바로 경로로 분석하고 싶으면 이 값만 보내도 동작
}