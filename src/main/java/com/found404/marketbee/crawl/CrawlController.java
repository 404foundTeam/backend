package com.found404.marketbee.crawl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class CrawlController {
    private final CrawlService crawlService;

    @PostMapping("/report")
    public ResponseEntity<Map<String, String>> startCrawling(@RequestBody CrawlDto crawlDto) {
        String storeUuid = crawlDto.getStoreUuid();
        String placeName = crawlDto.getPlaceName();

        if (storeUuid == null || storeUuid.trim().isEmpty() || placeName == null || placeName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "storeUuid와 placeName은 필수입니다."));
        }
        CrawlResult result = crawlService.triggerCrawling(storeUuid, placeName);

        switch (result) {
            case SUCCESS:
                return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", result.getMessage()));
            case SKIPPED:
                return ResponseEntity.ok(Map.of("status", "SKIPPED", "message", result.getMessage()));
            case FAILED:
                // 크롤링 중 오류가 발생한 경우 (서버 오류)
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "FAILED", "message", result.getMessage()));
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "UNKNOWN_ERROR", "message", "알 수 없는 오류가 발생했습니다."));
        }
    }
}