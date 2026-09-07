package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.message.CreateTestScenarioDto;
import org.example.signer.dto.message.TestScenarioDto;
import org.example.signer.entity.TestScenario;
import org.example.signer.security.RequireDeveloper;
import org.example.signer.service.TestScenarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "Test Scenarios", description = "Tenant-isolated ISO 20022 test scenarios and automated execution runs")
@RestController
@RequestMapping("/api/v1/test-scenarios")
@RequiredArgsConstructor
public class TestScenarioController {

    private final TestScenarioService scenarioService;

    @Operation(summary = "Create test scenario", description = "Defines a new test scenario with input payload and expected output")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Test scenario created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @RequireDeveloper
    @PostMapping
    public ResponseEntity<TestScenarioDto> createScenario(@Valid @RequestBody CreateTestScenarioDto dto) {
        TestScenarioDto created = scenarioService.createScenario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get test scenario by UUID", description = "Retrieves test scenario details within tenant boundary")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Test scenario retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Test scenario not found")
    })
    @GetMapping("/{scenarioUuid}")
    public ResponseEntity<TestScenarioDto> getScenario(@PathVariable UUID scenarioUuid) {
        return ResponseEntity.ok(scenarioService.getScenarioByUuid(scenarioUuid));
    }

    @Operation(summary = "List tenant test scenarios", description = "Paginated list with optional filters by type, category, or status")
    @ApiResponse(responseCode = "200", description = "Scenarios listed successfully")
    @GetMapping
    public ResponseEntity<Page<TestScenarioDto>> listScenarios(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) TestScenario.TestCategory testCategory,
            @RequestParam(required = false) TestScenario.ScenarioStatus status) {

        return ResponseEntity.ok(scenarioService.listScenarios(pageable, messageType, testCategory, status));
    }

    @Operation(summary = "Update test scenario status", description = "Changes scenario status between DRAFT, ACTIVE, and ARCHIVED")
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    @RequireDeveloper
    @PutMapping("/{scenarioUuid}/status")
    public ResponseEntity<TestScenarioDto> updateStatus(
            @PathVariable UUID scenarioUuid,
            @RequestParam TestScenario.ScenarioStatus status) {

        return ResponseEntity.ok(scenarioService.updateScenarioStatus(scenarioUuid, status));
    }

    @Operation(summary = "Record test scenario execution run", description = "Updates last execution timestamp, counter, and run status")
    @ApiResponse(responseCode = "200", description = "Scenario run recorded successfully")
    @RequireDeveloper
    @PostMapping("/{scenarioUuid}/run")
    public ResponseEntity<TestScenarioDto> recordRun(
            @PathVariable UUID scenarioUuid,
            @RequestParam(defaultValue = "PASSED") String runStatus) {

        return ResponseEntity.ok(scenarioService.recordScenarioRun(scenarioUuid, runStatus));
    }

    @Operation(summary = "Delete test scenario", description = "Permanently removes test scenario within tenant boundaries")
    @ApiResponse(responseCode = "204", description = "Scenario deleted successfully")
    @RequireDeveloper
    @DeleteMapping("/{scenarioUuid}")
    public ResponseEntity<Void> deleteScenario(@PathVariable UUID scenarioUuid) {
        scenarioService.deleteScenario(scenarioUuid);
        return ResponseEntity.noContent().build();
    }
}
