package io.sphynx.server.dto.agent;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivateAgentRequest {
    @NotBlank(message = "activationToken flag cannot be empty")
    private String activationToken;
}
