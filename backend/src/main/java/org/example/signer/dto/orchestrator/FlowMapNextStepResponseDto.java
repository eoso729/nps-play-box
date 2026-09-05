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
public class FlowMapNextStepResponseDto {
    private String flowId;
    private int targetStepIndex;
    private String targetMessageType;
    private Map<String, Object> prefilledPayload;
    private List<MappedFieldInfo> mappedFields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MappedFieldInfo {
        private String fieldKey;
        private Object value;
        private String sourceKey;
        private String description;
    }
}
