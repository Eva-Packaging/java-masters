package com.example.SupportDesk.service;

import com.example.SupportDesk.dtos.CreateAgentRequest;
import com.example.SupportDesk.entities.Agent;
import com.example.SupportDesk.mappers.AgentResponseMapper;
import com.example.SupportDesk.repository.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentServiceImpl implements AgentService {

    //bad idea as it could be null when singleton instance is spun up but project required so here it is
    @Autowired
    private AgentRepository agentRepository;

    //no check
    @Override
    public Agent createAgent(CreateAgentRequest request) {
        return agentRepository.save(AgentResponseMapper.toEntity(request));
    }


    @Override
    public Agent findAgentById(Long agentId) {
        return agentRepository.findById(agentId).orElse(null);
    }
}
