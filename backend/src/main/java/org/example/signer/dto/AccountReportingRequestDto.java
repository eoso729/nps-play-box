package org.example.signer.dto;

import lombok.Data;

@Data
public class AccountReportingRequestDto {
    private String sourceId;
    private String destinationId;
    
    // Agent BICs and Member IDs
    private String messageSenderBIC;
    private String messageSenderMemberId;
    private String accountOwnerBIC;
    private String accountOwnerMemberId;
    private String accountServicerBIC;
    private String accountServicerMemberId;
    
    // Reporting Request Info
    private String reportingRequestId;
    private String requestedMessageType; // e.g., STATEMENT, BALANCE
    
    // Account Info
    private String accountNumber;
    private String currency;
    
    // Reporting Period
    private String fromDate;
    private String toDate;
    private String reportingPeriodType;
    
    // Supplementary Data
    private String accountDesignation;
    private String idType;
    private String idValue;
    private String accountTier;
    private String transactionLocation;
    private String channelCode;
    private String fixedCollectionAmount; // Boolean as string
    private String mandateCode;
}
