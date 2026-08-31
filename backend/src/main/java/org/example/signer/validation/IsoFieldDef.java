package org.example.signer.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsoFieldDef {
    private String fieldName;       // e.g. "Message ID"
    private String xmlPath;         // e.g. "GrpHdr.MsgId"
    private String xpath;           // e.g. "//GrpHdr/MsgId"
    private String sampleValue;     // e.g. "99905820250801205622930239203831721"
    private String valueType;       // String, DateTime, Date, Decimal, Integer, Boolean, Enum
    private int maxLength;          // Max length or 0 if N/A
    private boolean mandatory;      // true if mandatory
    private boolean conditional;    // true if conditional
    private String ruleType;        // e.g. "BVN", "NUBAN", "CHANNEL_CODE", "NPS_ID", "ACCOUNT_DESIGNATION", "ACCOUNT_TIER", "ID_TYPE", "UTC1_DATETIME", "REASON_CODE"
    private String description;     // Description / notes
}
