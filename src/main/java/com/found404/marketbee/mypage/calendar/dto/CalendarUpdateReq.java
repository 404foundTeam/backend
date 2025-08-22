package com.found404.marketbee.mypage.calendar.dto;

import java.time.LocalDate;

public record CalendarUpdateReq(
        LocalDate calendarDate,
        String title
) {}