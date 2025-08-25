package com.found404.marketbee.store.dto;

public record StoreCreateResp(
        String storeUuid,
        String storeName,
        String roadAddress,
        boolean isNew
) {}

