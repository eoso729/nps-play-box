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
                            <IntrBkSttlmAmt Ccy="ngn">1000.00</IntrBkSttlmAmt>
                            <IntrBkSttlmDt>2025-02-25Z</IntrBkSttlmDt>
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
}
