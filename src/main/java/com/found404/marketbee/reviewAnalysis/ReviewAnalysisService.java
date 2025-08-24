package com.found404.marketbee.reviewAnalysis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.found404.marketbee.review.Review;
import com.found404.marketbee.review.ReviewRepository;
import com.found404.marketbee.sns.common.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewAnalysisService {
    private final OpenAiClient openAiClient;
    private final ReviewRepository reviewRepository;
    private final ReviewAnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper;

    @Value("${openai.chat.model}")
    private String gptModelName;

    @Transactional(readOnly = true)
    public ReviewAnalysis getAnalysis(String storeUuid) {
        return analysisRepository.findByStoreUuid(storeUuid).orElse(null);
    }

    @Transactional
    public void updateGptAnalysis(String storeUuid) {
        List<Review> reviews = reviewRepository.findByStoreUuid(storeUuid);
        if (reviews.isEmpty()) {
            log.warn("[{}] ID를 가진 가게 리뷰 데이터가 없어 GPT 분석을 건너뜁니다.", storeUuid);
            return;
        }

        String concatenatedReviews = reviews.stream()
                .map(Review::getContent)
                .collect(Collectors.joining("\n- "));

        String prompt = createPrompt(concatenatedReviews);
        try {
            Map<String, Object> gptResponse = openAiClient.chatRaw(
                    List.of(Map.of("role", "user", "content", prompt)),
                    gptModelName,
                    0.2,
                    500
            );

            List<Map<String, Object>> choices = (List<Map<String, Object>>) gptResponse.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String jsonResponse = (String) message.get("content");
            Map<String, Object> parsedContent = objectMapper.readValue(jsonResponse, new TypeReference<>() {});
            List<String> keywords = (List<String>) parsedContent.get("keywords");
            List<String> improvementTips = (List<String>) parsedContent.get("improvementTips");

            ReviewAnalysis analysis = analysisRepository.findByStoreUuid(storeUuid)
                    .orElseGet(() -> ReviewAnalysis.builder().storeUuid(storeUuid).build());
            analysis.update(
                    keywords.get(0),
                    keywords.get(1),
                    keywords.get(2),
                    improvementTips.get(0),
                    improvementTips.get(1),
                    LocalDate.now()
            );
            analysisRepository.save(analysis);
            log.info("[{}] GPT 리뷰 분석 완료 및 저장 성공.", storeUuid);

        } catch (Exception e) {
            log.error("[{}] GPT 리뷰 분석 중 오류 발생", storeUuid, e);
        }
    }

    private String createPrompt(String reviews) {
        return """
               당신은 고객 리뷰 텍스트를 기계적으로 처리하는 텍스트 마이닝 전문가입니다.
               당신의 임무는 고객 리뷰에서 **고객이 경험한 핵심 요소를 추출**하고,
               그중 **동일하거나 유사한 표현을 그룹화한 뒤**,
               가장 많이 언급된 3가지 대표 경험 문구를 선정하는 것입니다.
               또한, 분석 결과를 바탕으로 **가게의 개선 또는 유지 팁 2가지**를 제안합니다.
                
               [핵심 경험 문구 추출 규칙]
               
                1. **동일·유사 표현 그룹화**
                   - 의미가 비슷한 표현은 하나로 묶고, 대표 문구는 리뷰 원문의 자연스러운 표현을 살린 **짧은 명사구**로 작성합니다.
                   - 예:
                     - "튀김이 바삭하다", "튀김이 정말 바삭바삭해요", "바삭한 튀김" → **"바삭한 튀김"**
                     - "응대가 친절하다", "직원이 상냥하다" → **"친절한 응대"**
                
                2. **메뉴명 단독 금지**
                   - 메뉴 이름만 있는 경우는 **절대 선택하지 않습니다.** 
                   - 반드시 경험이나 특성이 결합된 형태로 재작성해야 합니다. 
                   - 예: 
                     - "물회" → **금지**
                     - "시원한 물회 육수" → **허용** 
                     - "야채볶음" → **금지** 
                     - "맛있는 야채볶음" → **허용**
                
                3. **금지어 처리**
                   - 금지어: "맛", "가성비", "퀄리티", "분위기", "서비스"
                   - 금지어가 **단독** 또는 **메뉴명과 단순 결합**된 표현은 **절대 금지**
                     - 예: "야채볶음 퀄리티", "국밥 맛" → **금지**
                   - 금지어가 포함되더라도 **리뷰의 구체적인 맥락을 살려 변환**해야 함. 꼭 리뷰 문맥을 바탕으로 적어야 함. 
                     - 예: 
                       - "국밥 맛" → **"진한 국밥 국물"**
                       - "매장 분위기" → **"차분한 분위기의 매장"** 
                       - "야채볶음 퀄리티" → **"정성이 담긴 야채볶음"**
                   
               4. **절대 규칙**
                     - 메뉴명 단독 금지
                     - 금지어 단독·단순 결합 금지
                     - 의미 없는 추상적 표현 금지
                     - 규칙을 위반할 경우 반드시 재작성 후 출력
                    
               [팁 작성 규칙]
               1. 가게 운영에 도움이 될 만한 구체적인 개선점 2개를 '각각의 완전한 문장'으로 만들어주세요.
               2. 만약 리뷰 내용에 뚜렷한 개선점이 없다면, 가장 칭찬받는 '강점' 2가지를 찾아 "이 강점을 계속 유지하고 발전시키면 좋습니다" 와 같은 형식의 '유지팁'으로 대체해주세요.
               3. 모든 문장은 반드시 "합니다" 또는 "습니다"와 같은 정중한 구어체 어조로 마무리해주세요. "~함", "~음"과 같은 개조식 말투는 절대 사용하지 마세요.
               4. 두 문장을 "또한" 이나 숫자로 연결하지 말고, 완전히 독립된 두 개의 항목으로 제공해야 합니다.
               
               [출력 형식 규칙]
               - 출력은 반드시 아래 예시와 동일한 구조의 유효한 JSON 객체여야 합니다.
               - JSON 앞뒤에 어떤 설명 텍스트도 추가하지 마세요.
               - 개선팁은 'improvementTips' 라는 키를 가진 '문자열 배열(List of Strings)' 형태여야 합니다.
               
               [출력 형식]
               - 반드시 유효한 JSON만 출력해야 합니다.
               - 구조:
                   {
                       "keywords": ["표현1", "표현2", "표현3"],
                       "improvementTips": [
                           "첫 번째 팁 문장.",
                           "두 번째 팁 문장."
                       ]
                   }
               
               --- 예시 (이 구조를 반드시 따를 것) ---
                리뷰: "- 쌀국수 국물이 미지근해서 아쉬웠어요. 고기는 많아서 좋았습니다.\\n- 직원분이 정말 친절하게 안내해주셨습니다. 다만 음식이 너무 늦게 나와요."
               {
                 "keywords": ["미지근한 국물", "넉넉한 고기 양", "친절한 직원"],
                 "improvementTips": [
                    "일부 고객이 국물 온도를 지적하니, 음식이 나갈 때의 온도를 점검하면 만족도를 높일 수 있을 것입니다.",
                    "직원들의 친절함과 넉넉한 재료가 큰 장점으로 보입니다. 이를 유지해 고객 만족도를 높이시기 바랍니다."
                 ]
               }
               --- 예시 끝 ---
               
               이제 아래 리뷰들을 분석하고 위의 모든 규칙을 준수하여 JSON 결과물을 제공해주세요.
               
               --- 리뷰 목록 ---
               - %s
               """.formatted(reviews);
    }
}
