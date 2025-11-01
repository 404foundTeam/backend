package com.found404.marketbee.auth;

import com.found404.marketbee.user.UserService;
import com.found404.marketbee.user.dto.AuthApiResponse;
import com.found404.marketbee.user.dto.LoginRequest;
import com.found404.marketbee.user.dto.LoginResponse;
import com.found404.marketbee.user.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public AuthApiResponse<?> signup(@RequestBody SignUpRequest request) {
        try {
            userService.signup(request);
            return AuthApiResponse.success("회원가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return AuthApiResponse.fail("회원가입에 실패했습니다.", e.getMessage());
        } catch (Exception e) {
            return AuthApiResponse.fail("회원가입에 실패했습니다.", "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    @PostMapping("/login")
    public Object login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return response;
        } catch (IllegalArgumentException e) {
            return new AuthApiResponse<>(false, "로그인에 실패했습니다.", null, e.getMessage());
        }
    }
}