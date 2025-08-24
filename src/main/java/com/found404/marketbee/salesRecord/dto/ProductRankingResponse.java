package com.found404.marketbee.salesRecord.dto;

import java.util.List;

public record ProductRankingResponse(
        List<RankedItem> top3,
        List<RankedItem> bottom3,
        List<ChartItem> salesDistributionChart
) {
    public record RankedItem(String productName, Long totalSales) {}
    public record ChartItem(String itemName, Double percentage) {}
}