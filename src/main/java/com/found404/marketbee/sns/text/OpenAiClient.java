package com.found404.marketbee.sns.text;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final RestClient openAiRestClient;

    @Value("${openai.chat.model:gpt-4o-mini}")
    private String model;

    public String chat(String userPrompt, double temperature) {
        var request = new ChatRequest(
                model,
                List.of(
                        new Message("system", "당신은 한국어 SNS 카드뉴스 전문 카피라이터입니다."),
                        new Message("user", userPrompt)
                ),
                Math.max(0.0, Math.min(1.2, temperature)),
                120
        );

        try {
            var response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new RuntimeException("OpenAI error: " + res.getStatusCode());
                    })
                    .body(ChatResponse.class);

            if (response == null || response.choices == null || response.choices.isEmpty()
                    || response.choices.get(0).message == null) {
                throw new RuntimeException("OpenAI empty response");
            }
            return response.choices.get(0).message.content;
        } catch (RestClientResponseException e) {
            throw new RuntimeException("OpenAI HTTP " + e.getRawStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI call failed", e);
        }
    }

    /* 내부 전용 DTO */

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class ChatRequest {
        public String model;
        public List<Message> messages;
        public Double temperature;
        public Integer max_tokens;

        ChatRequest(String model, List<Message> messages, Double temperature, Integer maxTokens) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
            this.max_tokens = maxTokens;
        }
    }

    @Data
    static class Message {
        public String role;
        public String content;
        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    static class ChatResponse {
        public List<Choice> choices;
    }

    @Data
    static class Choice {
        public Message message;
    }
}