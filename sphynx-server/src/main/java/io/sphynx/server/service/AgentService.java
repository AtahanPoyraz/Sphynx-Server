package io.sphynx.server.service;

import io.sphynx.server.dto.agent.ActivateAgentRequest;
import io.sphynx.server.dto.agent.CreateAgentRequest;
import io.sphynx.server.dto.agent.UpdateAgentByIdRequest;
import io.sphynx.server.model.AgentModel;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.AgentStatus;
import io.sphynx.server.repository.AgentRepository;
import io.sphynx.server.repository.UserRepository;
import io.sphynx.server.util.Generator;
import io.sphynx.server.util.Validator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgentService {
    private final AgentRepository agentRepository;
    private final UserRepository userRepository;

    @Autowired
    public AgentService(
            AgentRepository agentRepository,
            UserRepository userRepository
    ) {
        this.agentRepository = agentRepository;
        this.userRepository = userRepository;
    }

    public AgentModel getAgentByAgentId(UUID agentId) {
        return this.agentRepository.findById(agentId)
                .orElseThrow(() -> new EntityNotFoundException("Agent not found with id: " + agentId));
    }

    public List<AgentModel> getAgentByAgentName(String agentName) {
        return this.agentRepository.findByAgentName(agentName);
    }

    public List<AgentModel> getAgentsByUserId(UUID agentId) {
        return this.agentRepository.findAllByUser_UserId(agentId);
    }

    public Page<AgentModel> getAllAgents(Pageable pageable) {
        return this.agentRepository.findAll(pageable);
    }

    public AgentModel activateAgent(ActivateAgentRequest activateAgentRequest) {
        AgentModel agent = this.agentRepository.findByActivationToken(activateAgentRequest.getActivationToken())
                .orElseThrow(() -> new EntityNotFoundException("Agent not found with token: " + activateAgentRequest.getActivationToken()));

        agent.setAgentStatus(AgentStatus.ACTIVE);
        return this.agentRepository.save(agent);
    }

    public AgentModel createAgent(CreateAgentRequest createAgentRequest) {
        Optional<UserModel> user = this.userRepository.findById(createAgentRequest.getUserId());
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User not found with id: " + createAgentRequest.getUserId());
        }

        if (Validator.isValidAgentName(createAgentRequest.getAgentName())) {
            throw new IllegalArgumentException("Agent name is invalid. Only letters, digits, underscores and hyphens are allowed.");
        }

        AgentModel agent = new AgentModel();
        agent.setAgentName(createAgentRequest.getAgentName());
        agent.setAgentStatus(AgentStatus.INACTIVE);
        agent.setAgentConfig(createAgentRequest.getAgentConfig());
        agent.setUser(user.get());

        return this.agentRepository.save(agent);
    }

    public AgentModel refreshActivationToken(UUID agentId) {
        AgentModel agent = this.agentRepository.findById(agentId)
                .orElseThrow(() -> new EntityNotFoundException("Agent not found with id: " + agentId));

        String newActivationToken = Generator.GenerateRandomString(32);
        agent.setActivationToken(newActivationToken);

        return this.agentRepository.save(agent);
    }

    public AgentModel updateAgentByAgentId(UUID agentId, UpdateAgentByIdRequest updateAgentByIdRequest) {
        AgentModel agent = this.agentRepository.findById(agentId)
                .orElseThrow(() -> new EntityNotFoundException("Agent not found with id: " + agentId));

        if (Validator.isValidAgentName(updateAgentByIdRequest.getAgentName())) {
            throw new IllegalArgumentException("Agent name is invalid. Only letters, digits, underscores and hyphens are allowed.");
        }

        if (updateAgentByIdRequest.getAgentName() != null) {
            agent.setAgentName(updateAgentByIdRequest.getAgentName());
        }

        if (updateAgentByIdRequest.getAgentConfig() != null) {
            agent.setAgentConfig(updateAgentByIdRequest.getAgentConfig());
        }

        return this.agentRepository.save(agent);
    }

    public void deleteAgentByAgentId(UUID agentId) {
        if(!this.agentRepository.existsById(agentId)) {
            throw new EntityNotFoundException("Agent not found with id: " + agentId);
        }

        this.agentRepository.deleteById(agentId);
    }
}
