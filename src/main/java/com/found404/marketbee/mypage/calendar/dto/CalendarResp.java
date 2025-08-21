package com.found404.marketbee.mypage.calendar.dto;

import java.time.LocalDate;

public record CalendarResp(
        String id,
        LocalDate calendarDate,
        String title
) {}