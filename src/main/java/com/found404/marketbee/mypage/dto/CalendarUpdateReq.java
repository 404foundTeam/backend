package com.found404.marketbee.mypage.dto;

import java.time.LocalDate;

public record CalendarUpdateReq(
        LocalDate calendarDate,
        String title
) {}