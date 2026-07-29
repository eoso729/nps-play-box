package org.example.signer.dto;

import lombok.Data;

@Data
public class MandateCancellationRequestDto {
    private String sourceId;
    private String destinationId;
    private String sourceName;
    
    // Original Message Info
    private String originalMsgId;
    private String originalCreDtTm;
    
    // Cancellation Reason
    private String cancellationReasonCode;
    private String cancellationReasonDescription;
    
    // Original Mandate Info
    private String originalMandateId;
    private String sequenceType;
    private String frequencyType;
    private String firstCollectionDate;
    private String finalCollectionDate;
    private String trackingIndicator;
    
    // Creditor Info
    private String creditorName;
    private String creditorAccountNumber;
    private String creditorAgentBIC;
    private String creditorAgentMemberId;
    
    // Debtor Info
    private String debtorName;
    private String debtorAccountNumber;
    private String debtorAgentBIC;
    private String debtorAgentMemberId;
}
