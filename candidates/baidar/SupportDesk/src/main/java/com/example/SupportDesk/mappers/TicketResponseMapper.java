package com.example.SupportDesk.mappers;

import com.example.SupportDesk.dtos.CreateTicketRequest;
import com.example.SupportDesk.dtos.TicketResponse;
import com.example.SupportDesk.entities.Ticket;
import com.example.SupportDesk.enums.TicketStatus;

import java.time.LocalDateTime;

public final class TicketResponseMapper {

    private TicketResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static TicketResponse toDto(Ticket entity) {
        if (entity == null) {
            return null;
        }

        return new TicketResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                AgentResponseMapper.toDto(entity.getAssignedAgent()));
    }

    public static Ticket toEntity(TicketResponse dto) {
        if (dto == null) {
            return null;
        }

        Ticket ticket = new Ticket();
        ticket.setId(dto.id());
        ticket.setTitle(dto.title());
        ticket.setDescription(dto.description());
        ticket.setStatus(dto.status());
        ticket.setPriority(dto.priority());
        ticket.setCreatedAt(dto.createdAt());
        ticket.setUpdatedAt(dto.updatedAt());
        if (dto.assignedAgent() != null) {
            ticket.setAssignedAgent(AgentResponseMapper.toEntity(dto.assignedAgent()));
        }
        return ticket;
    }

    public static Ticket toEntity(CreateTicketRequest request) {
        if (request == null) {
            return null;
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.name());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        return ticket;
    }
}
