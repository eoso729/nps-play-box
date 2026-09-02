package org.example.signer.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BankStatementDto {
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

    private String openingBalanceCode; // "OPBD"
    private String openingBalanceType; // "CLRG"
    private BigDecimal openingBalanceAmount;
    private String openingBalanceCdtDbtInd; // "CRDT"
    private String openingBalanceDateTime;

    private String closingBalanceCode; // "CLBD"
    private String closingBalanceType; // "CLRG"
    private BigDecimal closingBalanceAmount;
    private String closingBalanceCdtDbtInd; // "CRDT"
    private String closingBalanceDate;
    private String closingBalanceDateTime;

    // Entry (Transaction) fields
    private BigDecimal entryAmount;
    private String entryCreditDebitIndicator; // "CRDT" or "DBIT"
    private String entryStatusCode; // "BOOK"
    private String entryStatus; // "BOOK"
    private String entryBookingDate; // e.g. "2025-04-20Z"
    private String entryValueDate; // e.g. "2025-04-20Z"
    private String accountServicerReference;
    private String domainCode; // "PMNT"
    private String familyCode; // "RCDT"
    private String subFamilyCode; // "ESCT"
    private String instructedAgentBIC;
}
