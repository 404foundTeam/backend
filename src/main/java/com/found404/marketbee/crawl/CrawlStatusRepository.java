package com.found404.marketbee.crawl;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CrawlStatusRepository extends JpaRepository<CrawlStatus, String> {
    Optional<CrawlStatus> findByPlaceName(String placeName);
}