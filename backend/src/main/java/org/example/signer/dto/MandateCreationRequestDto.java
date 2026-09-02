package org.example.signer.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MandateCreationRequestDto {

    // Required fields from user
    private String sourceId;
    private String destinationId;
    
    // Mandate Info
    private String mandateId;
    private String sequenceType;
    private String frequencyType;
    private String firstCollectionDate;
    private String finalCollectionDate;
    private String currency;
    private BigDecimal collectionAmount;
    private Boolean trackingIndicator;
    
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
    
    // Referenced Document
    private String referenceDocumentType;
    private String referenceDocumentNumber;
    
    // Supplementary Data - CustomData
    private String debtorAccountDesignation;
    private String debtorIdType;
    private String debtorIdValue;
    private String debtorAccountTier;
    private String debtorBiometricData;
    private String debtorAdrLine;
    private String debtorPhneNb;
    private String debtorEmailAdr;

    private String creditorAccountDesignation;
    private String creditorIdType;
    private String creditorIdValue;
    private String creditorAccountTier;

    private String transactionLocation;
    private String channelCode;
    private String mandateCategory;
    private Boolean fixedCollectionAmount;

    // Deprecated / Kept for compatibility
    private String destinationAccountNumber;
    private String destinationName;

}
