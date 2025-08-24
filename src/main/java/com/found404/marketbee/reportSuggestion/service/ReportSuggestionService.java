package com.found404.marketbee.reportSuggestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.found404.marketbee.reportSuggestion.MarketingSuggestion;
import com.found404.marketbee.reportSuggestion.MarketingSuggestionRepository;
import com.found404.marketbee.reportSuggestion.dto.ImprovementTipResponse;
import com.found404.marketbee.reportSuggestion.dto.MarketingSuggestionResponse;
import com.found404.marketbee.reviewAnalysis.ReviewAnalysis;
import com.found404.marketbee.reviewAnalysis.ReviewAnalysisRepository;
import com.found404.marketbee.reportSuggestion.dto.ReviewImprovementTipDto;
import com.found404.marketbee.salesRecord.SalesService;
import com.found404.marketbee.salesRecord.entity.MonthlyStat;
import com.found404.marketbee.reportSuggestion.dto.MarketingSuggestionResponse.SuggestionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportSuggestionService {
    private final SalesService salesService;
    private final MarketingSuggestionRepository marketingSuggestionRepository;
    private final ReviewAnalysisRepository reviewAnalysisRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ImprovementTipResponse getCombinedImprovementTips(String storeUuid) {
        MonthlyStat stat = salesService.findLatestMonthlyStat(storeUuid);
        List<String> salesTips = readJsonToList(stat.getImprovementTipsJson());
        Optional<ReviewAnalysis> reviewAnalysisOptional = reviewAnalysisRepository.findByStoreUuid(storeUuid);

        List<String> reviewTips;
        if (reviewAnalysisOptional.isPresent()) {
            ReviewImprovementTipDto reviewTipsDto = ReviewImprovementTipDto.from(reviewAnalysisOptional.get());
            reviewTips = reviewTipsDto.getReviewImprovementTipDto();
        } else {
            reviewTips = List.of("리뷰 데이터가 부족하여 개선팁을 생성할 수 없습니다.");
        }

        List<String> combinedList = new ArrayList<>(salesTips);
        combinedList.addAll(reviewTips.stream()
                .filter(tip -> tip != null && !tip.isBlank())
                .toList());

        String combinedString = String.join("\n", combinedList);
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
