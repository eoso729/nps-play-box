package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "test_scenarios", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_tenant_scenario_name", columnNames = {"tenant_id", "scenario_name"})
    },
    indexes = {
        @Index(name = "idx_scenarios_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_scenarios_tenant_type", columnList = "tenant_id,message_type"),
        @Index(name = "idx_scenarios_tenant_category", columnList = "tenant_id,test_category"),
        @Index(name = "idx_scenarios_tenant_status", columnList = "tenant_id,status")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "scenario_uuid", unique = true, nullable = false, updatable = false)
    private UUID scenarioUuid;

    @Column(name = "scenario_name", nullable = false, length = 255)
    private String scenarioName;

    @Column(name = "scenario_description", columnDefinition = "TEXT")
    private String scenarioDescription;

    @Column(name = "message_type", nullable = false, length = 100)
    private String messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_category", nullable = false, length = 100)
    private TestCategory testCategory;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> inputData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_output", columnDefinition = "jsonb")
    private Map<String, Object> expectedOutput;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private Map<String, Object> validationRules;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ScenarioStatus status;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "last_run_status", length = 50)
    private String lastRunStatus;

    @Column(name = "execution_count")
    private Integer executionCount;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (scenarioUuid == null) {
            scenarioUuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ScenarioStatus.DRAFT;
        }
        if (executionCount == null) {
            executionCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ScenarioStatus {
        DRAFT,
        ACTIVE,
        ARCHIVED
    }

    public enum TestCategory {
        FUNCTIONAL,
        VALIDATION,
        INTEGRATION,
        PERFORMANCE,
        SECURITY,
        REGRESSION
    }
}
