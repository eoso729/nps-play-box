package org.example.signer.dto;

import lombok.Data;

@Data
public class NameVerificationRequestDto {
    private String sourceId;
    private String beneficiaryId;
    private String partyToBeVerifiedName;
    private String partyToBeVerifiedAccountNumber;
    private String sendingPartyName;
}

//{
//        "sourceId": "999999",
//        "beneficiaryId": "999015",
//        "partyToBeVerifiedName": "Oso Emmanuel",
//        "partyToBeVerifiedAccountNumber": "1111111111",
//        "sendingPartyName": "Fidelity"
//}
