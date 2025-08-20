package com.found404.marketbee.sns.template.dto;

import com.found404.marketbee.sns.enums.CardRatio;
import com.found404.marketbee.sns.enums.TemplateType;

public record BackgroundResp(
        String url,
        CardRatio ratio,
        TemplateType template
) {}