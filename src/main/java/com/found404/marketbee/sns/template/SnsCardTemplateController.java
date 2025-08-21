package com.found404.marketbee.sns.template;


import com.found404.marketbee.sns.template.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sns-cards")
public class SnsCardTemplateController {

    private final SnsCardTemplateService composeService;
    private final FinalCardService finalCardService;

    @PostMapping(value = "/background", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BackgroundResp composeBackground(@RequestBody BackgroundReq req) {
        return composeService.generateBackground(req);
    }

    @PostMapping(value = "/final", consumes = MediaType.APPLICATION_JSON_VALUE)
    public FinalCardResp saveFinalCard(@RequestBody FinalCardReq req) {
        finalCardService.save(req.storeUuid(), req.finalUrl());
        return new FinalCardResp("저장 완료했습니다.");
    }

    @GetMapping("/final")
    public FinalCardMyPageResp listFinalCards(
            @RequestParam String storeUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return finalCardService.list(storeUuid, page, size);
    }

}