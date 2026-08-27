package org.example.signer.dto;

import lombok.Data;

@Data
public class MandateAcceptanceReportDto {
    private String originalMsgId;
    private String originalMsgNmId; // e.g. "pain.009.001.08"
    private String originalCreDtTm; // e.g. "2025-12-11T16:19:15.342Z"
    private String originalMandateId; // e.g. "MNDT-RCUR-00001"
    private String accepted; // "true" or "false"
    
    // Mandate terms
    private String sequenceType; // "RCUR", "OOFF"
    private String frequencyType; // "DAIL", "WEEK", "MNTH", "YEAR"
    private String firstCollectionDate; // "2025-09-08"
    private String finalCollectionDate; // "2025-12-31"
    private String trackingIndicator; // "false"

    // Creditor
    private String creditorName;
    private String creditorAccountNumber;
    private String creditorAccountName;
    private String creditorAgentBIC;
    private String creditorAgentMemberId;

    // Debtor
    private String debtorName;
    private String debtorAccountNumber;
    private String debtorAccountName;
    private String debtorAgentBIC;
    private String debtorAgentMemberId;
}
