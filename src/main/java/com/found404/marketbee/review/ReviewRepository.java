package com.found404.marketbee.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPlaceName(String placeName);

    @Transactional
    long deleteByPlaceNameAndReviewDateBefore(String placeName, LocalDate cutOffDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', r.reviewDate, '%Y-%m'), AVG(r.rating) " +
            "FROM Review r " +
            "WHERE r.placeName = :placeName " +
            "GROUP BY FUNCTION('DATE_FORMAT', r.reviewDate, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', r.reviewDate, '%Y-%m') ASC")
    List<Object[]> findMonthlyAverageRatingsByPlaceName(@Param("placeName") String placeName);
}
