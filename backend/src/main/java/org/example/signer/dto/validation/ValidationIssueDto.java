package org.example.signer.dto.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationIssueDto {
    private String id;              // Unique issue ID
    private String severity;        // ERROR, WARNING, INFO
    private String category;        // SCHEMA_STRUCTURE, FIELD_LENGTH, DATA_TYPE, BUSINESS_RULE, NIBSS_METADATA, TIMEZONE_FORMAT
    private String xpath;           // e.g. /Document/FIToFICstmrCdtTrf/GrpHdr/MsgId
    private String fieldPath;       // e.g. GrpHdr.MsgId
    private String fieldName;       // e.g. Message ID
    private int lineNumber;         // Exact line in XML
    private int columnNumber;       // Exact column in XML
    private String currentValue;    // What was found in payload
    private String expected;        // Rule expectation
    private String message;         // User-friendly diagnostic message
    private String ruleCode;        // e.g. NIBSS_BVN_LEN, NPS_ID_FORMAT, UTC1_WAT_DATETIME
    private boolean autoFixable;    // True if engine can auto-repair this issue
}
