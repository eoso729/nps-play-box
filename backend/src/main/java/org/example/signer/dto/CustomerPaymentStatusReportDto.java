package org.example.signer.dto;

import lombok.Data;

@Data
public class CustomerPaymentStatusReportDto {
    private String initiatingPartyName;
    private String debtorAgentBIC;
    private String debtorAgentMemberId;

    private String originalMsgId;
    private String originalMsgNmId; // e.g. "pain.001.001.12"
    private String groupStatus; // e.g. "ACSC", "RJCT", "ACCP"

    private String originalPmtInfId;
    private String statusId;
    private String originalEndToEndId;
    private String transactionStatus; // e.g. "ACSC", "RJCT"
    private String statusCode; // e.g. "000", "AM09"
    private String additionalInformation;
}
