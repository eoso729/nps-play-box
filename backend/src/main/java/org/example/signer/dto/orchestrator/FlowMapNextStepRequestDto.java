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
public class FlowMapNextStepRequestDto {
    private String flowId;
    private int targetStepIndex;
    private String targetMessageType;
    private Map<String, String> context;
    private Map<String, Object> previousStepPayload;
}
