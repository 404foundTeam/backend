package com.found404.marketbee.sns.template;

import com.found404.marketbee.sns.template.dto.BackgroundResp;
import com.found404.marketbee.sns.template.dto.BackgroundReq;

public interface SnsCardTemplateService {
    BackgroundResp generateBackground(BackgroundReq req);
}