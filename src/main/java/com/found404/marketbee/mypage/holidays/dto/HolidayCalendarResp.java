package com.found404.marketbee.mypage.holidays.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record HolidayCalendarResp(
        String calendarDate,
        String title,
        String type
) {
    public static HolidayCalendarResp from(HolidayResp resp) {
        String raw = String.valueOf(resp.locdate());
        LocalDate date = LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyyMMdd"));
        return new HolidayCalendarResp(
                date.format(DateTimeFormatter.ISO_DATE),
                resp.dateName(),
                resp.type()
        );
    }
}
