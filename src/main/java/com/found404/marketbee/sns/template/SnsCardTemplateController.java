package com.found404.marketbee.sns.template;


import com.found404.marketbee.sns.enums.CardRatio;
import com.found404.marketbee.sns.enums.TemplateType;
import com.found404.marketbee.sns.template.cloud.S3Uploader;
import com.found404.marketbee.sns.template.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sns-cards")
public class SnsCardTemplateController {

    private final SnsCardTemplateService composeService;
    private final FinalCardService finalCardService;
    private final S3Uploader s3Uploader;

//    @PostMapping(value = "/background", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public BackgroundResp composeBackground(@RequestBody BackgroundReq req) {
//        return composeService.generateBackground(req);
//    }

    @PostMapping(value = "/background2", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BackgroundResp composeBackground(@RequestBody BackgroundReq req) {
        return new BackgroundResp(
                "https://marketbee-assets.s3.ap-northeast-2.amazonaws.com/backgrounds/2025-08-21/img_1755702528515.png",
                CardRatio.SQUARE_1_1,
                TemplateType.T1_TEXT_ONLY,
                1
        );
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

    @PostMapping("/final/presigned-url")
    public Map<String, String> createPresignedUrl(@RequestParam String storeUuid) {
        URL uploadUrl = s3Uploader.generatePresignedPutUrl("final-cards/" + storeUuid);

        String fileUrl = uploadUrl.toString().split("\\?")[0];

        return Map.of(
                "uploadUrl", uploadUrl.toString(),
                "fileUrl", fileUrl
        );
    }
}
