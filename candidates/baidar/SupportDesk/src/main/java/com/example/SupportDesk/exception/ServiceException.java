package com.example.SupportDesk.exception;

/**
 * Exception thrown for unexpected service layer errors.
 * Maps to HTTP 500 Internal Server Error.
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
