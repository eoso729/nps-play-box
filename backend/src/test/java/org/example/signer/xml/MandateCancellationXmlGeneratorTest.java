package org.example.signer.xml;

import org.example.signer.dto.MandateCancellationRequestDto;
import org.example.signer.model.MandateCancellation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MandateCancellationXmlGeneratorTest {

    @Test
    void testGenerateWithValidDto() {
        // Arrange
        MandateCancellationRequestDto dto = new MandateCancellationRequestDto();
        dto.setSourceId("999998");
        dto.setDestinationId("999997");
        dto.setOriginalMandateId("MNDT-CXL-001");
        dto.setDebtorName("Debtor Name");
        dto.setDebtorAccountNumber("0987654321");

        String msgId = "CXL-MSG-001";

        // Act
        MandateCancellation result = MandateCancellationXmlGenerator.generate(dto, msgId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMndtCxlReq());
        assertEquals(msgId, result.getMndtCxlReq().getGrpHdr().getMsgId());
        
        MandateCancellation.UndrlygCxlDtls details = result.getMndtCxlReq().getUndrlygCxlDtls();
        assertEquals("MNDT-CXL-001", details.getOrgnlMndt().getOrgnlMndtId());
        assertEquals("Debtor Name", details.getOrgnlMndt().getDbtr().getNm());
        assertEquals("0987654321", details.getOrgnlMndt().getDbtrAcct().getId().getIban());
        assertEquals("999997", details.getOrgnlMndt().getDbtrAgt().getFinInstnId().getClrSysMmbId().getMmbId());
        assertEquals("999998", details.getOrgnlMndt().getCdtrAgt().getFinInstnId().getClrSysMmbId().getMmbId());
    }

    @Test
    void testGenerateWithMinimalDto() {
        // Arrange
        MandateCancellationRequestDto dto = new MandateCancellationRequestDto();
        String msgId = "MIN-CXL-001";

        // Act
        MandateCancellation result = MandateCancellationXmlGenerator.generate(dto, msgId);

        // Assert
        assertNotNull(result);
        assertEquals(msgId, result.getMndtCxlReq().getGrpHdr().getMsgId());
        
        MandateCancellation.UndrlygCxlDtls details = result.getMndtCxlReq().getUndrlygCxlDtls();
        assertEquals("MNDT-RCUR-00061", details.getOrgnlMndt().getOrgnlMndtId());
        assertEquals("999997", details.getOrgnlMndt().getDbtrAgt().getFinInstnId().getClrSysMmbId().getMmbId());
    }
}
