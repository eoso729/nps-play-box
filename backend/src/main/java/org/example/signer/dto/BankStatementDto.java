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
    private String accountServicerBIC;
    private String accountServicerMemberId;

    private String openingBalanceCode; // "OPBD"
    private String openingBalanceType; // "CLRG"
    private BigDecimal openingBalanceAmount;
    private String openingBalanceCdtDbtInd; // "CRDT"

    private String closingBalanceCode; // "CLBD"
    private String closingBalanceType; // "CLRG"
    private BigDecimal closingBalanceAmount;
    private String closingBalanceCdtDbtInd; // "CRDT"
}
