package org.example.signer.repository;

import org.example.signer.entity.ValidationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValidationResultRepository extends JpaRepository<ValidationResult, Long>, JpaSpecificationExecutor<ValidationResult> {

    Optional<ValidationResult> findByTenantIdAndResultUuid(Long tenantId, UUID resultUuid);

    List<ValidationResult> findByTenantIdAndMessageIdOrderByValidatedAtDesc(Long tenantId, Long messageId);

    List<ValidationResult> findByTenantIdAndScenarioIdOrderByValidatedAtDesc(Long tenantId, Long scenarioId);

    Page<ValidationResult> findByTenantIdOrderByValidatedAtDesc(Long tenantId, Pageable pageable);

    Page<ValidationResult> findByTenantIdAndValidationTypeOrderByValidatedAtDesc(
            Long tenantId, ValidationResult.ValidationType validationType, Pageable pageable);

    Page<ValidationResult> findByTenantIdAndIsValidOrderByValidatedAtDesc(
            Long tenantId, Boolean isValid, Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndIsValid(Long tenantId, Boolean isValid);
}
