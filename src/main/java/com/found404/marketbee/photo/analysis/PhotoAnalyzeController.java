package com.found404.marketbee.photo.analysis;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/photo")
@RequiredArgsConstructor
public class PhotoAnalyzeController {

    private final PhotoAnalyzeService photoAnalyzeService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponseDto> analyze(@RequestBody AnalyzeRequestDto req) {
        String result = photoAnalyzeService.analyze(req.getPhotoId(), req.getModel(), req.getGuideText());
        return ResponseEntity.ok(new AnalyzeResponseDto(result));
    }
}
