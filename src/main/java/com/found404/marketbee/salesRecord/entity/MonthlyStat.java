package com.found404.marketbee.salesRecord.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "monthly_stat")
public class MonthlyStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String storeUuid;

    @Column(name = "stat_year_month", nullable = false)
    private String yearMonth;

    private Long currentMonthSales;
    private Long previousMonthSales;
    private Double growthPercentage;
    private Long totalReceipts;

    @Column(columnDefinition = "TEXT")
    private String productRankingJson;

    @Column(columnDefinition = "TEXT")
    private String visitorStatsJson;

    @Column(columnDefinition = "TEXT")
    private String improvementTipsJson;

    @Builder
    public MonthlyStat(String storeUuid, String yearMonth) {
        this.storeUuid = storeUuid;
        this.yearMonth = yearMonth;
    }

    public void updateStats(Long currentMonthSales, Long previousMonthSales, Double growthPercentage, Long totalReceipts, String productRankingJson, String visitorStatsJson) {
        this.currentMonthSales = currentMonthSales;
        this.previousMonthSales = previousMonthSales;
        this.growthPercentage = growthPercentage;
        this.totalReceipts = totalReceipts;
        this.productRankingJson = productRankingJson;
        this.visitorStatsJson = visitorStatsJson;
    }

    public void updateImprovementTips(String improvementTipsJson) {
        this.improvementTipsJson = improvementTipsJson;
    }
}
