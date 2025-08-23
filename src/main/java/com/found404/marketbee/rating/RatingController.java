package com.found404.marketbee.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @GetMapping("/{storeUuid}/rating")
    public ResponseEntity<List<RatingDto>> getMonthlyAverageRatings(@PathVariable String storeUuid) {
        List<RatingDto> monthlyAverages = ratingService.getMonthlyAverageRatings(storeUuid);

        return ResponseEntity.ok(monthlyAverages);
    }
}
