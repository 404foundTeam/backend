package com.found404.marketbee.partnership.request.dto;

import com.found404.marketbee.store.Store;

import java.math.BigDecimal;

public record SearchedStore(
        Long id,
        String placeName,
        String roadAddress,
        BigDecimal longitude,
        BigDecimal latitude
) {
    public static SearchedStore from(Store store) {
        return new SearchedStore(
                store.getId(),
                store.getPlaceName(),
                store.getRoadAddress(),
                store.getLongitude(),
                store.getLatitude()
        );
    }
}
