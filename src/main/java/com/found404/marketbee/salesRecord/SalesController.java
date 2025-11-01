package com.found404.marketbee.salesRecord;

import com.found404.marketbee.salesRecord.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class SalesController {
    private final SalesService salesService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadSalesExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("storeUuid") String storeUuid) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new UploadResponse("업로드할 파일이 비어있습니다.", storeUuid, null));
        }

        try {
            LocalDate processedDate = salesService.parseAndSaveExcel(file, storeUuid);

            return ResponseEntity.ok(new UploadResponse("파일이 성공적으로 처리되었습니다.", storeUuid, processedDate.toString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new UploadResponse("파일 처리 중 오류 발생: " + e.getMessage(), storeUuid, null));
        }
    }

    @GetMapping("/{storeUuid}/product-ranking")
    public ResponseEntity<ProductRankingResponse> getProductRanking(@PathVariable String storeUuid) {
        return ResponseEntity.ok(salesService.getMonthlyProductSalesRanking(storeUuid));
    }

    @GetMapping("/{storeUuid}/monthly-sales")
    public ResponseEntity<MonthlySalesResponse> getMonthlySales(@PathVariable String storeUuid) {
        return ResponseEntity.ok(salesService.getMonthlySales(storeUuid));
    }

    @GetMapping("/{storeUuid}/receipt-count")
    public ResponseEntity<MonthlyReceiptCountResponse> getReceiptCount(@PathVariable String storeUuid) {
        return ResponseEntity.ok(salesService.getMonthlyReceiptCount(storeUuid));
    }

    @GetMapping("/{storeUuid}/visitor-stats")
    public ResponseEntity<VisitorStatsResponse> getVisitorStats(@PathVariable String storeUuid) {
        return ResponseEntity.ok(salesService.getMonthlyVisitorStats(storeUuid));
    }
}
