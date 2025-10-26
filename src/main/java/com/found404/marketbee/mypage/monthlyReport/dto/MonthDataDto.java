package com.found404.marketbee.mypage.monthlyReport.dto;

import lombok.Getter;

@Getter
public class MonthDataDto {
    private int month;
    private String period;

    public MonthDataDto(int month, String period) {
        this.month = month;
        this.period = period;
    }
}