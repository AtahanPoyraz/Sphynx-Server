package io.sphynx.server.scheduler;

import io.sphynx.server.model.AgentModel;
import io.sphynx.server.model.enums.AgentStatus;
import io.sphynx.server.repository.AgentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class AgentScheduler {
    private static final Logger logger = LoggerFactory.getLogger(AgentScheduler.class);

    private final AgentRepository agentRepository;

    @Autowired
    public AgentScheduler(
            AgentRepository agentRepository
    ) {
        this.agentRepository = agentRepository;
    }

    @Transactional
    @Scheduled(fixedRateString = "${agent.scheduler.fixedRate}")
    public void checkAndDeactivateAgents() {
        try {
            LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
            List<AgentModel> outdatedAgents = this.agentRepository.findByUpdatedAtBeforeAndStatusNot(tenSecondsAgo, AgentStatus.INACTIVE);

            if (!outdatedAgents.isEmpty()) {
                outdatedAgents.forEach(agent -> agent.setStatus(AgentStatus.INACTIVE));
                this.agentRepository.saveAll(outdatedAgents);
                logger.info("Deactivated {} agents due to inactivity.", outdatedAgents.size());
            }

        } catch (Exception e) {
            logger.error("Error while deactivating agents", e);
        }
    }
}
