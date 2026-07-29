package org.example.signer.xml;

import org.example.signer.dto.TransferRequestDto;
import org.example.signer.model.Transfer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransferXmlGeneratorTest {

    @Test
    void testGenerateWithValidDto() {
        // Arrange
        TransferRequestDto dto = new TransferRequestDto();
        dto.setSourceId("999999");
        dto.setDestinationId("999015");
        dto.setAmount(new BigDecimal("10000.00"));
        dto.setSenderName("Oso Emmanuel");
        dto.setSenderAccountNumber("1415447017");
        dto.setBeneficiaryName("Fidelity");
        dto.setBeneficiaryAccountNumber("1111111111");
        dto.setNarration("Test Transfer");
        dto.setNameEnquiryMsgId("NE-12345");

        String msgId = "TRANS-001";

        // Act
        Transfer result = TransferXmlGenerator.generate(dto, msgId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getFiToFICstmrCdtTrf());
        assertEquals(msgId, result.getFiToFICstmrCdtTrf().getGrpHdr().getMsgId());
        
        Transfer.CdtTrfTxInf txInf = result.getFiToFICstmrCdtTrf().getCdtTrfTxInf();
        assertEquals(new BigDecimal("10000.00"), txInf.getIntrBkSttlmAmt().getValue());
        assertEquals("Oso Emmanuel", txInf.getDbtr().getNm());
        assertEquals("1415447017", txInf.getDbtrAcct().getId().getIban());
        assertEquals("Fidelity", txInf.getCdtr().getNm());
        assertEquals("1111111111", txInf.getCdtrAcct().getId().getIban());
        assertEquals("999999", result.getFiToFICstmrCdtTrf().getGrpHdr().getInstgAgt().getFinInstnId().getClrSysMmbId().getMmbId());
        assertEquals("999015", result.getFiToFICstmrCdtTrf().getGrpHdr().getInstdAgt().getFinInstnId().getClrSysMmbId().getMmbId());
        
        assertEquals("NE-12345", result.getFiToFICstmrCdtTrf().getSplmtryData().getEnvlp().getCustomData().getTransactionInfo().getNameEnquiryMsgId());
    }

    @Test
    void testGenerateWithMinimalDto() {
        // Arrange
        TransferRequestDto dto = new TransferRequestDto();
        dto.setSourceId("999998");
        String msgId = "MIN-TRANS-001";

        // Act
        Transfer result = TransferXmlGenerator.generate(dto, msgId);

        // Assert
        assertNotNull(result);
        assertEquals(msgId, result.getFiToFICstmrCdtTrf().getGrpHdr().getMsgId());
        assertEquals("NGN", result.getFiToFICstmrCdtTrf().getCdtTrfTxInf().getIntrBkSttlmAmt().getCcy());
        assertEquals(new BigDecimal("0.00"), result.getFiToFICstmrCdtTrf().getCdtTrfTxInf().getIntrBkSttlmAmt().getValue());
    }
}
