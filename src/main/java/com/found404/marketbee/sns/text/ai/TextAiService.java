package com.found404.marketbee.sns.text.ai;

import com.found404.marketbee.sns.common.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class TextAiService {
    private final OpenAiClient openAi;

    public String generate(String userPrompt, double temperature) {
        var messages = List.of(
                Map.of("role","system","content","당신은 한국어 SNS 카드뉴스 전문 카피라이터입니다."),
                Map.of("role","user","content", userPrompt)
        );
        Map resp = openAi.chatRaw(messages, "gpt-4o-mini", temperature, 200);
        var choices = (List<Map>) resp.get("choices");
        return ((Map)choices.get(0).get("message")).get("content").toString();
    }
}
