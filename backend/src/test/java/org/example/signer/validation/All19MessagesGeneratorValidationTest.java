package org.example.signer.validation;

import org.example.signer.Utils.XmlUtils;
import org.example.signer.dto.*;
import org.example.signer.dto.validation.ValidationReportDto;
import org.example.signer.model.*;
import org.example.signer.xml.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end generator and validation test verifying that every single one of the 19 supported
 * ISO 20022 message generators generates valid XML matching NIBSS specifications.
 */
class All19MessagesGeneratorValidationTest {

    private XmlValidationEngine validationEngine;

    @BeforeEach
    void setUp() {
        validationEngine = new XmlValidationEngine();
    }

    private void assertValid(String messageType, Object model) throws Exception {
        assertNotNull(model, "Model for " + messageType + " should not be null");
        Document doc = XmlUtils.marshalToDocument(model);
        String xml = XmlUtils.documentToString(doc);
        assertNotNull(xml, "Generated XML for " + messageType + " should not be null");

        ValidationReportDto report = validationEngine.validate(xml, messageType);
        assertNotNull(report);
        assertTrue(report.isValid(), "Validation failed for " + messageType + ": " + report.getIssues());
        assertNotNull(report.getSummary());
        assertEquals(0, report.getSummary().getTotalErrors(), "Errors found in generated " + messageType + ": " + report.getIssues());
        assertTrue(report.getHealthScore() >= 95, "Health score for " + messageType + " was " + report.getHealthScore());
    }

    // ==========================================
    // Group 1: Credit Transfer & Returns
    // ==========================================

    @Test
    @DisplayName("pacs.008: Customer Direct Credit Transfer Generator Validation")
    void testPacs008Generation() throws Exception {
        TransferRequestDto dto = new TransferRequestDto();
        dto.setSourceId("999997");
        dto.setDestinationId("991040");
        dto.setAmount(new BigDecimal("200.00"));
        dto.setSenderName("Ponmile Joy");
        dto.setSenderAccountNumber("3157417712");
        dto.setBeneficiaryName("James");
        dto.setBeneficiaryAccountNumber("5000002101");
        dto.setNameEnquiryMsgId("99999720260820104556011402881687033");
        dto.setDebtorIdValue("22383706153");
        dto.setCreditorIdValue("22383706153");

        Transfer model = TransferXmlGenerator.generate(dto, "99999720260830230000123456789012345");
        assertValid("pacs.008", model);
    }

    @Test
    @DisplayName("pacs.002: Payment Status Report Generator Validation")
    void testPacs002Generation() throws Exception {
        TransferResponseDto dto = new TransferResponseDto();
        dto.setSendingInstitutionId("090004");
        dto.setReceivingInstitutionId("100022");
        dto.setOriginalMsgId("99905820250802112346977904433112345");
        dto.setOriginalMsgNmId("pacs.008.001.12");
        dto.setOriginalCreDtTm("2026-01-05T10:27:26.737+01:00");
        dto.setGroupStatus("ACSC");
        dto.setStatusId("AUTH");
        dto.setOriginalInstrId("99905810002220250802112346123456789");
        dto.setOriginalEndToEndId("99905820250802112346977904433112345");
        dto.setOriginalTxId("99905820250802112346977904433112345");
        dto.setSettlementDate("2026-02-25Z");

        TransferResponse model = TransferResponseXmlGenerator.generate(dto);
        assertValid("pacs.002", model);
    }

    @Test
    @DisplayName("pacs.028: Payment Status Request Generator Validation")
    void testPacs028Generation() throws Exception {
        PaymentStatusRequestDto dto = new PaymentStatusRequestDto();
        dto.setSourceId("999057");
        dto.setDestinationId("999012");
        dto.setOriginalMsgId("99905820250802112346977904433112345");
        dto.setOriginalMsgNmId("pacs.008.001.12");
        dto.setOriginalCreDtTm("2025-02-25T00:02:35.072+01:00");
        dto.setOriginalTxId("99905820250802112346977904433112345");
        dto.setSettlementDate("2025-02-25Z");

        PaymentStatusRequest model = PaymentStatusRequestXmlGenerator.generate(dto, "99999920250829174941709740087747292");
        assertValid("pacs.028", model);
    }

    @Test
    @DisplayName("pacs.004: Payment Return Generator Validation")
    void testPacs004Generation() throws Exception {
        PaymentReturnRequestDto dto = new PaymentReturnRequestDto();
        dto.setSourceId("999057");
        dto.setDestinationId("999058");
        dto.setOriginalMsgId("99905820250802112346977904433112345");
        dto.setOriginalMsgNameId("pacs.008.001.12");
        dto.setOriginalCreDtTm("2025-08-02T11:23:46.000+01:00");
        dto.setOriginalInstrId("99905899905720250802112346977904433");
        dto.setOriginalEndToEndId("99905820250802112346977904433112345");
        dto.setOriginalTxId("99905820250802112346977904433112345");
        dto.setOriginalIntrBkSttlmDt("2025-08-02Z");
        dto.setReturnedAmount(new BigDecimal("1000.00"));
        dto.setOriginalIntrBkSttlmAmt(new BigDecimal("1000.00"));
        dto.setReturnReasonCode("AC04");
        dto.setDebtorName("John Doe");
        dto.setDebtorAccountNumber("0123456789");
        dto.setCreditorName("Jane Smith");
        dto.setCreditorAccountNumber("9876543210");

        PaymentReturn model = PaymentReturnXmlGenerator.generate(dto, "99905720250803120000123456789012345");
        assertValid("pacs.004", model);
    }

    // ==========================================
    // Group 2: Mandate Management
    // ==========================================

    @Test
    @DisplayName("pain.009: Mandate Initiation Request Generator Validation")
    void testPain009Generation() throws Exception {
        MandateCreationRequestDto dto = new MandateCreationRequestDto();
        dto.setSourceId("999998");
        dto.setDestinationId("999997");
        dto.setCreditorName("INNOVATECH SOLUTIONS");
        dto.setCreditorAccountNumber("0987654321");
        dto.setDebtorName("ACME TECHNOLOGIES LTD");
        dto.setDebtorAccountNumber("0123456789");
        dto.setCollectionAmount(new BigDecimal("2200.00"));
        dto.setSequenceType("RCUR");
        dto.setFrequencyType("MNTH");
        dto.setFirstCollectionDate("2026-02-01");
        dto.setFinalCollectionDate("2027-01-31");

        MandateCreation model = MandateCreationXmlGenerator.generate(dto, "99999820260830230000123456789012345", "MNDT-RCUR-123456");
        assertValid("pain.009", model);
    }

    @Test
    @DisplayName("pain.010: Mandate Amendment Request Generator Validation")
    void testPain010Generation() throws Exception {
        MandateAmendmentRequestDto dto = new MandateAmendmentRequestDto();
        dto.setSourceId("999998");
        dto.setDestinationId("999997");
        dto.setOrgnlMsgId("99999820260331160816119597368459797");
        dto.setOrgnlMsgNmId("pain.009.001.08");
        dto.setOrgnlCreDtTm("2026-03-31T16:08:16+01:00");
        dto.setOrgnlMndtId("MNDT-RCUR-00061");
        dto.setAmdmntRsnCode("AC04");
        dto.setCreditorName("Tester");
        dto.setCreditorAccountNumber("0987654320");
        dto.setDebtorName("Ponmile Joy");
        dto.setDebtorAccountNumber("3157417712");
        dto.setSequenceType("RCUR");
        dto.setFrequencyType("MNTH");
        dto.setFirstCollectionDate("2026-04-01");
        dto.setFinalCollectionDate("2026-04-30");

        MandateAmendment model = MandateAmendmentXmlGenerator.generate(dto, "99999820260830230000123456789012345");
        assertValid("pain.010", model);
    }

    @Test
    @DisplayName("pain.011: Mandate Cancellation Request Generator Validation")
    void testPain011Generation() throws Exception {
        MandateCancellationRequestDto dto = new MandateCancellationRequestDto();
        dto.setSourceId("999998");
        dto.setDestinationId("999997");
        dto.setOriginalMsgId("99999820260331160816119597368459797");
        dto.setOriginalCreDtTm("2026-03-31T16:08:16+01:00");
        dto.setOriginalMandateId("MNDT-RCUR-00061");
        dto.setCancellationReasonCode("AC04");
        dto.setCancellationReasonDescription("Mandate cancelled");
        dto.setCreditorName("Tester");
        dto.setCreditorAccountNumber("0987654320");
        dto.setDebtorName("Ponmile Joy");
        dto.setDebtorAccountNumber("3157417712");
        dto.setSequenceType("RCUR");
        dto.setFrequencyType("MNTH");
        dto.setFirstCollectionDate("2026-04-01");
        dto.setFinalCollectionDate("2026-04-30");

        MandateCancellation model = MandateCancellationXmlGenerator.generate(dto, "99905720251222112349998878725905163");
        assertValid("pain.011", model);
    }

    @Test
    @DisplayName("pain.012: Mandate Acceptance Report Generator Validation")
    void testPain012Generation() throws Exception {
        MandateAcceptanceReportDto dto = new MandateAcceptanceReportDto();
        dto.setCreditorAgentMemberId("999058");
        dto.setOriginalMsgId("99905820251211112346125578725905163");
        dto.setOriginalMsgNmId("pain.009.001.08");
        dto.setOriginalCreDtTm("2025-12-11T16:19:15.342+01:00");
        dto.setOriginalMandateId("MNDT-RCUR-00001");
        dto.setAccepted("true");
        dto.setCreditorName("CreditorCorp");
        dto.setCreditorAccountNumber("5555544443");
        dto.setDebtorName("Debtor Customer");
        dto.setDebtorAccountNumber("8888899999");
        dto.setSequenceType("RCUR");
        dto.setFrequencyType("WEEK");
        dto.setFirstCollectionDate("2026-01-05");
        dto.setFinalCollectionDate("2026-12-31");

        MandateAcceptanceReport model = MandateAcceptanceXmlGenerator.generate(dto, "99905820251212112349998878725905163");
        assertValid("pain.012", model);
    }

    // ==========================================
    // Group 3: Direct Debit Operations
    // ==========================================

    @Test
    @DisplayName("pacs.003: Customer Direct Debit Transfer Generator Validation")
    void testPacs003Generation() throws Exception {
        CustomerDirectDebitRequestDto dto = new CustomerDirectDebitRequestDto();
        dto.setSourceId("999998");
        dto.setDestinationId("999997");
        dto.setCurrency("NGN");
        dto.setAmount(new BigDecimal("2200.00"));
        dto.setMandateId("MNDT-RCUR-123456");
        dto.setDateOfSignature("2026-01-01");
        dto.setFirstCollectionDate("2026-02-01");
        dto.setFinalCollectionDate("2027-01-31");
        dto.setFrequencyType("MNTH");
        dto.setCreditorName("INNOVATECH SOLUTIONS");
        dto.setCreditorAccountNumber("0987654321");
        dto.setDebtorName("ACME TECHNOLOGIES LTD");
        dto.setDebtorAccountNumber("0123456789");
        dto.setNarration("Monthly subscription fee");
        dto.setChannelCode("3");

        CustomerDirectDebit model = CustomerDirectDebitXmlGenerator.generate(dto, "99999820260830230000123456789012345");
        assertValid("pacs.003", model);
    }

    @Test
    @DisplayName("pain.008: Customer Direct Debit Initiation Generator Validation")
    void testPain008Generation() throws Exception {
        DirectDebitRequestDto dto = new DirectDebitRequestDto();
        dto.setInitiatorId("999057");
        dto.setDebtorId("999057");
        dto.setCreditorId("999058");
        dto.setAmount(new BigDecimal("100.00"));
        dto.setMandateId("0000004/001/0000070986");
        dto.setDtOfSgntr("2025-02-01Z");
        dto.setFrstColltnDt("2025-02-16Z");
        dto.setFnlColltnDt("2025-12-31Z");
        dto.setFreqTp("MNTH");
        dto.setCreditorName("ACME BILLING LIMITED");
        dto.setCreditorIban("3157417712");
        dto.setDebtorName("JOHN DOE");
        dto.setDebtorIban("0177136558");
        dto.setRemittanceInfo("UTILITY BILL FEB-2025");

        String msgId = "99905720260312195134657916589823152";
        String endToEndId = "99905720260312195134657916589823152";
        String instrId = "99905799905820260312195134657916589";

        DirectDebit model = DirectDebitXmlGenerator.generate(dto, msgId, endToEndId, instrId);
        assertValid("pain.008", model);
    }

    // ==========================================
    // Group 4: Payment Initiation & Activation
    // ==========================================

    @Test
    @DisplayName("pain.001: Customer Credit Transfer Initiation Generator Validation")
    void testPain001Generation() throws Exception {
        PaymentInitiationRequestDto dto = new PaymentInitiationRequestDto();
        dto.setInitiatorId("999057");
        dto.setDebtorId("999057");
        dto.setCreditorId("999058");
        dto.setAmount(new BigDecimal("120.51"));

        String msgId = "99905720260217195134657916589823152";
        String endToEndId = "99905720260217195134657916589823152";
        String reqdExctnDt = "2026-02-17";

        PaymentInitiation model = PaymentInitiationXmlGenerator.generate(dto, msgId, endToEndId, reqdExctnDt);
        assertValid("pain.001", model);
    }

    @Test
    @DisplayName("pain.002: Customer Payment Status Report Generator Validation")
    void testPain002Generation() throws Exception {
        CustomerPaymentStatusReportDto dto = new CustomerPaymentStatusReportDto();
        dto.setInitiatingPartyName("Musa");
        dto.setDebtorAgentBIC("DEUTDEFF");
        dto.setDebtorAgentMemberId("999057");
        dto.setOriginalMsgId("99905720260225192650869851166984847");
        dto.setOriginalMsgNmId("pain.001.001.12");
        dto.setGroupStatus("ACSC");
        dto.setOriginalPmtInfId("PMT-20251016-001-SINGLE");
        dto.setStatusId("99905774143804655117506058383208278");
        dto.setOriginalEndToEndId("99905774143804655117506058383208278");
        dto.setTransactionStatus("ACSC");
        dto.setStatusCode("000");
        dto.setAdditionalInformation("Accepted");

        CustomerPaymentStatusReport model = CustomerPaymentStatusReportXmlGenerator.generate(
                dto, "99999920260225192657029842136833211", "99905774143804655117506058383208278");
        assertValid("pain.002", model);
    }

    @Test
    @DisplayName("pain.013: Creditor Payment Activation Request Generator Validation")
    void testPain013Generation() throws Exception {
        PaymentActivationRequestDto dto = new PaymentActivationRequestDto();
        dto.setSourceId("999997");
        dto.setDestinationId("991015");
        dto.setSourceName("Ponmile Joy");
        dto.setDebtorName("Tunde Fabiyi");
        dto.setDebtorAccountNumber("0000002110");
        dto.setCreditorName("Ponmile Joy");
        dto.setCreditorAccountNumber("3157417712");
        dto.setAmount(new BigDecimal("1000.00"));
        dto.setRequestedExecutionDate("2026-08-24T10:00:00+01:00");
        dto.setPurpose("Testing 013");

        PaymentActivation model = PaymentActivationXmlGenerator.generate(
                dto, "99999720260830230000123456789012345", "99999720260830230000123456789012345");
        assertValid("pain.013", model);
    }

    @Test
    @DisplayName("pain.014: Payment Activation Status Report Generator Validation")
    void testPain014Generation() throws Exception {
        PaymentActivationStatusReportDto dto = new PaymentActivationStatusReportDto();
        dto.setInitiatingPartyName("Debtor Bank");
        dto.setCreditorName("CreditorCorp");
        dto.setCreditorAccountNumber("5555544443");
        dto.setCreditorAccountName("CreditorCorp");
        dto.setDebtorName("Debtor Customer");
        dto.setDebtorAccountNumber("8888899999");
        dto.setDebtorAccountName("Debtor Customer");
        dto.setForwardingAgentMemberId("999057");
        dto.setDebtorAgentMemberId("999058");
        dto.setCreditorAgentMemberId("999057");
        dto.setOriginalMsgId("99905820260105102349998878725905163");
        dto.setOriginalMsgNmId("pain.013.001.11");
        dto.setOriginalCreDtTm("2026-01-05T10:27:26.737+01:00");
        dto.setGroupStatus("ACCP");
        dto.setOriginalPmtInfId("GSFPMTINF035985837");
        dto.setOriginalEndToEndId("GSF035985837-E2E");
        dto.setTransactionStatus("ACCP");

        PaymentActivationStatusReport model = PaymentActivationStatusReportXmlGenerator.generate(
                dto, "99905820260105122349998878725905163");
        assertValid("pain.014", model);
    }

    // ==========================================
    // Group 5: Account Services & Statements
    // ==========================================

    @Test
    @DisplayName("acmt.023: Identification Verification Request Generator Validation")
    void testAcmt023Generation() throws Exception {
        NameVerificationRequestDto dto = new NameVerificationRequestDto();
        dto.setSourceId("999997");
        dto.setBeneficiaryId("991040");
        dto.setSendingPartyName("Ponmile Joy");
        dto.setPartyToBeVerifiedAccountNumber("5000002100");
        dto.setPartyToBeVerifiedName("James");

        NameVerification model = NameVerificationXmlGenerator.generate(dto, "99999720260830230000123456789012345");
        assertValid("acmt.023", model);
    }

    @Test
    @DisplayName("acmt.024: Identification Verification Report Generator Validation")
    void testAcmt024Generation() throws Exception {
        NameVerificationReportDto dto = new NameVerificationReportDto();
        dto.setSendingInstitutionId("999012");
        dto.setReceivingInstitutionId("999057");
        dto.setReceiverName("Assigned Org");
        dto.setOriginalMsgId("99905720260113101903741123456789012");
        dto.setOriginalCreDtTm("2026-01-13T10:19:03.741+01:00");
        dto.setVerificationResponse("true");
        dto.setVerifiedAccountNumber("1000000001");
        dto.setVerifiedAccountName("JOHN DOE ENTERPRISES");

        NameVerificationReport model = NameVerificationReportXmlGenerator.generate(dto);
        assertValid("acmt.024", model);
    }

    @Test
    @DisplayName("camt.060: Account Reporting Request Generator Validation")
    void testCamt060Generation() throws Exception {
        AccountReportingRequestDto dto = new AccountReportingRequestDto();
        dto.setSourceId("999998");
        dto.setDestinationId("999997");
        dto.setAccountNumber("0123456789");
        dto.setCurrency("NGN");
        dto.setAccountDesignation("1");
        dto.setChannelCode("1");
        dto.setRequestedMessageType("BALANCE");
        dto.setFromDate("2026-01-01");
        dto.setToDate("2026-01-31");

        BalanceEnquiry model = BalanceEnquiryXmlGenerator.generate(
                dto, "99999820260830230000123456789012345", "99999899999720260830230000123456789");
        assertValid("camt.060", model);
    }

    @Test
    @DisplayName("camt.052: Bank To Customer Account Report Generator Validation")
    void testCamt052Generation() throws Exception {
        BankAccountReportDto dto = new BankAccountReportDto();
        dto.setAccountServicerMemberId("999058");
        dto.setSchemeCode("999057");
        dto.setOriginalQueryMsgId("99905820260302123735603795909182287");
        dto.setAccountNumber("4488447166");
        dto.setCurrency("NGN");
        dto.setBalanceType("CLRG");
        dto.setBalanceAmount(new BigDecimal("500000.00"));
        dto.setCreditDebitIndicator("CRDT");

        BankAccountReport model = BankAccountReportXmlGenerator.generate(
                dto, "99905820260302123914844967272332044", "99905899905720260302123735604994726");
        assertValid("camt.052", model);
    }

    @Test
    @DisplayName("camt.053: Bank To Customer Statement Generator Validation")
    void testCamt053Generation() throws Exception {
        BankStatementDto dto = new BankStatementDto();
        dto.setAccountServicerMemberId("999058");
        dto.setSchemeCode("999057");
        dto.setOriginalQueryMsgId("99905820260213292033011112202634446");
        dto.setAccountNumber("8887788778");
        dto.setCurrency("NGN");
        dto.setOpeningBalanceAmount(new BigDecimal("482000.00"));
        dto.setOpeningBalanceCdtDbtInd("CRDT");
        dto.setClosingBalanceAmount(new BigDecimal("500000.00"));
        dto.setClosingBalanceCdtDbtInd("CRDT");

        BankStatement model = BankStatementXmlGenerator.generate(
                dto, "99905820260213292032209209131119988", "99905899905720260213292033011112202");
        assertValid("camt.053", model);
    }
}
