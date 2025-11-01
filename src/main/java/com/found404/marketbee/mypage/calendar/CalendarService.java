package com.found404.marketbee.mypage.calendar;

import com.found404.marketbee.mypage.calendar.dto.CalendarCreateReq;
import com.found404.marketbee.mypage.calendar.dto.CalendarDeleteResp;
import com.found404.marketbee.mypage.calendar.dto.CalendarResp;
import com.found404.marketbee.mypage.calendar.dto.CalendarUpdateReq;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository repository;

    // 일정 생성
    public CalendarResp create(CalendarCreateReq req) {
        Calendar c = Calendar.builder()
                .id(UUID.randomUUID().toString())
                .storeUuid(req.storeUuid())
                .calendarDate(req.calendarDate())
                .title(req.title())
                .build();

        repository.save(c);

        return new CalendarResp(c.getId(), c.getCalendarDate(), c.getTitle());
    }

    // 월별 조회
    @Transactional(readOnly = true)
    public List<CalendarResp> month(String storeUuid, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return repository.findByStoreUuidAndCalendarDateBetween(storeUuid, start, end)
                .stream()
                .map(c -> new CalendarResp(c.getId(), c.getCalendarDate(), c.getTitle()))
                .toList();
    }

    // 일정 상세 조회
    @Transactional(readOnly = true)
    public CalendarResp getOne(String id) {
        Calendar c = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Calendar not found with id: " + id));

        return new CalendarResp(c.getId(), c.getCalendarDate(), c.getTitle());
    }

    // 일정 수정
    @Transactional
    public CalendarResp update(String id, CalendarUpdateReq req) {
        Calendar c = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Calendar not found with id: " + id));

        if (req.calendarDate() != null) {
            c.setCalendarDate(req.calendarDate());
        }
        if (req.title() != null && !req.title().isBlank()) {
            c.setTitle(req.title());
        }

        repository.save(c);

        return new CalendarResp(c.getId(), c.getCalendarDate(), c.getTitle());
    }

    // 일정 삭제
    @Transactional
    public CalendarDeleteResp delete(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return new CalendarDeleteResp("일정이 삭제되었습니다.");
        } else {
            throw new EntityNotFoundException("Calendar not found with id: " + id);
        }
    }
}
