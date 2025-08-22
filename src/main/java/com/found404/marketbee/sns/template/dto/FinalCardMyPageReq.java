package com.found404.marketbee.sns.template.dto;

import java.time.LocalDateTime;

public record FinalCardMyPageReq (
    Long id,
    String imageUrl,
    LocalDateTime createdAt
) {}

