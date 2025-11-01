package com.found404.marketbee.reportSuggestion;

import com.found404.marketbee.salesRecord.entity.MonthlyStat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketingSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_stat_id", nullable = false)
    private MonthlyStat monthlyStat;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Builder
    public MarketingSuggestion(MonthlyStat monthlyStat, String title, String description) {
        this.monthlyStat = monthlyStat;
        this.title = title;
        this.description = description;
    }
}
