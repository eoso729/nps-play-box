package org.example.signer.dto;

import lombok.Data;

@Data
public class MandateAmendmentRequestDto {

    // Required fields from user
    private String sourceId;
    private String destinationId;
    
    // Original Message Info
    private String orgnlMsgId; // Original Message ID (Msg009)
    private String orgnlCreDtTm;
    private String orgnlMsgNmId;
    
    // Amendment Reason
    private String amdmntRsnCode;
    private String amdmntRsnProprietary;
    
    // Mandate Info
    private String mandateId;
    private String orgnlMndtId; // Original Mandate ID
    private String sequenceType;
    private String frequencyType;
    private String firstCollectionDate;
    private String finalCollectionDate;
    private Boolean trackingIndicator;
    
    // Initiating Party
    private String initiatingPartyName;

    // Creditor Info
    private String creditorName;
    private String creditorAccountName;
    private String creditorAccountNumber;
    private String creditorAgentBIC;
    private String creditorAgentMemberId;

    // Debtor Info
    private String debtorName;
    private String debtorAccountName;
    private String debtorAccountNumber;
    private String debtorAgentBIC;
    private String debtorAgentMemberId;

}
