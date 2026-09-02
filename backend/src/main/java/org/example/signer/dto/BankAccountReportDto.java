package org.example.signer.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankAccountReportDto {
    private String messageRecipientName;
    private String messageRecipientBIC;
    private String originalQueryMsgId;
    private String originalQueryMsgNmId; // e.g. "camt.060.001.07"
    private String originalQueryCreDtTm;

    private String fromDateTime;
    private String toDateTime;

    private String accountNumber;
    private String currency; // e.g. "NGN"
    private String schemeCode;
    private String proprietaryScheme;
    private String accountServicerBIC;
    private String accountServicerMemberId;

    private String balanceType; // e.g. "CLRG"
    private BigDecimal balanceAmount;
    private String creditDebitIndicator; // "CRDT" or "DBIT"
    private String balanceDateTime;

    // Entry (Transaction) fields
    private BigDecimal entryAmount;
    private String entryCreditDebitIndicator; // "CRDT" or "DBIT"
    private String entryStatus; // "BOOK"
    private String entryBookingDate; // e.g. "2026-03-02Z"
    private String entryValueDate; // e.g. "2026-03-02Z"
    private String accountServicerReference;
    private String domainCode; // "PMNT"
    private String familyCode; // "RCDT"
    private String subFamilyCode; // "ESCT"
    private String instructedAgentBIC;
}
