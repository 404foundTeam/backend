package com.found404.marketbee.sns;

import com.found404.marketbee.sns.dto.SnsCardGenerateReq;
import com.found404.marketbee.sns.dto.SnsCardGenerateResp;

public interface SnsCardService {
    SnsCardGenerateResp generateCard(SnsCardGenerateReq req);
}