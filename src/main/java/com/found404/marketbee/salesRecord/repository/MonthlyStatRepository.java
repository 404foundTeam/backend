package com.found404.marketbee.salesRecord.repository;

import com.found404.marketbee.salesRecord.entity.MonthlyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MonthlyStatRepository extends JpaRepository<MonthlyStat, Long> {
    Optional<MonthlyStat> findByStoreUuidAndYearMonth(String storeUuid, String yearMonth);

    @Modifying
    @Query("DELETE FROM MonthlyStat m WHERE m.storeUuid = :storeUuid AND m.yearMonth < :cutoffYearMonth")
    void deleteOldData(@Param("storeUuid") String storeUuid, @Param("cutoffYearMonth") String cutoffYearMonth);
}
