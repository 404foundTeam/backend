package com.found404.marketbee.store.place;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoClient {
    private final WebClient kakaoWebClient;

    public List<Map<String, Object>> searchByCoord(String x, String y, String code, int radius, int page, int size) {
        Map<?, ?> res = kakaoWebClient.get()
                .uri(uri -> uri.path("/v2/local/search/category.json")
                        .queryParam("category_group_code", code)
                        .queryParam("x", x)
                        .queryParam("y", y)
                        .queryParam("radius", radius)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .queryParam("sort", "distance")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        return (List<Map<String, Object>>) res.get("documents");
    }
}