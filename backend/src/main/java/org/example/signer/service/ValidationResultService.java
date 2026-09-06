package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.message.ValidationResultDto;
import org.example.signer.entity.ValidationResult;
import org.example.signer.exception.ResourceNotFoundException;
import org.example.signer.repository.ValidationResultRepository;
import org.example.signer.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationResultService {

    private final ValidationResultRepository validationResultRepository;

    @Transactional
    public ValidationResultDto recordValidationResult(
            Long messageId,
            Long scenarioId,
            ValidationResult.ValidationType validationType,
            ValidationResult.ValidationEngine validationEngine,
            boolean isValid,
            int errorCount,
            int warningCount,
            Object errors,
            Object warnings,
            int executionTimeMs,
            String validatedXml) {

        Long tenantId = resolveTenantId();

        ValidationResult result = ValidationResult.builder()
                .tenantId(tenantId)
                .messageId(messageId)
                .scenarioId(scenarioId)
                .validationType(validationType)
                .validationEngine(validationEngine)
                .isValid(isValid)
                .errorCount(errorCount)
                .warningCount(warningCount)
                .errors(errors)
                .warnings(warnings)
                .executionTimeMs(executionTimeMs)
                .validatedXml(validatedXml)
                .build();

        ValidationResult saved = validationResultRepository.save(result);
        log.info("Recorded validation result {} for tenant {}: isValid={}", saved.getResultUuid(), tenantId, isValid);
        return ValidationResultDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ValidationResultDto getResultByUuid(UUID resultUuid) {
        Long tenantId = resolveTenantId();
        ValidationResult result = validationResultRepository.findByTenantIdAndResultUuid(tenantId, resultUuid)
                .orElseThrow(() -> new ResourceNotFoundException("ValidationResult", "uuid", resultUuid));
        return ValidationResultDto.fromEntity(result);
    }

    @Transactional(readOnly = true)
    public List<ValidationResultDto> getResultsForMessage(Long messageId) {
        Long tenantId = resolveTenantId();
        return validationResultRepository.findByTenantIdAndMessageIdOrderByValidatedAtDesc(tenantId, messageId)
                .stream()
                .map(ValidationResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ValidationResultDto> getResultsForScenario(Long scenarioId) {
        Long tenantId = resolveTenantId();
        return validationResultRepository.findByTenantIdAndScenarioIdOrderByValidatedAtDesc(tenantId, scenarioId)
                .stream()
                .map(ValidationResultDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ValidationResultDto> listResults(
            Pageable pageable,
            ValidationResult.ValidationType validationType,
            Boolean isValid) {

        Long tenantId = resolveTenantId();
        Page<ValidationResult> page;

        if (validationType != null) {
            page = validationResultRepository.findByTenantIdAndValidationTypeOrderByValidatedAtDesc(tenantId, validationType, pageable);
        } else if (isValid != null) {
            page = validationResultRepository.findByTenantIdAndIsValidOrderByValidatedAtDesc(tenantId, isValid, pageable);
        } else {
            page = validationResultRepository.findByTenantIdOrderByValidatedAtDesc(tenantId, pageable);
        }

        return page.map(ValidationResultDto::fromEntity);
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant", "context", "No tenant context available in request");
        }
        return tenantId;
    }
}
