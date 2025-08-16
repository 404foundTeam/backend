package com.found404.marketbee.sns.text;

import com.found404.marketbee.sns.text.dto.SnsCardTextGenerateReq;
import com.found404.marketbee.sns.text.dto.SnsCardTextGenerateResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SnsCardTextTextServiceImpl implements SnsCardTextService {

    private final OpenAiClient openAiClient;

    @Override
    public SnsCardTextGenerateResp generateCard(SnsCardTextGenerateReq req) {
        String style = switch (req.getType()) {
            case NOTICE -> "공지";
            case PRODUCT_PROMO -> "신제품 홍보";
            case STORE_INTRO -> "매장 소개";
        };

        String prompt = """
                당신은 매장 SNS 카드뉴스 카피라이터입니다.
                - 톤: 간결하고 임팩트 있게, 이모지는 과하지 않게 1~2개
                - 길이: 1~2문장 (한글 60자 내외)
                - 해시태그는 넣지 마세요
                - 과장된 표현은 피하고 구체적 장점을 강조
                - 선택한 카드뉴스 유형에 맞는 문장
                - 이미지 안에 넣을 문장이니까 최대한 간결하게
              
                카드뉴스 유형: %s
                핵심 내용: %s

                위 조건에 맞춘 최종 문구만 출력하세요.
                """.formatted(style, req.getUserText());

        double temp = switch (req.getType()) {
            case NOTICE        -> 0.7;
            case PRODUCT_PROMO -> 0.85;
            case STORE_INTRO   -> 0.6;
        };

        String generated = openAiClient.chat(prompt, temp);
        return new SnsCardTextGenerateResp(generated.trim());
    }
}