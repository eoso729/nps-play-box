package org.example.signer.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DirectDebitRequestDto {

    // --- Header & Initiating Party ---
    private String initiatorId; // 6-digit clearing code
    private String initiatingPartyName;
    private String forwardingAgentBIC;

    // --- Payment Info & Routing ---
    private String paymentInformationId;
    private String serviceLevelCode; // e.g. NURG, NIP
    private String localInstrumentCode; // e.g. NPSDD
    private String sequenceType; // FRST, RCUR, OOFF, FNAL
    private String requestedCollectionDate; // ISO date e.g. 2025-02-16Z
    private String currency; // e.g. NGN

    // --- Creditor (Payee) ---
    private String creditorId; // Creditor Agent Member ID
    private String creditorAgentBIC;
    private String creditorName;
    private String creditorIban;

    // --- Transaction Info ---
    private String instructionId;
    private String endToEndId;
    private BigDecimal amount;

    // --- Mandate Related Info ---
    private String mandateId;
    private String dtOfSgntr;
    private String frstColltnDt;
    private String fnlColltnDt;
    private String freqTp; // DAIL, WEEK, MNTH, QURT, YEAR, ADHO

    // --- Debtor (Payer) ---
    private String debtorId; // Debtor Agent Member ID
    private String debtorName;
    private String debtorIban;
    private String otherAccountIdentifier;
    private String remittanceInfo;

    // --- Supplementary Verification Data (SplmtryData) ---
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
    private Boolean fixedCollectionAmount;
    private String mandateCode;

}
