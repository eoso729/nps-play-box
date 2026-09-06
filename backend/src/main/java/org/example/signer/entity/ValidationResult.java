package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "validation_results", indexes = {
    @Index(name = "idx_validation_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_validation_message_id", columnList = "tenant_id,message_id"),
    @Index(name = "idx_validation_scenario_id", columnList = "tenant_id,scenario_id"),
    @Index(name = "idx_validation_tenant_type", columnList = "tenant_id,validation_type"),
    @Index(name = "idx_validation_tenant_valid", columnList = "tenant_id,is_valid"),
    @Index(name = "idx_validation_created_at", columnList = "tenant_id,validated_at DESC")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "result_uuid", unique = true, nullable = false, updatable = false)
    private UUID resultUuid;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "scenario_id")
    private Long scenarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_type", nullable = false, length = 100)
    private ValidationType validationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_engine", nullable = false, length = 100)
    private ValidationEngine validationEngine;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "warning_count")
    private Integer warningCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "errors", columnDefinition = "jsonb")
    private Object errors;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "warnings", columnDefinition = "jsonb")
    private Object warnings;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "validated_at", nullable = false, updatable = false)
    private LocalDateTime validatedAt;

    @Column(name = "validated_by")
    private Long validatedBy;

    @Column(name = "validated_xml", columnDefinition = "TEXT")
    private String validatedXml;

    @PrePersist
    protected void onCreate() {
        if (resultUuid == null) {
            resultUuid = UUID.randomUUID();
        }
        if (validatedAt == null) {
            validatedAt = LocalDateTime.now();
        }
        if (errorCount == null) {
            errorCount = 0;
        }
        if (warningCount == null) {
            warningCount = 0;
        }
        if (isValid == null) {
            isValid = (errorCount == 0);
        }
    }

    public enum ValidationType {
        SCHEMA,
        BUSINESS_RULES,
        NIBSS_RULES,
        SECURITY,
        COMPREHENSIVE
    }

    public enum ValidationEngine {
        XSD,
        NIBSS_VALIDATOR,
        CUSTOM,
        COMBINED
    }
}
