package com.found404.marketbee.sns.template.exception;

public class DuplicateFinalCardException extends RuntimeException {
    public DuplicateFinalCardException() {
        super("이미 저장된 이미지입니다.");
    }
}
