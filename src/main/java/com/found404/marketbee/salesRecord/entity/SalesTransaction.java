package com.found404.marketbee.salesRecord.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String storeUuid;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime transactionDateTime;

    @Column(nullable = false)
    private Integer customerCount;

    @Builder
    public SalesTransaction(String storeUuid, LocalDateTime transactionDateTime, Integer customerCount) {
        this.storeUuid = storeUuid;
        this.transactionDateTime = transactionDateTime;
        this.customerCount = customerCount;
    }
}
