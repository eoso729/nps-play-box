package org.example.signer.repository;

import org.example.signer.entity.TestScenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestScenarioRepository extends JpaRepository<TestScenario, Long>, JpaSpecificationExecutor<TestScenario> {

    Optional<TestScenario> findByTenantIdAndScenarioUuid(Long tenantId, UUID scenarioUuid);

    Optional<TestScenario> findByTenantIdAndScenarioName(Long tenantId, String scenarioName);

    Page<TestScenario> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<TestScenario> findByTenantIdAndMessageTypeOrderByCreatedAtDesc(
            Long tenantId, String messageType, Pageable pageable);

    Page<TestScenario> findByTenantIdAndTestCategoryOrderByCreatedAtDesc(
            Long tenantId, TestScenario.TestCategory testCategory, Pageable pageable);

    Page<TestScenario> findByTenantIdAndStatusOrderByCreatedAtDesc(
            Long tenantId, TestScenario.ScenarioStatus status, Pageable pageable);

    long countByTenantId(Long tenantId);
}
