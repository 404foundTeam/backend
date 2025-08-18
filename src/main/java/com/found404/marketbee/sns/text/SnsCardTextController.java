package com.found404.marketbee.sns.text;

import com.found404.marketbee.sns.text.dto.SnsCardTextGenerateReq;
import com.found404.marketbee.sns.text.dto.SnsCardTextGenerateResp;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sns-cards")
@RequiredArgsConstructor
public class SnsCardTextController {

    private final SnsCardTextService snsCardTextService;

    @PostMapping("/generate")
    public SnsCardTextGenerateResp generateCard(@RequestBody SnsCardTextGenerateReq req) {
        return snsCardTextService.generateCard(req);
    }
}