package com.found404.marketbee.salesRecord.dto;

import java.util.List;

public record VisitorStatsResponse(
        List<HourlyStat> mostVisitedHours,
        List<HourlyStat> leastVisitedHours,
        List<DailyRank> dailyVisitorRank
) {
    public record HourlyStat(int hour, long totalCustomers) {}
    public record DailyRank(String dayOfWeek, long totalCustomers) {}
}