package io.sphynx.server.controller;

import io.sphynx.server.dto.GenericResponse;
import io.sphynx.server.dto.agent.ActivateAgentRequest;
import io.sphynx.server.dto.agent.CreateAgentRequest;
import io.sphynx.server.dto.agent.UpdateAgentByIdRequest;
import io.sphynx.server.model.AgentModel;
import io.sphynx.server.model.UserModel;
import io.sphynx.server.model.enums.UserRole;
import io.sphynx.server.service.AgentService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {
    private final AgentService agentService;

    @Autowired
    public AgentController(
            AgentService agentService
    ) {
        this.agentService = agentService;
    }

    @GetMapping("/refresh-activation-token")
    public ResponseEntity<GenericResponse<?>> updateActivationToken(
            @RequestParam UUID agentId
    ) {
        try {
            AgentModel agent = this.agentService.refreshActivationToken(agentId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                                    HttpStatus.OK.value(),
                                    "Agent activation token refreshed successfully",
                                    agent.getActivationToken()
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while fetching the agents: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @GetMapping("/get")
    public ResponseEntity<GenericResponse<?>> getAgent(
            @RequestParam(required = false) UUID agentId,
            @RequestParam(required = false) String agentName,
            @RequestParam(required = false) UUID userId,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        try {
            if (agentId != null) {
                AgentModel agent = this.agentService.getAgentByAgentId(agentId);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Agent fetched successfully",
                                agent
                                )
                        );
            }

            if (agentName != null && currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
                List<AgentModel> agents = this.agentService.getAgentByAgentName(agentName);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Agent fetched successfully",
                                agents
                                )
                        );
            }

            if (userId != null) {
                List<AgentModel> agents = this.agentService.getAgentsByUserId(userId);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new GenericResponse<>(
                                HttpStatus.OK.value(),
                                "Agents fetched successfully",
                                agents
                                )
                        );
            }

            if (!currentUser.getRoles().contains(UserRole.ROLE_ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new GenericResponse<>(
                                HttpStatus.FORBIDDEN.value(),
                                "You are not authorized to access all agents.",
                                null
                                )
                        );
            }

            Page<AgentModel> agents = this.agentService.getAllAgents(pageable);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "Agent list fetched successfully",
                            agents
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while fetching the agents: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<GenericResponse<?>> activateAgent(
            @Valid @RequestBody ActivateAgentRequest activateAgentRequest
    ) {
        try {
            AgentModel agent = this.agentService.activateAgent(activateAgentRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "Agent activated successfully",
                            agent
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while creating the agent: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<GenericResponse<?>> createAgent(
            @Valid @RequestBody CreateAgentRequest createAgentRequest
    ) {
        try {
            AgentModel agent = this.agentService.createAgent(createAgentRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new GenericResponse<>(
                            HttpStatus.CREATED.value(),
                            "Agent created successfully",
                            agent
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while creating the agent: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<GenericResponse<?>> updateAgentById(
            @RequestParam UUID id,
            @Valid @RequestBody UpdateAgentByIdRequest updateAgentByIdRequest
    ) {
        try {
            AgentModel agent = this.agentService.updateAgentByAgentId(id, updateAgentByIdRequest);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "Agent updated successfully",
                            agent
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while updating the agent: " + e.getMessage(),
                            null
                            )
                    );
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<GenericResponse<?>> deleteAgentById(
            @RequestParam UUID id
    ) {
        try {
            this.agentService.deleteAgentByAgentId(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GenericResponse<>(
                            HttpStatus.OK.value(),
                            "Agent deleted successfully",
                            null
                            )
                    );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "An error occurred while deleting the agent: " + e.getMessage(),
                            null
                            )
                    );
        }
    }
}
