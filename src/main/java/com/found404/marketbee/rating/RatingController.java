package com.found404.marketbee.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/report/reviews")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @GetMapping("/rating")
    public ResponseEntity<List<RatingDto>> getMonthlyAverageRatings(@RequestParam String placeName) {
        if (placeName == null || placeName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<RatingDto> monthlyAverages = ratingService.getMonthlyAverageRatings(placeName);
        return ResponseEntity.ok(monthlyAverages);
    }
}
