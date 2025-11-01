package com.found404.marketbee.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByStoreUuidAndRatingMonthBetweenOrderByRatingMonthAsc(String storeUuid, YearMonth startMonth, YearMonth endMonth);
}
