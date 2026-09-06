package org.example.signer.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.signer.entity.TestScenario;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestScenarioDto {

    private Long id;
    private Long tenantId;
    private UUID scenarioUuid;
    private String scenarioName;
    private String scenarioDescription;
    private String messageType;
    private TestScenario.TestCategory testCategory;
    private Map<String, Object> inputData;
    private Map<String, Object> expectedOutput;
    private Map<String, Object> validationRules;
    private TestScenario.ScenarioStatus status;
    private LocalDateTime lastRunAt;
    private String lastRunStatus;
    private Integer executionCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TestScenarioDto fromEntity(TestScenario entity) {
        if (entity == null) return null;
        return TestScenarioDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .scenarioUuid(entity.getScenarioUuid())
                .scenarioName(entity.getScenarioName())
                .scenarioDescription(entity.getScenarioDescription())
                .messageType(entity.getMessageType())
                .testCategory(entity.getTestCategory())
                .inputData(entity.getInputData())
                .expectedOutput(entity.getExpectedOutput())
                .validationRules(entity.getValidationRules())
                .status(entity.getStatus())
                .lastRunAt(entity.getLastRunAt())
                .lastRunStatus(entity.getLastRunStatus())
                .executionCount(entity.getExecutionCount())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
