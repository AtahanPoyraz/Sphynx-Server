package io.sphynx.server.repository;

import io.sphynx.server.model.AgentModel;
import io.sphynx.server.model.enums.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRepository extends JpaRepository<AgentModel, UUID> {
    Optional<AgentModel> findByActivationToken(String activationToken);
    List<AgentModel> findByAgentName(String agentName);
    List<AgentModel> findByUpdatedAtBeforeAndAgentStatusNot(LocalDateTime time, AgentStatus status);
    List<AgentModel> findAllByUser_UserId(UUID userId);
}
