package com.found404.marketbee.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByPlaceNameOrderByRatingMonthAsc(String placeName);

    @Transactional
    void deleteByPlaceName(String placeName);
}
