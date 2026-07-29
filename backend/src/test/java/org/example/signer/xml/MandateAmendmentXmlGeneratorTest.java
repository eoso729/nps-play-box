package org.example.signer.xml;

import org.example.signer.dto.MandateAmendmentRequestDto;
import org.example.signer.model.MandateAmendment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MandateAmendmentXmlGeneratorTest {

    @Test
    void testGenerateWithValidDto() {
        // Arrange
        MandateAmendmentRequestDto dto = new MandateAmendmentRequestDto();
        dto.setSourceId("999998");
        dto.setDestinationId("999997");
        dto.setOrgnlMsgId("PAIN009-12345");
        dto.setOrgnlMndtId("MNDT-001");
        dto.setInitiatingPartyName("Test Corp");
        dto.setDebtorAccountNumber("1234567890");
        dto.setFirstCollectionDate("2026-05-01");
        dto.setFinalCollectionDate("2026-12-31");
        dto.setTrackingIndicator(true);

        String msgId = "AMEND-MSG-001";

        // Act
        MandateAmendment result = MandateAmendmentXmlGenerator.generate(dto, msgId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMndtAmdmntReq());
        assertEquals(msgId, result.getMndtAmdmntReq().getGrpHdr().getMsgId());
        assertEquals("Test Corp", result.getMndtAmdmntReq().getGrpHdr().getInitgPty().getNm());
        
        MandateAmendment.UndrlygAmdmntDtls details = result.getMndtAmdmntReq().getUndrlygAmdmntDtls().get(0);
        assertEquals("PAIN009-12345", details.getOrgnlMsgInf().getMsgId());
        assertEquals("MNDT-001", details.getMndt().getMndtId());
        assertEquals("1234567890", details.getMndt().getDbtrAcct().getId().getIban());
        assertTrue(details.getMndt().getTrckgInd());
        assertEquals("999997", details.getMndt().getDbtrAgt().getFinInstnId().getClrSysMmbId().getMmbId());
        assertEquals("999998", details.getMndt().getCdtrAgt().getFinInstnId().getClrSysMmbId().getMmbId());
    }

    @Test
    void testGenerateWithMinimalDto() {
        // Arrange
        MandateAmendmentRequestDto dto = new MandateAmendmentRequestDto();
        String msgId = "MIN-AMEND-001";

        // Act
        MandateAmendment result = MandateAmendmentXmlGenerator.generate(dto, msgId);

        // Assert
        assertNotNull(result);
        assertEquals(msgId, result.getMndtAmdmntReq().getGrpHdr().getMsgId());
        assertEquals("ABC Tech Pvt Ltd", result.getMndtAmdmntReq().getGrpHdr().getInitgPty().getNm());
        
        MandateAmendment.UndrlygAmdmntDtls details = result.getMndtAmdmntReq().getUndrlygAmdmntDtls().get(0);
        assertEquals("99999820260331160816119597368459797", details.getOrgnlMsgInf().getMsgId());
        assertFalse(details.getMndt().getTrckgInd());
    }
}
