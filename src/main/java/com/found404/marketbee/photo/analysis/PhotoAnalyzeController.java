package com.found404.marketbee.photo.analysis;

import com.found404.marketbee.photo.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/photo")
public class PhotoAnalyzeController {

    private final PhotoAnalyzeService analyzeService;
    private final GuideTextService guideTextService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponseDto> analyze(@RequestBody AnalyzeRequestDto req) throws Exception {
        return ResponseEntity.ok(analyzeService.analyze(req));
    }

    @PostMapping(value = "/guide-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GuideTextResp> guideFile(@RequestPart("file") MultipartFile file) throws Exception {
        String text = guideTextService.fromFile(file);
        return ResponseEntity.ok(GuideTextResp.builder().guideText(text).build());
    }

    @PostMapping("/guide-b64")
    public ResponseEntity<GuideTextResp> guideB64(@RequestBody DataUrlReq req) throws Exception {
        String text = guideTextService.fromDataUrl(req.getDataUrl());
        return ResponseEntity.ok(GuideTextResp.builder().guideText(text).build());
    }
}
