package org.example.signer.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.signer.entity.ValidationResult;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResultDto {

    private Long id;
    private Long tenantId;
    private UUID resultUuid;
    private Long messageId;
    private Long scenarioId;
    private ValidationResult.ValidationType validationType;
    private ValidationResult.ValidationEngine validationEngine;
    private Boolean isValid;
    private Integer errorCount;
    private Integer warningCount;
    private Object errors;
    private Object warnings;
    private Integer executionTimeMs;
    private LocalDateTime validatedAt;
    private Long validatedBy;
    private String validatedXml;

    public static ValidationResultDto fromEntity(ValidationResult entity) {
        if (entity == null) return null;
        return ValidationResultDto.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .resultUuid(entity.getResultUuid())
                .messageId(entity.getMessageId())
                .scenarioId(entity.getScenarioId())
                .validationType(entity.getValidationType())
                .validationEngine(entity.getValidationEngine())
                .isValid(entity.getIsValid())
                .errorCount(entity.getErrorCount())
                .warningCount(entity.getWarningCount())
                .errors(entity.getErrors())
                .warnings(entity.getWarnings())
                .executionTimeMs(entity.getExecutionTimeMs())
                .validatedAt(entity.getValidatedAt())
                .validatedBy(entity.getValidatedBy())
                .validatedXml(entity.getValidatedXml())
                .build();
    }
}
