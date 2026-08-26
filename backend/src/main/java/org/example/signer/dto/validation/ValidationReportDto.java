package org.example.signer.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReportDto {
    private boolean valid;                  // True if no ERRORs
    private String detectedMessageType;     // e.g. pacs.008
    private String isoCode;                 // e.g. pacs.008.001.12
    private String messageName;             // e.g. Customer Direct Credit
    private String category;                // e.g. Credit Transfer
    private int healthScore;                // 0 to 100 percentage score
    private Summary summary;
    private List<ValidationIssueDto> issues;
    private List<String> passedRules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalErrors;
        private int totalWarnings;
        private int totalInfo;
        private int totalPassed;
        private int totalFixable;
        private Map<String, Integer> categoryCounts;
    }
}
