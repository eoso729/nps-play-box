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
public class FlowStepDefinitionDto {
    private int stepIndex;
    private String stepId;
    private String messageType;
    private String isoCode;
    private String title;
    private String description;
    private String role; // e.g. "Initiator", "Debtor Bank", "Clearing Switch"
    private List<String> requiredContextKeys;
    private List<String> producedContextKeys;
    private Map<String, Object> defaultPayload;
}
