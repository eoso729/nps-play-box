package org.example.signer.dto;

import lombok.Data;

@Data
public class BalanceEnquiryRequestDto {

    // Required fields from user
    private String sourceId;
    private String destinationId;

}
