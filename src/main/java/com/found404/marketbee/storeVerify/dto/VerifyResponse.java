package com.found404.marketbee.storeVerify.dto;

public record VerifyResponse(
        boolean verified,
        String message
) {}