package com.found404.marketbee.salesRecord.dto;

public record MonthlySalesResponse(
        Long currentMonthSales,
        Long previousMonthSales,
        Double growthPercentage
) {
}