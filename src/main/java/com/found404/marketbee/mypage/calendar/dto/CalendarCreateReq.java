package com.found404.marketbee.mypage.calendar.dto;

import java.time.LocalDate;

public record CalendarCreateReq(
        String storeUuid,
        LocalDate calendarDate,
        String title
) {}