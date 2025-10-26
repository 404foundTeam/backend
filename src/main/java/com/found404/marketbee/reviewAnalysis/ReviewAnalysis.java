package com.found404.marketbee.reviewAnalysis;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Entity
@Getter
@Table(name = "review_analysis",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_analysis_store_month",
                columnNames = {"store_uuid", "analysis_month"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_uuid", nullable = false)
    private String storeUuid;

    @Column(nullable = false)
    private YearMonth analysisMonth;

    private String keyword1;
    private String keyword2;
    private String keyword3;

    @Column(columnDefinition = "TEXT")
    private String improvementTip1;

    @Column(columnDefinition = "TEXT")
    private String improvementTip2;

    @Builder
    public ReviewAnalysis(String storeUuid, YearMonth analysisMonth, String keyword1, String keyword2, String keyword3, String improvementTip1, String improvementTip2) {
        this.storeUuid = storeUuid;
        this.analysisMonth = analysisMonth;
        this.keyword1 = keyword1;
        this.keyword2 = keyword2;
        this.keyword3 = keyword3;
        this.improvementTip1 = improvementTip1;
        this.improvementTip2 = improvementTip2;
    }

    public void update(YearMonth analysisMonth, String keyword1, String keyword2, String keyword3, String improvementTip1, String improvementTip2) {
        this.analysisMonth = analysisMonth;
        this.keyword1 = keyword1;
        this.keyword2 = keyword2;
        this.keyword3 = keyword3;
        this.improvementTip1 = improvementTip1;
        this.improvementTip2 = improvementTip2;
    }
}
