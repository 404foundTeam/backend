package com.found404.marketbee.salesRecord.repository;

import com.found404.marketbee.salesRecord.entity.DailySalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface DailySalesSummaryRepository extends JpaRepository<DailySalesSummary, Long> {
    void deleteByStoreUuidAndSalesDate(String storeUuid, LocalDate salesDate);

    @Modifying
    @Query("DELETE FROM DailySalesSummary d WHERE d.storeUuid = :storeUuid AND d.salesDate < :cutoffDate")
    void deleteOldData(@Param("storeUuid") String storeUuid, @Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT SUM(d.netSales) FROM DailySalesSummary d WHERE d.storeUuid = :storeUuid AND d.salesDate BETWEEN :startDate AND :endDate")
    Optional<Long> sumNetSalesByPeriod(@Param("storeUuid") String storeUuid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(d.receiptCount) FROM DailySalesSummary d WHERE d.storeUuid = :storeUuid AND d.salesDate BETWEEN :startDate AND :endDate")
    Optional<Long> sumReceiptCountByPeriod(@Param("storeUuid") String storeUuid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT MAX(d.salesDate) FROM DailySalesSummary d WHERE d.storeUuid = :storeUuid AND d.salesDate >= :startDate AND d.salesDate <= :endDate")
    Optional<LocalDate> findLatestSalesDateInMonth(@Param("storeUuid") String storeUuid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT MAX(d.salesDate) FROM DailySalesSummary d WHERE d.storeUuid = :storeUuid")
    Optional<LocalDate> findLatestSalesDateByStoreUuid(@Param("storeUuid") String storeUuid);
}
