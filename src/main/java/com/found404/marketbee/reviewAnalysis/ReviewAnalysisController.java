package com.found404.marketbee.reviewAnalysis;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReviewAnalysisController {
    private final ReviewAnalysisService reviewAnalysisService;

    @GetMapping("/{storeUuid}/keywords")
    public ResponseEntity<KeywordsDto> getKeywords(@PathVariable String storeUuid) {
        ReviewAnalysis reviewAnalysis = reviewAnalysisService.getAnalysis(storeUuid);

        if (reviewAnalysis != null) {
            return ResponseEntity.ok(KeywordsDto.from(reviewAnalysis));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
