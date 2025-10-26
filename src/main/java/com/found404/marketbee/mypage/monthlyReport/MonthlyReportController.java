package com.found404.marketbee.mypage.monthlyReport;

import com.found404.marketbee.mypage.monthlyReport.dto.YearDataDto;
import com.found404.marketbee.rating.RatingDto;
import com.found404.marketbee.rating.RatingService;
import com.found404.marketbee.reportSuggestion.dto.ImprovementTipResponse;
import com.found404.marketbee.reportSuggestion.service.ReportSuggestionService;
import com.found404.marketbee.reviewAnalysis.KeywordsDto;
import com.found404.marketbee.reviewAnalysis.ReviewAnalysis;
import com.found404.marketbee.reviewAnalysis.ReviewAnalysisService;
import com.found404.marketbee.salesRecord.SalesService;
import com.found404.marketbee.salesRecord.dto.MonthlyReceiptCountResponse;
import com.found404.marketbee.salesRecord.dto.MonthlySalesResponse;
import com.found404.marketbee.salesRecord.dto.ProductRankingResponse;
import com.found404.marketbee.salesRecord.dto.VisitorStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/monthly-report")
@RequiredArgsConstructor
public class MonthlyReportController {
    private final SalesService salesService;
    private final RatingService ratingService;
    private final ReviewAnalysisService reviewAnalysisService;
    private final ReportSuggestionService reportSuggestionService;
    private final MonthlyReportService monthlyReportService;

    @GetMapping("/{storeUuid}")
    public ResponseEntity<List<YearDataDto>> getAvailableReports(@PathVariable String storeUuid) {
        List<YearDataDto> availablePeriods = monthlyReportService.getAvailableReportPeriods(storeUuid);
        return ResponseEntity.ok(availablePeriods);
    }

    @GetMapping("/{storeUuid}/{year}/{month}/product-ranking")
    public ResponseEntity<ProductRankingResponse> getProductRankingByDate(
            @PathVariable String storeUuid, @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(salesService.getMonthlyProductSalesRanking(storeUuid, year, month));
    }

    @GetMapping("/{storeUuid}/{year}/{month}/monthly-sales")
    public ResponseEntity<MonthlySalesResponse> getMonthlySalesByDate(
            @PathVariable String storeUuid, @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(salesService.getMonthlySales(storeUuid, year, month));
    }

    @GetMapping("/{storeUuid}/{year}/{month}/receipt-count")
    public ResponseEntity<MonthlyReceiptCountResponse> getReceiptCountByDate(
            @PathVariable String storeUuid, @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(salesService.getMonthlyReceiptCount(storeUuid, year, month));
    }

    @GetMapping("/{storeUuid}/{year}/{month}/visitor-stats")
    public ResponseEntity<VisitorStatsResponse> getVisitorStatsByDate(
            @PathVariable String storeUuid, @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(salesService.getMonthlyVisitorStats(storeUuid, year, month));
    }

    @GetMapping("/{storeUuid}/{year}/{month}/rating")
    public ResponseEntity<List<RatingDto>> getRatingsByDate(@PathVariable String storeUuid, @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(ratingService.getMonthlyAverageRatingsByDate(storeUuid, year, month));
    }

    @GetMapping("/{storeUuid}/{year}/{month}/keywords")
    public ResponseEntity<KeywordsDto> getKeywordsByDate(@PathVariable String storeUuid, @PathVariable int year, @PathVariable int month) {
        ReviewAnalysis reviewAnalysis = reviewAnalysisService.getAnalysisByDate(storeUuid, year, month);

        if (reviewAnalysis != null) {
            return ResponseEntity.ok(KeywordsDto.from(reviewAnalysis));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{storeUuid}/{year}/{month}/improvement-tip")
    public ResponseEntity<?> getImprovementTipsByDate(@PathVariable String storeUuid, @PathVariable int year, @PathVariable int month) {
        try {
            ImprovementTipResponse response = reportSuggestionService.getCombinedImprovementTipsByDate(storeUuid, year, month);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = Map.of("error", "Not Found", "message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}
