package com.example.SupportDesk.controller;

import com.example.SupportDesk.dtos.CreateTicketRequest;
import com.example.SupportDesk.dtos.TicketAnalyticsResponse;
import com.example.SupportDesk.dtos.TicketResponse;
import com.example.SupportDesk.dtos.UpdateTicketStatusRequest;
import com.example.SupportDesk.entities.Ticket;
import com.example.SupportDesk.mappers.TicketResponseMapper;
import com.example.SupportDesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Ticket createdTicket = ticketService.createTicket(request);
        TicketResponse response = TicketResponseMapper.toDto(createdTicket);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.findTicketById(id);
        TicketResponse response = TicketResponseMapper.toDto(ticket);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ticketId}/assign/{agentId}")
    public ResponseEntity<TicketResponse> assignTicketToAgent(
            @PathVariable Long ticketId,
            @PathVariable Long agentId) {
        Ticket assignedTicket = ticketService.assignTicket(ticketId, agentId);
        TicketResponse response = TicketResponseMapper.toDto(assignedTicket);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest statusRequest) {
        Ticket updatedTicket = ticketService.updateTicketStatus(ticketId, statusRequest);
        TicketResponse response = TicketResponseMapper.toDto(updatedTicket);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<TicketResponse>> searchTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long agentId) {
        List<Ticket> tickets = ticketService.searchTickets(status, priority, agentId);
        List<TicketResponse> responses = tickets.stream()
                .map(TicketResponseMapper::toDto)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/analytics")
    public ResponseEntity<TicketAnalyticsResponse> getTicketAnalytics() {
        TicketAnalyticsResponse analytics = ticketService.getAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        List<Ticket> tickets = ticketService.findAllTickets();
        List<TicketResponse> responses = tickets.stream()
                .map(TicketResponseMapper::toDto)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

}
