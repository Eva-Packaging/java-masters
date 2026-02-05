package com.example.SupportDesk.mappers;

import com.example.SupportDesk.dtos.AgentResponse;
import com.example.SupportDesk.dtos.CreateAgentRequest;
import com.example.SupportDesk.entities.Agent;

import java.time.LocalDateTime;

public final class AgentResponseMapper {

    private AgentResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static AgentResponse toDto(Agent entity) {
        if (entity == null) {
            return null;
        }

        return new AgentResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getCreatedAt());
    }

    public static Agent toEntity(AgentResponse dto) {
        if (dto == null) {
            return null;
        }

        Agent agent = new Agent();
        agent.setId(dto.id());
        agent.setName(dto.name());
        agent.setEmail(dto.email());
        agent.setCreatedAt(dto.createdAt());
        return agent;
    }

    public static Agent toEntity(CreateAgentRequest request) {
        if (request == null) {
            return null;
        }

        Agent agent = new Agent();
        agent.setName(request.name());
        agent.setEmail(request.email());
        agent.setCreatedAt(LocalDateTime.now());
        return agent;
    }
}
