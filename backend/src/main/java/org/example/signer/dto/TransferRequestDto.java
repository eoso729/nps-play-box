package org.example.signer.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequestDto {

    private String sourceId;
    private String destinationId;
    private BigDecimal amount;
    private String currency;
    
    // Party Info
    private String senderName;
    private String senderAccountNumber;
    private String senderAccountName;
    private String beneficiaryName;
    private String beneficiaryAccountNumber;
    private String beneficiaryAccountName;
    
    // Payment IDs
    private String instructionId;
    private String endToEndId;
    
    // Remittance
    private String narration;
    
    // Supplementary Data - CustomData
    private String debtorAccountDesignation;
    private String debtorIdType;
    private String debtorIdValue;
    private String debtorAccountTier;
    
    private String creditorAccountDesignation;
    private String creditorIdType;
    private String creditorIdValue;
    private String creditorAccountTier;
    private String creditorBvn; // Kept for backward compatibility

    private String transactionLocation;
    private String nameEnquiryMsgId;
    private String channelCode;
    private String riskRating;
    
    // Optional / Internal
    private String settlementMethod;
    private String clearingChannel;
    private String serviceLevel;
    private String localInstrument;
    private String categoryPurpose;
    private String chargeBearer;

}
