package com.found404.marketbee.rating;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RatingDto {
    private String month;
    private BigDecimal averageRating;
}
