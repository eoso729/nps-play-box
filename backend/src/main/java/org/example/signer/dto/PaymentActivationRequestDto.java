package org.example.signer.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentActivationRequestDto {
    private String sourceId;
    private String destinationId;
    private BigDecimal amount;

    private String sourceName;
    private String debtorName;
    private String debtorAccountNumber;
    private String creditorName;
    private String creditorAccountNumber;
    private String requestedExecutionDate;
    private String purpose;
}
