package com.found404.marketbee.salesRecord.repository;

import com.found404.marketbee.salesRecord.entity.SalesTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalesTransactionRepository extends JpaRepository<SalesTransaction, Long> {
    void deleteByStoreUuidAndTransactionDateTimeBetween(String storeUuid, LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query("DELETE FROM SalesTransaction s WHERE s.storeUuid = :storeUuid AND s.transactionDateTime < :cutoffDateTime")
    void deleteOldData(@Param("storeUuid") String storeUuid, @Param("cutoffDateTime") LocalDateTime cutoffDateTime);

    @Query("SELECT FUNCTION('HOUR', s.transactionDateTime) as hour, SUM(s.customerCount) as totalCustomers " +
            "FROM SalesTransaction s " +
            "WHERE s.storeUuid = :storeUuid AND s.transactionDateTime BETWEEN :startDateTime AND :endDateTime " +
            "GROUP BY FUNCTION('HOUR', s.transactionDateTime) ORDER BY FUNCTION('HOUR', s.transactionDateTime) ASC")
    List<Map<String, Object>> getHourlyVisitorCount(@Param("storeUuid") String storeUuid, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT FUNCTION('DAYOFWEEK', s.transactionDateTime) as dayOfWeek, SUM(s.customerCount) as totalCustomers " +
            "FROM SalesTransaction s " +
            "WHERE s.storeUuid = :storeUuid AND s.transactionDateTime BETWEEN :startDateTime AND :endDateTime " +
            "GROUP BY dayOfWeek ORDER BY totalCustomers DESC")
    List<Map<String, Object>> getDailyVisitorRank(@Param("storeUuid") String storeUuid, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
}
