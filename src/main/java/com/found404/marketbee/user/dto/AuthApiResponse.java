package com.found404.marketbee.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    private final String reason;

    public static <T> AuthApiResponse<T> success(String message, T data) {
        return new AuthApiResponse<>(true, message, data, null);
    }

    public static AuthApiResponse<Void> success(String message) {
        return new AuthApiResponse<>(true, message, null, null);
    }

    public static AuthApiResponse<Void> fail(String message, String reason) {
        return new AuthApiResponse<>(false, message, null, reason);
    }
}