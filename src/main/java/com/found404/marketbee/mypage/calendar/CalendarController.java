package com.found404.marketbee.mypage.calendar;

import com.found404.marketbee.mypage.calendar.dto.CalendarCreateReq;
import com.found404.marketbee.mypage.calendar.dto.CalendarDeleteResp;
import com.found404.marketbee.mypage.calendar.dto.CalendarResp;
import com.found404.marketbee.mypage.calendar.dto.CalendarUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService service;

    // 월별 조회
    @GetMapping("/month")
    public List<CalendarResp> month(@RequestParam String storeUuid,
                                    @RequestParam int year,
                                    @RequestParam int month) {
        return service.month(storeUuid, year, month);
    }

    // 일정 생성
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public CalendarResp create(@RequestBody CalendarCreateReq req) {
        return service.create(req);
    }

    // 일정 상세 조회
    @GetMapping("/events/{id}")
    public CalendarResp getOne(@PathVariable String id) {
        return service.getOne(id);
    }

    // 일정 수정
    @PatchMapping("/events/{id}")
    public CalendarResp update(@PathVariable String id,
                               @RequestBody CalendarUpdateReq req) {
        return service.update(id, req);
    }

    // 일정 삭제
    @DeleteMapping("/events/{id}")
    public ResponseEntity<CalendarDeleteResp> delete(@PathVariable String id) {
        CalendarDeleteResp resp = service.delete(id);
        return ResponseEntity.ok(resp);
    }
}