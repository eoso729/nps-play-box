package org.example.signer.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class TransferResponseDto {

    @JsonAlias("sourceId")
    private String sendingInstitutionId;

    @JsonAlias("destinationId")
    private String receivingInstitutionId;

    private String originalMsgId;
    private String originalMsgNmId;
    private String originalCreDtTm;
    private String groupStatus; // e.g., "ACSC"
    private String statusId;
    private String originalInstrId;
    private String originalEndToEndId;
    private String originalTxId;
    private String settlementDate;

    public String getSourceId() {
        return sendingInstitutionId;
    }

    public void setSourceId(String sourceId) {
        this.sendingInstitutionId = sourceId;
    }

    public String getDestinationId() {
        return receivingInstitutionId;
    }

    public void setDestinationId(String destinationId) {
        this.receivingInstitutionId = destinationId;
    }
}

