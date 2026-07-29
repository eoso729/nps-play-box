package org.example.signer.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DirectDebitRequestDto {

    private String initiatorId;
    private String debtorId;
    private String creditorId;
    private BigDecimal amount;
    private String mandateId;
    private String dtOfSgntr;
    private String frstColltnDt;
    private String fnlColltnDt;
    private String freqTp;
    private String debtorName;
    private String debtorIban;
    private String creditorName;
    private String creditorIban;
    private String remittanceInfo;
    private String nameEnquiryMsgId;

}
