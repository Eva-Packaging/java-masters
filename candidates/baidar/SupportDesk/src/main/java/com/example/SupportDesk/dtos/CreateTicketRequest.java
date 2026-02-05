package com.example.SupportDesk.dtos;

import com.example.SupportDesk.enums.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(
                @NotBlank(message = "Title is required") String name,
                @NotBlank(message = "Description is required") String description,
                @NotNull(message = "Priority is required") TicketPriority priority) {
}
