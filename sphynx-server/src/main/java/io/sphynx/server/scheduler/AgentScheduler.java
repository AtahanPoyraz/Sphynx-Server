package io.sphynx.server.scheduler;

import io.sphynx.server.model.AgentModel;
import io.sphynx.server.model.enums.AgentStatus;
import io.sphynx.server.repository.AgentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class AgentScheduler {
    private final AgentRepository agentRepository;

    public AgentScheduler(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Transactional
    @Scheduled(fixedRateString = "${agent.scheduler.fixedRate}")
    public void checkAndDeactivateAgents() {
        try {
            LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
            List<AgentModel> outdatedAgents = agentRepository.findByUpdatedAtBeforeAndAgentStatusNot(
                    tenSecondsAgo,
                    AgentStatus.INACTIVE
            );

            if (!outdatedAgents.isEmpty()) {
                outdatedAgents.forEach(agent -> agent.setAgentStatus(AgentStatus.INACTIVE));
                this.agentRepository.saveAll(outdatedAgents);
                log.info("Deactivated {} agents due to inactivity.", outdatedAgents.size());
            }

        } catch (Exception e) {
            log.error("Error while deactivating agents", e);
        }
    }
}
