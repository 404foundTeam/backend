package com.found404.marketbee.reviewAnalysis;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Table(name = "review_analysis",
        uniqueConstraints = @UniqueConstraint(columnNames = {"storeUuid"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_uuid", nullable = false, unique = true)
    private String storeUuid;

    private String keyword1;
    private String keyword2;
    private String keyword3;

    @Column(columnDefinition = "TEXT")
    private String improvementTip1;

    @Column(columnDefinition = "TEXT")
    private String improvementTip2;

    private LocalDate lastUpdated;

    @Builder
    public ReviewAnalysis(String storeUuid, String keyword1, String keyword2, String keyword3, String improvementTip1, String improvementTip2, LocalDate lastUpdated) {
        this.storeUuid = storeUuid;
        this.keyword1 = keyword1;
        this.keyword2 = keyword2;
        this.keyword3 = keyword3;
        this.improvementTip1 = improvementTip1;
        this.improvementTip2 = improvementTip2;
        this.lastUpdated = lastUpdated;
    }

    public void update(String keyword1, String keyword2, String keyword3, String improvementTip1, String improvementTip2, LocalDate lastUpdated) {
        this.keyword1 = keyword1;
        this.keyword2 = keyword2;
        this.keyword3 = keyword3;
        this.improvementTip1 = improvementTip1;
        this.improvementTip2 = improvementTip2;
        this.lastUpdated = lastUpdated;
    }
}
