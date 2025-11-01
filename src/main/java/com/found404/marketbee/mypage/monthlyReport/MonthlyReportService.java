package com.found404.marketbee.mypage.monthlyReport;

import com.found404.marketbee.mypage.monthlyReport.dto.MonthDataDto;
import com.found404.marketbee.mypage.monthlyReport.dto.YearDataDto;
import com.found404.marketbee.salesRecord.repository.DailySalesSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private final DailySalesSummaryRepository summaryRepository;
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Transactional(readOnly = true)
    public List<YearDataDto> getAvailableReportPeriods(String storeUuid) {
        Optional<LocalDate> latestDateOptional = summaryRepository.findLatestSalesDateByStoreUuid(storeUuid);

        if (latestDateOptional.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate latestDate = latestDateOptional.get();
        LocalDate cutoffDate = latestDate.withDayOfMonth(1);
        List<LocalDate> salesDates = summaryRepository.findDistinctSalesDatesByStoreUuidBefore(storeUuid, cutoffDate);

        if (salesDates.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, Map<Integer, List<LocalDate>>> groupedDates = salesDates.stream()
                .collect(Collectors.groupingBy(
                        LocalDate::getYear,
                        TreeMap::new,
                        Collectors.groupingBy(
                                LocalDate::getMonthValue,
                                TreeMap::new,
                                Collectors.toList()
                        )
                ));

        List<YearDataDto> result = new ArrayList<>();
        groupedDates.forEach((year, monthMap) -> {
            List<MonthDataDto> monthDataList = new ArrayList<>();
            monthMap.forEach((month, datesInMonth) -> {
                LocalDate minDate = datesInMonth.get(0);
                LocalDate maxDate = datesInMonth.get(datesInMonth.size() - 1);

                String period = minDate.equals(maxDate)
                        ? minDate.format(PERIOD_FORMATTER)
                        : String.format("%s ~ %s",
                        minDate.format(PERIOD_FORMATTER),
                        maxDate.format(PERIOD_FORMATTER));

                monthDataList.add(new MonthDataDto(month, period));
            });
            result.add(new YearDataDto(year, monthDataList));
        });

        return result;
    }
}