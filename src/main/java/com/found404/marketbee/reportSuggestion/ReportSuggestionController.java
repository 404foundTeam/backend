package com.found404.marketbee.reportSuggestion;

import com.found404.marketbee.reportSuggestion.dto.ImprovementTipResponse;
import com.found404.marketbee.reportSuggestion.dto.MarketingSuggestionResponse;
import com.found404.marketbee.reportSuggestion.service.ReportSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportSuggestionController {
    private final ReportSuggestionService reportSuggestionService;

    @GetMapping("/{storeUuid}/improvement-tip")
    public ResponseEntity<ImprovementTipResponse> getImprovementTips(@PathVariable String storeUuid) {
        return ResponseEntity.ok(reportSuggestionService.getCombinedImprovementTips(storeUuid));
    }

    @GetMapping("/{storeUuid}/marketing")
    public ResponseEntity<MarketingSuggestionResponse> getMarketingSuggestions(@PathVariable String storeUuid) {
        return ResponseEntity.ok(reportSuggestionService.getMarketingSuggestions(storeUuid));
    }

    @DeleteMapping("/{storeUuid}/marketing/{suggestionId}")
    public ResponseEntity<Void> deleteMarketingSuggestion(
            @PathVariable String storeUuid,
            @PathVariable Long suggestionId) {

        reportSuggestionService.deleteMarketingSuggestion(storeUuid, suggestionId);
        return ResponseEntity.noContent().build();
    }
}
