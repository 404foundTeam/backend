package com.found404.marketbee.sns.template.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FreeUsageExceededException.class)
    public ResponseEntity<ErrorResponse> handleFreeUsageExceeded(FreeUsageExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400
                .body(new ErrorResponse("FREE_USAGE_EXCEEDED", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateFinalCardException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateFinalCard(DuplicateFinalCardException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT) // 409
                .body(new ErrorResponse("DUPLICATE_FINAL_CARD", ex.getMessage()));
    }

    public record ErrorResponse(String errorCode, String message) {}
}