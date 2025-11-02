package com.found404.marketbee.sns.template;


import com.found404.marketbee.sns.template.cloud.S3Uploader;
import com.found404.marketbee.sns.template.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @DeleteMapping("/final/{id}")
    public ResponseEntity<Map<String, String>> deleteFinalCard(@PathVariable Long id) {
        finalCardService.delete(id);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
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
