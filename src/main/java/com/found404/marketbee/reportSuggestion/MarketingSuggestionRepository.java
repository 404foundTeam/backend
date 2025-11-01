package com.found404.marketbee.reportSuggestion;

import com.found404.marketbee.salesRecord.entity.MonthlyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarketingSuggestionRepository extends JpaRepository<MarketingSuggestion, Long> {
    List<MarketingSuggestion> findByMonthlyStatOrderByIdAsc(MonthlyStat monthlyStat);

    @Modifying
    @Query("DELETE FROM MarketingSuggestion ms WHERE ms.monthlyStat = :monthlyStat")
    void deleteByMonthlyStat(@Param("monthlyStat") MonthlyStat monthlyStat);

    @Query("SELECT ms FROM MarketingSuggestion ms JOIN ms.monthlyStat stat WHERE ms.id = :suggestionId AND stat.storeUuid = :storeUuid")
    Optional<MarketingSuggestion> findByIdAndMonthlyStat_StoreUuid(@Param("suggestionId") Long suggestionId, @Param("storeUuid") String storeUuid);
}
