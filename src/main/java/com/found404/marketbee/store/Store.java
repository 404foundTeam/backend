package com.found404.marketbee.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "stores", indexes = @Index(columnList = "place_id", unique = true))
@Getter @Setter @NoArgsConstructor
public class Store {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_uuid", nullable = false, unique = true, length = 36)
    private String storeUuid;

    @Column(name = "place_id", nullable = false, length = 100)
    private String placeId;

    @Column(name = "place_name", length = 100)
    private String placeName;

    @Column(name = "road_address", length = 255)
    private String roadAddress;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude; // x

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;  // y
}
