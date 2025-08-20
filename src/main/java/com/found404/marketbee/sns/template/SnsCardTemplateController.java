package com.found404.marketbee.sns.template;


import com.found404.marketbee.sns.template.dto.BackgroundResp;
import com.found404.marketbee.sns.template.dto.BackgroundReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sns-cards")
public class SnsCardTemplateController {

    private final SnsCardTemplateService composeService;

    @PostMapping(value = "/background", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BackgroundResp composeBackground(@RequestBody BackgroundReq req) {
        return composeService.generateBackground(req);
    }
}