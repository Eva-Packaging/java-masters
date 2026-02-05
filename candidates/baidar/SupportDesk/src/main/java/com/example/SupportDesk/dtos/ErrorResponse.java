package com.example.SupportDesk.dtos;

import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

/**
 * Standard error response DTO for API error responses.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp) {
    public static ErrorResponse of(HttpStatus httpStatus, String message, String path) {
        return new ErrorResponse(
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                path,
                LocalDateTime.now());
    }
}
