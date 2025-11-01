package com.found404.marketbee.mypage.calendar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendar, String> {
    List<Calendar> findByStoreUuidAndCalendarDateBetween(String storeUuid, LocalDate start, LocalDate end);
}