package com.found404.marketbee.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByStoreUuid(String storeUuid);

    @Modifying(clearAutomatically = true)
    @Transactional
    long deleteByStoreUuidAndReviewDateBefore(String storeUuid, LocalDate cutOffDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', r.reviewDate, '%Y-%m'), AVG(r.rating) " +
            "FROM Review r " +
            "WHERE r.storeUuid = :storeUuid " +
            "GROUP BY FUNCTION('DATE_FORMAT', r.reviewDate, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', r.reviewDate, '%Y-%m') ASC")
    List<Object[]> findMonthlyAverageRatingsByStoreUuid(@Param("storeUuid") String storeUuid);
}
