package com.found404.marketbee.mypage.holidays;

import com.found404.marketbee.mypage.holidays.dto.HolidayCalendarResp;
import com.found404.marketbee.mypage.holidays.dto.HolidayResp;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final WebClient holidayWebClient;

    @Value("${external.holiday.service-key}")
    private String serviceKey;

    /** ✅ 월 단위 조회 */
    public List<HolidayCalendarResp> getHolidays(int year, int month) {
        String response = holidayWebClient.get()
                .uri(b -> b.path("/getHoliDeInfo")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("solYear", year)
                        .queryParam("solMonth", String.format("%02d", month))
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

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
                    holidays.add(HolidayCalendarResp.from(resp)); // ✅ 변환
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Holiday parsing error", e);
        }

        return holidays;
    }

    /** ✅ 연 단위 조회 */
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
