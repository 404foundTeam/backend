package com.found404.marketbee.reportSuggestion;

import com.found404.marketbee.reportSuggestion.dto.ImprovementTipResponse;
import com.found404.marketbee.reportSuggestion.dto.MarketingSuggestionResponse;
import com.found404.marketbee.reportSuggestion.service.ReportSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportSuggestionController {
    private final ReportSuggestionService reportSuggestionService;

    @GetMapping("/{storeUuid}/improvement-tip")
    public ResponseEntity<?> getImprovementTips(@PathVariable String storeUuid) {
        try {
            ImprovementTipResponse response = reportSuggestionService.getCombinedImprovementTips(storeUuid);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = Map.of(
                    "error", "Not Found",
                    "message", e.getMessage()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{storeUuid}/marketing")
    public ResponseEntity<MarketingSuggestionResponse> getMarketingSuggestions(@PathVariable String storeUuid) {
        return ResponseEntity.ok(reportSuggestionService.getMarketingSuggestions(storeUuid));
    }

    @DeleteMapping("/{storeUuid}/marketing/{suggestionId}")
    public ResponseEntity<?> deleteMarketingSuggestion(
            @PathVariable String storeUuid,
            @PathVariable Long suggestionId) {

        try {
            reportSuggestionService.deleteMarketingSuggestion(storeUuid, suggestionId);

            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = Map.of(
                    "error", "Not Found",
                    "message", e.getMessage()
            );

            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}
