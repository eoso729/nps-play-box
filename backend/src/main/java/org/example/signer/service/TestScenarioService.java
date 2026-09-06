package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.message.CreateTestScenarioDto;
import org.example.signer.dto.message.TestScenarioDto;
import org.example.signer.entity.TestScenario;
import org.example.signer.exception.ResourceNotFoundException;
import org.example.signer.repository.TestScenarioRepository;
import org.example.signer.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestScenarioService {

    private final TestScenarioRepository scenarioRepository;

    @Transactional
    public TestScenarioDto createScenario(CreateTestScenarioDto dto) {
        Long tenantId = resolveTenantId();

        TestScenario scenario = TestScenario.builder()
                .tenantId(tenantId)
                .scenarioName(dto.getScenarioName())
                .scenarioDescription(dto.getScenarioDescription())
                .messageType(dto.getMessageType())
                .testCategory(dto.getTestCategory())
                .inputData(dto.getInputData())
                .expectedOutput(dto.getExpectedOutput())
                .validationRules(dto.getValidationRules())
                .status(TestScenario.ScenarioStatus.DRAFT)
                .executionCount(0)
                .build();

        TestScenario saved = scenarioRepository.save(scenario);
        log.info("Created test scenario {} ('{}') for tenant {}", saved.getScenarioUuid(), saved.getScenarioName(), tenantId);
        return TestScenarioDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TestScenarioDto getScenarioByUuid(UUID scenarioUuid) {
        Long tenantId = resolveTenantId();
        TestScenario scenario = scenarioRepository.findByTenantIdAndScenarioUuid(tenantId, scenarioUuid)
                .orElseThrow(() -> new ResourceNotFoundException("TestScenario", "uuid", scenarioUuid));
        return TestScenarioDto.fromEntity(scenario);
    }

    @Transactional(readOnly = true)
    public Page<TestScenarioDto> listScenarios(
            Pageable pageable,
            String messageType,
            TestScenario.TestCategory testCategory,
            TestScenario.ScenarioStatus status) {

        Long tenantId = resolveTenantId();
        Page<TestScenario> page;

        if (messageType != null) {
            page = scenarioRepository.findByTenantIdAndMessageTypeOrderByCreatedAtDesc(tenantId, messageType, pageable);
        } else if (testCategory != null) {
            page = scenarioRepository.findByTenantIdAndTestCategoryOrderByCreatedAtDesc(tenantId, testCategory, pageable);
        } else if (status != null) {
            page = scenarioRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status, pageable);
        } else {
            page = scenarioRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
        }

        return page.map(TestScenarioDto::fromEntity);
    }

    @Transactional
    public TestScenarioDto updateScenarioStatus(UUID scenarioUuid, TestScenario.ScenarioStatus status) {
        Long tenantId = resolveTenantId();
        TestScenario scenario = scenarioRepository.findByTenantIdAndScenarioUuid(tenantId, scenarioUuid)
                .orElseThrow(() -> new ResourceNotFoundException("TestScenario", "uuid", scenarioUuid));

        scenario.setStatus(status);
        scenario.setUpdatedAt(LocalDateTime.now());
        TestScenario saved = scenarioRepository.save(scenario);
        return TestScenarioDto.fromEntity(saved);
    }

    @Transactional
    public TestScenarioDto recordScenarioRun(UUID scenarioUuid, String runStatus) {
        Long tenantId = resolveTenantId();
        TestScenario scenario = scenarioRepository.findByTenantIdAndScenarioUuid(tenantId, scenarioUuid)
                .orElseThrow(() -> new ResourceNotFoundException("TestScenario", "uuid", scenarioUuid));

        scenario.setLastRunAt(LocalDateTime.now());
        scenario.setLastRunStatus(runStatus);
        scenario.setExecutionCount(scenario.getExecutionCount() + 1);
        scenario.setUpdatedAt(LocalDateTime.now());

        TestScenario saved = scenarioRepository.save(scenario);
        return TestScenarioDto.fromEntity(saved);
    }

    @Transactional
    public void deleteScenario(UUID scenarioUuid) {
        Long tenantId = resolveTenantId();
        TestScenario scenario = scenarioRepository.findByTenantIdAndScenarioUuid(tenantId, scenarioUuid)
                .orElseThrow(() -> new ResourceNotFoundException("TestScenario", "uuid", scenarioUuid));
        scenarioRepository.delete(scenario);
        log.info("Deleted test scenario {} for tenant {}", scenarioUuid, tenantId);
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant", "context", "No tenant context available in request");
        }
        return tenantId;
    }
}
