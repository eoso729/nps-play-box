package org.example.signer.xml;

import org.example.signer.dto.MandateCreationRequestDto;
import org.example.signer.model.MandateCreation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MandateCreationXmlGeneratorTest {

    @Test
    void testGenerateWithValidDto() {
        // Arrange
        MandateCreationRequestDto dto = new MandateCreationRequestDto();
        dto.setSourceId("999058");
        dto.setDestinationId("999057");
        dto.setFirstCollectionDate("2025-01-25");
        dto.setFinalCollectionDate("2026-01-25");
        dto.setCollectionAmount(new BigDecimal("50000.00"));
        dto.setCreditorName("CreditorCorp");
        dto.setDebtorName("Debtor Customer");

        String msgId = "MSG-INIT-001";
        String mandateId = "MNDT-123456";

        // Act
        MandateCreation result = MandateCreationXmlGenerator.generate(dto, msgId, mandateId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMndtInitnReq());
        assertEquals(msgId, result.getMndtInitnReq().getGrpHdr().getMsgId());
        assertEquals(mandateId, result.getMndtInitnReq().getMndt().getMndtId());
        assertEquals(new BigDecimal("50000.00"), result.getMndtInitnReq().getMndt().getColltnAmt().getValue());
        assertEquals("999057", result.getMndtInitnReq().getMndt().getDbtrAgt().getFinInstnId().getClrSysMmbId().getMmbId());
        assertEquals("999058", result.getMndtInitnReq().getMndt().getCdtrAgt().getFinInstnId().getClrSysMmbId().getMmbId());
    }

    @Test
    void testGenerateWithMinimalDto() {
        // Arrange
        MandateCreationRequestDto dto = new MandateCreationRequestDto();
        dto.setSourceId("999999");
        String msgId = "TEST-MSG-ID";
        String mandateId = "TEST-MNDT-ID";

        // Act
        MandateCreation result = MandateCreationXmlGenerator.generate(dto, msgId, mandateId);

        // Assert
        assertNotNull(result);
        assertEquals(msgId, result.getMndtInitnReq().getGrpHdr().getMsgId());
        
        // Check defaults
        assertEquals(new BigDecimal("50000.00"), result.getMndtInitnReq().getMndt().getColltnAmt().getValue());
        assertEquals("RCUR", result.getMndtInitnReq().getMndt().getOcrncs().getSeqTp());
    }
}
