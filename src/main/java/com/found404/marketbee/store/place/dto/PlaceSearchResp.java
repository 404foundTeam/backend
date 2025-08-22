package com.found404.marketbee.store.place.dto;

import java.math.BigDecimal;
import java.util.List;

public record PlaceSearchResp(
        List<Item> items
) {
    public record Item(
            String placeId,
            String placeName,
            String roadAddress,
            BigDecimal longitude,
            BigDecimal latitude
    ) {}
}
