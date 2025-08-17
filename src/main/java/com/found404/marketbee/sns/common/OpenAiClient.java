package com.found404.marketbee.sns.common;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiClient {
    private final RestClient openAiRestClient;

    /** 공용 Chat (텍스트 전용) */
    public Map<String, Object> chatRaw(
            List<Map<String, String>> messages,
            String model,
            Double temperature,
            Integer maxTokens
    ) {
        Map<String, Object> req = new HashMap<>();
        req.put("model", model);
        req.put("messages", messages);
        if (temperature != null) req.put("temperature", temperature);
        if (maxTokens != null) req.put("max_tokens", maxTokens);

        return openAiRestClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
