package org.example.signer.xml;

import org.example.signer.dto.NameVerificationReportDto;
import org.example.signer.model.NameVerificationReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameVerificationReportXmlGeneratorTest {

    @Test
    void testGenerateSuccessReport() {
        NameVerificationReportDto dto = new NameVerificationReportDto();
        dto.setSendingInstitutionId("999012");
        dto.setReceivingInstitutionId("999999");
        dto.setReceiverName("Oso International Bank");
        dto.setOriginalMsgId("99999920250829150504887742643314693");
        dto.setOriginalCreDtTm("2025-08-29T15:05:04.347Z");
        dto.setVerificationResponse(true);
        dto.setVerifiedAccountNumber("1029384756");
        dto.setVerifiedAccountName("Emmanuel Oso");
        dto.setCreditorAccountDesignation("1");
        dto.setCreditorIdType("BVN");
        dto.setCreditorIdValue("2211232346");
        dto.setCreditorAccountTier("1");
        dto.setTransactionRiskRating("R000000000000000000B9");

        String msgId = "99901220250829140722546736145961156";

        NameVerificationReport result = NameVerificationReportXmlGenerator.generate(dto, msgId);

        assertNotNull(result);
        assertNotNull(result.getIdVrfctnRpt());
        assertEquals(msgId, result.getIdVrfctnRpt().getAssgnmt().getMsgId());
        assertEquals("999012", result.getIdVrfctnRpt().getAssgnmt().getAssgnr().getAgt().getFinInstnId().getClrSysMmbId().getMmbId());
        assertEquals("Oso International Bank", result.getIdVrfctnRpt().getAssgnmt().getAssgne().getPty().getNm());
        assertEquals("999999", result.getIdVrfctnRpt().getAssgnmt().getAssgne().getAgt().getFinInstnId().getClrSysMmbId().getMmbId());

        assertEquals("99999920250829150504887742643314693", result.getIdVrfctnRpt().getOrgnlAssgnmt().getMsgId());
        assertEquals("2025-08-29T15:05:04.347Z", result.getIdVrfctnRpt().getOrgnlAssgnmt().getCreDtTm());

        assertTrue(result.getIdVrfctnRpt().getRpt().isVrfctn());
        assertNull(result.getIdVrfctnRpt().getRpt().getRsn());
        assertEquals("1029384756", result.getIdVrfctnRpt().getRpt().getOrgnlPtyAndAcctId().getAcct().getId().getIban());
        assertEquals("Emmanuel Oso", result.getIdVrfctnRpt().getRpt().getUpdtdPtyAndAcctId().getPty().getNm());

        assertEquals("AdditionalVerificationDetails", result.getIdVrfctnRpt().getSplmtryData().getPlcAndNm());
        assertEquals("BVN", result.getIdVrfctnRpt().getSplmtryData().getEnvlp().getCustomData().getCreditorInfo().getIdType());
        assertEquals("2211232346", result.getIdVrfctnRpt().getSplmtryData().getEnvlp().getCustomData().getCreditorInfo().getIdValue());
    }

    @Test
    void testGenerateFailureReport() {
        NameVerificationReportDto dto = new NameVerificationReportDto();
        dto.setSendingInstitutionId("999012");
        dto.setReceivingInstitutionId("999999");
        dto.setReceiverName("Oso International Bank");
        dto.setOriginalMsgId("99999920250829150504887742643314693");
        dto.setOriginalCreDtTm("2025-08-29T15:05:04.347Z");
        dto.setVerificationResponse(false);
        dto.setReasonCode("33");
        dto.setReasonProprietary("Account number mismatch");
        dto.setVerifiedAccountNumber("1029384756");

        String msgId = "FAIL-001";

        NameVerificationReport result = NameVerificationReportXmlGenerator.generate(dto, msgId);

        assertNotNull(result);
        assertFalse(result.getIdVrfctnRpt().getRpt().isVrfctn());
        assertNotNull(result.getIdVrfctnRpt().getRpt().getRsn());
        assertEquals("33", result.getIdVrfctnRpt().getRpt().getRsn().getCd());
        assertEquals("Account number mismatch", result.getIdVrfctnRpt().getRpt().getRsn().getPrtry());
        assertNull(result.getIdVrfctnRpt().getRpt().getUpdtdPtyAndAcctId());
    }
}
