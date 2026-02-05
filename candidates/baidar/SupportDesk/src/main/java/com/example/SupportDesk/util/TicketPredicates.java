package com.example.SupportDesk.util;

import com.example.SupportDesk.enums.TicketStatus;

public class TicketPredicates {
    private TicketPredicates() {

    }

    public static boolean isValidStatusTransition(TicketStatus from, TicketStatus to) {
        // Define valid transitions
        return switch (from) {
            case OPEN -> to == TicketStatus.IN_PROGRESS || to == TicketStatus.CLOSED;
            case IN_PROGRESS -> to == TicketStatus.CLOSED || to == TicketStatus.OPEN;
            case CLOSED -> to == TicketStatus.OPEN; // Can reopen closed tickets
            case RESOLVED -> throw new UnsupportedOperationException("Unimplemented case: " + from);
            default -> throw new IllegalArgumentException("Unexpected value: " + from);
        };
    }
}
