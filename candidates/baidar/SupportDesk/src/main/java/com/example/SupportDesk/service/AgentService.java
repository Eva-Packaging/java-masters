package com.example.SupportDesk.service;

import com.example.SupportDesk.dtos.CreateAgentRequest;
import com.example.SupportDesk.entities.Agent;

public interface AgentService {

    Agent createAgent(CreateAgentRequest request);

    Agent findAgentById(Long agentId);
}
