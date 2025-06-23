package io.sphynx.server.dto.agent;

import io.sphynx.server.model.pojos.AgentConfig;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAgentByIdRequest {
    private String agentName;

    @Valid
    private AgentConfig agentConfig;
}
