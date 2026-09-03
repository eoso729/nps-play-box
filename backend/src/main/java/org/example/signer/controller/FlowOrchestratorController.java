package org.example.signer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.orchestrator.*;
import org.example.signer.service.FlowOrchestratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class FlowOrchestratorController {

    private final FlowOrchestratorService flowOrchestratorService;

    @GetMapping("/flows")
    public ResponseEntity<List<FlowDefinitionDto>> getAllFlows() {
        try {
            return ResponseEntity.ok(flowOrchestratorService.getAllFlows());
        } catch (Exception e) {
            log.error("Error retrieving workflow journeys", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/flows/{flowId}")
    public ResponseEntity<FlowDefinitionDto> getFlowById(@PathVariable String flowId) {
        try {
            return ResponseEntity.ok(flowOrchestratorService.getFlowById(flowId));
        } catch (Exception e) {
            log.error("Error retrieving workflow journey {}", flowId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/execute-step")
    public ResponseEntity<FlowStepExecutionResponseDto> executeStep(@RequestBody FlowStepExecutionRequestDto requestDto) {
        try {
            FlowStepExecutionResponseDto response = flowOrchestratorService.executeStep(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error executing flow step", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FlowStepExecutionResponseDto.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }

    @PostMapping("/map-next-step")
    public ResponseEntity<FlowMapNextStepResponseDto> mapNextStep(@RequestBody FlowMapNextStepRequestDto requestDto) {
        try {
            FlowMapNextStepResponseDto response = flowOrchestratorService.mapNextStep(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error mapping next flow step", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/run-flow")
    public ResponseEntity<FlowAutoRunResponseDto> runFlow(@RequestBody FlowAutoRunRequestDto requestDto) {
        try {
            FlowAutoRunResponseDto response = flowOrchestratorService.autoRunFlow(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error auto running flow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(FlowAutoRunResponseDto.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        }
    }
}
