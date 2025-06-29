package io.sphynx.server.service;

import io.sphynx.server.model.AgentModel;
import io.sphynx.server.repository.AgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AgentTokenService {
    private final AgentRepository agentRepository;

    @Autowired
    public AgentTokenService(
            AgentRepository agentRepository
    ) {
        this.agentRepository = agentRepository;
    }

    public String generateAgentToken(UUID agentId) {
        return "";
    }

    public boolean isAgentTokenValid(String agentToken) {
        return true;
    }

    public AgentModel extractAgentFromToken(String agentToken) {
        return new AgentModel();
    }
}
