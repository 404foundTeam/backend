package com.found404.marketbee.reportSuggestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.found404.marketbee.reportSuggestion.MarketingSuggestion;
import com.found404.marketbee.reportSuggestion.MarketingSuggestionRepository;
import com.found404.marketbee.salesRecord.dto.ProductRankingResponse;
import com.found404.marketbee.salesRecord.dto.VisitorStatsResponse;
import com.found404.marketbee.salesRecord.entity.MonthlyStat;
import com.found404.marketbee.salesRecord.repository.MonthlyStatRepository;
import com.found404.marketbee.sns.common.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportSuggestionGenerationService {
    private final MonthlyStatRepository monthlyStatRepository;
    private final MarketingSuggestionRepository marketingSuggestionRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.chat.model}")
    private String gptModelName;

    @Transactional
    public void generateAndSaveSuggestions(String storeUuid, LocalDate date) {
        String yearMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        MonthlyStat monthlyStat = monthlyStatRepository.findByStoreUuidAndYearMonth(storeUuid, yearMonth)
                .orElseThrow(() -> new IllegalStateException(storeUuid + "의 " + yearMonth + "월 통계 데이터가 없습니다."));

        try {
            ProductRankingResponse rankingData = objectMapper.readValue(monthlyStat.getProductRankingJson(), new TypeReference<>() {});
            VisitorStatsResponse visitorData = objectMapper.readValue(monthlyStat.getVisitorStatsJson(), new TypeReference<>() {});

            String prompt = createCombinedPrompt(rankingData, visitorData);
            Map<String, Object> gptResponse = openAiClient.chatRaw(
                    List.of(Map.of("role", "user", "content", prompt)), gptModelName, 0.7, 1000);

            Map<String, Object> parsedSuggestions = parseCombinedGptResponse(gptResponse);

            List<String> improvementTips = getListFromParsedSuggestions(parsedSuggestions, "improvementTips", String.class);
            String tipsJson = objectMapper.writeValueAsString(improvementTips);
            monthlyStat.updateImprovementTips(tipsJson);
            monthlyStatRepository.save(monthlyStat);

            List<Map<String, String>> marketingSuggestionsData = getListOfMapsFromParsedSuggestions(parsedSuggestions, "marketingSuggestions");

            marketingSuggestionRepository.deleteByMonthlyStat(monthlyStat);
            List<MarketingSuggestion> suggestionsToSave = marketingSuggestionsData.stream()
                    .map(suggestionMap -> MarketingSuggestion.builder()
                            .monthlyStat(monthlyStat)
                            .title(suggestionMap.get("title"))
                            .description(suggestionMap.get("description"))
                            .build())
                    .toList();
            marketingSuggestionRepository.saveAll(suggestionsToSave);

            log.info("Successfully generated and saved suggestions for {} in {}", storeUuid, yearMonth);

        } catch (Exception e) {
            log.error("Failed to generate and save suggestions for place: {}. This operation will be skipped.", storeUuid, e);
        }
    }

    private <T> List<T> getListFromParsedSuggestions(Map<String, Object> parsedSuggestions, String key, Class<T> elementType) {
        Object value = parsedSuggestions.get(key);
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .filter(elementType::isInstance)
                    .map(elementType::cast)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<Map<String, String>> getListOfMapsFromParsedSuggestions(Map<String, Object> parsedSuggestions, String key) {
        Object value = parsedSuggestions.get(key);
        if (value instanceof List) {
            List<Map<String, String>> resultList = new ArrayList<>();
            for (Object item : (List<?>) value) {
                if (item instanceof Map) {
                    resultList.add((Map<String, String>) item);
                }
            }
            return resultList;
        }
        return new ArrayList<>();
    }

    private String createCombinedPrompt(ProductRankingResponse ranking, VisitorStatsResponse visitors) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("당신은 매장을 운영하는 사장님을 위한 유능한 비즈니스 컨설턴트입니다.\n");
        promptBuilder.append("아래의 월간 판매 데이터를 분석해서, 사장님이 바로 실행할 수 있는 제안들을 해주세요.\n\n");

        promptBuilder.append("### 매장의 강점 (Strength) ###\n");
        promptBuilder.append("# 인기 메뉴: ");
        promptBuilder.append(ranking.top3().stream()
                .map(ProductRankingResponse.RankedItem::productName)
                .collect(Collectors.joining(", ")));
        promptBuilder.append("\n# 손님 많은 요일: ");
        promptBuilder.append(visitors.dailyVisitorRank().stream().limit(2)
                .map(r -> r.dayOfWeek() + "요일")
                .collect(Collectors.joining(", ")));
        promptBuilder.append("\n# 손님 많은 시간대: ");
        promptBuilder.append(visitors.mostVisitedHours().stream()
                .map(s -> String.valueOf(s.hour()) + "시")
                .collect(Collectors.joining(", ")));

        promptBuilder.append("\n\n### 매장의 약점 (Weakness) ###\n");
        promptBuilder.append("# 비인기 메뉴: ");
        promptBuilder.append(ranking.bottom3().stream()
                .map(ProductRankingResponse.RankedItem::productName)
                .collect(Collectors.joining(", ")));
        promptBuilder.append("\n# 손님 적은 요일: ");
        promptBuilder.append(visitors.dailyVisitorRank().stream()
                .sorted(Comparator.comparingLong(VisitorStatsResponse.DailyRank::totalCustomers))
                .limit(2)
                .map(r -> r.dayOfWeek() + "요일")
                .collect(Collectors.joining(", ")));
        promptBuilder.append("\n# 손님 적은 시간대: ");
        promptBuilder.append(visitors.leastVisitedHours().stream()
                .map(s -> String.valueOf(s.hour()) + "시")
                .collect(Collectors.joining(", ")));

        promptBuilder.append("\n### 지시사항 ###\n");
        promptBuilder.append("1. 위 데이터를 바탕으로 현실적인 조언을 해주세요.\n");
        promptBuilder.append("2. 답변은 반드시 JSON 형식이어야 하며, 두 개의 키를 가져야 합니다: 'improvementTips'와 'marketingSuggestions'.\n");
        promptBuilder.append("3. 'improvementTips'의 값은, 위 데이터를 종합하여 매장 운영에 대한 핵심 개선팁 2개를 담은 JSON 문자열 배열**이어야 합니다. 각 팁은 반드시 **적당한 길이의 한 문장으로** 생성해주세요.\n");
        promptBuilder.append("4. 2개의 개선팁은 형식적인 답이 아닌 위 데이터의 내용(메뉴명, 시간대, 요일)을 100% 활용하여, 답변에 직간접적으로 사용해야 합니다.\n");
        promptBuilder.append("5. 'marketingSuggestions' 키의 값은 **JSON 객체들의 배열**이어야 하며, 정확히 8개의 창의적이고 실현 가능한 마케팅 제안을 포함해야 합니다.\n");
        promptBuilder.append("6. 8개의 마케팅 제안은 **강점 활용 전략 최소 2개**와 **약점 보완 전략 최소 2개**를 반드시 골고루 포함해야 합니다.\n");
        promptBuilder.append("7. 각 마케팅 제안 객체는 'title'(짧은 제목)과 'description'(구체적인 설명) 두 개의 키를 가져야 합니다.\n");
        promptBuilder.append("8. 예시: {\"improvementTips\": [\"팁1 요약.\", \"팁2 요약.\"], \"marketingSuggestions\": [{\"title\": \"제목1\", \"description\": \"설명1\"}, ..., {\"title\": \"제목8\", \"description\": \"설명8\"}]}");

        return promptBuilder.toString();
    }

    private Map<String, Object> parseCombinedGptResponse(Map<String, Object> gptResponse) throws JsonProcessingException {
        if (gptResponse == null || !gptResponse.containsKey("choices") || ((List<?>) gptResponse.get("choices")).isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> choice = ((List<Map<String, Object>>) gptResponse.get("choices")).get(0);
        Map<String, String> message = (Map<String, String>) choice.get("message");
        String content = message.get("content");

        if (content == null || content.isBlank()) {
            return Collections.emptyMap();
        }

        String cleanedJson = content.trim();

        if (cleanedJson.startsWith("```json")) {
            cleanedJson = cleanedJson.substring(7);
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
            }
        }

        if (cleanedJson.startsWith("```")) {
            cleanedJson = cleanedJson.substring(3);
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
            }
        }
        return objectMapper.readValue(cleanedJson, new TypeReference<>() {});
    }
}
