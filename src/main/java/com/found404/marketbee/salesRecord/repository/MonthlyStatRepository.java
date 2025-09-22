package com.found404.marketbee.salesRecord.repository;

import com.found404.marketbee.salesRecord.entity.MonthlyStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyStatRepository extends JpaRepository<MonthlyStat, Long> {
    Optional<MonthlyStat> findByStoreUuidAndYearMonth(String storeUuid, String yearMonth);

    List<MonthlyStat> findByStoreUuidAndYearMonthLessThan(String storeUuid, String cutoffYearMonth);
}
