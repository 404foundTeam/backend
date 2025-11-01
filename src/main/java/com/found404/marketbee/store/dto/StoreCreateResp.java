package com.found404.marketbee.store.dto;

public record StoreCreateResp(
        String storeUuid,
        String placeName,
        String roadAddress,
        boolean isNew
) {}

