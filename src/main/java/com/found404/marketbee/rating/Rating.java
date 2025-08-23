package com.found404.marketbee.rating;

import com.found404.marketbee.common.YearMonthAttributeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"storeUuid", "ratingMonth"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String storeUuid;

    @Column(nullable = false)
    private String placeName;

    @Column(nullable = false)
    @Convert(converter = YearMonthAttributeConverter.class)
    private YearMonth ratingMonth;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Builder
    public Rating(String storeUuid, String placeName, YearMonth ratingMonth, BigDecimal averageRating) {
        this.storeUuid = storeUuid;
        this.placeName = placeName;
        this.ratingMonth = ratingMonth;
        this.averageRating = averageRating;
    }
}
