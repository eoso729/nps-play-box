package org.example.signer.dto.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowStepExecutionRequestDto {
    private String flowId;
    private int stepIndex;
    private String messageType;
    /**
     * "GENERATE" (plain & signed XML without sending) or "SEND" (push to simulator)
     */
    @Builder.Default
    private String action = "GENERATE";
    private Map<String, Object> payload;
    private Map<String, String> currentContext;
}
