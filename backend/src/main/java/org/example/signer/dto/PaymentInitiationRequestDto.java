package org.example.signer.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentInitiationRequestDto {

    // Required fields from user
    private String initiatorId;
    private String debtorId;
    private String creditorId;
    
    // Optional amount field - defaults to 120.51 if not provided
    private BigDecimal amount;

}
