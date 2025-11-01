package com.found404.marketbee.reportSuggestion.dto;

import java.util.List;

public record MarketingSuggestionResponse(
        List<SuggestionDto> marketingSuggestions
) {
    public record SuggestionDto(
            Long id,
            String title,
            String description
    ) {}
}