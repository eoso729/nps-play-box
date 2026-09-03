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
public class FlowAutoRunRequestDto {
    private String flowId;
    @Builder.Default
    private String action = "GENERATE"; // "GENERATE" or "SEND"
    private Map<String, Object> initialStepPayload;
    private Map<String, String> initialContext;
}
