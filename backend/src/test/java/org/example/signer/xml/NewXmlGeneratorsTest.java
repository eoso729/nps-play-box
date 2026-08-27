package org.example.signer.xml;

import org.example.signer.dto.*;
import org.example.signer.model.*;
import org.example.signer.Utils.XmlUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NewXmlGeneratorsTest {

    @Test
    void testPaymentStatusRequestPacs028() throws Exception {
        PaymentStatusRequestDto dto = new PaymentStatusRequestDto();
        dto.setSourceId("999057");
        dto.setDestinationId("999012");
        dto.setOriginalMsgId("99905820250802112346977904433112345");
        dto.setOriginalMsgNmId("pacs.008.001.12");
        dto.setOriginalCreDtTm("2025-02-25T00:02:35.072Z");
        dto.setOriginalTxId("99905820250802112346977904433112345");
        dto.setSettlementDate("2025-02-25");

        PaymentStatusRequest model = PaymentStatusRequestXmlGenerator.generate(dto, "99999920250829174941709740087747292");
        assertNotNull(model);
        Document doc = XmlUtils.marshalToDocument(model);
        String xml = XmlUtils.documentToString(doc);
        assertTrue(xml.contains("<FIToFIPmtStsReq>"));
        assertTrue(xml.contains("99905820250802112346977904433112345"));
    }

    @Test
    void testMandateAcceptancePain012() throws Exception {
        MandateAcceptanceReportDto dto = new MandateAcceptanceReportDto();
        dto.setOriginalMsgId("99905820251211112346125578725905163");
        dto.setOriginalMsgNmId("pain.009.001.08");
        dto.setOriginalCreDtTm("2025-12-11T16:19:15.342Z");
        dto.setOriginalMandateId("MNDT-RCUR-00001");
        dto.setAccepted("true");
        dto.setCreditorName("CreditorCorp");
        dto.setCreditorAccountNumber("5555544443");
        dto.setDebtorName("Debtor Customer");
        dto.setDebtorAccountNumber("8888899999");

        MandateAcceptanceReport model = MandateAcceptanceXmlGenerator.generate(dto, "99905820251212112349998878725905163");
        assertNotNull(model);
        Document doc = XmlUtils.marshalToDocument(model);
        String xml = XmlUtils.documentToString(doc);
        assertTrue(xml.contains("<MndtAccptncRpt>"));
        assertTrue(xml.contains("MNDT-RCUR-00001"));
        assertTrue(xml.contains("5555544443"));
    }

    @Test
    void testPaymentActivationStatusReportPain014() throws Exception {
        PaymentActivationStatusReportDto dto = new PaymentActivationStatusReportDto();
        dto.setInitiatingPartyName("Debtor Bank");
        dto.setOriginalMsgId("99905820260105102349998878725905163");
        dto.setOriginalMsgNmId("pain.013.001.11");
        dto.setGroupStatus("ACCP");
        dto.setOriginalPmtInfId("GSFPMTINF035985837");
        dto.setOriginalEndToEndId("GSF035985837-E2E");
        dto.setTransactionStatus("ACCP");
        dto.setCreditorAccountNumber("5555544443");
        dto.setDebtorAccountNumber("8888899999");

        PaymentActivationStatusReport model = PaymentActivationStatusReportXmlGenerator.generate(dto, "99905820260105122349998878725905163");
        assertNotNull(model);
        Document doc = XmlUtils.marshalToDocument(model);
        String xml = XmlUtils.documentToString(doc);
        assertTrue(xml.contains("<CdtrPmtActvtnReqStsRpt>"));
        assertTrue(xml.contains("GSFPMTINF035985837"));
        assertTrue(xml.contains("ACCP"));
    }

    @Test
    void testBankAccountReportCamt052() throws Exception {
        BankAccountReportDto dto = new BankAccountReportDto();
        dto.setOriginalQueryMsgId("99905820260302123735603795909182287");
        dto.setAccountNumber("4488447166");
        dto.setCurrency("NGN");
        dto.setBalanceType("CLRG");
        dto.setBalanceAmount(new BigDecimal("500000.00"));
        dto.setCreditDebitIndicator("CRDT");

        BankAccountReport model = BankAccountReportXmlGenerator.generate(dto, "99905820260302123914844967272332044", "99905899905720260302123735604994726");
        assertNotNull(model);
        Document doc = XmlUtils.marshalToDocument(model);
        String xml = XmlUtils.documentToString(doc);
        assertTrue(xml.contains("<BkToCstmrAcctRpt>"));
        assertTrue(xml.contains("4488447166"));
        assertTrue(xml.contains("500000.00"));
    }

    @Test
    void testBankStatementCamt053() throws Exception {
        BankStatementDto dto = new BankStatementDto();
        dto.setOriginalQueryMsgId("99905820260213292033011112202634446");
        dto.setAccountNumber("8887788778");
        dto.setCurrency("NGN");
        dto.setOpeningBalanceAmount(new BigDecimal("482000.00"));
        dto.setOpeningBalanceCdtDbtInd("CRDT");
        dto.setClosingBalanceAmount(new BigDecimal("500000.00"));
        dto.setClosingBalanceCdtDbtInd("CRDT");

        BankStatement model = BankStatementXmlGenerator.generate(dto, "99905820260213292032209209131119988", "99905899905720260213292033011112202");
        assertNotNull(model);
        Document doc = XmlUtils.marshalToDocument(model);
        String xml = XmlUtils.documentToString(doc);
        assertTrue(xml.contains("<BkToCstmrStmt>"));
        assertTrue(xml.contains("8887788778"));
        assertTrue(xml.contains("482000.00"));
    }

    @Test
    void testCustomerPaymentStatusPain002() throws Exception {
        CustomerPaymentStatusReportDto dto = new CustomerPaymentStatusReportDto();
        dto.setInitiatingPartyName("Musa");
        dto.setDebtorAgentBIC("DEUTDEFF");
        dto.setOriginalMsgId("99905720260225192650869851166984847");
        dto.setOriginalMsgNmId("pain.001.001.12");
        dto.setGroupStatus("ACSC");
        dto.setOriginalPmtInfId("PMT-20251016-001-SINGLE");
        dto.setStatusId("99905774143804655117506058383208278");
        dto.setOriginalEndToEndId("99905774143804655117506058383208278");
        dto.setTransactionStatus("ACSC");
        dto.setStatusCode("000");
        dto.setAdditionalInformation("Accepted");

        CustomerPaymentStatusReport model = CustomerPaymentStatusReportXmlGenerator.generate(dto, "99999920260225192657029842136833211", "99905774143804655117506058383208278");
        assertNotNull(model);
        Document doc = XmlUtils.marshalToDocument(model);
        String xml = XmlUtils.documentToString(doc);
        assertTrue(xml.contains("<CstmrPmtStsRpt>"));
        assertTrue(xml.contains("PMT-20251016-001-SINGLE"));
        assertTrue(xml.contains("ACSC"));
    }
}
