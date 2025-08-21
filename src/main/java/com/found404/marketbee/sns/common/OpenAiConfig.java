package com.found404.marketbee.sns.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;


@Configuration
public class OpenAiConfig {

    @Bean
    RestClient openAiRestClient(@Value("${openai.api.key}") String apiKey) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30_000); // 연결 타임아웃 (30초)
        factory.setReadTimeout(120_000);    // 응답 읽기 타임아웃 (120초)

        return RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(factory)
                .build();
    }
}
