package com.found404.marketbee.reviewAnalysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Optional;

public interface ReviewAnalysisRepository extends JpaRepository<ReviewAnalysis, Long> {
    Optional<ReviewAnalysis> findByStoreUuidAndAnalysisMonth(String storeUuid, YearMonth analysisMonth);
    Optional<ReviewAnalysis> findTopByStoreUuidOrderByAnalysisMonthDesc(String storeUuid);
}
