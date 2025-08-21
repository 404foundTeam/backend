package com.found404.marketbee.store.place;

import com.found404.marketbee.store.place.dto.PlaceSearchResp;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceSearchService placeSearchService;

    @GetMapping("/search-by-coord")
    public PlaceSearchResp searchByCoord(
            @RequestParam double x,
            @RequestParam double y,
            @RequestParam(defaultValue = "1000") int radius
    ) {
        return placeSearchService.searchByCoord(x, y, radius);
    }
}