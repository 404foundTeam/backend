package com.found404.marketbee.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String placeName;
    private String storeUuid;
    private String roadAddress;
}