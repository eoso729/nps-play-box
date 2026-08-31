package org.example.signer.dto;

import lombok.Data;

@Data
public class NameVerificationReportDto {

    private String sendingInstitutionId;
    private String receivingInstitutionId;
    private String receiverName;
    private String originalMsgId;
    private String originalCreDtTm;
    private String verificationResponse;
    private String verifiedAccountNumber;
    private String verifiedAccountName;
    private String reasonCode;
    private String reasonProprietary;

    // Supplementary Data fields
    private String creditorAccountDesignation;
    private String creditorIdType;
    private String creditorIdValue;
    private String creditorAccountTier;
    private String transactionRiskRating;
}
