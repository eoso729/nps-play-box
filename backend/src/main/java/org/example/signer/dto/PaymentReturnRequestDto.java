package org.example.signer.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentReturnRequestDto {

    // Group Header
    private String sourceId;          // Instructing agent MmbId (e.g. 999057)
    private String destinationId;     // Instructed agent MmbId (e.g. 999998)
    private String bicfi;             // BICFI of instructing agent (e.g. 999057)

    // Original Group Info
    private String originalMsgId;         // OrgnlMsgId - mandatory
    private String originalMsgNameId;     // OrgnlMsgNmId (e.g. pacs.008.001.12) - mandatory
    private String originalCreDtTm;       // OrgnlCreDtTm - mandatory

    // Transaction Info
    private String originalInstrId;       // OrgnlInstrId - mandatory
    private String originalEndToEndId;    // OrgnlEndToEndId - mandatory
    private String originalTxId;          // OrgnlTxId - mandatory
    private String originalIntrBkSttlmDt; // OrgnlIntrBkSttlmDt (yyyy-MM-dd'Z') - mandatory
    private BigDecimal returnedAmount;     // RtrdIntrBkSttlmAmt - mandatory
    private String currency;              // Currency code (e.g. NGN) - mandatory
    private String intrBkSttlmDt;        // IntrBkSttlmDt (yyyy-MM-dd'Z') - mandatory
    private String chargeBrr;            // ChrgBr (e.g. SLEV) - optional

    // Return Reason
    private String returnReasonCode;     // Prtry reason code (e.g. AC04) - mandatory
    private String returnReasonInfo;     // AddtlInf - optional

    // Original Transaction Reference
    private BigDecimal originalIntrBkSttlmAmt; // Original interbank settlement amount - mandatory
    private String clearingChannel;             // ClrChanl (e.g. RTNS) - mandatory
    private String localInstrument;             // LclInstrm Prtry (e.g. CTAA) - mandatory

    // Debtor (original payer)
    private String debtorName;              // Dbtr Nm - mandatory
    private String debtorAccountNumber;     // DbtrAcct IBAN - mandatory
    private String debtorAccountName;       // DbtrAcct Nm - mandatory
    private String debtorAgentMmbId;        // DbtrAgt MmbId - mandatory

    // Creditor (original beneficiary)
    private String creditorName;            // Cdtr Nm - mandatory
    private String creditorAccountNumber;   // CdtrAcct IBAN - mandatory
    private String creditorAgentMmbId;      // CdtrAgt MmbId - mandatory
}
