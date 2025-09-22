package com.found404.marketbee.salesRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.found404.marketbee.reportSuggestion.service.ReportSuggestionGenerationService;
import com.found404.marketbee.salesRecord.entity.DailySalesSummary;
import com.found404.marketbee.salesRecord.entity.ProductSales;
import com.found404.marketbee.salesRecord.entity.SalesTransaction;
import com.found404.marketbee.salesRecord.dto.MonthlyReceiptCountResponse;
import com.found404.marketbee.salesRecord.dto.MonthlySalesResponse;
import com.found404.marketbee.salesRecord.dto.ProductRankingResponse;
import com.found404.marketbee.salesRecord.dto.VisitorStatsResponse;
import com.found404.marketbee.salesRecord.entity.*;
import com.found404.marketbee.salesRecord.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesService {
    private final DailySalesSummaryRepository summaryRepository;
    private final SalesTransactionRepository transactionRepository;
    private final ProductSalesRepository productRepository;
    private final MonthlyStatRepository monthlyStatRepository;
    private final ReportSuggestionGenerationService suggestionGenerationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public LocalDate parseAndSaveExcel(MultipartFile file, String storeUuid) {
        LocalDate salesDate = extractDateFromFileName(file.getOriginalFilename())
                .orElseThrow(() -> new IllegalArgumentException("파일명에 'YYYYMMDD' 형식의 유효한 날짜가 포함되어야 합니다. (예: 20250821_sales.xlsx)"));

        log.info("Starting Excel parsing for place: {}, date from filename: {}", storeUuid, salesDate);

        deleteOldData(storeUuid, salesDate);
        deleteExistingDataForDate(storeUuid, salesDate);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            parseDailySummarySheet(workbook.getSheetAt(0), storeUuid, salesDate);
            parseSalesTransactionSheet(workbook.getSheetAt(1), storeUuid, salesDate);
            parseProductSalesSheet(workbook.getSheetAt(2), storeUuid, salesDate);
            updateMonthlyStats(storeUuid, salesDate);

            LocalDate latestDataDateOverall = summaryRepository.findLatestSalesDateByStoreUuid(storeUuid)
                    .orElseThrow(() -> new IllegalStateException("데이터 저장 후 날짜를 찾을 수 없습니다: " + storeUuid));
            suggestionGenerationService.generateAndSaveSuggestions(storeUuid, latestDataDateOverall);

            return salesDate;

        } catch (Exception e) {
            log.error("Failed to process Excel file for place: " + storeUuid, e);
            throw new RuntimeException("엑셀 파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private Optional<LocalDate> extractDateFromFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return Optional.empty();
        }
        String dateString = fileName.replaceAll("[^0-9]", "");
        if (dateString.length() >= 8) {
            try {
                return Optional.of(LocalDate.parse(dateString.substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd")));
            } catch (DateTimeParseException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Row findHeaderRow(Sheet sheet, String... keywords) {
        for (Row row : sheet) {
            if (row == null) continue;
            boolean foundAll = true;
            for (String keyword : keywords) {
                boolean foundKeyword = false;
                for (Cell cell : row) {
                    if (ExcelParseUtil.getCellStringValue(cell).replaceAll("\\s+", "").contains(keyword)) {
                        foundKeyword = true;
                        break;
                    }
                }
                if (!foundKeyword) {
                    foundAll = false;
                    break;
                }
            }
            if (foundAll) return row;
        }
        return null;
    }

    private Row findFirstDataRow(Sheet sheet, Row headerRow, int... keyColumnIndices) {
        for (int i = headerRow.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            Row currentRow = sheet.getRow(i);
            if (currentRow == null) continue;

            for (int colIndex : keyColumnIndices) {
                Cell cell = currentRow.getCell(colIndex);
                if (cell != null && !ExcelParseUtil.getCellStringValue(cell).isEmpty() && ExcelParseUtil.getCellLongValue(cell) != 0) {
                    return currentRow;
                }
            }
        }
        return null;
    }

    private void parseDailySummarySheet(Sheet sheet, String storeUuid, LocalDate salesDate) {
        Row headerRow = findHeaderRow(sheet, "실매출액", "영수건수");
        if (headerRow == null) throw new IllegalArgumentException("첫 번째 시트에서 '실매출액', '영수건수' 헤더를 찾을 수 없습니다.");

        int netSalesCol = ExcelParseUtil.findColumnIndex(headerRow, "실매출액");
        int receiptCountCol = ExcelParseUtil.findColumnIndex(headerRow, "영수건수");

        Row dataRow = findFirstDataRow(sheet, headerRow, netSalesCol, receiptCountCol);
        if (dataRow == null) throw new IllegalArgumentException("첫 번째 시트에서 유효한 데이터 행을 찾을 수 없습니다.");

        Long netSales = ExcelParseUtil.getCellLongValue(dataRow.getCell(netSalesCol));
        Integer receiptCount = ExcelParseUtil.getCellIntValue(dataRow.getCell(receiptCountCol));

        saveDailySalesSummary(storeUuid, salesDate, netSales, receiptCount);
    }

    private void parseSalesTransactionSheet(Sheet sheet, String storeUuid, LocalDate salesDate) {
        Row headerRow = findHeaderRow(sheet, "최초주문시각", "객수");
        if (headerRow == null) throw new IllegalArgumentException("두 번째 시트에서 '최초주문시각', '객수' 헤더를 찾을 수 없습니다.");

        int timeCol = ExcelParseUtil.findColumnIndex(headerRow, "최초주문시각", "주문시각");
        int customerCountCol = ExcelParseUtil.findColumnIndex(headerRow, "객수");

        int startRowIndex = -1;
        for(int i = headerRow.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            Row currentRow = sheet.getRow(i);
            if(currentRow != null && !ExcelParseUtil.isSummaryOrEmptyRow(currentRow)) {
                startRowIndex = i;
                break;
            }
        }

        if (startRowIndex == -1) {
            log.warn("두 번째 시트에서 유효한 거래 데이터를 찾지 못했습니다.");
            return;
        }

        List<SalesTransaction> transactions = new ArrayList<>();
        for (int i = startRowIndex; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (ExcelParseUtil.isSummaryOrEmptyRow(row)) break;

            Optional<LocalTime> transactionTime = ExcelParseUtil.getCellTimeValue(row.getCell(timeCol));
            if (transactionTime.isPresent()) {
                transactions.add(SalesTransaction.builder()
                        .storeUuid(storeUuid)
                        .transactionDateTime(LocalDateTime.of(salesDate, transactionTime.get()))
                        .customerCount(ExcelParseUtil.getCellIntValue(row.getCell(customerCountCol)))
                        .build());
            }
        }
        transactionRepository.saveAll(transactions);
    }

    private void parseProductSalesSheet(Sheet sheet, String storeUuid, LocalDate salesDate) {
        Row headerRow = findHeaderRow(sheet, "상품명", "실매출액");
        if (headerRow == null) throw new IllegalArgumentException("세 번째 시트에서 '상품명', '실매출액' 헤더를 찾을 수 없습니다.");

        int productNameCol = ExcelParseUtil.findColumnIndex(headerRow, "상품명");
        int netSalesCol = ExcelParseUtil.findColumnIndex(headerRow, "실매출액", "실매출");

        int startRowIndex = -1;
        for(int i = headerRow.getRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
            Row currentRow = sheet.getRow(i);
            if(currentRow != null && !ExcelParseUtil.isSummaryOrEmptyRow(currentRow)) {
                startRowIndex = i;
                break;
            }
        }

        if (startRowIndex == -1) {
            log.warn("세 번째 시트에서 유효한 상품 데이터를 찾지 못했습니다.");
            return;
        }

        List<ProductSales> productSalesList = new ArrayList<>();
        for (int i = startRowIndex; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (ExcelParseUtil.isSummaryOrEmptyRow(row)) break;

            String productName = ExcelParseUtil.getCellStringValue(row.getCell(productNameCol));
            Long netSales = ExcelParseUtil.getCellLongValue(row.getCell(netSalesCol));

            if (!productName.isEmpty()) {
                productSalesList.add(ProductSales.builder()
                        .storeUuid(storeUuid)
                        .salesDate(salesDate)
                        .productName(productName)
                        .netSales(netSales)
                        .build());
            }
        }
        productRepository.saveAll(productSalesList);
    }

    private void saveDailySalesSummary(String storeUuid, LocalDate salesDate, Long netSales, Integer receiptCount) {
        summaryRepository.save(DailySalesSummary.builder()
                .storeUuid(storeUuid)
                .salesDate(salesDate)
                .netSales(netSales)
                .receiptCount(receiptCount)
                .build());
    }

    private void deleteOldData(String storeUuid, LocalDate currentDate) {
        LocalDate cutoffDate = currentDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        String cutoffYearMonth = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        log.info("Deleting data older than date: {} and yearMonth: {}", cutoffDate, cutoffYearMonth);

        summaryRepository.deleteOldData(storeUuid, cutoffDate);
        productRepository.deleteOldData(storeUuid, cutoffDate);
        transactionRepository.deleteOldData(storeUuid, cutoffDate.atStartOfDay());

        List<MonthlyStat> statsToDelete = monthlyStatRepository.findByStoreUuidAndYearMonthLessThan(storeUuid, cutoffYearMonth);

        if (statsToDelete != null && !statsToDelete.isEmpty()) {
            monthlyStatRepository.deleteAll(statsToDelete);
        }
    }

    private void deleteExistingDataForDate(String storeUuid, LocalDate date) {
        summaryRepository.deleteByStoreUuidAndSalesDate(storeUuid, date);
        productRepository.deleteByStoreUuidAndSalesDate(storeUuid, date);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        transactionRepository.deleteByStoreUuidAndTransactionDateTimeBetween(storeUuid, startOfDay, endOfDay);
    }

    @Transactional
    public void updateMonthlyStats(String storeUuid, LocalDate uploadedFileDate) {
        LocalDate startOfMonth = uploadedFileDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = uploadedFileDate.with(TemporalAdjusters.lastDayOfMonth());
        String yearMonth = uploadedFileDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        LocalDate latestDateInMonth = summaryRepository.findLatestSalesDateInMonth(storeUuid, startOfMonth, endOfMonth)
                .orElse(uploadedFileDate);

        MonthlySalesResponse salesData = calculateMonthlySales(storeUuid, latestDateInMonth);
        MonthlyReceiptCountResponse receiptData = calculateMonthlyReceiptCount(storeUuid, latestDateInMonth);
        ProductRankingResponse rankingData = calculateMonthlyProductRanking(storeUuid, latestDateInMonth);
        VisitorStatsResponse visitorData = calculateMonthlyVisitorStats(storeUuid, latestDateInMonth);
        MonthlyStat monthlyStat = monthlyStatRepository.findByStoreUuidAndYearMonth(storeUuid, yearMonth)
                .orElse(new MonthlyStat(storeUuid, yearMonth));

        try {
            monthlyStat.updateStats(
                    salesData.currentMonthSales(),
                    salesData.previousMonthSales(),
                    salesData.growthPercentage(),
                    receiptData.totalReceipts(),
                    objectMapper.writeValueAsString(rankingData),
                    objectMapper.writeValueAsString(visitorData)
            );
            monthlyStatRepository.save(monthlyStat);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize monthly stats to JSON for place: {}", storeUuid, e);
            throw new RuntimeException("월간 통계 데이터 JSON 변환 실패", e);
        }

        LocalDate startOfNextMonth = uploadedFileDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfNextMonth = startOfNextMonth.with(TemporalAdjusters.lastDayOfMonth());

        summaryRepository.findLatestSalesDateInMonth(storeUuid, startOfNextMonth, endOfNextMonth)
                .ifPresent(latestDateInNextMonth -> {
                    log.info("Data for the following month ({}) exists. Triggering stats update for it.", latestDateInNextMonth.getMonth());
                    updateMonthlyStats(storeUuid, latestDateInNextMonth);
                });
    }

    private MonthlySalesResponse calculateMonthlySales(String storeUuid, LocalDate today) {
        LocalDate startOfCurrentMonth = today.withDayOfMonth(1);
        Long currentMonthSales = summaryRepository.sumNetSalesByPeriod(storeUuid, startOfCurrentMonth, today).orElse(0L);

        LocalDate startOfPreviousMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfPreviousMonthPeriod = today.minusMonths(1);
        Long previousMonthSales = summaryRepository.sumNetSalesByPeriod(storeUuid, startOfPreviousMonth, endOfPreviousMonthPeriod).orElse(0L);

        double growthPercentage = 0.0;
        if (previousMonthSales > 0) {
            growthPercentage = ((double) (currentMonthSales - previousMonthSales) / previousMonthSales) * 100.0;
        } else if (currentMonthSales > 0) {
            growthPercentage = 100.0;
        }
        return new MonthlySalesResponse(currentMonthSales, previousMonthSales, Math.round(growthPercentage * 100.0) / 100.0);
    }

    private MonthlyReceiptCountResponse calculateMonthlyReceiptCount(String storeUuid, LocalDate today) {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        return new MonthlyReceiptCountResponse(summaryRepository.sumReceiptCountByPeriod(storeUuid, startOfMonth, today).orElse(0L));
    }

    private ProductRankingResponse calculateMonthlyProductRanking(String storeUuid, LocalDate today) {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        List<Map<String, Object>> rankingsFromDb = productRepository.getProductSalesRankByPeriod(storeUuid, startOfMonth, today);

        List<ProductRankingResponse.RankedItem> rankedItems = rankingsFromDb.stream()
                .map(r -> new ProductRankingResponse.RankedItem((String) r.get("productName"), ((Number) r.get("totalSales")).longValue()))
                .toList();

        List<ProductRankingResponse.RankedItem> sortedForTop = rankedItems.stream()
                .sorted(Comparator.comparing(ProductRankingResponse.RankedItem::totalSales).reversed()
                        .thenComparing(ProductRankingResponse.RankedItem::productName))
                .toList();

        List<ProductRankingResponse.RankedItem> top3 = sortedForTop.stream().limit(3).toList();

        List<ProductRankingResponse.RankedItem> sortedForBottom = rankedItems.stream()
                .sorted(Comparator.comparing(ProductRankingResponse.RankedItem::totalSales)
                        .thenComparing(ProductRankingResponse.RankedItem::productName))
                .toList();

        List<ProductRankingResponse.RankedItem> bottom3 = sortedForBottom.stream().limit(3).toList();

        long top3Sales = top3.stream().mapToLong(ProductRankingResponse.RankedItem::totalSales).sum();
        long totalSales = rankedItems.stream().mapToLong(ProductRankingResponse.RankedItem::totalSales).sum();
        long etcSales = totalSales - top3Sales;

        List<ProductRankingResponse.ChartItem> chartItems = new ArrayList<>();
        if (totalSales > 0) {
            for (var item : top3) {
                chartItems.add(new ProductRankingResponse.ChartItem(item.productName(), Math.round(((double) item.totalSales() / totalSales) * 10000.0) / 100.0));
            }
            chartItems.add(new ProductRankingResponse.ChartItem("기타", Math.round(((double) etcSales / totalSales) * 10000.0) / 100.0));
        }

        return new ProductRankingResponse(top3, bottom3, chartItems);
    }

    private VisitorStatsResponse calculateMonthlyVisitorStats(String storeUuid, LocalDate today) {
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.atTime(LocalTime.MAX);

        List<Map<String, Object>> hourlyResult = transactionRepository.getHourlyVisitorCount(storeUuid, startOfMonth, endOfMonth);
        Map<Integer, Long> hourlyDistribution = IntStream.range(0, 24).boxed().collect(Collectors.toMap(h -> h, h -> 0L));
        hourlyResult.forEach(r -> hourlyDistribution.put(((Number) r.get("hour")).intValue(), ((Number) r.get("totalCustomers")).longValue()));

        List<VisitorStatsResponse.HourlyStat> allHourlyStats = hourlyDistribution.entrySet().stream()
                .map(e -> new VisitorStatsResponse.HourlyStat(e.getKey(), e.getValue()))
                .toList();

        List<VisitorStatsResponse.HourlyStat> mostVisited = allHourlyStats.stream()
                .sorted(Comparator.comparingLong(VisitorStatsResponse.HourlyStat::totalCustomers).reversed()
                        .thenComparingInt(VisitorStatsResponse.HourlyStat::hour))
                .limit(2)
                .toList();

        List<VisitorStatsResponse.HourlyStat> leastVisited = allHourlyStats.stream()
                .filter(stat -> stat.totalCustomers() > 0)
                .sorted(Comparator.comparingLong(VisitorStatsResponse.HourlyStat::totalCustomers)
                        .thenComparingInt(VisitorStatsResponse.HourlyStat::hour))
                .limit(2)
                .toList();

        List<Map<String, Object>> dailyResultFromDb = transactionRepository.getDailyVisitorRank(storeUuid, startOfMonth, endOfMonth);
        Map<Integer, String> dayOfWeekMap = Map.of(1, "일", 2, "월", 3, "화", 4, "수", 5, "목", 6, "금", 7, "토");

        List<String> dayOrder = List.of("월", "화", "수", "목", "금", "토", "일");

        List<VisitorStatsResponse.DailyRank> dailyRank = dailyResultFromDb.stream()
                .map(r -> new VisitorStatsResponse.DailyRank(
                        dayOfWeekMap.get(((Number) r.get("dayOfWeek")).intValue()),
                        ((Number) r.get("totalCustomers")).longValue()
                ))
                .sorted(Comparator.comparingLong(VisitorStatsResponse.DailyRank::totalCustomers).reversed()
                        .thenComparing(Comparator.comparingInt(rank -> dayOrder.indexOf(rank.dayOfWeek()))))
                .toList();

        return new VisitorStatsResponse(mostVisited, leastVisited, dailyRank);
    }

    @Transactional(readOnly = true)
    public ProductRankingResponse getMonthlyProductSalesRanking(String storeUuid) {
        MonthlyStat stat = findLatestMonthlyStat(storeUuid);
        try {
            return objectMapper.readValue(stat.getProductRankingJson(), ProductRankingResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize ProductRanking for place: {}", storeUuid, e);
            throw new RuntimeException("상품 랭킹 데이터 변환 실패", e);
        }
    }

    @Transactional(readOnly = true)
    public MonthlySalesResponse getMonthlySales(String storeUuid) {
        MonthlyStat stat = findLatestMonthlyStat(storeUuid);
        return new MonthlySalesResponse(stat.getCurrentMonthSales(), stat.getPreviousMonthSales(), stat.getGrowthPercentage());
    }

    @Transactional(readOnly = true)
    public MonthlyReceiptCountResponse getMonthlyReceiptCount(String storeUuid) {
        MonthlyStat stat = findLatestMonthlyStat(storeUuid);
        return new MonthlyReceiptCountResponse(stat.getTotalReceipts());
    }

    @Transactional(readOnly = true)
    public VisitorStatsResponse getMonthlyVisitorStats(String storeUuid) {
        MonthlyStat stat = findLatestMonthlyStat(storeUuid);
        try {
            return objectMapper.readValue(stat.getVisitorStatsJson(), VisitorStatsResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize VisitorStats for place: {}", storeUuid, e);
            throw new RuntimeException("방문객 통계 데이터 변환 실패", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public MonthlyStat findLatestMonthlyStat(String storeUuid) {
        LocalDate latestDate = summaryRepository.findLatestSalesDateByStoreUuid(storeUuid)
                .orElseThrow(() -> new IllegalArgumentException(storeUuid + "에 대한 매출 데이터가 존재하지 않습니다."));

        String yearMonth = latestDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        return monthlyStatRepository.findByStoreUuidAndYearMonth(storeUuid, yearMonth)
                .orElseThrow(() -> new IllegalArgumentException(storeUuid + "의 " + yearMonth + "월 통계 데이터가 존재하지 않습니다."));
    }
}