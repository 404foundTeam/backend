package com.found404.marketbee.photo.analysis;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/photo")
public class PhotoAnalyzeController {

    private final PhotoAnalyzeService analyzeService;

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponseDto> analyze(@RequestBody AnalyzeRequestDto req) throws Exception {
        return ResponseEntity.ok(analyzeService.analyze(req));
    }
}
