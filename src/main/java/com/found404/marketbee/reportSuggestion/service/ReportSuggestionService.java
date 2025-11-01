package com.found404.marketbee.reportSuggestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.found404.marketbee.reportSuggestion.MarketingSuggestion;
import com.found404.marketbee.reportSuggestion.MarketingSuggestionRepository;
import com.found404.marketbee.reportSuggestion.dto.ImprovementTipResponse;
import com.found404.marketbee.reportSuggestion.dto.MarketingSuggestionResponse;
import com.found404.marketbee.reviewAnalysis.ReviewAnalysis;
import com.found404.marketbee.reviewAnalysis.ReviewAnalysisService;
import com.found404.marketbee.salesRecord.SalesService;
import com.found404.marketbee.salesRecord.entity.MonthlyStat;
import com.found404.marketbee.reportSuggestion.dto.MarketingSuggestionResponse.SuggestionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportSuggestionService {
    private final SalesService salesService;
    private final MarketingSuggestionRepository marketingSuggestionRepository;
    private final ReviewAnalysisService reviewAnalysisService;
    private final ObjectMapper objectMapper;

    public ImprovementTipResponse getCombinedImprovementTips(String storeUuid) {
        YearMonth salesLatestMonth = null;
        try {
            salesLatestMonth = salesService.findLatestMonthlyStat(storeUuid).getYearMonthAsType();
        } catch (IllegalArgumentException e) {
            log.warn("최신 월 탐색: Sales 데이터 없음.");
        }

        YearMonth reviewLatestMonth = null;
        ReviewAnalysis latestReview = reviewAnalysisService.getAnalysis(storeUuid);
        if (latestReview != null) {
            reviewLatestMonth = latestReview.getAnalysisMonth();
        }

        YearMonth finalLatestMonth = salesLatestMonth;
        if (finalLatestMonth == null || (reviewLatestMonth != null && reviewLatestMonth.isAfter(finalLatestMonth))) {
            finalLatestMonth = reviewLatestMonth;
        }

        if (finalLatestMonth == null) {
            throw new IllegalArgumentException("분석할 엑셀 및 리뷰 데이터가 모두 부족하여 개선팁을 생성할 수 없습니다.");
        }

        return getCombinedImprovementTipsByDate(storeUuid, finalLatestMonth.getYear(), finalLatestMonth.getMonthValue());
    }

    public ImprovementTipResponse getCombinedImprovementTipsByDate(String storeUuid, int year, int month) {
        YearMonth targetMonth = YearMonth.of(year, month);

        Optional<MonthlyStat> statOptional = findMonthlyStatSafely(storeUuid, targetMonth);
        Optional<ReviewAnalysis> analysisOptional = findReviewAnalysisSafely(storeUuid, targetMonth);

        List<String> salesTips = new ArrayList<>();
        if (statOptional.isPresent()) {
            salesTips.addAll(readJsonToList(statOptional.get().getImprovementTipsJson()));
        }

        List<String> reviewTips = new ArrayList<>();
        if (analysisOptional.isPresent()) {
            reviewTips.add(analysisOptional.get().getImprovementTip1());
            reviewTips.add(analysisOptional.get().getImprovementTip2());
        }

        return buildResponse(salesTips, reviewTips);
    }

    private Optional<MonthlyStat> findMonthlyStatSafely(String storeUuid, YearMonth targetMonth) {
        try {
            return Optional.of(salesService.findMonthlyStatByYearAndMonth(storeUuid, targetMonth.getYear(), targetMonth.getMonthValue()));
        } catch (IllegalArgumentException e) {
            log.warn("Sales data for {}/{} not found, skipping sales tips. Message: {}", storeUuid, targetMonth, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<ReviewAnalysis> findReviewAnalysisSafely(String storeUuid, YearMonth targetMonth) {
        try {
            return Optional.ofNullable(reviewAnalysisService.getAnalysisByDate(storeUuid, targetMonth.getYear(), targetMonth.getMonthValue()));
        } catch (Exception e) {
            log.warn("Review analysis for {}/{} not found, skipping review tips. Message: {}", storeUuid, targetMonth, e.getMessage());
            return Optional.empty();
        }
    }

    private ImprovementTipResponse buildResponse(List<String> salesTips, List<String> reviewTips) {
        List<String> combinedList = new ArrayList<>(salesTips);
        combinedList.addAll(reviewTips);

        List<String> finalList = combinedList.stream()
                .filter(tip -> tip != null && !tip.isBlank())
                .collect(Collectors.toList());

        if (finalList.isEmpty()) {
            throw new IllegalArgumentException("분석할 엑셀 및 리뷰 데이터가 모두 부족하여 개선팁을 생성할 수 없습니다.");
        }

        String combinedString = String.join("\n", finalList);
        return new ImprovementTipResponse(combinedString);
    }

    @Transactional(readOnly = true)
    public MarketingSuggestionResponse getMarketingSuggestions(String storeUuid) {
        MonthlyStat stat = salesService.findLatestMonthlyStat(storeUuid);
        List<MarketingSuggestion> suggestions = marketingSuggestionRepository.findByMonthlyStatOrderByIdAsc(stat);

        List<SuggestionDto> dtoList = suggestions.stream()
                .map(s -> new SuggestionDto(s.getId(), s.getTitle(), s.getDescription()))
                .toList();

        return new MarketingSuggestionResponse(dtoList);
    }

    @Transactional
    public void deleteMarketingSuggestion(String storeUuid, Long suggestionId) {
        MarketingSuggestion suggestionToDelete = marketingSuggestionRepository
                .findByIdAndMonthlyStat_StoreUuid(suggestionId, storeUuid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ID " + suggestionId + "에 해당하는 마케팅 제안을 찾을 수 없거나, 해당 매장의 제안이 아닙니다."
                ));

        marketingSuggestionRepository.delete(suggestionToDelete);
        log.info("Successfully deleted marketing suggestion with ID: {} for store: {}", suggestionId, storeUuid);
    }

    private List<String> readJsonToList(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to List", e);
            return new ArrayList<>();
        }
    }
}
