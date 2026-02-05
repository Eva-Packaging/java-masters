package com.example.SupportDesk.dtos;

import com.example.SupportDesk.enums.TicketPriority;
import com.example.SupportDesk.enums.TicketStatus;

import java.util.Map;

/**
 * Analytics response DTO for ticket statistics and metrics.
 */
public record TicketAnalyticsResponse(
        long totalTickets,
        Map<TicketStatus, Long> countByStatus,
        Map<TicketPriority, Long> countByPriority,
        AgentResponse topAgentByAssignedTickets,
        TicketResponse oldestOpenTicket) {
    public static TicketAnalyticsResponse of(
            long totalTickets,
            Map<TicketStatus, Long> countByStatus,
            Map<TicketPriority, Long> countByPriority,
            AgentResponse topAgent,
            TicketResponse oldestOpenTicket) {
        return new TicketAnalyticsResponse(
                totalTickets,
                countByStatus,
                countByPriority,
                topAgent,
                oldestOpenTicket);
    }
}
