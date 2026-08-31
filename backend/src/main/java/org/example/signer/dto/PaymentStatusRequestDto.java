package org.example.signer.dto;

import lombok.Data;

@Data
public class PaymentStatusRequestDto {
    private String sourceId;
    private String destinationId;
    private String originalMsgId;
    private String originalMsgNmId; // e.g. "pacs.008.001.12"
    private String originalCreDtTm;
    private String originalTxId;
    private String settlementDate; // e.g. "2025-02-25"
}
