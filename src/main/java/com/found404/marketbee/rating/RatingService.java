package com.found404.marketbee.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");


    @Transactional(readOnly = true)
    public List<RatingDto> getMonthlyAverageRatings(String placeName) {
        List<Rating> stats = ratingRepository.findByPlaceNameOrderByRatingMonthAsc(placeName);

        return stats.stream()
                .map(stat -> new RatingDto(
                        stat.getRatingMonth().format(FORMATTER),
                        stat.getAverageRating()
                ))
                .collect(Collectors.toList());
    }
}
