package com.found404.marketbee.salesRecord.repository;

import com.found404.marketbee.salesRecord.entity.ProductSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProductSalesRepository extends JpaRepository<ProductSales, Long> {
    void deleteByStoreUuidAndSalesDate(String storeUuid, LocalDate salesDate);

    @Modifying
    @Query("DELETE FROM ProductSales p WHERE p.storeUuid = :storeUuid AND p.salesDate < :cutoffDate")
    void deleteOldData(@Param("storeUuid") String storeUuid, @Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT p.productName as productName, SUM(p.netSales) as totalSales " +
            "FROM ProductSales p " +
            "WHERE p.storeUuid = :storeUuid AND p.salesDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.productName " +
            "ORDER BY totalSales DESC")
    List<Map<String, Object>> getProductSalesRankByPeriod(@Param("storeUuid") String storeUuid, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}