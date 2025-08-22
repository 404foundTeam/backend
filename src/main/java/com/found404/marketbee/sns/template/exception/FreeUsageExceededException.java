package com.found404.marketbee.sns.template.exception;

public class FreeUsageExceededException extends RuntimeException {
    public FreeUsageExceededException() {
        super("이번 달 무료 사용 횟수를 모두 소진했습니다.");
    }
}