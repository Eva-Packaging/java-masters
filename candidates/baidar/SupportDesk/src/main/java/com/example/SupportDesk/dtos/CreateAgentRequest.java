package com.example.SupportDesk.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAgentRequest(
        @NotBlank
        String name,
        @Email
        String email
) {
}
