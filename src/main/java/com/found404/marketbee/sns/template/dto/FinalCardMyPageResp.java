package com.found404.marketbee.sns.template.dto;

import java.util.List;

public record FinalCardMyPageResp (
    List<FinalCardMyPageReq> items,
    int page,
    int size,
    long total
) {}