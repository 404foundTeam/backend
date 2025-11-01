package com.found404.marketbee.crawl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CrawlResult {
    SUCCESS("성공적으로 크롤링을 완료했습니다."),
    SKIPPED("최신 데이터이므로 크롤링을 건너뛰었습니다."),
    FAILED("크롤링 실행 중 오류가 발생했습니다.");

    private final String message;
}
