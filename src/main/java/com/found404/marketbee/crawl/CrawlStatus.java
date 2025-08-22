package com.found404.marketbee.crawl;

import com.found404.marketbee.common.YearMonthAttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.YearMonth;

@Entity
@Table(name = "crawl_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawlStatus {
    @Id
    private String placeName;

    @Convert(converter = YearMonthAttributeConverter.class)
    private YearMonth lastCrawled;

    public CrawlStatus(String placeName, YearMonth lastCrawled) {
        this.placeName = placeName;
        this.lastCrawled = lastCrawled;
    }

    public void updateLastCrawled(YearMonth lastCrawled) {
        this.lastCrawled = lastCrawled;
    }
}
