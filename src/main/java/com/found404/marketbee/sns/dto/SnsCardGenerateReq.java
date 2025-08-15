package com.found404.marketbee.sns.dto;

import com.found404.marketbee.sns.SnsCardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnsCardGenerateReq {
    private SnsCardType type;
    private String userText;
}