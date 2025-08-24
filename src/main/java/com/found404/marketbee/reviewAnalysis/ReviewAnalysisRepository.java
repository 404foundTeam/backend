package com.found404.marketbee.reviewAnalysis;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewAnalysisRepository extends JpaRepository<ReviewAnalysis, Long> {
    Optional<ReviewAnalysis> findByStoreUuid(String storeUuid);
}
