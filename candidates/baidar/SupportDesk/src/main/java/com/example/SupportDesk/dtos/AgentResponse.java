package com.example.SupportDesk.dtos;

import java.time.LocalDateTime;

public record AgentResponse(
                long id,
                String name,
                String email,
                LocalDateTime createdAt) {
        public static AgentResponse of(long id, String name, String email, LocalDateTime createdAt) {
                return new AgentResponse(id, name, email, createdAt);
        }
}
