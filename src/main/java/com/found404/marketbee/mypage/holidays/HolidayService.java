package com.found404.marketbee.mypage.holidays;

import com.found404.marketbee.mypage.holidays.dto.HolidayCalendarResp;
import com.found404.marketbee.mypage.holidays.dto.HolidayResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

    private final WebClient holidayWebClient;

    @Value("${external.holiday.service-key}")
    private String serviceKey;

    private Instant lastForbiddenLogTime = Instant.EPOCH;

    //월 단위 조회
    public List<HolidayCalendarResp> getHolidays(int year, int month) {
        String response;

        try {
            response = holidayWebClient.get()
                    .uri(b -> b.path("/getHoliDeInfo")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("solYear", year)
                            .queryParam("solMonth", String.format("%02d", month))
                            .queryParam("_type", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (WebClientResponseException e) {
            // 공공데이터 화재로 인한 에러 방지 코드 추가
            if (e.getRawStatusCode() == 403) {
                if (Instant.now().isAfter(lastForbiddenLogTime.plusSeconds(60))) {
                    log.warn("공공데이터포털 호출 실패 (403): {}", e.getStatusText());
                    lastForbiddenLogTime = Instant.now();
                }
            } else {
                log.warn("공공데이터포털 호출 실패 ({}): {}", e.getRawStatusCode(), e.getStatusText());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("공휴일 데이터 요청 중 오류 발생", e);
            return Collections.emptyList();
        }

        List<HolidayCalendarResp> holidays = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    HolidayResp resp = new HolidayResp(
                            item.path("dateName").asText(),
                            item.path("locdate").asInt(),
                            "HOLIDAY"
                    );
                    holidays.add(HolidayCalendarResp.from(resp));
                }
            }
        } catch (Exception e) {
            log.error("Holiday parsing error", e);
        }

        return holidays;
    }

    //연 단위 조회
    public List<HolidayCalendarResp> getHolidaysByYear(int year) {
        List<HolidayCalendarResp> all = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            all.addAll(getHolidays(year, month));
        }
        return all.stream()
                .sorted(Comparator.comparing(HolidayCalendarResp::calendarDate))
                .toList();
    }
}

