package org.example.signer.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CustomerDirectDebitRequestDto {
    private String sourceId;
    private String destinationId;
    private BigDecimal amount;
    private String currency;
    
    // Group Header Defaults
    private String instructingBankMemberId;
    private String debtorBankMemberId;
    
    // Payment IDs
    private String instructionId;
    private String endToEndId;
    private String transactionId;
    
    // Dates
    private String settlementDate;
    
    // Mandate Related Info
    private String mandateId;
    private String dateOfSignature;
    private String firstCollectionDate;
    private String finalCollectionDate;
    private String frequencyType;
    
    // Creditor Info
    private String creditorName;
    private String creditorAccountNumber;
    
    // Debtor Info
    private String debtorName;
    private String debtorAccountNumber;
    
    // Remittance
    private String narration;
    
    // Supplementary Data - CustomData
    private String debtorAccountDesignation;
    private String debtorIdType;
    private String debtorIdValue;
    private String debtorAccountTier;
    private String debtorBiometricData;
    
    private String creditorAccountDesignation;
    private String creditorIdType;
    private String creditorIdValue;
    private String creditorAccountTier;
    
    private String transactionLocation;
    private String nameEnquiryMsgId;
    private String channelCode;
    private String riskRating;
    private String fixedCollectionAmount; // In sample it's a value "2200.00", but in structure it's "true|false". Sample shows "2200.00".
}
