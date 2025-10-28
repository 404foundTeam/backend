package com.found404.marketbee.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> checkUserIdExists(@RequestParam String userId) {
        boolean exists = userService.checkDuplicateUserId(userId);

        Map<String, Object> response = new HashMap<>();
        if (exists) {
            response.put("available", false);
            response.put("message", "이미 존재하는 아이디입니다.");
        } else {
            response.put("available", true);
            response.put("message", "사용 가능한 아이디입니다.");
        }

        return ResponseEntity.ok(response);
    }
}
