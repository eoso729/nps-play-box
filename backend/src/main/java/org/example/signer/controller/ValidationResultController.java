package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.message.ValidationResultDto;
import org.example.signer.entity.ValidationResult;
import org.example.signer.service.ValidationResultService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Validation Results", description = "Tenant-isolated ISO 20022 validation reports and compliance history")
@RestController
@RequestMapping("/api/v1/validation-results")
@RequiredArgsConstructor
public class ValidationResultController {

    private final ValidationResultService validationResultService;

    @Operation(summary = "Get validation result by UUID", description = "Retrieves validation report strictly within tenant boundary")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Validation result retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Validation result not found")
    })
    @GetMapping("/{resultUuid}")
    public ResponseEntity<ValidationResultDto> getResult(@PathVariable UUID resultUuid) {
        return ResponseEntity.ok(validationResultService.getResultByUuid(resultUuid));
    }

    @Operation(summary = "Get validation results for a message", description = "Lists all validation passes recorded for a message")
    @ApiResponse(responseCode = "200", description = "Results retrieved successfully")
    @GetMapping("/message/{messageId}")
    public ResponseEntity<List<ValidationResultDto>> getResultsForMessage(@PathVariable Long messageId) {
        return ResponseEntity.ok(validationResultService.getResultsForMessage(messageId));
    }

    @Operation(summary = "Get validation results for a test scenario", description = "Lists all validation passes for a test scenario")
    @ApiResponse(responseCode = "200", description = "Results retrieved successfully")
    @GetMapping("/scenario/{scenarioId}")
    public ResponseEntity<List<ValidationResultDto>> getResultsForScenario(@PathVariable Long scenarioId) {
        return ResponseEntity.ok(validationResultService.getResultsForScenario(scenarioId));
    }

    @Operation(summary = "List tenant validation results", description = "Paginated list with optional filters by validation type or validity")
    @ApiResponse(responseCode = "200", description = "Results listed successfully")
    @GetMapping
    public ResponseEntity<Page<ValidationResultDto>> listResults(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) ValidationResult.ValidationType validationType,
            @RequestParam(required = false) Boolean isValid) {

        return ResponseEntity.ok(validationResultService.listResults(pageable, validationType, isValid));
    }
}
