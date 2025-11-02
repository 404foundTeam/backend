package com.found404.marketbee.partnership.request.dto;

import com.found404.marketbee.store.Store;

import java.math.BigDecimal;

public record UserStoreInfoDto(
        BigDecimal longitude,
        BigDecimal latitude
) {
    public static UserStoreInfoDto from(Store store) {
        if (store == null) {
            return null;
        }
        return new UserStoreInfoDto(
                store.getLongitude(),
                store.getLatitude()
        );
    }
}