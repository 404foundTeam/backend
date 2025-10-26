package com.found404.marketbee.mypage.monthlyReport.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class YearDataDto {
    private int year;
    private List<MonthDataDto> months;

    public YearDataDto(int year, List<MonthDataDto> months) {
        this.year = year;
        this.months = months;
    }
}