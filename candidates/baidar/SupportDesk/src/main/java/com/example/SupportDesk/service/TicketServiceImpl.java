package com.example.SupportDesk.service;

import com.example.SupportDesk.dtos.AgentResponse;
import com.example.SupportDesk.dtos.CreateTicketRequest;
import com.example.SupportDesk.dtos.TicketAnalyticsResponse;
import com.example.SupportDesk.dtos.TicketResponse;
import com.example.SupportDesk.dtos.UpdateTicketStatusRequest;
import com.example.SupportDesk.entities.Ticket;
import com.example.SupportDesk.enums.TicketPriority;
import com.example.SupportDesk.enums.TicketStatus;
import com.example.SupportDesk.exception.InvalidStatusTransitionException;
import com.example.SupportDesk.exception.ResourceNotFoundException;
import com.example.SupportDesk.mappers.AgentResponseMapper;
import com.example.SupportDesk.mappers.TicketResponseMapper;
import com.example.SupportDesk.repository.AgentRepository;
import com.example.SupportDesk.repository.TicketRepository;
import com.example.SupportDesk.util.TicketPredicates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;
    private final AgentRepository agentRepository;

    @Override
    public Ticket createTicket(CreateTicketRequest request) {
        return ticketRepository.save(TicketResponseMapper.toEntity(request));
    }

    @Override
    public Ticket assignTicket(Long ticketId, Long agentId) {
        if (!ticketRepository.existsById(ticketId))
            throw new ResourceNotFoundException("Ticket not found!");
        if (!agentRepository.existsById(agentId))
            throw new ResourceNotFoundException("No agent found with that id!");

        Ticket ticket = ticketRepository.findById(ticketId).get();
        ticket.setAssignedAgent(agentRepository.findById(agentId).get());
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket updateTicketStatus(Long ticketId, UpdateTicketStatusRequest status) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));

        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = status.status();

        if (!TicketPredicates.isValidStatusTransition(currentStatus, newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus, newStatus);
        }

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);

    }

    @Override
    public List<Ticket> searchTickets(String status, String priority, Long agentId) {
        List<Ticket> allTickets = ticketRepository.findAll();

        return allTickets.stream()
                .filter(ticket -> status == null ||
                        ticket.getStatus().toString().equalsIgnoreCase(status))
                .filter(ticket -> priority == null ||
                        ticket.getPriority().toString().equalsIgnoreCase(priority))
                .filter(ticket -> agentId == null ||
                        (ticket.getAssignedAgent() != null &&
                                ticket.getAssignedAgent().getId() == agentId))
                .collect(Collectors.toList());
    }

    @Override
    public TicketAnalyticsResponse getAnalytics() {
        List<Ticket> tickets = ticketRepository.findAll();

        long totalTickets = tickets.size();

        Map<TicketStatus, Long> countByStatus = tickets.stream()
                .collect(Collectors.groupingBy(
                        Ticket::getStatus,
                        Collectors.counting()));

        Map<TicketPriority, Long> countByPriority = tickets.stream()
                .collect(Collectors.groupingBy(
                        Ticket::getPriority,
                        Collectors.counting()));

        AgentResponse topAgent = tickets.stream()
                .filter(ticket -> ticket.getAssignedAgent() != null)
                .collect(Collectors.groupingBy(
                        Ticket::getAssignedAgent,
                        Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> AgentResponseMapper.toDto(entry.getKey()))
                .orElse(null);

        TicketResponse oldestOpenTicket = tickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.OPEN)
                .min(Comparator.comparing(Ticket::getCreatedAt))
                .map(TicketResponseMapper::toDto)
                .orElse(null);

        return TicketAnalyticsResponse.of(
                totalTickets,
                countByStatus,
                countByPriority,
                topAgent,
                oldestOpenTicket);
    }

    @Override
    public Ticket findTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", ticketId));
    }

    @Override
    public List<Ticket> findAllTickets() {
        return ticketRepository.findAll();
    }

    @Override
    public void deleteTicket(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket", ticketId);
        }
        ticketRepository.deleteById(ticketId);
    }
}
