package com.found404.marketbee.partnership.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum Category {
    FOOD("FD6", "음식점"),
    CAFE("CE7", "카페"),
    ACCOMMODATION("AD5", "숙박"),
    PARKING("PK6", "주차장");

    private final String code;
    private final String description;

    public static Optional<Category> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(category -> category.getCode().equalsIgnoreCase(code))
                .findFirst();
    }
}