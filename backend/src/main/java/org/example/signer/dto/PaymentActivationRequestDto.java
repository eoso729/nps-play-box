package org.example.signer.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentActivationRequestDto {
    private String sourceId;
    private String destinationId;
    private BigDecimal amount;
    private String currency;

    // Initiating Party
    private String sourceName;
    private String clientId;

    // Payment Information
    private String paymentInformationId;
    private String requestedExecutionDate;

    // Debtor
    private String debtorName;
    private String debtorAccountNumber;
    private String debtorAccountName;
    private String otherAccountId;
    private String debtorAgentBIC;
    private String debtorAgentMemberId;

    // Creditor
    private String creditorName;
    private String creditorAccountNumber;
    private String creditorAccountName;
    private String otherCreditorAccountId;
    private String creditorAgentBIC;
    private String creditorAgentMemberId;

    // Transaction Details
    private String endToEndId;
    private String purpose;

    // Supplementary Data - Debtor Info & Metadata
    private String debtorAccountDesignation;
    private String debtorIdType;
    private String debtorIdValue;
    private String debtorAccountTier;
    private String debtorBiometricData;
    private String debtorAddressLine;
    private String debtorPhoneNumber;
    private String debtorEmailAddress;

    // Supplementary Data - Creditor Info
    private String creditorAccountDesignation;
    private String creditorIdType;
    private String creditorIdValue;
    private String creditorAccountTier;

    // Supplementary Data - Transaction Info
    private String transactionLocation;
    private String channelCode;
    private String mandateCategory;
}
