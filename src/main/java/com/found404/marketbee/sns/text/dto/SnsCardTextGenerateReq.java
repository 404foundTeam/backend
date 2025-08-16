package com.found404.marketbee.sns.text.dto;

import com.found404.marketbee.sns.text.SnsCardType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnsCardTextGenerateReq {
    private SnsCardType type;
    private String userText;
}