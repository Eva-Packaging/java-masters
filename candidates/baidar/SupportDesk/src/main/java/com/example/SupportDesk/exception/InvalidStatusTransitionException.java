package com.example.SupportDesk.exception;

import com.example.SupportDesk.enums.TicketStatus;

/**
 * Exception thrown when an invalid ticket status transition is attempted.
 * Maps to HTTP 409 Conflict.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(TicketStatus currentStatus, TicketStatus newStatus) {
        super(String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
    }

    public InvalidStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
