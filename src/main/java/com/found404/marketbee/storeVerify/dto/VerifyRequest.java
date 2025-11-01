package com.found404.marketbee.storeVerify.dto;

public record VerifyRequest(
        String storeNumber,
        String representativeName,
        String openDate
) {}