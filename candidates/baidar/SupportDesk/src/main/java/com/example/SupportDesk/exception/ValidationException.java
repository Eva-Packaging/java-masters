package com.example.SupportDesk.exception;

/**
 * Exception thrown when validation fails for request data.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
