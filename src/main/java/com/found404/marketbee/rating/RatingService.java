package com.found404.marketbee.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Transactional(readOnly = true)
    public List<RatingDto> getMonthlyAverageRatings(String storeUuid) {
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(6);

        List<Rating> stats = ratingRepository.findByStoreUuidAndRatingMonthBetweenOrderByRatingMonthAsc(storeUuid, startMonth, endMonth.minusMonths(1));

        return stats.stream()
                .map(stat -> new RatingDto(
                        stat.getRatingMonth().format(FORMATTER),
                        stat.getAverageRating()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RatingDto> getMonthlyAverageRatingsByDate(String storeUuid, int year, int month) {
        try {
            YearMonth endMonth = YearMonth.of(year, month);
            YearMonth startMonth = endMonth.minusMonths(6);

            List<Rating> stats = ratingRepository.findByStoreUuidAndRatingMonthBetweenOrderByRatingMonthAsc(storeUuid, startMonth, endMonth.minusMonths(1));

            return stats.stream()
                    .map(stat -> new RatingDto(
                            stat.getRatingMonth().format(FORMATTER),
                            stat.getAverageRating()
                    ))
                    .collect(Collectors.toList());
        } catch (DateTimeException e) {
            return Collections.emptyList();
        }
    }
}
