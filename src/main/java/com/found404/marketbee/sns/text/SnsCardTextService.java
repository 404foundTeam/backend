package com.found404.marketbee.sns.text;

import com.found404.marketbee.sns.text.dto.SnsCardTextGenerateReq;
import com.found404.marketbee.sns.text.dto.SnsCardTextGenerateResp;

public interface SnsCardTextService {
    SnsCardTextGenerateResp generateCard(SnsCardTextGenerateReq req);
}