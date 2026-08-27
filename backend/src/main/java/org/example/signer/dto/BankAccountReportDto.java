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
    private String accountServicerBIC;
    private String accountServicerMemberId;

    private String balanceType; // e.g. "CLRG"
    private BigDecimal balanceAmount;
    private String creditDebitIndicator; // "CRDT" or "DBIT"
    private String balanceDateTime;
}
