package com.example.SupportDesk.controller;

import com.example.SupportDesk.dtos.AgentResponse;
import com.example.SupportDesk.dtos.CreateAgentRequest;
import com.example.SupportDesk.mappers.AgentResponseMapper;
import com.example.SupportDesk.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agents")
public class AgentController {
    private final AgentService agentService;

    @PostMapping
    ResponseEntity<AgentResponse> createAgent(@RequestBody CreateAgentRequest request) {
        return ResponseEntity.created(null).body(AgentResponseMapper.toDto(agentService.createAgent(request)));
    }

}
