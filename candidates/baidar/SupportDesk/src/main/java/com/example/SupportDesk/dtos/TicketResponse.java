package com.example.SupportDesk.dtos;

import com.example.SupportDesk.enums.TicketPriority;
import com.example.SupportDesk.enums.TicketStatus;

import java.time.LocalDateTime;

public record TicketResponse(
                long id,
                String title,
                String description,
                TicketStatus status,
                TicketPriority priority,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                AgentResponse assignedAgent

) {
        public static TicketResponse of(long id, String title, String description,
                        TicketStatus status, TicketPriority priority,
                        LocalDateTime createdAt, LocalDateTime updatedAt,
                        AgentResponse assignedAgent) {
                return new TicketResponse(id, title, description, status, priority,
                                createdAt, updatedAt, assignedAgent);
        }
}
