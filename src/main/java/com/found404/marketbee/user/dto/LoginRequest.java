package com.found404.marketbee.user.dto;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String userId;
    private String password;
}