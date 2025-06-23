package io.sphynx.server.model.pojos;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentConfig {
    @Min(value = 0, message = "cpuMetricsCount flag must be zero or positive")
    private Integer cpuMetricsCount;

    @Min(value = 0, message = "memoryMetricsCount flag must be zero or positive")
    private Integer memoryMetricsCount;

    @Min(value = 0, message = "diskMetricsCount flag must be zero or positive")
    private Integer diskMetricsCount;

    @Min(value = 1, message = "interval flag must be at least 1")
    private Integer interval;
}
