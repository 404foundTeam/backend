package com.found404.marketbee.review;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "idx_unique_review",
                    columnNames = {"place_name", "review_hash"}
            )
        },
        indexes = @Index(name = "idx_reviews_place_name", columnList = "place_name"))
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "review_hash", nullable = false, length = 64)
    private String reviewHash;
}
