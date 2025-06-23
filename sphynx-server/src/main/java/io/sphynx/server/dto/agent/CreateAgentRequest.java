package io.sphynx.server.dto.agent;

import io.sphynx.server.model.pojos.AgentConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAgentRequest {
    @NotNull(message = "userId flag cannot be null")
    private UUID userId;

    @NotBlank(message = "agentName flag cannot be empty")
    private String agentName;

    @NotNull(message = "agentConfig flag cannot be null")
    @Valid
    private AgentConfig agentConfig;
}
