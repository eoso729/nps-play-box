package org.example.signer.dto;

import lombok.Data;

@Data
public class TransferResponseDto {

    private String sendingInstitutionId;
    private String receivingInstitutionId;
    private String originalMsgId;
    private String originalCreDtTm;
    private String groupStatus; // e.g., "ACSC"
    private String settlementDate;

}
