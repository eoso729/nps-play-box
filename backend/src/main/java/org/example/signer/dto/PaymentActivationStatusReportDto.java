package org.example.signer.dto;

import lombok.Data;

@Data
public class PaymentActivationStatusReportDto {
    private String sourceId;
    private String destinationId;

    private String initiatingPartyName;
    private String creditorName;
    private String creditorAccountNumber;
    private String creditorAccountName;
    private String debtorName;
    private String debtorAccountNumber;
    private String debtorAccountName;
    private String forwardingAgentBIC;
    private String forwardingAgentMemberId;
    private String debtorAgentBIC;
    private String debtorAgentMemberId;
    private String creditorAgentBIC;
    private String creditorAgentMemberId;

    // Original Group Info & Status
    private String originalMsgId;
    private String originalMsgNmId; // e.g. "pain.013.001.11"
    private String originalCreDtTm;
    private String groupStatus; // e.g. "ACCP", "RJCT"

    // Original Payment Info
    private String originalPmtInfId;
    private String originalEndToEndId;
    private String transactionStatus; // e.g. "ACCP", "RJCT"
}
