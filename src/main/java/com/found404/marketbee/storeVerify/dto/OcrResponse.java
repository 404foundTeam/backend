package com.found404.marketbee.storeVerify.dto;

public record OcrResponse(
        String storeNumber,
        String representativeName,
        String openDate,
        String message
) {}
