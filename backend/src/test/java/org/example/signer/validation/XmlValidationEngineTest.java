package org.example.signer.validation;

import org.example.signer.dto.validation.ValidationIssueDto;
import org.example.signer.dto.validation.ValidationReportDto;
import org.example.signer.dto.validation.XmlAutoFixRequestDto;
import org.example.signer.dto.validation.XmlAutoFixResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class XmlValidationEngineTest {

    private XmlValidationEngine validationEngine;
    private XmlAutoFixEngine autoFixEngine;

    @BeforeEach
    public void setUp() {
        validationEngine = new XmlValidationEngine();
        autoFixEngine = new XmlAutoFixEngine(validationEngine);
    }

    @Test
    public void testAll19SamplesValidateSuccessfully() {
        for (IsoMessageDefinition def : IsoMessageRegistry.getAllDefinitions()) {
            assertNotNull(def.getSampleXml(), "Sample XML should not be null for " + def.getKey());
            ValidationReportDto report = validationEngine.validate(def.getSampleXml(), def.getKey());
            
            assertTrue(report.isValid(), "Sample for " + def.getKey() + " should be valid. Errors: " + 
                    report.getIssues().stream().filter(i -> "ERROR".equals(i.getSeverity())).map(ValidationIssueDto::getMessage).toList());
            assertEquals(def.getKey(), report.getDetectedMessageType(), "Detected message type mismatch for " + def.getKey());
            assertTrue(report.getHealthScore() >= 80, "Health score for sample " + def.getKey() + " should be high");
        }
    }

    @Test
    public void testAutoFixPreservesAll19ValidSamplesWithoutAddingErrors() {
        for (IsoMessageDefinition def : IsoMessageRegistry.getAllDefinitions()) {
            XmlAutoFixRequestDto req = XmlAutoFixRequestDto.builder()
                    .xmlContent(def.getSampleXml())
                    .messageType(def.getKey())
                    .fixDates(true)
                    .fixIds(true)
                    .fixSupplementaryData(true)
                    .build();

            XmlAutoFixResponseDto resp = autoFixEngine.autoFix(req);
            assertTrue(resp.isSuccess(), "Auto-fix should succeed for " + def.getKey());
            assertNotNull(resp.getFixedXml());

            ValidationReportDto report = validationEngine.validate(resp.getFixedXml(), def.getKey());
            assertTrue(report.isValid(), "Auto-fixed XML for " + def.getKey() + " must remain valid. Errors: " +
                    report.getIssues().stream().filter(i -> "ERROR".equals(i.getSeverity())).map(ValidationIssueDto::getMessage).toList());
            assertTrue(report.getHealthScore() >= 80, "Health score for " + def.getKey() + " must remain high");
        }
    }

    @Test
    public void testAutoFixFormattingIsIdempotent() {
        IsoMessageDefinition pacs008 = IsoMessageRegistry.getDefinition("pacs.008");
        XmlAutoFixRequestDto req1 = XmlAutoFixRequestDto.builder()
                .xmlContent(pacs008.getSampleXml())
                .messageType("pacs.008")
                .build();

        XmlAutoFixResponseDto resp1 = autoFixEngine.autoFix(req1);
        assertTrue(resp1.isSuccess());
        String firstFix = resp1.getFixedXml();

        XmlAutoFixRequestDto req2 = XmlAutoFixRequestDto.builder()
                .xmlContent(firstFix)
                .messageType("pacs.008")
                .build();

        XmlAutoFixResponseDto resp2 = autoFixEngine.autoFix(req2);
        assertTrue(resp2.isSuccess());
        String secondFix = resp2.getFixedXml();

        assertEquals(firstFix, secondFix, "Subsequent formatting passes must be idempotent and not accumulate spaces or blank lines");
    }

    @Test
    public void testInvalidBvnDetected() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>99905820250801205622930239203831721</MsgId>
                            <CreDtTm>2025-08-01T08:08:12.954+01:00</CreDtTm>
                            <BtchBookg>false</BtchBookg>
                            <NbOfTxs>1</NbOfTxs>
                            <SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>
                            <InstgAgt><FinInstnId><ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId></FinInstnId></InstgAgt>
                            <InstdAgt><FinInstnId><ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId></FinInstnId></InstdAgt>
                        </GrpHdr>
                        <CdtTrfTxInf>
                            <PmtId>
                                <InstrId>99905899905720250801205722893090687</InstrId>
                                <EndToEndId>99905899905798653637383920281615142</EndToEndId>
                                <TxId>99905820250801205622930239203831721</TxId>
                            </PmtId>
                            <IntrBkSttlmAmt Ccy="NGN">1000.00</IntrBkSttlmAmt>
                            <IntrBkSttlmDt>2025-02-25Z</IntrBkSttlmDt>
                            <Dbtr><Nm>James</Nm></Dbtr>
                            <DbtrAcct><Id><IBAN>0177136558</IBAN></Id><Nm>James</Nm></DbtrAcct>
                            <DbtrAgt><FinInstnId><ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId></FinInstnId></DbtrAgt>
                            <CdtrAgt><FinInstnId><ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId></FinInstnId></CdtrAgt>
                            <Cdtr><Nm>Musa</Nm></Cdtr>
                            <CdtrAcct><Id><IBAN>0177136558</IBAN></Id><Nm>Musa</Nm></CdtrAcct>
                        </CdtTrfTxInf>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <DebtorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>12345</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </DebtorInfo>
                                    <CreditorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22112323460</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </CreditorInfo>
                                    <TransactionInfo>
                                        <TransactionLocation>01080652440N020900337921E</TransactionLocation>
                                        <NameEnquiryMsgId>99905820250801205622930239203831720</NameEnquiryMsgId>
                                        <ChannelCode>1</ChannelCode>
                                    </TransactionInfo>
                                </CustomData>
                            </Envlp>
                        </SplmtryData>
                    </FIToFICstmrCdtTrf>
                </Document>
                """;

        ValidationReportDto report = validationEngine.validate(xml, "pacs.008");
        assertFalse(report.isValid());
        boolean hasBvnError = report.getIssues().stream()
                .anyMatch(i -> "NIBSS_BVN_LEN".equals(i.getRuleCode()));
        assertTrue(hasBvnError, "Should report NIBSS_BVN_LEN violation for 5-digit BVN");
    }

    @Test
    public void testRemovingMandatoryTagsFlaggedAsErrors() {
        // 1. pacs.008 with MsgId removed
        IsoMessageDefinition pacs008Def = IsoMessageRegistry.getDefinition("pacs.008");
        String samplePacs008 = pacs008Def.getSampleXml();
        String missingMsgId = samplePacs008.replace("<MsgId>99905820250801205622930239203831721</MsgId>", "");
        ValidationReportDto rep1 = validationEngine.validate(missingMsgId, "pacs.008");
        assertFalse(rep1.isValid());
        assertTrue(rep1.getIssues().stream().anyMatch(i -> "MANDATORY_FIELD_MISSING".equals(i.getRuleCode()) && i.getMessage().contains("Message ID")));

        // 2. pacs.008 with Debtor Name removed
        String missingDbtrNm = samplePacs008.replace("<Nm>James</Nm>", "");
        ValidationReportDto rep2 = validationEngine.validate(missingDbtrNm, "pacs.008");
        assertFalse(rep2.isValid());
        assertTrue(rep2.getIssues().stream().anyMatch(i -> "MANDATORY_FIELD_MISSING".equals(i.getRuleCode()) && i.getMessage().contains("Debtor Name")));

        // 3. pacs.008 with GrpHdr completely removed
        String missingGrpHdr = samplePacs008.replaceAll("(?s)<GrpHdr>.*?</GrpHdr>", "");
        ValidationReportDto rep3 = validationEngine.validate(missingGrpHdr, "pacs.008");
        assertFalse(rep3.isValid());
        assertTrue(rep3.getIssues().stream().anyMatch(i -> "MANDATORY_HEADER_MISSING".equals(i.getRuleCode())));

        // 4. pain.009 with Mandate ID removed
        IsoMessageDefinition pain009Def = IsoMessageRegistry.getDefinition("pain.009");
        String missingMndtId = pain009Def.getSampleXml().replace("<MndtId>MNDT-RCUR-00001</MndtId>", "");
        ValidationReportDto rep4 = validationEngine.validate(missingMndtId, "pain.009");
        assertFalse(rep4.isValid());
        assertTrue(rep4.getIssues().stream().anyMatch(i -> "MANDATORY_FIELD_MISSING".equals(i.getRuleCode()) && i.getMessage().contains("Mandate ID")));

        // 5. camt.052 with Bal block removed
        IsoMessageDefinition camt052Def = IsoMessageRegistry.getDefinition("camt.052");
        String missingBal = camt052Def.getSampleXml().replaceAll("(?s)<Bal>.*?</Bal>", "");
        ValidationReportDto rep5 = validationEngine.validate(missingBal, "camt.052");
        assertFalse(rep5.isValid());
        assertTrue(rep5.getIssues().stream().anyMatch(i -> "MANDATORY_FIELD_MISSING".equals(i.getRuleCode())));

        // 6. pain.012 with Acceptance result removed
        IsoMessageDefinition pain012Def = IsoMessageRegistry.getDefinition("pain.012");
        String missingAccptd = pain012Def.getSampleXml().replace("<Accptd>true</Accptd>", "");
        ValidationReportDto rep6 = validationEngine.validate(missingAccptd, "pain.012");
        assertFalse(rep6.isValid());
        assertTrue(rep6.getIssues().stream().anyMatch(i -> "MANDATORY_FIELD_MISSING".equals(i.getRuleCode()) && i.getMessage().contains("Acceptance Indicator")));
    }

    @Test
    public void testEmptyMandatoryTagFlagged() {
        IsoMessageDefinition pacs008Def = IsoMessageRegistry.getDefinition("pacs.008");
        String sample = pacs008Def.getSampleXml();
        String emptyMsgId = sample.replace("<MsgId>99905820250801205622930239203831721</MsgId>", "<MsgId></MsgId>");
        ValidationReportDto report = validationEngine.validate(emptyMsgId, "pacs.008");
        assertFalse(report.isValid());
        assertTrue(report.getIssues().stream().anyMatch(i -> "MANDATORY_FIELD_EMPTY".equals(i.getRuleCode()) && i.getMessage().contains("Message ID")));
    }

    @Test
    public void testAutoFixRepairsDateTimeAndSupplementaryData() {
        String brokenXml = """
                <FIToFICstmrCdtTrf>
                    <GrpHdr>
                        <MsgId>123</MsgId>
                        <CreDtTm>2025-08-01T08:08:12</CreDtTm>
                    </GrpHdr>
                </FIToFICstmrCdtTrf>
                """;

        XmlAutoFixRequestDto req = XmlAutoFixRequestDto.builder()
                .xmlContent(brokenXml)
                .messageType("pacs.008")
                .fixDates(true)
                .fixSupplementaryData(true)
                .build();

        XmlAutoFixResponseDto resp = autoFixEngine.autoFix(req);
        assertTrue(resp.isSuccess());
        assertNotNull(resp.getFixedXml());
        assertTrue(resp.getFixedXml().contains("<Document"));
        assertTrue(resp.getFixedXml().contains("<SplmtryData>"));
        assertTrue(resp.getFixedXml().contains("+01:00"));
    }

    @Test
    public void testTagCaseMismatchAndLowercaseValueDetected() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12">
                    <fitoficstmrcdttrf>
                        <grphdr>
                            <MsgId>99905820250801205622930239203831721</MsgId>
                            <CreDtTm>2025-08-01T08:08:12.954+01:00</CreDtTm>
                            <BtchBookg>false</BtchBookg>
                            <NbOfTxs>1</NbOfTxs>
                            <SttlmInf><SttlmMtd>clrg</SttlmMtd></SttlmInf>
                            <InstgAgt><FinInstnId><ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId></FinInstnId></InstgAgt>
                            <InstdAgt><FinInstnId><ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId></FinInstnId></InstdAgt>
                        </grphdr>
                        <CdtTrfTxInf>
                            <PmtId>
                                <InstrId>99905899905720250801205722893090687</InstrId>
                                <EndToEndId>99905899905798653637383920281615142</EndToEndId>
                                <TxId>99905820250801205622930239203831721</TxId>
                            </PmtId>
                            <PmtTpInf><ClrChanl>RTNS</ClrChanl></PmtTpInf>
                            <IntrBkSttlmAmt Ccy="ngn">1000.00</IntrBkSttlmAmt>
                            <IntrBkSttlmDt>2025-02-25Z</IntrBkSttlmDt>
                            <ChrgBr>SLEV</ChrgBr>
                            <Dbtr><Nm>James</Nm></Dbtr>
                            <DbtrAcct><Id><iban>0177136558</iban></Id><Nm>James</Nm></DbtrAcct>
                            <DbtrAgt><FinInstnId><ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId></FinInstnId></DbtrAgt>
                            <CdtrAgt><FinInstnId><ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId></FinInstnId></CdtrAgt>
                            <Cdtr><Nm>Musa</Nm></Cdtr>
                            <CdtrAcct><Id><iban>0177136558</iban></Id><Nm>Musa</Nm></CdtrAcct>
                        </CdtTrfTxInf>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <DebtorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>bvn</IdType>
                                        <IdValue>22112323460</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </DebtorInfo>
                                    <CreditorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>bvn</IdType>
                                        <IdValue>22112323460</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </CreditorInfo>
                                    <TransactionInfo>
                                        <TransactionLocation>01080652440N020900337921E</TransactionLocation>
                                        <NameEnquiryMsgId>99905820250801205622930239203831720</NameEnquiryMsgId>
                                        <ChannelCode>1</ChannelCode>
                                    </TransactionInfo>
                                </CustomData>
                            </Envlp>
                        </SplmtryData>
                    </fitoficstmrcdttrf>
                </Document>
                """;

        ValidationReportDto report = validationEngine.validate(xml, "pacs.008");
        assertFalse(report.isValid());
        
        // Assert tag casing error for <fitoficstmrcdttrf> and <iban>
        boolean hasTagCaseError = report.getIssues().stream()
                .anyMatch(i -> "TAG_CASE_MISMATCH".equals(i.getRuleCode()));
        assertTrue(hasTagCaseError, "Should report TAG_CASE_MISMATCH for mis-cased tags");

        // Assert uppercase value error for IdType "bvn" or SttlmMtd "clrg"
        boolean hasUppercaseError = report.getIssues().stream()
                .anyMatch(i -> "VALUE_UPPERCASE_REQUIRED".equals(i.getRuleCode()));
        assertTrue(hasUppercaseError, "Should report VALUE_UPPERCASE_REQUIRED for lowercase code values");

        // Test Auto-Fix on this payload
        XmlAutoFixRequestDto fixReq = XmlAutoFixRequestDto.builder()
                .xmlContent(xml)
                .messageType("pacs.008")
                .fixDates(true)
                .fixSupplementaryData(true)
                .build();
        XmlAutoFixResponseDto fixResp = autoFixEngine.autoFix(fixReq);
        assertTrue(fixResp.isSuccess());
        assertTrue(fixResp.getFixedXml().contains("<FIToFICstmrCdtTrf>"));
        assertTrue(fixResp.getFixedXml().contains("<IBAN>0177136558</IBAN>"));
        assertTrue(fixResp.getFixedXml().contains("<IdType>BVN</IdType>"));
        assertTrue(fixResp.getFixedXml().contains("<SttlmMtd>CLRG</SttlmMtd>"));
        assertTrue(fixResp.getFixedXml().contains("Ccy=\"NGN\""));
        assertTrue(fixResp.getValidationReport().isValid());
    }

    @Test
    public void testTagCharacterLengthExceededDetected() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("pacs.008");
        String sampleXml = def.getSampleXml();

        // 1. ChannelCode exceeds max length of 2 (e.g. 123)
        String badChannel = sampleXml.replace("<ChannelCode>1</ChannelCode>", "<ChannelCode>12345</ChannelCode>");
        ValidationReportDto rep1 = validationEngine.validate(badChannel, "pacs.008");
        assertFalse(rep1.isValid());
        assertTrue(rep1.getIssues().stream().anyMatch(i -> "FIELD_LENGTH_EXCEEDED".equals(i.getRuleCode()) && (i.getMessage().contains("ChannelCode") || i.getMessage().contains("Channel Code"))),
                "Should flag FIELD_LENGTH_EXCEEDED for ChannelCode > 2 chars");

        // 2. Member ID exceeds 11 characters
        String badMmbId = sampleXml.replace("<MmbId>999058</MmbId>", "<MmbId>999058123456789</MmbId>");
        ValidationReportDto rep2 = validationEngine.validate(badMmbId, "pacs.008");
        assertFalse(rep2.isValid());
        assertTrue(rep2.getIssues().stream().anyMatch(i -> "FIELD_LENGTH_EXCEEDED".equals(i.getRuleCode()) && (i.getMessage().contains("MmbId") || i.getMessage().contains("Member ID"))),
                "Should flag FIELD_LENGTH_EXCEEDED for MmbId > 11 chars");

        // 3. TransactionLocation exceeds 30 characters
        String badTxLoc = sampleXml.replace("<TransactionLocation>01080652440N020900337921E</TransactionLocation>",
                "<TransactionLocation>01080652440N020900337921E_EXCESSIVE_LENGTH_STRING_123456789</TransactionLocation>");
        ValidationReportDto rep3 = validationEngine.validate(badTxLoc, "pacs.008");
        assertFalse(rep3.isValid());
        assertTrue(rep3.getIssues().stream().anyMatch(i -> "FIELD_LENGTH_EXCEEDED".equals(i.getRuleCode()) && (i.getMessage().contains("TransactionLocation") || i.getMessage().contains("Transaction Location"))),
                "Should flag FIELD_LENGTH_EXCEEDED for TransactionLocation > 30 chars");
    }

    @Test
    public void testTagExactCharacterLengthMismatchDetected() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("pacs.008");
        String sampleXml = def.getSampleXml();

        // 1. IBAN / NUBAN not exactly 10 digits (e.g. 7 digits)
        String shortIban = sampleXml.replace("<IBAN>0177136558</IBAN>", "<IBAN>1234567</IBAN>");
        ValidationReportDto rep1 = validationEngine.validate(shortIban, "pacs.008");
        assertFalse(rep1.isValid());
        assertTrue(rep1.getIssues().stream().anyMatch(i -> i.getMessage().contains("IBAN") || i.getMessage().contains("NUBAN")),
                "Should flag invalid length for IBAN != 10 digits");

        // 2. MsgId not exactly 35 characters
        String shortMsgId = sampleXml.replace("<MsgId>99905820250801205622930239203831721</MsgId>", "<MsgId>99905812345</MsgId>");
        ValidationReportDto rep2 = validationEngine.validate(shortMsgId, "pacs.008");
        assertFalse(rep2.isValid());
        assertTrue(rep2.getIssues().stream().anyMatch(i -> i.getMessage().contains("MsgId") || "FIELD_LENGTH_MISMATCH".equals(i.getRuleCode()) || "NPS_ID_FORMAT".equals(i.getRuleCode())),
                "Should flag length mismatch for MsgId != 35 chars");
    }

    @Test
    public void testAutoFixCharacterLengthTruncationAndPadding() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("pacs.008");
        String xmlWithLongFields = def.getSampleXml()
                .replace("<ChannelCode>1</ChannelCode>", "<ChannelCode>12345</ChannelCode>")
                .replace("<MmbId>999058</MmbId>", "<MmbId>999058123456789</MmbId>")
                .replace("<IBAN>0177136558</IBAN>", "<IBAN>136558</IBAN>"); // 6 digits, needs zero-padding to 10

        XmlAutoFixRequestDto fixReq = XmlAutoFixRequestDto.builder()
                .xmlContent(xmlWithLongFields)
                .messageType("pacs.008")
                .fixDates(true)
                .fixIds(true)
                .fixSupplementaryData(true)
                .build();

        XmlAutoFixResponseDto fixResp = autoFixEngine.autoFix(fixReq);
        assertTrue(fixResp.isSuccess());
        assertNotNull(fixResp.getFixedXml());

        // ChannelCode truncated to 2 chars max
        assertTrue(fixResp.getFixedXml().contains("<ChannelCode>12</ChannelCode>") || fixResp.getFixedXml().contains("<ChannelCode>1</ChannelCode>"));
        // MmbId truncated to 6 digits max
        assertTrue(fixResp.getFixedXml().contains("<MmbId>999058</MmbId>"));
        // IBAN padded to 10 digits
        assertTrue(fixResp.getFixedXml().contains("<IBAN>0000136558</IBAN>"));
    }

    @Test
    public void testAcmt024MissingOriginalCreationDateTimeAutoFix() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("acmt.024");
        String sampleXml = def.getSampleXml();
        
        // Remove <CreDtTm> from <OrgnlAssgnmt>
        String brokenXml = sampleXml.replace("<CreDtTm>2025-08-29T15:05:04.347+01:00</CreDtTm>", "");
        ValidationReportDto report = validationEngine.validate(brokenXml, "acmt.024");
        assertFalse(report.isValid());
        assertTrue(report.getIssues().stream().anyMatch(i -> "MANDATORY_FIELD_MISSING".equals(i.getRuleCode()) && i.getMessage().contains("Original Assignment Creation DateTime")));

        // Execute auto-fix
        XmlAutoFixRequestDto fixReq = XmlAutoFixRequestDto.builder()
                .xmlContent(brokenXml)
                .messageType("acmt.024")
                .fixDates(true)
                .fixSupplementaryData(true)
                .build();
        XmlAutoFixResponseDto fixResp = autoFixEngine.autoFix(fixReq);
        assertTrue(fixResp.isSuccess());
        assertTrue(fixResp.getFixedXml().contains("<CreDtTm>"));
        assertTrue(fixResp.getValidationReport().isValid());
    }

    @Test
    public void testPain014SampleHasZeroWarningsAnd100PercentHealth() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("pain.014");
        assertNotNull(def);
        ValidationReportDto report = validationEngine.validate(def.getSampleXml(), "pain.014");
        assertTrue(report.isValid(), "pain.014 sample XML must be valid");
        assertEquals(100, report.getHealthScore(), "pain.014 health score should be 100%");
        assertEquals(0, report.getSummary().getTotalErrors(), "pain.014 error count should be 0");
        assertEquals(0, report.getSummary().getTotalWarnings(), "pain.014 warning count should be 0");
        assertTrue(report.getIssues().isEmpty(), "pain.014 should have 0 issues");
    }

    @Test
    public void testStrict6DigitInstitutionCodeValidation() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("pacs.008");
        String sampleXml = def.getSampleXml();

        // 1. Invalid 3-digit institution code should fail validation
        String invalidShortXml = sampleXml.replace("<MmbId>999058</MmbId>", "<MmbId>058</MmbId>");
        ValidationReportDto shortReport = validationEngine.validate(invalidShortXml, "pacs.008");
        assertFalse(shortReport.isValid());
        assertTrue(shortReport.getIssues().stream().anyMatch(i -> i.getMessage().contains("6 numeric digits")));

        // 2. Non-numeric institution code should fail validation
        String invalidAlphaXml = sampleXml.replace("<MmbId>999058</MmbId>", "<MmbId>ABCDEF</MmbId>");
        ValidationReportDto alphaReport = validationEngine.validate(invalidAlphaXml, "pacs.008");
        assertFalse(alphaReport.isValid());
        assertTrue(alphaReport.getIssues().stream().anyMatch(i -> i.getMessage().contains("6 numeric digits")));

        // 3. Auto-fix should pad short institution code to 6 digits
        XmlAutoFixRequestDto fixReq = XmlAutoFixRequestDto.builder()
                .xmlContent(invalidShortXml)
                .messageType("pacs.008")
                .fixIds(true)
                .build();
        XmlAutoFixResponseDto fixResp = autoFixEngine.autoFix(fixReq);
        assertTrue(fixResp.isSuccess());
        assertTrue(fixResp.getFixedXml().contains("<MmbId>000058</MmbId>"));
    }

    @Test
    public void testDynamicBeneficiaryKycIdValueValidation() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("pacs.008");
        String sampleXml = def.getSampleXml();

        // 1. Valid 11-digit BVN passes
        ValidationReportDto bvnReport = validationEngine.validate(sampleXml, "pacs.008");
        assertTrue(bvnReport.isValid());

        // 2. Invalid 8-digit BVN fails
        String invalidBvnXml = sampleXml.replace("<IdValue>22112323460</IdValue>", "<IdValue>12345678</IdValue>");
        ValidationReportDto shortBvnReport = validationEngine.validate(invalidBvnXml, "pacs.008");
        assertFalse(shortBvnReport.isValid());
        assertTrue(shortBvnReport.getIssues().stream().anyMatch(i -> i.getMessage().contains("11 numeric digits")));

        // 3. Corporate RC IdType with alphanumeric ID value (e.g. RC-987654) passes
        String corporateKycXml = sampleXml
                .replace("<IdType>BVN</IdType>", "<IdType>RC</IdType>")
                .replace("<IdValue>22112323460</IdValue>", "<IdValue>RC-987654</IdValue>");
        ValidationReportDto rcReport = validationEngine.validate(corporateKycXml, "pacs.008");
        assertTrue(rcReport.isValid());
    }

    @Test
    public void testSessionIdFormatAndAutoFix() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("pacs.008");
        String sampleXml = def.getSampleXml();
        String brokenXml = sampleXml.replace("</GrpHdr>", "<SessionID>12345</SessionID></GrpHdr>");

        // 1. Invalid short SessionID triggers error
        ValidationReportDto report = validationEngine.validate(brokenXml, "pacs.008");
        assertTrue(report.getIssues().stream().anyMatch(i -> i.getRuleCode().equals("FIELD_LENGTH_MISMATCH") && i.getMessage().contains("SessionID")));

        XmlAutoFixRequestDto fixReq = XmlAutoFixRequestDto.builder()
                .xmlContent(brokenXml)
                .messageType("pacs.008")
                .fixIds(true)
                .build();
        XmlAutoFixResponseDto fixResp = autoFixEngine.autoFix(fixReq);
        assertTrue(fixResp.isSuccess());
        assertTrue(fixResp.getFixedXml().matches("(?is).*<sessionid[^>]*>\\d{30}</sessionid>.*"), "Fixed XML was:\n" + fixResp.getFixedXml());
    }

    @Test
    public void testCrossFieldSourceAndDestInstitutionCodeValidation() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("pacs.008");
        String sampleXml = def.getSampleXml();

        // Sample has DbtrAgt/MmbId = 999058 and MsgId starting with 999058 -> Valid with zero warnings
        ValidationReportDto report = validationEngine.validate(sampleXml, "pacs.008");
        assertTrue(report.isValid());
        assertEquals(0, report.getSummary().getTotalWarnings());

        // Alter MsgId prefix to mismatched 999111 -> triggers SOURCE_INSTITUTION_MISMATCH warning
        String mismatchedXml = sampleXml.replace("<MsgId>999058", "<MsgId>999111");
        ValidationReportDto mismatchReport = validationEngine.validate(mismatchedXml, "pacs.008");
        assertTrue(mismatchReport.isValid()); // warning does not invalidate XML
        assertTrue(mismatchReport.getIssues().stream().anyMatch(i -> "SOURCE_INSTITUTION_MISMATCH".equals(i.getRuleCode())));
    }

    @Test
    public void testUserAcmt023SampleValidation() throws Exception {
        String userSampleXml = """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?><ns2:Document xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:acmt.023.001.04">
                    <IdVrfctnReq>
                        <Assgnmt>
                            <MsgId>99999920250829150504887742643314693</MsgId>
                            <CreDtTm>2025-08-29T15:05:04.954Z</CreDtTm>
                            <Cretr>
                                <Pty>
                                    <Nm>Crystal Bank</Nm>
                                </Pty>
                            </Cretr>
                            <Assgnr>
                                <Pty>
                                    <Nm>Oso International Bank</Nm>
                                </Pty>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999999</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999999</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgnr>
                            <Assgne>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999012</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999012</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </Assgne>
                        </Assgnmt>
                        <Vrfctn>
                            <Id>99999920250829150504887742643314693</Id>
                            <PtyAndAcctId>
                                <Pty>
                                    <Nm>Emmanuel Osod</Nm>
                                </Pty>
                                <Acct>
                                    <Id>
                                        <IBAN>1029384756</IBAN>
                                    </Id>
                                </Acct>
                            </PtyAndAcctId>
                        </Vrfctn>
                    </IdVrfctnReq>
                <Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo><CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/><SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"/><Reference URI=""><Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms><DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256"/><DigestValue>XUMjZ/yGhCMtByUNiT8BiVLQXUGzBfDA8c1hOkMwpdo=</DigestValue></Reference></SignedInfo><SignatureValue>LPDvbtUFSTYrpw84CU+TGsUHzr2PmS81CzJzQPT1DSCEe9phbJgM7VgxTsX7yBkeGtQ+YII6FFXb&#13;
                CFWgGQiM/0SeQRaLmzyPCdAz/EoxZrM3xMl/8lRemgzmR5oRj3nlXgnWCxJyQDT6GT/6Et/YADhk&#13;
                wUVqzYbPrxQQOnCJEKa8Z+T/xoKZGxx2PMpGa8r7uPRjPJsCwxwMOHKBROduBvLqa9+eKN15q0sv&#13;
                9TNlhCcRmWBppSq2up7UBGwPs26cDLEyDuGpMKQ2PUGTbBng6iWSu9hFOwHChfu6qDmmHMOD&#13;
                HWLcRkKuiOa4eeJhAY/JaiRvyCiDufpaPfPOIQ==</SignatureValue></Signature></ns2:Document>
                """;

        ValidationReportDto report = validationEngine.validate(userSampleXml, "acmt.023");
        assertTrue(report.isValid(), "User sample acmt.023 XML should be valid");
        assertEquals(100, report.getHealthScore());
        assertEquals("acmt.023", report.getDetectedMessageType());
        assertTrue(report.getIssues().isEmpty());

        // Now test what is generated by NameVerificationXmlGenerator
        org.example.signer.dto.NameVerificationRequestDto dto = new org.example.signer.dto.NameVerificationRequestDto();
        dto.setSourceId("999999");
        dto.setBeneficiaryId("999012");
        dto.setSendingPartyName("Crystal Bank");
        dto.setPartyToBeVerifiedName("Emmanuel Osod");
        dto.setPartyToBeVerifiedAccountNumber("1029384756");

        org.example.signer.model.NameVerification model = org.example.signer.xml.NameVerificationXmlGenerator.generate(dto, "99999920250829150504887742643314693");
        org.w3c.dom.Document genDoc = org.example.signer.Utils.XmlUtils.marshalToDocument(model);
        String genXml = org.example.signer.Utils.XmlUtils.documentToString(genDoc);

        ValidationReportDto genReport = validationEngine.validate(genXml, "acmt.023");
        assertTrue(genReport.isValid(), "Generated acmt.023 XML should be valid");
        assertEquals(100, genReport.getHealthScore());
        assertTrue(genReport.getIssues().isEmpty());
    }

    @Test
    public void testAcmt023VerificationIdMismatchDetectedAndAutoFixed() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("acmt.023");
        String sampleXml = def.getSampleXml();

        // Alter Vrfctn.Id to a different ID than Assgnmt.MsgId
        String mismatchXml = sampleXml.replace(
                "<Id>99999920250829150504887742643314693</Id>",
                "<Id>99999920250829150504887742649999999</Id>"
        );

        // 1. Validation should fail with VERIFICATION_ID_MISMATCH error
        ValidationReportDto report = validationEngine.validate(mismatchXml, "acmt.023");
        assertFalse(report.isValid(), "Validation should fail when Vrfctn.Id != MsgId");
        assertTrue(report.getIssues().stream().anyMatch(i -> "VERIFICATION_ID_MISMATCH".equals(i.getRuleCode())),
                "Should report VERIFICATION_ID_MISMATCH");

        // 2. Auto-fix should synchronize Vrfctn.Id to match Assgnmt.MsgId
        XmlAutoFixRequestDto fixReq = XmlAutoFixRequestDto.builder()
                .xmlContent(mismatchXml)
                .messageType("acmt.023")
                .fixIds(true)
                .build();
        XmlAutoFixResponseDto fixResp = autoFixEngine.autoFix(fixReq);
        assertTrue(fixResp.isSuccess());
        assertTrue(fixResp.getValidationReport().isValid());
        assertTrue(fixResp.getFixedXml().contains("<Id>99999920250829150504887742643314693</Id>"));
    }

    @Test
    public void testAcmt023BicfiValidationAndAutoFix() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("acmt.023");
        String sampleXml = def.getSampleXml();

        // 1. Invalid short numeric BICFI (e.g. 999 instead of 6 digits)
        String shortBicfiXml = sampleXml.replace("<BICFI>999999</BICFI>", "<BICFI>999</BICFI>");
        ValidationReportDto shortReport = validationEngine.validate(shortBicfiXml, "acmt.023");
        assertFalse(shortReport.isValid());
        assertTrue(shortReport.getIssues().stream().anyMatch(i -> i.getMessage().contains("BICFI") || "FIELD_LENGTH_MISMATCH".equals(i.getRuleCode())));

        // 2. Assigner BICFI mismatch with MmbId (e.g. BICFI 999001 vs MmbId 999999)
        String mismatchBicfiXml = sampleXml.replace("<BICFI>999999</BICFI>", "<BICFI>999001</BICFI>");
        ValidationReportDto mismatchReport = validationEngine.validate(mismatchBicfiXml, "acmt.023");
        assertTrue(mismatchReport.getIssues().stream().anyMatch(i -> "INSTITUTION_CODE_MISMATCH".equals(i.getRuleCode()) && i.getMessage().contains("Assigner Agent BICFI")));

        // 3. Auto-fix should format and synchronize BICFI with MmbId
        XmlAutoFixRequestDto fixReq = XmlAutoFixRequestDto.builder()
                .xmlContent(shortBicfiXml)
                .messageType("acmt.023")
                .fixIds(true)
                .build();
        XmlAutoFixResponseDto fixResp = autoFixEngine.autoFix(fixReq);
        assertTrue(fixResp.isSuccess());
        assertTrue(fixResp.getFixedXml().contains("<BICFI>000999</BICFI>") || fixResp.getFixedXml().contains("<BICFI>999999</BICFI>"));
        assertTrue(fixResp.getValidationReport().isValid());

        // 4. Test User Scenario: Empty <BICFI></BICFI> in Assgne with MmbId 991040
        String emptyBicfiXml = sampleXml.replace("<BICFI>999012</BICFI>", "<BICFI></BICFI>")
                                        .replace("<MmbId>999012</MmbId>", "<MmbId>991040</MmbId>");
        ValidationReportDto emptyReport = validationEngine.validate(emptyBicfiXml, "acmt.023");
        assertFalse(emptyReport.isValid(), "Empty <BICFI></BICFI> tag should fail validation");
        assertTrue(emptyReport.getIssues().stream().anyMatch(i -> "EMPTY_TAG_NOT_ALLOWED".equals(i.getRuleCode()) && i.getMessage().contains("BICFI")),
                "Should report EMPTY_TAG_NOT_ALLOWED on empty <BICFI>");

        // 5. Auto-fix should populate empty <BICFI> with MmbId 991040
        XmlAutoFixRequestDto emptyFixReq = XmlAutoFixRequestDto.builder()
                .xmlContent(emptyBicfiXml)
                .messageType("acmt.023")
                .fixIds(true)
                .build();
        XmlAutoFixResponseDto emptyFixResp = autoFixEngine.autoFix(emptyFixReq);
        assertTrue(emptyFixResp.isSuccess());
        assertTrue(emptyFixResp.getFixedXml().contains("<BICFI>991040</BICFI>"), "Auto-fix should populate empty <BICFI> with 991040");
        assertTrue(emptyFixResp.getValidationReport().isValid(), "Fixed XML should be valid");
    }

    @Test
    public void testAcmt024ValidationAndAutoFix() {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition("acmt.024");
        String sampleXml = def.getSampleXml();

        // 1. Valid sample XML passes
        ValidationReportDto report = validationEngine.validate(sampleXml, "acmt.024");
        assertTrue(report.isValid(), "Sample acmt.024 XML should be valid");
        assertEquals(100, report.getHealthScore());

        // 2. Mismatched Rpt.OrgnlId vs OrgnlAssgnmt.MsgId
        String mismatchXml = sampleXml.replace(
                "<OrgnlId>99999920250829150504887742643314693</OrgnlId>",
                "<OrgnlId>99999920250829150504887742649999999</OrgnlId>"
        );
        ValidationReportDto mismatchReport = validationEngine.validate(mismatchXml, "acmt.024");
        assertFalse(mismatchReport.isValid());
        assertTrue(mismatchReport.getIssues().stream().anyMatch(i -> "ORIGINAL_ID_MISMATCH".equals(i.getRuleCode())),
                "Should report ORIGINAL_ID_MISMATCH when Rpt.OrgnlId != OrgnlAssgnmt.MsgId");

        // 3. Auto-fix repairs mismatched OrgnlId
        XmlAutoFixRequestDto fixReq = XmlAutoFixRequestDto.builder()
                .xmlContent(mismatchXml)
                .messageType("acmt.024")
                .fixIds(true)
                .build();
        XmlAutoFixResponseDto fixResp = autoFixEngine.autoFix(fixReq);
        assertTrue(fixResp.isSuccess());
        assertTrue(fixResp.getFixedXml().contains("<OrgnlId>99999920250829150504887742643314693</OrgnlId>"));
        assertTrue(fixResp.getValidationReport().isValid());

        // 4. Invalid boolean in Vrfctn (e.g. "invalid_bool")
        String invalidBoolXml = sampleXml.replace("<Vrfctn>true</Vrfctn>", "<Vrfctn>invalid_bool</Vrfctn>");
        ValidationReportDto boolReport = validationEngine.validate(invalidBoolXml, "acmt.024");
        assertFalse(boolReport.isValid());
        assertTrue(boolReport.getIssues().stream().anyMatch(i -> "INVALID_BOOLEAN".equals(i.getRuleCode())));

        // 5. Missing updated account name when Vrfctn is true
        String missingUpdtdNmXml = sampleXml.replace("<Nm>Israel Kayole</Nm>", "<Nm></Nm>");
        ValidationReportDto nmReport = validationEngine.validate(missingUpdtdNmXml, "acmt.024");
        assertFalse(nmReport.isValid());
        assertTrue(nmReport.getIssues().stream().anyMatch(i -> i.getMessage().contains("verified account name") || "EMPTY_TAG_NOT_ALLOWED".equals(i.getRuleCode())));
    }
}
