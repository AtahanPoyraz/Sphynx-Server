package io.sphynx.server.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.sphynx.server.model.enums.AgentStatus;
import io.sphynx.server.model.pojos.AgentConfig;
import io.sphynx.server.util.Generator;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "agents")
public class AgentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "agent_id", nullable = false, updatable = false)
    private UUID agentId;

    @Column(name = "agent_name", nullable = false)
    private String agentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_status", nullable = false)
    private AgentStatus agentStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "agent_config", columnDefinition = "jsonb")
    private AgentConfig agentConfig;

    @Column(name = "activation_token", nullable = false)
    private String activationToken;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.activationToken = Generator.GenerateRandomString(32);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
