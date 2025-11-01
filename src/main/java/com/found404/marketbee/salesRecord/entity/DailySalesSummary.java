package com.found404.marketbee.salesRecord.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailySalesSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String storeUuid;

    @Column(nullable = false)
    private LocalDate salesDate;

    @Column(nullable = false)
    private Long netSales;

    @Column(nullable = false)
    private Integer receiptCount;

    @Builder
    public DailySalesSummary(String storeUuid, LocalDate salesDate, Long netSales, Integer receiptCount) {
        this.storeUuid = storeUuid;
        this.salesDate = salesDate;
        this.netSales = netSales;
        this.receiptCount = receiptCount;
    }
}
