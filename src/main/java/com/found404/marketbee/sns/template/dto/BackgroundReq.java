package com.found404.marketbee.sns.template.dto;

import com.found404.marketbee.sns.enums.CardRatio;
import com.found404.marketbee.sns.enums.SnsCardType;
import com.found404.marketbee.sns.enums.TemplateType;
import com.found404.marketbee.sns.enums.ThemeType;

public record BackgroundReq(
        String storeUuid,
        String storeName,
        SnsCardType cardType,
        String menuName, // NOTICE, STORE_INTRO일 때 필수
        String generatedText,
        TemplateType template,
        CardRatio ratio,
        ThemeType theme
) {}