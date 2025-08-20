package com.found404.marketbee.photo.analysis;

import lombok.Data;

@Data
public class AnalyzeRequestDto {
    private Long photoId;
    private String model;
    private String guideText;
}
