package com.found404.marketbee.mypage.holidays;

import com.found404.marketbee.mypage.holidays.dto.HolidayCalendarResp;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendar")
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping("/holidays")
    public List<HolidayCalendarResp> holidaysByYear(@RequestParam int year) {
        return holidayService.getHolidaysByYear(year);
    }
}