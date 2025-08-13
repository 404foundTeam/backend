package com.found404.marketbee.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${kakao.base-url}")
    private String kakaoBaseUrl;

    @Value("${kakao.rest-api-key}")
    private String kakaoApiKey;

    @Bean
    public WebClient kakaoWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(kakaoBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)
                .build();
    }
}
