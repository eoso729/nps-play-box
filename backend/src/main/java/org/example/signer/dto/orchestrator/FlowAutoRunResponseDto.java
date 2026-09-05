package org.example.signer.dto.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowAutoRunResponseDto {
    private boolean success;
    private String flowId;
    private String flowName;
    private int totalSteps;
    private int executedSteps;
    private List<FlowStepExecutionResponseDto> stepsTranscript;
    private Map<String, String> finalContext;
    private String executionDuration;
    private String errorMessage;
}
