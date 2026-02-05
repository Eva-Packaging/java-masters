package com.example.SupportDesk.service;

import com.example.SupportDesk.dtos.CreateTicketRequest;
import com.example.SupportDesk.dtos.TicketAnalyticsResponse;
import com.example.SupportDesk.dtos.UpdateTicketStatusRequest;
import com.example.SupportDesk.entities.Ticket;

import java.util.List;

public interface TicketService {
    Ticket createTicket(CreateTicketRequest request);

    Ticket assignTicket(Long ticketId, Long agentId);

    Ticket updateTicketStatus(Long ticketId, UpdateTicketStatusRequest status);

    List<Ticket> searchTickets(String status,
            String priority,
            Long agentId);

    TicketAnalyticsResponse getAnalytics();

    Ticket findTicketById(Long ticketId);

    List<Ticket> findAllTickets();

    void deleteTicket(Long ticketId);
}
