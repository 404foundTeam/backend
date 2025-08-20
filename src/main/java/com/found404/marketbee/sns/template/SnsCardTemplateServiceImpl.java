package com.found404.marketbee.sns.template;

import com.found404.marketbee.sns.enums.SnsCardType;
import com.found404.marketbee.sns.template.ai.ImageAiService;
import com.found404.marketbee.sns.template.ai.TemplatePrompt;
import com.found404.marketbee.sns.template.dto.BackgroundReq;
import com.found404.marketbee.sns.template.dto.BackgroundResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SnsCardTemplateServiceImpl implements SnsCardTemplateService {

    private final ImageAiService imageAiService;

    @Override
    public BackgroundResp generateBackground(BackgroundReq req) {

        if ((req.cardType() == SnsCardType.NOTICE || req.cardType() == SnsCardType.STORE_INTRO)
                && (req.menuName() == null || req.menuName().isBlank())) {
            throw new IllegalArgumentException("공지 또는 매장 소개일 때는 menuName이 반드시 필요합니다.");
        }

        String prompt = TemplatePrompt.buildForBackground(
                req.template(),
                req.ratio(),
                req.theme(),
                req.generatedText(),
                req.storeName(),
                req.cardType(),
                req.menuName()
        );

        String apiSize;
        switch (req.ratio()) {
            case SQUARE_1_1 -> apiSize = "1024x1024";
            case RATIO_2_3   -> apiSize = "1024x1536";
            case RATIO_3_2   -> apiSize = "1536x1024";
            default -> throw new IllegalArgumentException("Unknown ratio: " + req.ratio());
        }

        String s3Url = imageAiService.generate(prompt, apiSize);

        return new BackgroundResp(s3Url, req.ratio(), req.template());
    }
}

