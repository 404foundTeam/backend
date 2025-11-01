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
public class ProductSales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String storeUuid;

    @Column(nullable = false)
    private LocalDate salesDate;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Long netSales;

    @Builder
    public ProductSales(String storeUuid, LocalDate salesDate, String productName, Long netSales) {
        this.storeUuid = storeUuid;
        this.salesDate = salesDate;
        this.productName = productName;
        this.netSales = netSales;
    }
}
