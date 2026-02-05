package com.example.SupportDesk.dtos;

import com.example.SupportDesk.enums.TicketStatus;

public record UpdateTicketStatusRequest(
        TicketStatus status
) {
}
