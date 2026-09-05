package org.example.signer.dto.orchestrator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.signer.dto.response.ServicePushResult;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowStepExecutionResponseDto {
    private boolean success;
    private String flowId;
    private int stepIndex;
    private String messageType;
    private String messageId;
    private String plainXml;
    private String signedXml;
    private ServicePushResult serviceResponse;
    private String executionTime;

    /**
     * Newly extracted variables from this step.
     */
    private Map<String, String> extractedContext;

    /**
     * Cumulative updated context map after merging newly extracted variables.
     */
    private Map<String, String> updatedContext;

    /**
     * Recommended prefilled payload for the next step, if a next step exists in the flow.
     */
    private Map<String, Object> nextStepPrefill;

    private String nextMessageType;
    private Integer nextStepIndex;
    private String errorMessage;
}
