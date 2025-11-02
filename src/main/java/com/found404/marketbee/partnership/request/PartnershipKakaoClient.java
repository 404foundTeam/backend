package com.found404.marketbee.partnership.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PartnershipKakaoClient {
    private final WebClient kakaoWebClient;

    public List<Map<String, Object>> searchByKeyword(String keyword, String longitude, String latitude, String category) {
        Map<?, ?> response = kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v2/local/search/keyword.json")
                        .queryParam("query", keyword)
                        .queryParam("x", longitude)
                        .queryParam("y", latitude)
                        .queryParam("size", 15)
                        .queryParam("sort", "distance")
                        .queryParamIfPresent("category_group_code", Optional.ofNullable(category))
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return Optional.ofNullable(response)
                .map(res -> (List<Map<String, Object>>) res.get("documents"))
                .orElse(Collections.emptyList());
    }

    public List<Map<String, Object>> searchByCategory(String categoryGroupCode, String longitude, String latitude, int radius) {
        Map<?, ?> response = kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v2/local/search/category.json")
                        .queryParam("category_group_code", categoryGroupCode)
                        .queryParam("x", longitude)
                        .queryParam("y", latitude)
                        .queryParam("radius", radius)
                        .queryParam("size", 15)
                        .queryParam("sort", "distance")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return Optional.ofNullable(response)
                .map(res -> (List<Map<String, Object>>) res.get("documents"))
                .orElse(Collections.emptyList());
    }
}