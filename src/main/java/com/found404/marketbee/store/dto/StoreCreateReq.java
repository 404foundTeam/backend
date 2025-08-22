package com.found404.marketbee.store.dto;

import java.math.BigDecimal;

public record StoreCreateReq(
        String placeId,
        String placeName,
        String roadAddress,
        BigDecimal longitude,
        BigDecimal latitude
) {}