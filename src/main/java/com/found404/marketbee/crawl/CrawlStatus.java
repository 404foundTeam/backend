package com.found404.marketbee.crawl;

import com.found404.marketbee.common.YearMonthAttributeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.YearMonth;

@Entity
@Table(name = "crawl_status",
        uniqueConstraints = @UniqueConstraint(columnNames = {"storeUuid"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawlStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String storeUuid;

    @Column(nullable = false)
    private String placeName;

    @Convert(converter = YearMonthAttributeConverter.class)
    private YearMonth lastCrawled;

    public CrawlStatus(String storeUuid, String placeName, YearMonth lastCrawled) {
        this.storeUuid = storeUuid;
        this.placeName = placeName;
        this.lastCrawled = lastCrawled;
    }

    public void updateLastCrawled(YearMonth lastCrawled) {
        this.lastCrawled = lastCrawled;
    }
}
