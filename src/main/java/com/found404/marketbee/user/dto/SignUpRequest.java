package com.found404.marketbee.user.dto;

import lombok.Getter;

@Getter
public class SignUpRequest {
    private String userName;
    private String email;
    private String userId;
    private String password;
    private String placeId;
    private String placeName;
    private String roadAddress;
    private Double longitude;
    private Double latitude;
    private boolean verified;
}