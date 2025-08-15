package com.found404.marketbee.sns;

import com.found404.marketbee.sns.dto.SnsCardGenerateReq;
import com.found404.marketbee.sns.dto.SnsCardGenerateResp;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sns-cards")
@RequiredArgsConstructor
public class SnsCardController {

    private final SnsCardService snsCardService;

    @PostMapping("/generate")
    public SnsCardGenerateResp generateCard(@RequestBody SnsCardGenerateReq req) {
        return snsCardService.generateCard(req);
    }
}