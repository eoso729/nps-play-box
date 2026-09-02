package org.example.signer.validation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

public class IsoMessageRegistry {

    private static final Map<String, IsoMessageDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<String, String> ROOT_TAG_TO_KEY = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        registerPacs008();
        registerPacs002();
        registerPacs028();
        registerAcmt023();
        registerAcmt024();
        registerPain009();
        registerPain010();
        registerPain011();
        registerPain012();
        registerPacs003();
        registerPain013();
        registerPain014();
        registerCamt060();
        registerCamt052();
        registerCamt053();
        registerPain001();
        registerPain002();
        registerPain008();
        registerPacs004();
    }

    public static Collection<IsoMessageDefinition> getAllDefinitions() {
        return Collections.unmodifiableCollection(DEFINITIONS.values());
    }

    public static IsoMessageDefinition getDefinition(String key) {
        if (key == null) return null;
        String normalizedKey = normalizeKey(key);
        return DEFINITIONS.get(normalizedKey);
    }

    public static String normalizeKey(String key) {
        if (key == null) return "";
        String k = key.trim().toLowerCase();
        if (k.contains("pacs") && k.contains("008")) return "pacs.008";
        if (k.contains("pacs") && k.contains("002")) return "pacs.002";
        if (k.contains("pacs") && k.contains("028")) return "pacs.028";
        if (k.contains("pacs") && k.contains("003")) return "pacs.003";
        if (k.contains("pacs") && k.contains("004")) return "pacs.004";
        if (k.contains("acmt") && k.contains("023")) return "acmt.023";
        if (k.contains("acmt") && k.contains("024")) return "acmt.024";
        if (k.contains("pain") && k.contains("009")) return "pain.009";
        if (k.contains("pain") && k.contains("010")) return "pain.010";
        if (k.contains("pain") && k.contains("011")) return "pain.011";
        if (k.contains("pain") && k.contains("012")) return "pain.012";
        if (k.contains("pain") && k.contains("013")) return "pain.013";
        if (k.contains("pain") && k.contains("014")) return "pain.014";
        if (k.contains("pain") && k.contains("001")) return "pain.001";
        if (k.contains("pain") && k.contains("002")) return "pain.002";
        if (k.contains("pain") && k.contains("008")) return "pain.008";
        if (k.contains("camt") && k.contains("060")) return "camt.060";
        if (k.contains("camt") && k.contains("052")) return "camt.052";
        if (k.contains("camt") && k.contains("053")) return "camt.053";
        return key;
    }

    /**
     * Automatically detects message type from XML Document elements or raw text.
     */
    public static String detectMessageType(Document doc, String rawXml) {
        if (doc != null && doc.getDocumentElement() != null) {
            Element root = doc.getDocumentElement();
            String rootName = root.getLocalName() != null ? root.getLocalName() : root.getTagName();

            if (ROOT_TAG_TO_KEY.containsKey(rootName)) {
                return ROOT_TAG_TO_KEY.get(rootName);
            }

            if ("Document".equalsIgnoreCase(rootName)) {
                NodeList children = root.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i) instanceof Element child) {
                        String childName = child.getLocalName() != null ? child.getLocalName() : child.getTagName();
                        if (ROOT_TAG_TO_KEY.containsKey(childName)) {
                            return ROOT_TAG_TO_KEY.get(childName);
                        }
                    }
                }
            }

            String ns = root.getNamespaceURI();
            if (ns == null || ns.isEmpty()) {
                ns = root.getAttribute("xmlns");
            }
            if (ns != null && !ns.isEmpty()) {
                String detected = detectFromNamespaceOrContent(ns);
                if (detected != null) return detected;
            }
        }

        if (rawXml != null) {
            return detectFromNamespaceOrContent(rawXml);
        }

        return null;
    }

    private static String detectFromNamespaceOrContent(String text) {
        if (text == null) return null;
        String t = text.toLowerCase();
        if (t.contains("pacs.008") || t.contains("fitoficstmrcdttrf")) return "pacs.008";
        if (t.contains("pacs.002") || t.contains("fitofipmtstrpt")) return "pacs.002";
        if (t.contains("pacs.028") || t.contains("fitofipmtstreq")) return "pacs.028";
        if (t.contains("pacs.003") || t.contains("fitoficstmrdrctdbt")) return "pacs.003";
        if (t.contains("pacs.004") || t.contains("pmtrtr")) return "pacs.004";
        if (t.contains("acmt.023") || t.contains("idvrfctnreq")) return "acmt.023";
        if (t.contains("acmt.024") || t.contains("idvrfctnrpt")) return "acmt.024";
        if (t.contains("pain.009") || t.contains("mndtinitnreq")) return "pain.009";
        if (t.contains("pain.010") || t.contains("mndtamdmntreq")) return "pain.010";
        if (t.contains("pain.011") || t.contains("mndtcxlreq")) return "pain.011";
        if (t.contains("pain.012") || t.contains("mndtaccptncrpt") || t.contains("undrlygaccptncdtls")) return "pain.012";
        if (t.contains("pain.013") || t.contains("cdtrpmtactvtnreq")) return "pain.013";
        if (t.contains("pain.014") || t.contains("cdtrpmtactvtnreqstrpt") || t.contains("cdtrpmtactvtnstrpt")) return "pain.014";
        if (t.contains("camt.060") || t.contains("acctrptgreq")) return "camt.060";
        if (t.contains("camt.052") || t.contains("bktocstmracctrpt")) return "camt.052";
        if (t.contains("camt.053") || t.contains("bktocstmrstmt")) return "camt.053";
        if (t.contains("pain.001") || t.contains("cstmrcdtrfinitn")) return "pain.001";
        if (t.contains("pain.002") || t.contains("cstmrpmtstrpt")) return "pain.002";
        if (t.contains("pain.008") || t.contains("cstmrdrctdbtinitn")) return "pain.008";
        return null;
    }

    private static void register(IsoMessageDefinition def) {
        DEFINITIONS.put(def.getKey(), def);
        ROOT_TAG_TO_KEY.put(def.getMainElement(), def.getKey());
        if ("pain.014".equals(def.getKey())) {
            ROOT_TAG_TO_KEY.put("CdtrPmtActvtnReqStsRpt", def.getKey());
            ROOT_TAG_TO_KEY.put("CdtrPmtActvtnStsRpt", def.getKey());
        }
    }

    // ==========================================
    // 1. pacs.008: Customer Direct Credit
    // ==========================================
    private static void registerPacs008() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "GrpHdr.MsgId", "//GrpHdr/MsgId", "99905820250801205622930239203831721", "String", 35, true, false, "NPS_ID", "Point to point message identifier"),
                new IsoFieldDef("Creation DateTime", "GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2025-08-01T08:08:12.954+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Message creation timestamp in UTC+1"),
                new IsoFieldDef("Batch Booking", "GrpHdr.BtchBookg", "//GrpHdr/BtchBookg", "false", "Boolean", 5, true, false, null, "Batch booking flag"),
                new IsoFieldDef("Number of Transactions", "GrpHdr.NbOfTxs", "//GrpHdr/NbOfTxs", "1", "Numeric", 15, true, false, null, "Total number of transactions"),
                new IsoFieldDef("Settlement Method", "GrpHdr.SttlmInf.SttlmMtd", "//GrpHdr/SttlmInf/SttlmMtd", "CLRG", "String", 4, true, false, "SETTLEMENT_METHOD", "Settlement method (CLRG)"),
                new IsoFieldDef("Instructing Agent Member ID", "GrpHdr.InstgAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/InstgAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Instructing Agent Member ID"),
                new IsoFieldDef("Instructed Agent Member ID", "GrpHdr.InstdAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/InstdAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Instructed Agent Member ID"),
                new IsoFieldDef("Instruction ID", "CdtTrfTxInf.PmtId.InstrId", "//CdtTrfTxInf/PmtId/InstrId", "99905899905720250801205722893090687", "String", 35, true, false, "NPS_ID", "Instruction ID"),
                new IsoFieldDef("End-to-End ID", "CdtTrfTxInf.PmtId.EndToEndId", "//CdtTrfTxInf/PmtId/EndToEndId", "99905899905798653637383920281615142", "String", 35, true, false, "NPS_ID", "End-to-End ID"),
                new IsoFieldDef("Transaction ID", "CdtTrfTxInf.PmtId.TxId", "//CdtTrfTxInf/PmtId/TxId", "99905820250801205622930239203831721", "String", 35, true, false, "NPS_ID", "Transaction ID"),
                new IsoFieldDef("Clearing Channel", "CdtTrfTxInf.PmtTpInf.ClrChanl", "//CdtTrfTxInf/PmtTpInf/ClrChanl", "RTNS", "String", 4, true, false, "CLEARING_CHANNEL", "Clearing channel"),
                new IsoFieldDef("Currency Code", "CdtTrfTxInf.IntrBkSttlmAmt.@Ccy", "//CdtTrfTxInf/IntrBkSttlmAmt/@Ccy", "NGN", "String", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Interbank Settlement Amount", "CdtTrfTxInf.IntrBkSttlmAmt", "//CdtTrfTxInf/IntrBkSttlmAmt", "78000.00", "Decimal", 18, true, false, "AMOUNT", "Interbank settlement amount"),
                new IsoFieldDef("Interbank Settlement Date", "CdtTrfTxInf.IntrBkSttlmDt", "//CdtTrfTxInf/IntrBkSttlmDt", "2025-02-25Z", "Date", 10, true, false, "DATE", "Settlement date"),
                new IsoFieldDef("Charge Bearer", "CdtTrfTxInf.ChrgBr", "//CdtTrfTxInf/ChrgBr", "SLEV", "String", 4, true, false, null, "Charge bearer (SLEV)"),
                new IsoFieldDef("Debtor Name", "CdtTrfTxInf.Dbtr.Nm", "//CdtTrfTxInf/Dbtr/Nm", "James", "String", 100, true, false, null, "Debtor name"),
                new IsoFieldDef("Debtor Account Number (IBAN)", "CdtTrfTxInf.DbtrAcct.Id.IBAN", "//CdtTrfTxInf/DbtrAcct/Id/IBAN", "0177136558", "String", 10, true, false, "NUBAN", "10-digit Debtor account number"),
                new IsoFieldDef("Debtor Account Name", "CdtTrfTxInf.DbtrAcct.Nm", "//CdtTrfTxInf/DbtrAcct/Nm", "James", "String", 100, true, false, null, "Debtor account name"),
                new IsoFieldDef("Debtor Agent Member ID", "CdtTrfTxInf.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//CdtTrfTxInf/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Debtor Agent Member ID"),
                new IsoFieldDef("Creditor Agent Member ID", "CdtTrfTxInf.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//CdtTrfTxInf/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Creditor Agent Member ID"),
                new IsoFieldDef("Creditor Name", "CdtTrfTxInf.Cdtr.Nm", "//CdtTrfTxInf/Cdtr/Nm", "Musa", "String", 100, true, false, null, "Creditor name"),
                new IsoFieldDef("Creditor Account Number (IBAN)", "CdtTrfTxInf.CdtrAcct.Id.IBAN", "//CdtTrfTxInf/CdtrAcct/Id/IBAN", "0177136558", "String", 10, true, false, "NUBAN", "10-digit Creditor account number"),
                new IsoFieldDef("Creditor Account Name", "CdtTrfTxInf.CdtrAcct.Nm", "//CdtTrfTxInf/CdtrAcct/Nm", "Musa", "String", 100, true, false, null, "Creditor account name"),
                new IsoFieldDef("Place and Name", "SplmtryData.PlcAndNm", "//SplmtryData/PlcAndNm", "AdditionalVerificationDetails", "String", 35, true, false, null, "Supplementary Data descriptor"),
                new IsoFieldDef("Debtor Account Designation", "SplmtryData.Envlp.CustomData.DebtorInfo.AccountDesignation", "//DebtorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Debtor ID Type", "SplmtryData.Envlp.CustomData.DebtorInfo.IdType", "//DebtorInfo/IdType", "BVN", "Enum", 7, true, false, "ID_TYPE", "ID Type (BVN/NIN/RC/FIRSTIN/JTBTIN)"),
                new IsoFieldDef("Debtor ID Value", "SplmtryData.Envlp.CustomData.DebtorInfo.IdValue", "//DebtorInfo/IdValue", "22112323440", "String", 35, true, false, "ID_VALUE", "11-digit BVN or ID Value"),
                new IsoFieldDef("Debtor Account Tier", "SplmtryData.Envlp.CustomData.DebtorInfo.AccountTier", "//DebtorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Creditor Account Designation", "SplmtryData.Envlp.CustomData.CreditorInfo.AccountDesignation", "//CreditorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Creditor ID Type", "SplmtryData.Envlp.CustomData.CreditorInfo.IdType", "//CreditorInfo/IdType", "BVN", "Enum", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Creditor ID Value", "SplmtryData.Envlp.CustomData.CreditorInfo.IdValue", "//CreditorInfo/IdValue", "22112323460", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Creditor Account Tier", "SplmtryData.Envlp.CustomData.CreditorInfo.AccountTier", "//CreditorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Transaction Location", "SplmtryData.Envlp.CustomData.TransactionInfo.TransactionLocation", "//TransactionInfo/TransactionLocation", "01080652440N020900337921E", "String", 30, true, false, null, "Transaction Location Coordinates"),
                new IsoFieldDef("Name Enquiry Msg ID", "SplmtryData.Envlp.CustomData.TransactionInfo.NameEnquiryMsgId", "//TransactionInfo/NameEnquiryMsgId", "99905820250801205622930239203831720", "String", 35, true, false, "NPS_ID", "Name Enquiry Message ID"),
                new IsoFieldDef("Channel Code", "SplmtryData.Envlp.CustomData.TransactionInfo.ChannelCode", "//TransactionInfo/ChannelCode", "1", "Integer", 2, true, false, "CHANNEL_CODE", "Channel Code (1-11)")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12">
                    <FIToFICstmrCdtTrf>
                        <GrpHdr>
                            <MsgId>99905820250801205622930239203831721</MsgId>
                            <CreDtTm>2025-08-01T08:08:12.954+01:00</CreDtTm>
                            <BtchBookg>false</BtchBookg>
                            <NbOfTxs>1</NbOfTxs>
                            <SttlmInf>
                                <SttlmMtd>CLRG</SttlmMtd>
                            </SttlmInf>
                            <InstgAgt>
                                <FinInstnId>
                                    <ClrSysMmbId>
                                        <MmbId>999058</MmbId>
                                    </ClrSysMmbId>
                                </FinInstnId>
                            </InstgAgt>
                            <InstdAgt>
                                <FinInstnId>
                                    <ClrSysMmbId>
                                        <MmbId>999057</MmbId>
                                    </ClrSysMmbId>
                                </FinInstnId>
                            </InstdAgt>
                        </GrpHdr>
                        <CdtTrfTxInf>
                            <PmtId>
                                <InstrId>99905899905720250801205722893090687</InstrId>
                                <EndToEndId>99905899905798653637383920281615142</EndToEndId>
                                <TxId>99905820250801205622930239203831721</TxId>
                            </PmtId>
                            <PmtTpInf>
                                <ClrChanl>RTNS</ClrChanl>
                                <SvcLvl><Prtry>0100</Prtry></SvcLvl>
                                <LclInstrm><Prtry>CTAA</Prtry></LclInstrm>
                                <CtgyPurp><Prtry>001</Prtry></CtgyPurp>
                            </PmtTpInf>
                            <IntrBkSttlmAmt Ccy="NGN">78000.00</IntrBkSttlmAmt>
                            <IntrBkSttlmDt>2025-02-25Z</IntrBkSttlmDt>
                            <ChrgBr>SLEV</ChrgBr>
                            <InstgAgt>
                                <FinInstnId>
                                    <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstgAgt>
                            <InstdAgt>
                                <FinInstnId>
                                    <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstdAgt>
                            <Dbtr>
                                <Nm>James</Nm>
                            </Dbtr>
                            <DbtrAcct>
                                <Id><IBAN>0177136558</IBAN></Id>
                                <Nm>James</Nm>
                            </DbtrAcct>
                            <DbtrAgt>
                                <FinInstnId>
                                    <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </DbtrAgt>
                            <CdtrAgt>
                                <FinInstnId>
                                    <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </CdtrAgt>
                            <Cdtr>
                                <Nm>Musa</Nm>
                            </Cdtr>
                            <CdtrAcct>
                                <Id><IBAN>0177136558</IBAN></Id>
                                <Nm>Musa</Nm>
                            </CdtrAcct>
                            <RmtInf>
                                <Ustrd>Payment for services</Ustrd>
                            </RmtInf>
                        </CdtTrfTxInf>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <DebtorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22112323440</IdValue>
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
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pacs.008")
                .name("Customer Direct Credit")
                .isoCode("pacs.008.001.12")
                .category("Credit Transfer & Returns")
                .rootElement("Document")
                .mainElement("FIToFICstmrCdtTrf")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 2. pacs.002: Payment Status Report
    // ==========================================
    private static void registerPacs002() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "FIToFIPmtStsRpt.GrpHdr.MsgId", "//GrpHdr/MsgId", "09000420260402112316902158352242175", "String", 35, true, false, "NPS_ID", "Status Report Msg ID"),
                new IsoFieldDef("Creation Date Time", "FIToFIPmtStsRpt.GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-04-02T10:30:00.000+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp"),
                new IsoFieldDef("Instructing Agent BICFI", "FIToFIPmtStsRpt.GrpHdr.InstgAgt.FinInstnId.BICFI", "//GrpHdr/InstgAgt/FinInstnId/BICFI", "090004", "String", 11, true, false, "INSTITUTION_CODE", "Instructing Agent BICFI"),
                new IsoFieldDef("Instructing Agent Member ID", "FIToFIPmtStsRpt.GrpHdr.InstgAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/InstgAgt/FinInstnId/ClrSysMmbId/MmbId", "090004", "String", 6, true, false, "MEMBER_ID", "Instructing agent ID"),
                new IsoFieldDef("Instructed Agent BICFI", "FIToFIPmtStsRpt.GrpHdr.InstdAgt.FinInstnId.BICFI", "//GrpHdr/InstdAgt/FinInstnId/BICFI", "100022", "String", 11, false, false, "INSTITUTION_CODE", "Instructed Agent BICFI"),
                new IsoFieldDef("Instructed Agent Member ID", "FIToFIPmtStsRpt.GrpHdr.InstdAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/InstdAgt/FinInstnId/ClrSysMmbId/MmbId", "100022", "String", 6, true, false, "MEMBER_ID", "Instructed agent ID"),
                new IsoFieldDef("Original Message ID", "FIToFIPmtStsRpt.OrgnlGrpInfAndSts.OrgnlMsgId", "//OrgnlGrpInfAndSts/OrgnlMsgId", "10002220260402170095982371426577881", "String", 35, true, false, "NPS_ID", "Original message ID"),
                new IsoFieldDef("Original Message Name ID", "FIToFIPmtStsRpt.OrgnlGrpInfAndSts.OrgnlMsgNmId", "//OrgnlGrpInfAndSts/OrgnlMsgNmId", "pacs.008.001.12", "String", 35, true, false, null, "Original message name"),
                new IsoFieldDef("Original Creation Date Time", "FIToFIPmtStsRpt.OrgnlGrpInfAndSts.OrgnlCreDtTm", "//OrgnlGrpInfAndSts/OrgnlCreDtTm", "2026-03-27T16:30:35.072+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Original creation timestamp"),
                new IsoFieldDef("Group Status", "FIToFIPmtStsRpt.OrgnlGrpInfAndSts.GrpSts", "//OrgnlGrpInfAndSts/GrpSts", "ACSC", "String", 4, true, false, "GROUP_STATUS", "Group status code"),
                new IsoFieldDef("Status ID", "FIToFIPmtStsRpt.TxInfAndSts.StsId", "//TxInfAndSts/StsId", "AUTH", "String", 35, true, false, null, "Status ID"),
                new IsoFieldDef("Original Instruction ID", "FIToFIPmtStsRpt.TxInfAndSts.OrgnlInstrId", "//TxInfAndSts/OrgnlInstrId", "10002299905720260331122123453011923", "String", 35, true, false, "NPS_ID", "Original instruction ID"),
                new IsoFieldDef("Original End-to-End ID", "FIToFIPmtStsRpt.TxInfAndSts.OrgnlEndToEndId", "//TxInfAndSts/OrgnlEndToEndId", "10002221234519115702293163242525113", "String", 35, true, false, "NPS_ID", "Original EndToEnd ID"),
                new IsoFieldDef("Original Transaction ID", "FIToFIPmtStsRpt.TxInfAndSts.OrgnlTxId", "//TxInfAndSts/OrgnlTxId", "10002220260402170095982371426577881", "String", 35, true, false, "NPS_ID", "Original Transaction ID"),
                new IsoFieldDef("Tx Instructing Agent BICFI", "FIToFIPmtStsRpt.TxInfAndSts.InstgAgt.FinInstnId.BICFI", "//TxInfAndSts/InstgAgt/FinInstnId/BICFI", "090004", "String", 11, false, false, "INSTITUTION_CODE", "Tx Instructing Agent BICFI"),
                new IsoFieldDef("Tx Instructing Agent Member ID", "FIToFIPmtStsRpt.TxInfAndSts.InstgAgt.FinInstnId.ClrSysMmbId.MmbId", "//TxInfAndSts/InstgAgt/FinInstnId/ClrSysMmbId/MmbId", "090004", "String", 6, true, false, "MEMBER_ID", "Tx Instructing Agent Member ID"),
                new IsoFieldDef("Tx Instructed Agent BICFI", "FIToFIPmtStsRpt.TxInfAndSts.InstdAgt.FinInstnId.BICFI", "//TxInfAndSts/InstdAgt/FinInstnId/BICFI", "100022", "String", 11, false, false, "INSTITUTION_CODE", "Tx Instructed Agent BICFI"),
                new IsoFieldDef("Tx Instructed Agent Member ID", "FIToFIPmtStsRpt.TxInfAndSts.InstdAgt.FinInstnId.ClrSysMmbId.MmbId", "//TxInfAndSts/InstdAgt/FinInstnId/ClrSysMmbId/MmbId", "100022", "String", 6, true, false, "MEMBER_ID", "Tx Instructed Agent Member ID"),
                new IsoFieldDef("Original Transaction Settlement Date", "FIToFIPmtStsRpt.TxInfAndSts.OrgnlTxRef.IntrBkSttlmDt", "//OrgnlTxRef/IntrBkSttlmDt", "2026-04-02Z", "Date", 10, true, false, "DATE", "Settlement date")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12">
                    <FIToFIPmtStsRpt>
                        <GrpHdr>
                            <MsgId>09000420260402112316902158352242175</MsgId>
                            <CreDtTm>2026-04-02T10:30:00.000+01:00</CreDtTm>
                            <InstgAgt>
                                <FinInstnId>
                                    <BICFI>090004</BICFI>
                                    <ClrSysMmbId><MmbId>090004</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstgAgt>
                            <InstdAgt>
                                <FinInstnId>
                                    <BICFI>100022</BICFI>
                                    <ClrSysMmbId><MmbId>100022</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstdAgt>
                        </GrpHdr>
                        <OrgnlGrpInfAndSts>
                            <OrgnlMsgId>10002220260402170095982371426577881</OrgnlMsgId>
                            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>
                            <OrgnlCreDtTm>2026-03-27T16:30:35.072+01:00</OrgnlCreDtTm>
                            <GrpSts>ACSC</GrpSts>
                        </OrgnlGrpInfAndSts>
                        <TxInfAndSts>
                            <StsId>AUTH</StsId>
                            <OrgnlInstrId>10002299905720260331122123453011923</OrgnlInstrId>
                            <OrgnlEndToEndId>10002221234519115702293163242525113</OrgnlEndToEndId>
                            <OrgnlTxId>10002220260402170095982371426577881</OrgnlTxId>
                            <InstgAgt>
                                <FinInstnId>
                                    <BICFI>090004</BICFI>
                                    <ClrSysMmbId><MmbId>090004</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstgAgt>
                            <InstdAgt>
                                <FinInstnId>
                                    <BICFI>100022</BICFI>
                                    <ClrSysMmbId><MmbId>100022</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstdAgt>
                            <OrgnlTxRef>
                                <IntrBkSttlmDt>2026-04-02Z</IntrBkSttlmDt>
                            </OrgnlTxRef>
                        </TxInfAndSts>
                    </FIToFIPmtStsRpt>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pacs.002")
                .name("Payment Status Report")
                .isoCode("pacs.002.001.12")
                .category("Credit Transfer & Returns")
                .rootElement("Document")
                .mainElement("FIToFIPmtStsRpt")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 3. pacs.028: Payment Status Request
    // ==========================================
    private static void registerPacs028() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "FIToFIPmtStsReq.GrpHdr.MsgId", "//GrpHdr/MsgId", "99999920250829174941709740087747292", "String", 35, true, false, "NPS_ID", "Status Request Message ID"),
                new IsoFieldDef("Creation Date Time", "FIToFIPmtStsReq.GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2025-08-18T09:05:46.973+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation date time"),
                new IsoFieldDef("Instructing Agent Member ID", "FIToFIPmtStsReq.GrpHdr.InstgAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/InstgAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Instructing Agent Member ID"),
                new IsoFieldDef("Original Message ID", "FIToFIPmtStsReq.OrgnlGrpInf.OrgnlMsgId", "//OrgnlGrpInf/OrgnlMsgId", "99905820250802112346977904433112345", "String", 35, true, false, "NPS_ID", "Original message ID"),
                new IsoFieldDef("Original Message Name ID", "FIToFIPmtStsReq.OrgnlGrpInf.OrgnlMsgNmId", "//OrgnlGrpInf/OrgnlMsgNmId", "pacs.008.001.12", "String", 35, true, false, null, "Original message name"),
                new IsoFieldDef("Original Creation Date Time", "FIToFIPmtStsReq.OrgnlGrpInf.OrgnlCreDtTm", "//OrgnlGrpInf/OrgnlCreDtTm", "2025-02-25T00:02:35.072Z", "DateTime", 35, true, false, "UTC1_DATETIME", "Original creation timestamp"),
                new IsoFieldDef("Status Request ID", "FIToFIPmtStsReq.TxInf.StsReqId", "//TxInf/StsReqId", "99999920250829174941709740087747292", "String", 35, true, false, "NPS_ID", "Status request ID (Same as MsgId)"),
                new IsoFieldDef("Original Transaction ID", "FIToFIPmtStsReq.TxInf.OrgnlTxId", "//TxInf/OrgnlTxId", "99905820250802112346977904433112345", "String", 35, true, false, "NPS_ID", "Original Transaction ID"),
                new IsoFieldDef("Tx Instructing Agent BICFI", "FIToFIPmtStsReq.TxInf.InstgAgt.FinInstnId.BICFI", "//TxInf/InstgAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Tx Instructing Agent BICFI"),
                new IsoFieldDef("Tx Instructing Agent Member ID", "FIToFIPmtStsReq.TxInf.InstgAgt.FinInstnId.ClrSysMmbId.MmbId", "//TxInf/InstgAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Tx Instructing Agent Member ID"),
                new IsoFieldDef("Tx Instructed Agent BICFI", "FIToFIPmtStsReq.TxInf.InstdAgt.FinInstnId.BICFI", "//TxInf/InstdAgt/FinInstnId/BICFI", "999012", "String", 11, false, false, "INSTITUTION_CODE", "Tx Instructed Agent BICFI"),
                new IsoFieldDef("Tx Instructed Agent Member ID", "FIToFIPmtStsReq.TxInf.InstdAgt.FinInstnId.ClrSysMmbId.MmbId", "//TxInf/InstdAgt/FinInstnId/ClrSysMmbId/MmbId", "999012", "String", 6, true, false, "MEMBER_ID", "Tx Instructed Agent Member ID"),
                new IsoFieldDef("Original Transaction Settlement Date", "FIToFIPmtStsReq.TxInf.OrgnlTxRef.IntrBkSttlmDt", "//OrgnlTxRef/IntrBkSttlmDt", "2025-02-25", "Date", 10, true, false, "DATE", "Settlement date")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.028.001.06">
                    <FIToFIPmtStsReq>
                        <GrpHdr>
                            <MsgId>99999920250829174941709740087747292</MsgId>
                            <CreDtTm>2025-08-18T09:05:46.973+01:00</CreDtTm>
                            <InstgAgt>
                                <FinInstnId><ClrSysMmbId><MmbId>999999</MmbId></ClrSysMmbId></FinInstnId>
                            </InstgAgt>
                        </GrpHdr>
                        <OrgnlGrpInf>
                            <OrgnlMsgId>99905820250802112346977904433112345</OrgnlMsgId>
                            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>
                            <OrgnlCreDtTm>2025-02-25T00:02:35.072Z</OrgnlCreDtTm>
                        </OrgnlGrpInf>
                        <TxInf>
                            <StsReqId>99999920250829174941709740087747292</StsReqId>
                            <OrgnlTxId>99905820250802112346977904433112345</OrgnlTxId>
                            <InstgAgt>
                                <FinInstnId>
                                    <BICFI>999999</BICFI>
                                    <ClrSysMmbId><MmbId>999999</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstgAgt>
                            <InstdAgt>
                                <FinInstnId>
                                    <BICFI>999012</BICFI>
                                    <ClrSysMmbId><MmbId>999012</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstdAgt>
                            <OrgnlTxRef>
                                <IntrBkSttlmDt>2025-02-25</IntrBkSttlmDt>
                            </OrgnlTxRef>
                        </TxInf>
                    </FIToFIPmtStsReq>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pacs.028")
                .name("Payment Status Request")
                .isoCode("pacs.028.001.06")
                .category("Credit Transfer & Returns")
                .rootElement("Document")
                .mainElement("FIToFIPmtStsReq")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pacs.028.001.06")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 4. acmt.023: Identification Verification Request
    // ==========================================
    private static void registerAcmt023() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "IdVrfctnReq.Assgnmt.MsgId", "//Assgnmt/MsgId", "99999920250829150504887742643314693", "String", 35, true, false, "NPS_ID", "Assignment Message ID"),
                new IsoFieldDef("Creation DateTime", "IdVrfctnReq.Assgnmt.CreDtTm", "//Assgnmt/CreDtTm", "2025-08-29T15:05:04.954+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp in UTC+1"),
                new IsoFieldDef("Creator Name", "IdVrfctnReq.Assgnmt.Cretr.Pty.Nm", "//Cretr/Pty/Nm", "Crystal Bank", "String", 100, true, false, null, "Creator Name"),
                new IsoFieldDef("Assigner Name", "IdVrfctnReq.Assgnmt.Assgnr.Pty.Nm", "//Assgnr/Pty/Nm", "Oso International Bank", "String", 100, true, false, null, "Assigner Name"),
                new IsoFieldDef("Assigner Agent BICFI", "IdVrfctnReq.Assgnmt.Assgnr.Agt.FinInstnId.BICFI", "//Assgnr/Agt/FinInstnId/BICFI", "999999", "String", 11, false, false, "INSTITUTION_CODE", "Assigner BICFI / Source ID"),
                new IsoFieldDef("Assigner Agent Member ID", "IdVrfctnReq.Assgnmt.Assgnr.Agt.FinInstnId.ClrSysMmbId.MmbId", "//Assgnr/Agt/FinInstnId/ClrSysMmbId/MmbId", "999999", "String", 6, false, false, "MEMBER_ID", "Assigner Member ID"),
                new IsoFieldDef("Assignee Agent BICFI", "IdVrfctnReq.Assgnmt.Assgne.Agt.FinInstnId.BICFI", "//Assgne/Agt/FinInstnId/BICFI", "999012", "String", 11, false, false, "INSTITUTION_CODE", "Assignee BICFI / Beneficiary ID"),
                new IsoFieldDef("Assignee Agent Member ID", "IdVrfctnReq.Assgnmt.Assgne.Agt.FinInstnId.ClrSysMmbId.MmbId", "//Assgne/Agt/FinInstnId/ClrSysMmbId/MmbId", "999012", "String", 6, false, false, "MEMBER_ID", "Assignee Member ID"),
                new IsoFieldDef("Verification ID", "IdVrfctnReq.Vrfctn.Id", "//Vrfctn/Id", "99999920250829150504887742643314693", "String", 35, true, false, "NPS_ID", "Verification ID"),
                new IsoFieldDef("Party Name (to be verified)", "IdVrfctnReq.Vrfctn.PtyAndAcctId.Pty.Nm", "//Vrfctn/PtyAndAcctId/Pty/Nm", "Israel Kayole", "String", 100, true, false, null, "Party Name"),
                new IsoFieldDef("Account Number (IBAN)", "IdVrfctnReq.Vrfctn.PtyAndAcctId.Acct.Id.IBAN", "//Vrfctn/PtyAndAcctId/Acct/Id/IBAN", "1029384756", "String", 10, true, false, "NUBAN", "10-digit NUBAN")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:acmt.023.001.04">
                    <IdVrfctnReq>
                        <Assgnmt>
                            <MsgId>99999920250829150504887742643314693</MsgId>
                            <CreDtTm>2025-08-29T15:05:04.954+01:00</CreDtTm>
                            <Cretr><Pty><Nm>Crystal Bank</Nm></Pty></Cretr>
                            <Assgnr>
                                <Pty><Nm>Oso International Bank</Nm></Pty>
                                <Agt><FinInstnId><BICFI>999999</BICFI><ClrSysMmbId><MmbId>999999</MmbId></ClrSysMmbId></FinInstnId></Agt>
                            </Assgnr>
                            <Assgne>
                                <Agt><FinInstnId><BICFI>999012</BICFI><ClrSysMmbId><MmbId>999012</MmbId></ClrSysMmbId></FinInstnId></Agt>
                            </Assgne>
                        </Assgnmt>
                        <Vrfctn>
                            <Id>99999920250829150504887742643314693</Id>
                            <PtyAndAcctId>
                                <Pty><Nm>Israel Kayole</Nm></Pty>
                                <Acct><Id><IBAN>1029384756</IBAN></Id></Acct>
                            </PtyAndAcctId>
                        </Vrfctn>
                    </IdVrfctnReq>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("acmt.023")
                .name("Identification Verification Request")
                .isoCode("acmt.023.001.04")
                .category("Account Services & Statements")
                .rootElement("Document")
                .mainElement("IdVrfctnReq")
                .namespace("urn:iso:std:iso:20022:tech:xsd:acmt.023.001.04")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 5. acmt.024: Identification Verification Report
    // ==========================================
    private static void registerAcmt024() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "IdVrfctnRpt.Assgnmt.MsgId", "//Assgnmt/MsgId", "99901220250829140722546736145961156", "String", 35, true, false, "NPS_ID", "Report Message ID"),
                new IsoFieldDef("Creation DateTime", "IdVrfctnRpt.Assgnmt.CreDtTm", "//Assgnmt/CreDtTm", "2025-08-29T14:07:22.357+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp in UTC+1"),
                new IsoFieldDef("Assigner Agent BICFI", "IdVrfctnRpt.Assgnmt.Assgnr.Agt.FinInstnId.BICFI", "//Assgnr/Agt/FinInstnId/BICFI", "999012", "String", 11, false, false, "INSTITUTION_CODE", "Assigner BICFI / Sending Institution ID"),
                new IsoFieldDef("Assigner Agent Member ID", "IdVrfctnRpt.Assgnmt.Assgnr.Agt.FinInstnId.ClrSysMmbId.MmbId", "//Assgnr/Agt/FinInstnId/ClrSysMmbId/MmbId", "999012", "String", 6, false, false, "MEMBER_ID", "Assigner Member ID"),
                new IsoFieldDef("Assignee Party Name", "IdVrfctnRpt.Assgnmt.Assgne.Pty.Nm", "//Assgne/Pty/Nm", "Oso International Bank", "String", 100, true, false, null, "Assignee Party Name"),
                new IsoFieldDef("Assignee Agent BICFI", "IdVrfctnRpt.Assgnmt.Assgne.Agt.FinInstnId.BICFI", "//Assgne/Agt/FinInstnId/BICFI", "999999", "String", 11, false, false, "INSTITUTION_CODE", "Assignee BICFI / Receiving Institution ID"),
                new IsoFieldDef("Assignee Agent Member ID", "IdVrfctnRpt.Assgnmt.Assgne.Agt.FinInstnId.ClrSysMmbId.MmbId", "//Assgne/Agt/FinInstnId/ClrSysMmbId/MmbId", "999999", "String", 6, false, false, "MEMBER_ID", "Assignee Member ID"),
                new IsoFieldDef("Original Assignment Message ID", "IdVrfctnRpt.OrgnlAssgnmt.MsgId", "//OrgnlAssgnmt/MsgId", "99999920250829150504887742643314693", "String", 35, true, false, "NPS_ID", "Original request Msg ID"),
                new IsoFieldDef("Original Assignment Creation DateTime", "IdVrfctnRpt.OrgnlAssgnmt.CreDtTm", "//OrgnlAssgnmt/CreDtTm", "2025-08-29T15:05:04.347+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Original creation timestamp"),
                new IsoFieldDef("Report Original ID", "IdVrfctnRpt.Rpt.OrgnlId", "//Rpt/OrgnlId", "99999920250829150504887742643314693", "String", 35, true, false, "NPS_ID", "Original ID"),
                new IsoFieldDef("Verification Result", "IdVrfctnRpt.Rpt.Vrfctn", "//Rpt/Vrfctn", "true", "Boolean", 5, true, false, null, "Verification result flag (true/false)"),
                new IsoFieldDef("Original Account Number (IBAN)", "IdVrfctnRpt.Rpt.OrgnlPtyAndAcctId.Acct.Id.IBAN", "//OrgnlPtyAndAcctId/Acct/Id/IBAN", "1029384756", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Updated Party Name", "IdVrfctnRpt.Rpt.UpdtdPtyAndAcctId.Pty.Nm", "//UpdtdPtyAndAcctId/Pty/Nm", "Israel Kayole", "String", 100, false, false, null, "Verified Name"),
                new IsoFieldDef("Supplementary Data Type", "IdVrfctnRpt.SplmtryData.PlcAndNm", "//SplmtryData/PlcAndNm", "AdditionalVerificationDetails", "String", 35, true, false, null, "Supplementary descriptor"),
                new IsoFieldDef("Creditor Account Designation", "IdVrfctnRpt.SplmtryData.Envlp.CustomData.CreditorInfo.AccountDesignation", "//CreditorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Creditor ID Type", "IdVrfctnRpt.SplmtryData.Envlp.CustomData.CreditorInfo.IdType", "//CreditorInfo/IdType", "BVN", "Enum", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Creditor ID Value", "IdVrfctnRpt.SplmtryData.Envlp.CustomData.CreditorInfo.IdValue", "//CreditorInfo/IdValue", "22112323460", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Creditor Account Tier", "IdVrfctnRpt.SplmtryData.Envlp.CustomData.CreditorInfo.AccountTier", "//CreditorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Transaction Risk Rating", "IdVrfctnRpt.SplmtryData.Envlp.CustomData.TransactionInfo.RiskRating", "//TransactionInfo/RiskRating", "R000000000000000000B9", "String", 35, false, false, null, "Risk Rating")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:acmt.024.001.04">
                    <IdVrfctnRpt>
                        <Assgnmt>
                            <MsgId>99901220250829140722546736145961156</MsgId>
                            <CreDtTm>2025-08-29T14:07:22.357+01:00</CreDtTm>
                            <Assgnr>
                                <Agt><FinInstnId><BICFI>999012</BICFI><ClrSysMmbId><MmbId>999012</MmbId></ClrSysMmbId></FinInstnId></Agt>
                            </Assgnr>
                            <Assgne>
                                <Pty><Nm>Oso International Bank</Nm></Pty>
                                <Agt><FinInstnId><BICFI>999999</BICFI><ClrSysMmbId><MmbId>999999</MmbId></ClrSysMmbId></FinInstnId></Agt>
                            </Assgne>
                        </Assgnmt>
                        <OrgnlAssgnmt>
                            <MsgId>99999920250829150504887742643314693</MsgId>
                            <CreDtTm>2025-08-29T15:05:04.347+01:00</CreDtTm>
                        </OrgnlAssgnmt>
                        <Rpt>
                            <OrgnlId>99999920250829150504887742643314693</OrgnlId>
                            <Vrfctn>true</Vrfctn>
                            <OrgnlPtyAndAcctId>
                                <Acct><Id><IBAN>1029384756</IBAN></Id></Acct>
                            </OrgnlPtyAndAcctId>
                            <UpdtdPtyAndAcctId>
                                <Pty><Nm>Israel Kayole</Nm></Pty>
                            </UpdtdPtyAndAcctId>
                        </Rpt>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                     <CreditorInfo>
                                         <AccountDesignation>1</AccountDesignation>
                                         <IdType>BVN</IdType>
                                         <IdValue>22112323460</IdValue>
                                         <AccountTier>1</AccountTier>
                                     </CreditorInfo>
                                     <TransactionInfo>
                                         <RiskRating>R000000000000000000B9</RiskRating>
                                     </TransactionInfo>
                                 </CustomData>
                            </Envlp>
                        </SplmtryData>
                    </IdVrfctnRpt>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("acmt.024")
                .name("Identification Verification Report")
                .isoCode("acmt.024.001.04")
                .category("Account Services & Statements")
                .rootElement("Document")
                .mainElement("IdVrfctnRpt")
                .namespace("urn:iso:std:iso:20022:tech:xsd:acmt.024.001.04")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 6. pain.009: Mandate Initiation Request
    // ==========================================
    private static void registerPain009() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "GrpHdr.MsgId", "//GrpHdr/MsgId", "99905820251211112346125578725905163", "String", 35, true, false, "NPS_ID", "Mandate Msg ID"),
                new IsoFieldDef("Creation DateTime", "GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-01-06T13:16:44.976+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp in UTC+1"),
                new IsoFieldDef("Mandate ID", "Mndt.MndtId", "//Mndt/MndtId", "MNDT-RCUR-00001", "String", 35, true, false, null, "Mandate ID"),
                new IsoFieldDef("Sequence Type", "Mndt.Ocrncs.SeqTp", "//Mndt/Ocrncs/SeqTp", "RCUR", "Enum", 4, true, false, "SEQUENCE_TYPE", "Sequence Type (RCUR/OOFF)"),
                new IsoFieldDef("Frequency Type", "Mndt.Ocrncs.Frqcy.Tp", "//Mndt/Ocrncs/Frqcy/Tp", "WEEK", "Enum", 4, true, false, "FREQUENCY_TYPE", "Frequency Type (DAIL, WEEK, MNTH, etc)"),
                new IsoFieldDef("First Collection Date", "Mndt.Ocrncs.FrstColltnDt", "//Mndt/Ocrncs/FrstColltnDt", "2025-09-08", "Date", 10, true, false, "DATE", "First collection date"),
                new IsoFieldDef("Final Collection Date", "Mndt.Ocrncs.FnlColltnDt", "//Mndt/Ocrncs/FnlColltnDt", "2025-12-31", "Date", 10, true, false, "DATE", "Final collection date"),
                new IsoFieldDef("Tracking Indicator", "Mndt.TrckgInd", "//Mndt/TrckgInd", "false", "Boolean", 5, true, false, null, "Tracking indicator"),
                new IsoFieldDef("Collection Amount Currency", "Mndt.ColltnAmt.@Ccy", "//Mndt/ColltnAmt/@Ccy", "NGN", "String", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Collection Amount", "Mndt.ColltnAmt", "//Mndt/ColltnAmt", "50000.00", "Decimal", 18, true, false, "AMOUNT", "Collection amount"),
                new IsoFieldDef("Creditor Name", "Mndt.Cdtr.Nm", "//Mndt/Cdtr/Nm", "CreditorCorp", "String", 100, true, false, null, "Creditor name"),
                new IsoFieldDef("Creditor Account Number (IBAN)", "Mndt.CdtrAcct.Id.IBAN", "//Mndt/CdtrAcct/Id/IBAN", "3829837329", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Creditor Account Name", "Mndt.CdtrAcct.Nm", "//Mndt/CdtrAcct/Nm", "CreditorCorp", "String", 100, true, false, null, "Creditor Account Name"),
                new IsoFieldDef("Creditor Agent BICFI", "Mndt.CdtrAgt.FinInstnId.BICFI", "//Mndt/CdtrAgt/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Creditor Agent BICFI"),
                new IsoFieldDef("Creditor Agent Member ID", "Mndt.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//Mndt/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Creditor Member ID"),
                new IsoFieldDef("Debtor Name", "Mndt.Dbtr.Nm", "//Mndt/Dbtr/Nm", "Debtor Customer", "String", 100, true, false, null, "Debtor name"),
                new IsoFieldDef("Debtor Account Number (IBAN)", "Mndt.DbtrAcct.Id.IBAN", "//Mndt/DbtrAcct/Id/IBAN", "3829736273", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Debtor Account Name", "Mndt.DbtrAcct.Nm", "//Mndt/DbtrAcct/Nm", "Debtor Customer", "String", 100, true, false, null, "Debtor Account Name"),
                new IsoFieldDef("Debtor Agent BICFI", "Mndt.DbtrAgt.FinInstnId.BICFI", "//Mndt/DbtrAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Debtor Agent BICFI"),
                new IsoFieldDef("Debtor Agent Member ID", "Mndt.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//Mndt/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Debtor Member ID"),
                new IsoFieldDef("Referenced Document Type", "Mndt.RfrdDoc.Tp.CdOrPrtry.Cd", "//Mndt/RfrdDoc/Tp/CdOrPrtry/Cd", "INV", "String", 4, false, false, null, "Referenced Document Type"),
                new IsoFieldDef("Referenced Document Number", "Mndt.RfrdDoc.Nb", "//Mndt/RfrdDoc/Nb", "INV-2025-001", "String", 35, false, false, null, "Referenced Document Number"),
                new IsoFieldDef("Supplementary Data Place & Name", "SplmtryData.PlcAndNm", "//SplmtryData/PlcAndNm", "AdditionalVerificationDetails", "String", 35, true, false, null, "Supplementary descriptor"),
                new IsoFieldDef("Debtor Account Designation", "SplmtryData.Envlp.CustomData.DebtorInfo.AccountDesignation", "//DebtorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Debtor ID Type", "SplmtryData.Envlp.CustomData.DebtorInfo.IdType", "//DebtorInfo/IdType", "BVN", "Enum", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Debtor ID Value", "SplmtryData.Envlp.CustomData.DebtorInfo.IdValue", "//DebtorInfo/IdValue", "22222222222", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Debtor Account Tier", "SplmtryData.Envlp.CustomData.DebtorInfo.AccountTier", "//DebtorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Debtor Biometric Data", "SplmtryData.Envlp.CustomData.DebtorMetadata.BiometricData", "//DebtorMetadata/BiometricData", "a", "String", 100, false, false, null, "Debtor Biometric Data"),
                new IsoFieldDef("Debtor Address Line", "SplmtryData.Envlp.CustomData.DebtorMetadata.AdrLine", "//DebtorMetadata/AdrLine", "12 Adeola Ode Street, Victoria Island", "String", 100, true, false, null, "Debtor Address"),
                new IsoFieldDef("Debtor Phone Number", "SplmtryData.Envlp.CustomData.DebtorMetadata.PhneNb", "//DebtorMetadata/PhneNb", "09038472264", "String", 15, true, false, null, "Debtor Phone"),
                new IsoFieldDef("Debtor Email Address", "SplmtryData.Envlp.CustomData.DebtorMetadata.EmailAdr", "//DebtorMetadata/EmailAdr", "mt@nibss.com", "String", 100, true, false, null, "Debtor Email"),
                new IsoFieldDef("Creditor Account Designation", "SplmtryData.Envlp.CustomData.CreditorInfo.AccountDesignation", "//CreditorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Creditor ID Type", "SplmtryData.Envlp.CustomData.CreditorInfo.IdType", "//CreditorInfo/IdType", "BVN", "Enum", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Creditor ID Value", "SplmtryData.Envlp.CustomData.CreditorInfo.IdValue", "//CreditorInfo/IdValue", "22222222222", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Creditor Account Tier", "SplmtryData.Envlp.CustomData.CreditorInfo.AccountTier", "//CreditorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Transaction Location", "SplmtryData.Envlp.CustomData.TransactionInfo.TransactionLocation", "//TransactionInfo/TransactionLocation", "01080652440N020900337921E", "String", 30, true, false, null, "Location Coordinates"),
                new IsoFieldDef("Channel Code", "SplmtryData.Envlp.CustomData.TransactionInfo.ChannelCode", "//TransactionInfo/ChannelCode", "4", "Integer", 2, true, false, "CHANNEL_CODE", "Channel Code (1-11)"),
                new IsoFieldDef("Mandate Category", "SplmtryData.Envlp.CustomData.TransactionInfo.MandateCategory", "//TransactionInfo/MandateCategory", "0", "Integer", 1, true, false, null, "Mandate Category"),
                new IsoFieldDef("Fixed Collection Amount Indicator", "SplmtryData.Envlp.CustomData.TransactionInfo.FixedCollectionAmount", "//TransactionInfo/FixedCollectionAmount", "false", "Boolean", 5, true, false, null, "Fixed amount indicator")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.009.001.08">
                    <MndtInitnReq>
                        <GrpHdr>
                            <MsgId>99905820251211112346125578725905163</MsgId>
                            <CreDtTm>2026-01-06T13:16:44.976+01:00</CreDtTm>
                        </GrpHdr>
                        <Mndt>
                            <MndtId>MNDT-RCUR-00001</MndtId>
                            <Ocrncs>
                                <SeqTp>RCUR</SeqTp>
                                <Frqcy><Tp>WEEK</Tp></Frqcy>
                                <FrstColltnDt>2025-09-08</FrstColltnDt>
                                <FnlColltnDt>2025-12-31</FnlColltnDt>
                            </Ocrncs>
                            <TrckgInd>false</TrckgInd>
                            <ColltnAmt Ccy="NGN">50000.00</ColltnAmt>
                            <Cdtr><Nm>CreditorCorp</Nm></Cdtr>
                            <CdtrAcct><Id><IBAN>3829837329</IBAN></Id><Nm>CreditorCorp</Nm></CdtrAcct>
                            <CdtrAgt>
                                <FinInstnId>
                                    <BICFI>999058</BICFI>
                                    <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </CdtrAgt>
                            <Dbtr><Nm>Debtor Customer</Nm></Dbtr>
                            <DbtrAcct><Id><IBAN>3829736273</IBAN></Id><Nm>Debtor Customer</Nm></DbtrAcct>
                            <DbtrAgt>
                                <FinInstnId>
                                    <BICFI>999057</BICFI>
                                    <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </DbtrAgt>
                            <RfrdDoc>
                                <Tp>
                                    <CdOrPrtry><Cd>INV</Cd></CdOrPrtry>
                                </Tp>
                                <Nb>INV-2025-001</Nb>
                            </RfrdDoc>
                        </Mndt>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <DebtorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22222222222</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </DebtorInfo>
                                    <DebtorMetadata>
                                        <BiometricData>a</BiometricData>
                                        <AdrLine>12 Adeola Ode Street, Victoria Island</AdrLine>
                                        <PhneNb>09038472264</PhneNb>
                                        <EmailAdr>mt@nibss.com</EmailAdr>
                                    </DebtorMetadata>
                                    <CreditorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22222222222</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </CreditorInfo>
                                    <TransactionInfo>
                                        <TransactionLocation>01080652440N020900337921E</TransactionLocation>
                                        <ChannelCode>4</ChannelCode>
                                        <MandateCategory>0</MandateCategory>
                                        <FixedCollectionAmount>false</FixedCollectionAmount>
                                    </TransactionInfo>
                                </CustomData>
                            </Envlp>
                        </SplmtryData>
                    </MndtInitnReq>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pain.009")
                .name("Mandate Initiation Request")
                .isoCode("pain.009.001.08")
                .category("Mandate Management")
                .rootElement("Document")
                .mainElement("MndtInitnReq")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pain.009.001.08")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 7. pain.010: Mandate Amendment Request
    // ==========================================
    private static void registerPain010() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "GrpHdr.MsgId", "//GrpHdr/MsgId", "99905820251229112349998878725905165", "String", 35, true, false, "NPS_ID", "Message ID"),
                new IsoFieldDef("Creation DateTime", "GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-01-06T13:23:02.559+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp in UTC+1"),
                new IsoFieldDef("Initiating Party Name", "GrpHdr.InitgPty.Nm", "//GrpHdr/InitgPty/Nm", "ABC Tech Pvt Ltd", "String", 100, true, false, null, "Initiating Party Name"),
                new IsoFieldDef("Original Message ID", "UndrlygAmdmntDtls.OrgnlMsgInf.MsgId", "//OrgnlMsgInf/MsgId", "99905820251211112346125578725905163", "String", 35, true, false, "NPS_ID", "Original Mandate Msg ID"),
                new IsoFieldDef("Original Message Name ID", "UndrlygAmdmntDtls.OrgnlMsgInf.MsgNmId", "//OrgnlMsgInf/MsgNmId", "pain.009.001.08", "String", 35, true, false, null, "Original Message Name ID"),
                new IsoFieldDef("Original Message Creation DateTime", "UndrlygAmdmntDtls.OrgnlMsgInf.CreDtTm", "//OrgnlMsgInf/CreDtTm", "2025-12-11T16:19:15.342+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Original creation timestamp"),
                new IsoFieldDef("Amendment Reason Code", "UndrlygAmdmntDtls.AmdmntRsn.Rsn.Cd", "//AmdmntRsn/Rsn/Cd", "AC04", "String", 4, true, false, "REASON_CODE", "Amendment Reason Code"),
                new IsoFieldDef("Amendment Reason Description", "UndrlygAmdmntDtls.AmdmntRsn.Rsn.Prtry", "//AmdmntRsn/Rsn/Prtry", "Debtor Info update", "String", 100, false, false, null, "Amendment Reason Description"),
                new IsoFieldDef("Mandate ID", "UndrlygAmdmntDtls.Mndt.MndtId", "//UndrlygAmdmntDtls/Mndt/MndtId", "MNDT-RCUR-00001", "String", 35, true, false, null, "Mandate ID"),
                new IsoFieldDef("Sequence Type", "UndrlygAmdmntDtls.Mndt.Ocrncs.SeqTp", "//UndrlygAmdmntDtls/Mndt/Ocrncs/SeqTp", "RCUR", "Enum", 4, true, false, "SEQUENCE_TYPE", "Sequence Type"),
                new IsoFieldDef("Frequency Type", "UndrlygAmdmntDtls.Mndt.Ocrncs.Frqcy.Tp", "//UndrlygAmdmntDtls/Mndt/Ocrncs/Frqcy/Tp", "WEEK", "Enum", 4, true, false, "FREQUENCY_TYPE", "Frequency Type"),
                new IsoFieldDef("First Collection Date", "UndrlygAmdmntDtls.Mndt.Ocrncs.FrstColltnDt", "//UndrlygAmdmntDtls/Mndt/Ocrncs/FrstColltnDt", "2025-09-08", "Date", 10, true, false, "DATE", "First collection date"),
                new IsoFieldDef("Final Collection Date", "UndrlygAmdmntDtls.Mndt.Ocrncs.FnlColltnDt", "//UndrlygAmdmntDtls/Mndt/Ocrncs/FnlColltnDt", "2025-12-31", "Date", 10, true, false, "DATE", "Final collection date"),
                new IsoFieldDef("Tracking Indicator", "UndrlygAmdmntDtls.Mndt.TrckgInd", "//Mndt/TrckgInd", "false", "Boolean", 5, false, false, null, "Tracking indicator"),
                new IsoFieldDef("Creditor Name", "UndrlygAmdmntDtls.Mndt.Cdtr.Nm", "//UndrlygAmdmntDtls/Mndt/Cdtr/Nm", "ABC Tech Pvt Ltd", "String", 100, true, false, null, "Creditor Name"),
                new IsoFieldDef("Creditor Account Number (IBAN)", "UndrlygAmdmntDtls.Mndt.CdtrAcct.Id.IBAN", "//UndrlygAmdmntDtls/Mndt/CdtrAcct/Id/IBAN", "3232444422", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Creditor Account Name", "UndrlygAmdmntDtls.Mndt.CdtrAcct.Nm", "//UndrlygAmdmntDtls/Mndt/CdtrAcct/Nm", "ABC Tech Pvt Ltd", "String", 100, true, false, null, "Creditor Account Name"),
                new IsoFieldDef("Creditor Agent BICFI", "UndrlygAmdmntDtls.Mndt.CdtrAgt.FinInstnId.BICFI", "//UndrlygAmdmntDtls/Mndt/CdtrAgt/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Creditor Agent BICFI"),
                new IsoFieldDef("Creditor Agent Member ID", "UndrlygAmdmntDtls.Mndt.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//UndrlygAmdmntDtls/Mndt/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Creditor Agent Member ID"),
                new IsoFieldDef("Debtor Name", "UndrlygAmdmntDtls.Mndt.Dbtr.Nm", "//UndrlygAmdmntDtls/Mndt/Dbtr/Nm", "Mr. Fred", "String", 100, true, false, null, "Debtor Name"),
                new IsoFieldDef("Debtor Account Number (IBAN)", "UndrlygAmdmntDtls.Mndt.DbtrAcct.Id.IBAN", "//UndrlygAmdmntDtls/Mndt/DbtrAcct/Id/IBAN", "4343211111", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Debtor Account Name", "UndrlygAmdmntDtls.Mndt.DbtrAcct.Nm", "//UndrlygAmdmntDtls/Mndt/DbtrAcct/Nm", "Mr. Fred", "String", 100, true, false, null, "Debtor Account Name"),
                new IsoFieldDef("Debtor Agent BICFI", "UndrlygAmdmntDtls.Mndt.DbtrAgt.FinInstnId.BICFI", "//UndrlygAmdmntDtls/Mndt/DbtrAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Debtor Agent BICFI"),
                new IsoFieldDef("Debtor Agent Member ID", "UndrlygAmdmntDtls.Mndt.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//UndrlygAmdmntDtls/Mndt/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Debtor Agent Member ID"),
                new IsoFieldDef("Original Mandate ID", "UndrlygAmdmntDtls.OrgnlMndt.OrgnlMndtId", "//OrgnlMndt/OrgnlMndtId", "MNDT-RCUR-00001", "String", 35, true, false, null, "Original Mandate ID")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.010.001.08">
                    <MndtAmdmntReq>
                        <GrpHdr>
                            <MsgId>99905820251229112349998878725905165</MsgId>
                            <CreDtTm>2026-01-06T13:23:02.559+01:00</CreDtTm>
                            <InitgPty><Nm>ABC Tech Pvt Ltd</Nm></InitgPty>
                        </GrpHdr>
                        <UndrlygAmdmntDtls>
                            <OrgnlMsgInf>
                                <MsgId>99905820251211112346125578725905163</MsgId>
                                <MsgNmId>pain.009.001.08</MsgNmId>
                                <CreDtTm>2025-12-11T16:19:15.342+01:00</CreDtTm>
                            </OrgnlMsgInf>
                            <AmdmntRsn>
                                <Rsn>
                                    <Cd>AC04</Cd>
                                    <Prtry>Debtor Info update</Prtry>
                                </Rsn>
                            </AmdmntRsn>
                            <Mndt>
                                <MndtId>MNDT-RCUR-00001</MndtId>
                                <Ocrncs>
                                    <SeqTp>RCUR</SeqTp>
                                    <Frqcy><Tp>WEEK</Tp></Frqcy>
                                    <FrstColltnDt>2025-09-08</FrstColltnDt>
                                    <FnlColltnDt>2025-12-31</FnlColltnDt>
                                </Ocrncs>
                                <TrckgInd>false</TrckgInd>
                                <Cdtr><Nm>ABC Tech Pvt Ltd</Nm></Cdtr>
                                <CdtrAcct><Id><IBAN>3232444422</IBAN></Id><Nm>ABC Tech Pvt Ltd</Nm></CdtrAcct>
                                <CdtrAgt>
                                    <FinInstnId>
                                        <BICFI>999058</BICFI>
                                        <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                    </FinInstnId>
                                </CdtrAgt>
                                <Dbtr><Nm>Mr. Fred</Nm></Dbtr>
                                <DbtrAcct><Id><IBAN>4343211111</IBAN></Id><Nm>Mr. Fred</Nm></DbtrAcct>
                                <DbtrAgt>
                                    <FinInstnId>
                                        <BICFI>999057</BICFI>
                                        <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                    </FinInstnId>
                                </DbtrAgt>
                            </Mndt>
                            <OrgnlMndt><OrgnlMndtId>MNDT-RCUR-00001</OrgnlMndtId></OrgnlMndt>
                        </UndrlygAmdmntDtls>
                    </MndtAmdmntReq>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pain.010")
                .name("Mandate Amendment Request")
                .isoCode("pain.010.001.08")
                .category("Mandate Management")
                .rootElement("Document")
                .mainElement("MndtAmdmntReq")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pain.010.001.08")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 8. pain.011: Mandate Cancellation Request
    // ==========================================
    private static void registerPain011() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "GrpHdr.MsgId", "//GrpHdr/MsgId", "99905720251222112349998878725905163", "String", 35, true, false, "NPS_ID", "Message ID"),
                new IsoFieldDef("Creation DateTime", "GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-01-06T13:39:01.110+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp"),
                new IsoFieldDef("Original Message ID", "UndrlygCxlDtls.OrgnlMsgInf.MsgId", "//OrgnlMsgInf/MsgId", "99905820251211112346125578725905163", "String", 35, true, false, "NPS_ID", "Original Msg ID"),
                new IsoFieldDef("Original Message Name ID", "UndrlygCxlDtls.OrgnlMsgInf.MsgNmId", "//OrgnlMsgInf/MsgNmId", "pain.009.001.08", "String", 35, true, false, null, "Original Message Name ID"),
                new IsoFieldDef("Original Message Creation DateTime", "UndrlygCxlDtls.OrgnlMsgInf.CreDtTm", "//OrgnlMsgInf/CreDtTm", "2025-12-11T16:19:15.342+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Original message creation timestamp"),
                new IsoFieldDef("Cancellation Reason Code", "UndrlygCxlDtls.CxlRsn.Rsn.Cd", "//CxlRsn/Rsn/Cd", "AC04", "String", 4, true, false, "REASON_CODE", "Cancellation Reason Code"),
                new IsoFieldDef("Cancellation Reason Description", "UndrlygCxlDtls.CxlRsn.Rsn.Prtry", "//CxlRsn/Rsn/Prtry", "Mandate cancelled", "String", 100, false, false, null, "Cancellation Reason Description"),
                new IsoFieldDef("Original Mandate ID", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndtId", "//OrgnlMndt/OrgnlMndtId", "MNDT-RCUR-00001", "String", 35, true, false, null, "Original Mandate ID"),
                new IsoFieldDef("Sequence Type", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.Ocrncs.SeqTp", "//OrgnlMndt/OrgnlMndt/Ocrncs/SeqTp", "RCUR", "Enum", 4, true, false, "SEQUENCE_TYPE", "Sequence Type"),
                new IsoFieldDef("Frequency Type", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.Ocrncs.Frqcy.Tp", "//OrgnlMndt/OrgnlMndt/Ocrncs/Frqcy/Tp", "WEEK", "Enum", 4, true, false, "FREQUENCY_TYPE", "Frequency Type"),
                new IsoFieldDef("First Collection Date", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.Ocrncs.FrstColltnDt", "//OrgnlMndt/OrgnlMndt/Ocrncs/FrstColltnDt", "2025-09-08", "Date", 10, true, false, "DATE", "First collection date"),
                new IsoFieldDef("Final Collection Date", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.Ocrncs.FnlColltnDt", "//OrgnlMndt/OrgnlMndt/Ocrncs/FnlColltnDt", "2025-12-31", "Date", 10, true, false, "DATE", "Final collection date"),
                new IsoFieldDef("Tracking Indicator", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.TrckgInd", "//OrgnlMndt/OrgnlMndt/TrckgInd", "false", "Boolean", 5, true, false, null, "Tracking indicator"),
                new IsoFieldDef("Creditor Name", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.Cdtr.Nm", "//OrgnlMndt/OrgnlMndt/Cdtr/Nm", "CreditorCorp", "String", 100, true, false, null, "Creditor Name"),
                new IsoFieldDef("Creditor Account Number (IBAN)", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.CdtrAcct.Id.IBAN", "//OrgnlMndt/OrgnlMndt/CdtrAcct/Id/IBAN", "8593109384", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Creditor Account Name", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.CdtrAcct.Nm", "//OrgnlMndt/OrgnlMndt/CdtrAcct/Nm", "CreditorCorp", "String", 100, true, false, null, "Creditor Account Name"),
                new IsoFieldDef("Creditor Agent BICFI", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.CdtrAgt.FinInstnId.BICFI", "//OrgnlMndt/OrgnlMndt/CdtrAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Creditor Agent BICFI"),
                new IsoFieldDef("Creditor Agent Member ID", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//OrgnlMndt/OrgnlMndt/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Creditor Agent Member ID"),
                new IsoFieldDef("Debtor Name", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.Dbtr.Nm", "//OrgnlMndt/OrgnlMndt/Dbtr/Nm", "Debtor Customer", "String", 100, true, false, null, "Debtor Name"),
                new IsoFieldDef("Debtor Account Number (IBAN)", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.DbtrAcct.Id.IBAN", "//OrgnlMndt/OrgnlMndt/DbtrAcct/Id/IBAN", "5498573829", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Debtor Account Name", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.DbtrAcct.Nm", "//OrgnlMndt/OrgnlMndt/DbtrAcct/Nm", "Debtor Customer", "String", 100, true, false, null, "Debtor Account Name"),
                new IsoFieldDef("Debtor Agent BICFI", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.DbtrAgt.FinInstnId.BICFI", "//OrgnlMndt/OrgnlMndt/DbtrAgt/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Debtor Agent BICFI"),
                new IsoFieldDef("Debtor Agent Member ID", "UndrlygCxlDtls.OrgnlMndt.OrgnlMndt.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//OrgnlMndt/OrgnlMndt/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Debtor Agent Member ID")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.011.001.08">
                    <MndtCxlReq>
                        <GrpHdr>
                            <MsgId>99905720251222112349998878725905163</MsgId>
                            <CreDtTm>2026-01-06T13:39:01.110+01:00</CreDtTm>
                        </GrpHdr>
                        <UndrlygCxlDtls>
                            <OrgnlMsgInf>
                                <MsgId>99905820251211112346125578725905163</MsgId>
                                <MsgNmId>pain.009.001.08</MsgNmId>
                                <CreDtTm>2025-12-11T16:19:15.342+01:00</CreDtTm>
                            </OrgnlMsgInf>
                            <CxlRsn>
                                <Rsn>
                                    <Cd>AC04</Cd>
                                    <Prtry>Mandate cancelled</Prtry>
                                </Rsn>
                            </CxlRsn>
                            <OrgnlMndt>
                                <OrgnlMndtId>MNDT-RCUR-00001</OrgnlMndtId>
                                <OrgnlMndt>
                                    <Ocrncs>
                                        <SeqTp>RCUR</SeqTp>
                                        <Frqcy><Tp>WEEK</Tp></Frqcy>
                                        <FrstColltnDt>2025-09-08</FrstColltnDt>
                                        <FnlColltnDt>2025-12-31</FnlColltnDt>
                                    </Ocrncs>
                                    <TrckgInd>false</TrckgInd>
                                    <Cdtr><Nm>CreditorCorp</Nm></Cdtr>
                                    <CdtrAcct>
                                        <Id><IBAN>8593109384</IBAN></Id>
                                        <Nm>CreditorCorp</Nm>
                                    </CdtrAcct>
                                    <CdtrAgt>
                                        <FinInstnId>
                                            <BICFI>999057</BICFI>
                                            <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                        </FinInstnId>
                                    </CdtrAgt>
                                    <Dbtr><Nm>Debtor Customer</Nm></Dbtr>
                                    <DbtrAcct>
                                        <Id><IBAN>5498573829</IBAN></Id>
                                        <Nm>Debtor Customer</Nm>
                                    </DbtrAcct>
                                    <DbtrAgt>
                                        <FinInstnId>
                                            <BICFI>999058</BICFI>
                                            <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                        </FinInstnId>
                                    </DbtrAgt>
                                </OrgnlMndt>
                            </OrgnlMndt>
                        </UndrlygCxlDtls>
                    </MndtCxlReq>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pain.011")
                .name("Mandate Cancellation Request")
                .isoCode("pain.011.001.08")
                .category("Mandate Management")
                .rootElement("Document")
                .mainElement("MndtCxlReq")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pain.011.001.08")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 9. pain.012: Mandate Acceptance Report
    // ==========================================
    private static void registerPain012() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "GrpHdr.MsgId", "//GrpHdr/MsgId", "99905820251212112349998878725905163", "String", 35, true, false, "NPS_ID", "Message ID"),
                new IsoFieldDef("Creation DateTime", "GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-01-06T13:36:15.999+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp in UTC+1"),
                new IsoFieldDef("Original Message ID", "UndrlygAccptncDtls.OrgnlMsgInf.MsgId", "//OrgnlMsgInf/MsgId", "99905820251211112346125578725905163", "String", 35, true, false, "NPS_ID", "Original Mandate Msg ID"),
                new IsoFieldDef("Original Message Name ID", "UndrlygAccptncDtls.OrgnlMsgInf.MsgNmId", "//OrgnlMsgInf/MsgNmId", "pain.009.001.08", "String", 35, true, false, null, "Original Message Name"),
                new IsoFieldDef("Original Message Creation DateTime", "UndrlygAccptncDtls.OrgnlMsgInf.CreDtTm", "//OrgnlMsgInf/CreDtTm", "2025-12-11T16:19:15.342+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Original creation timestamp"),
                new IsoFieldDef("Acceptance Indicator", "UndrlygAccptncDtls.AccptncRslt.Accptd", "//AccptncRslt/Accptd", "true", "Boolean", 5, true, false, null, "Accepted flag (true/false)"),
                new IsoFieldDef("Original Mandate ID", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndtId", "//OrgnlMndt/OrgnlMndtId", "MNDT-RCUR-00001", "String", 35, true, false, null, "Original Mandate ID"),
                new IsoFieldDef("Sequence Type", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.Ocrncs.SeqTp", "//OrgnlMndt/OrgnlMndt/Ocrncs/SeqTp", "RCUR", "Enum", 4, true, false, "SEQUENCE_TYPE", "Sequence Type"),
                new IsoFieldDef("Frequency Type", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.Ocrncs.Frqcy.Tp", "//OrgnlMndt/OrgnlMndt/Ocrncs/Frqcy/Tp", "WEEK", "Enum", 4, true, false, "FREQUENCY_TYPE", "Frequency Type"),
                new IsoFieldDef("First Collection Date", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.Ocrncs.FrstColltnDt", "//OrgnlMndt/OrgnlMndt/Ocrncs/FrstColltnDt", "2025-09-08", "Date", 10, true, false, "DATE", "First collection date"),
                new IsoFieldDef("Final Collection Date", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.Ocrncs.FnlColltnDt", "//OrgnlMndt/OrgnlMndt/Ocrncs/FnlColltnDt", "2025-12-31", "Date", 10, true, false, "DATE", "Final collection date"),
                new IsoFieldDef("Tracking Indicator", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.TrckgInd", "//OrgnlMndt/OrgnlMndt/TrckgInd", "false", "Boolean", 5, false, false, null, "Tracking indicator"),
                new IsoFieldDef("Creditor Name", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.Cdtr.Nm", "//OrgnlMndt/OrgnlMndt/Cdtr/Nm", "CreditorCorp", "String", 100, true, false, null, "Creditor Name"),
                new IsoFieldDef("Creditor Account Number (IBAN)", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.CdtrAcct.Id.IBAN", "//OrgnlMndt/OrgnlMndt/CdtrAcct/Id/IBAN", "5555544443", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Creditor Account Name", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.CdtrAcct.Nm", "//OrgnlMndt/CdtrAcct/Nm", "CreditorCorp", "String", 100, true, false, null, "Creditor Account Name"),
                new IsoFieldDef("Creditor Agent BICFI", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.CdtrAgt.FinInstnId.BICFI", "//OrgnlMndt/OrgnlMndt/CdtrAgt/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Creditor Agent BICFI"),
                new IsoFieldDef("Creditor Agent Member ID", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//OrgnlMndt/OrgnlMndt/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Creditor Agent Member ID"),
                new IsoFieldDef("Debtor Name", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.Dbtr.Nm", "//OrgnlMndt/OrgnlMndt/Dbtr/Nm", "Debtor Customer", "String", 100, true, false, null, "Debtor Name"),
                new IsoFieldDef("Debtor Account Number (IBAN)", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.DbtrAcct.Id.IBAN", "//OrgnlMndt/DbtrAcct/Id/IBAN", "8888899999", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Debtor Account Name", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.DbtrAcct.Nm", "//OrgnlMndt/DbtrAcct/Nm", "Debtor Customer", "String", 100, true, false, null, "Debtor Account Name"),
                new IsoFieldDef("Debtor Agent BICFI", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.DbtrAgt.FinInstnId.BICFI", "//OrgnlMndt/OrgnlMndt/DbtrAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Debtor Agent BICFI"),
                new IsoFieldDef("Debtor Agent Member ID", "UndrlygAccptncDtls.OrgnlMndt.OrgnlMndt.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//OrgnlMndt/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Debtor Agent Member ID")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.012.001.08">
                    <MndtAccptncRpt>
                        <GrpHdr>
                            <MsgId>99905820251212112349998878725905163</MsgId>
                            <CreDtTm>2026-01-06T13:36:15.999+01:00</CreDtTm>
                        </GrpHdr>
                        <UndrlygAccptncDtls>
                            <OrgnlMsgInf>
                                <MsgId>99905820251211112346125578725905163</MsgId>
                                <MsgNmId>pain.009.001.08</MsgNmId>
                                <CreDtTm>2025-12-11T16:19:15.342+01:00</CreDtTm>
                            </OrgnlMsgInf>
                            <AccptncRslt>
                                <Accptd>true</Accptd>
                            </AccptncRslt>
                            <OrgnlMndt>
                                <OrgnlMndtId>MNDT-RCUR-00001</OrgnlMndtId>
                                <OrgnlMndt>
                                    <Ocrncs>
                                        <SeqTp>RCUR</SeqTp>
                                        <Frqcy><Tp>WEEK</Tp></Frqcy>
                                        <FrstColltnDt>2025-09-08</FrstColltnDt>
                                        <FnlColltnDt>2025-12-31</FnlColltnDt>
                                    </Ocrncs>
                                    <TrckgInd>false</TrckgInd>
                                    <Cdtr><Nm>CreditorCorp</Nm></Cdtr>
                                    <CdtrAcct>
                                        <Id><IBAN>5555544443</IBAN></Id>
                                        <Nm>CreditorCorp</Nm>
                                    </CdtrAcct>
                                    <CdtrAgt>
                                        <FinInstnId>
                                            <BICFI>999058</BICFI>
                                            <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                        </FinInstnId>
                                    </CdtrAgt>
                                    <Dbtr><Nm>Debtor Customer</Nm></Dbtr>
                                    <DbtrAcct>
                                        <Id><IBAN>8888899999</IBAN></Id>
                                        <Nm>Debtor Customer</Nm>
                                    </DbtrAcct>
                                    <DbtrAgt>
                                        <FinInstnId>
                                            <BICFI>999057</BICFI>
                                            <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                        </FinInstnId>
                                    </DbtrAgt>
                                </OrgnlMndt>
                            </OrgnlMndt>
                        </UndrlygAccptncDtls>
                    </MndtAccptncRpt>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pain.012")
                .name("Mandate Acceptance Report")
                .isoCode("pain.012.001.08")
                .category("Mandate Management")
                .rootElement("Document")
                .mainElement("MndtAccptncRpt")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pain.012.001.08")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // ==========================================
    // 10. pacs.003: Direct Debit Transfer
    // ==========================================
    private static void registerPacs003() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "GrpHdr.MsgId", "//GrpHdr/MsgId", "99999820260127101256312032527582141", "String", 35, true, false, "NPS_ID", "Direct Debit Msg ID"),
                new IsoFieldDef("Creation DateTime", "GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-01-27T10:12:56+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp"),
                new IsoFieldDef("Number of Transactions", "GrpHdr.NbOfTxs", "//GrpHdr/NbOfTxs", "1", "Numeric", 15, true, false, null, "Nb of transactions"),
                new IsoFieldDef("Control Sum", "GrpHdr.CtrlSum", "//GrpHdr/CtrlSum", "1000.00", "Decimal", 18, true, false, "AMOUNT", "Control Sum"),
                new IsoFieldDef("Instructing Agent BICFI", "GrpHdr.InstgAgt.FinInstnId.BICFI", "//GrpHdr/InstgAgt/FinInstnId/BICFI", "999998", "String", 11, false, false, "INSTITUTION_CODE", "Instructing Agent BICFI"),
                new IsoFieldDef("Instructing Agent Member ID", "GrpHdr.InstgAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/InstgAgt/FinInstnId/ClrSysMmbId/MmbId", "999998", "String", 6, true, false, "MEMBER_ID", "Instructing Agent Member ID"),
                new IsoFieldDef("Instructed Agent BICFI", "GrpHdr.InstdAgt.FinInstnId.BICFI", "//GrpHdr/InstdAgt/FinInstnId/BICFI", "999997", "String", 11, false, false, "INSTITUTION_CODE", "Instructed Agent BICFI"),
                new IsoFieldDef("Instructed Agent Member ID", "GrpHdr.InstdAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/InstdAgt/FinInstnId/ClrSysMmbId/MmbId", "999997", "String", 6, true, false, "MEMBER_ID", "Instructed Agent Member ID"),
                new IsoFieldDef("Instruction ID", "DrctDbtTxInf.PmtId.InstrId", "//DrctDbtTxInf/PmtId/InstrId", "99999899999720260127101256532309267", "String", 35, true, false, "NPS_ID", "Instruction ID"),
                new IsoFieldDef("End-to-End ID", "DrctDbtTxInf.PmtId.EndToEndId", "//DrctDbtTxInf/PmtId/EndToEndId", "99999813263863598554981095085144921", "String", 35, true, false, "NPS_ID", "EndToEnd ID"),
                new IsoFieldDef("Transaction ID", "DrctDbtTxInf.PmtId.TxId", "//DrctDbtTxInf/PmtId/TxId", "99999820260127101256312032527582141", "String", 35, true, false, "NPS_ID", "Transaction ID"),
                new IsoFieldDef("Local Instrument Code", "DrctDbtTxInf.PmtTpInf.LclInstrm.Prtry", "//DrctDbtTxInf/PmtTpInf/LclInstrm/Prtry", "NPS", "String", 35, false, false, null, "Local Instrument Code"),
                new IsoFieldDef("Interbank Settlement Amount", "DrctDbtTxInf.IntrBkSttlmAmt", "//DrctDbtTxInf/IntrBkSttlmAmt", "1000.00", "Decimal", 18, true, false, "AMOUNT", "Settlement amount"),
                new IsoFieldDef("Interbank Settlement Currency", "DrctDbtTxInf.IntrBkSttlmAmt@Ccy", "//DrctDbtTxInf/IntrBkSttlmAmt/@Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Interbank Settlement Date", "DrctDbtTxInf.IntrBkSttlmDt", "//DrctDbtTxInf/IntrBkSttlmDt", "2026-01-27", "Date", 10, true, false, "DATE", "Settlement Date"),
                new IsoFieldDef("Instructed Amount", "DrctDbtTxInf.InstdAmt", "//DrctDbtTxInf/InstdAmt", "1000.00", "Decimal", 18, true, false, "AMOUNT", "Instructed Amount"),
                new IsoFieldDef("Mandate ID", "DrctDbtTxInf.DrctDbtTx.MndtRltdInf.MndtId", "//MndtRltdInf/MndtId", "MNDT-RCUR-00001", "String", 35, true, false, null, "Mandate ID"),
                new IsoFieldDef("Date of Signature", "DrctDbtTxInf.DrctDbtTx.MndtRltdInf.DtOfSgntr", "//MndtRltdInf/DtOfSgntr", "2026-01-27", "Date", 10, true, false, "DATE", "Date of Signature"),
                new IsoFieldDef("First Collection Date", "DrctDbtTxInf.DrctDbtTx.MndtRltdInf.FrstColltnDt", "//MndtRltdInf/FrstColltnDt", "2026-01-27", "Date", 10, true, false, "DATE", "First Collection Date"),
                new IsoFieldDef("Final Collection Date", "DrctDbtTxInf.DrctDbtTx.MndtRltdInf.FnlColltnDt", "//MndtRltdInf/FnlColltnDt", "2026-01-28", "Date", 10, false, false, "DATE", "Final Collection Date"),
                new IsoFieldDef("Frequency Type", "DrctDbtTxInf.DrctDbtTx.MndtRltdInf.Frqcy.Tp", "//MndtRltdInf/Frqcy/Tp", "WEEK", "String", 4, true, false, "FREQUENCY_TYPE", "Frequency Type"),
                new IsoFieldDef("Creditor Name", "DrctDbtTxInf.Cdtr.Nm", "//DrctDbtTxInf/Cdtr/Nm", "KaYole", "String", 100, true, false, null, "Creditor Name"),
                new IsoFieldDef("Creditor Account IBAN", "DrctDbtTxInf.CdtrAcct.Id.IBAN", "//DrctDbtTxInf/CdtrAcct/Id/IBAN", "2210664433", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Creditor Account Name", "DrctDbtTxInf.CdtrAcct.Nm", "//DrctDbtTxInf/CdtrAcct/Nm", "KaYole", "String", 100, true, false, null, "Creditor Account Name"),
                new IsoFieldDef("Creditor Agent BICFI", "DrctDbtTxInf.CdtrAgt.FinInstnId.BICFI", "//DrctDbtTxInf/CdtrAgt/FinInstnId/BICFI", "999998", "String", 11, false, false, "INSTITUTION_CODE", "Creditor Agent BICFI"),
                new IsoFieldDef("Creditor Agent Member ID", "DrctDbtTxInf.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//DrctDbtTxInf/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999998", "String", 6, true, false, "MEMBER_ID", "Creditor Agent Member ID"),
                new IsoFieldDef("Debtor Name", "DrctDbtTxInf.Dbtr.Nm", "//DrctDbtTxInf/Dbtr/Nm", "Musa", "String", 100, true, false, null, "Debtor Name"),
                new IsoFieldDef("Debtor Account IBAN", "DrctDbtTxInf.DbtrAcct.Id.IBAN", "//DrctDbtTxInf/DbtrAcct/Id/IBAN", "2110334983", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Debtor Account Name", "DrctDbtTxInf.DbtrAcct.Nm", "//DrctDbtTxInf/DbtrAcct/Nm", "Musa", "String", 100, true, false, null, "Debtor Account Name"),
                new IsoFieldDef("Debtor Agent BICFI", "DrctDbtTxInf.DbtrAgt.FinInstnId.BICFI", "//DrctDbtTxInf/DbtrAgt/FinInstnId/BICFI", "999997", "String", 11, false, false, "INSTITUTION_CODE", "Debtor Agent BICFI"),
                new IsoFieldDef("Debtor Agent Member ID", "DrctDbtTxInf.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//DrctDbtTxInf/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999997", "String", 6, true, false, "MEMBER_ID", "Debtor Agent Member ID"),
                new IsoFieldDef("Place and Name", "SplmtryData.PlcAndNm", "//SplmtryData/PlcAndNm", "AdditionalVerificationDetails", "String", 35, true, false, null, "Supplementary descriptor"),
                new IsoFieldDef("Debtor Account Designation", "SplmtryData.Envlp.CustomData.DebtorInfo.AccountDesignation", "//DebtorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Debtor ID Type", "SplmtryData.Envlp.CustomData.DebtorInfo.IdType", "//DebtorInfo/IdType", "BVN", "String", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Debtor ID Value", "SplmtryData.Envlp.CustomData.DebtorInfo.IdValue", "//DebtorInfo/IdValue", "22222222222", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Debtor Account Tier", "SplmtryData.Envlp.CustomData.DebtorInfo.AccountTier", "//DebtorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Creditor Account Designation", "SplmtryData.Envlp.CustomData.CreditorInfo.AccountDesignation", "//CreditorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Creditor ID Type", "SplmtryData.Envlp.CustomData.CreditorInfo.IdType", "//CreditorInfo/IdType", "BVN", "String", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Creditor ID Value", "SplmtryData.Envlp.CustomData.CreditorInfo.IdValue", "//CreditorInfo/IdValue", "22222222222", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Creditor Account Tier", "SplmtryData.Envlp.CustomData.CreditorInfo.AccountTier", "//CreditorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Transaction Location", "SplmtryData.Envlp.CustomData.TransactionInfo.TransactionLocation", "//TransactionInfo/TransactionLocation", "01080652440N020900337921E", "String", 30, true, false, null, "Location Coordinates"),
                new IsoFieldDef("Name Enquiry Msg ID", "SplmtryData.Envlp.CustomData.TransactionInfo.NameEnquiryMsgId", "//TransactionInfo/NameEnquiryMsgId", "99999820260127101157171722205219993", "String", 35, true, false, "NPS_ID", "Name Enquiry Message ID"),
                new IsoFieldDef("Channel Code", "SplmtryData.Envlp.CustomData.TransactionInfo.ChannelCode", "//TransactionInfo/ChannelCode", "1", "String", 2, true, false, "CHANNEL_CODE", "Channel Code (1-11)")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.003.001.11">
                    <FIToFICstmrDrctDbt>
                        <GrpHdr>
                            <MsgId>99999820260127101256312032527582141</MsgId>
                            <CreDtTm>2026-01-27T10:12:56+01:00</CreDtTm>
                            <NbOfTxs>1</NbOfTxs>
                            <CtrlSum>1000.00</CtrlSum>
                            <InstgAgt>
                                <FinInstnId>
                                    <BICFI>999998</BICFI>
                                    <ClrSysMmbId><MmbId>999998</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstgAgt>
                            <InstdAgt>
                                <FinInstnId>
                                    <BICFI>999997</BICFI>
                                    <ClrSysMmbId><MmbId>999997</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstdAgt>
                        </GrpHdr>
                        <DrctDbtTxInf>
                            <PmtId>
                                <InstrId>99999899999720260127101256532309267</InstrId>
                                <EndToEndId>99999813263863598554981095085144921</EndToEndId>
                                <TxId>99999820260127101256312032527582141</TxId>
                            </PmtId>
                            <PmtTpInf>
                                <LclInstrm>
                                    <Prtry>NPS</Prtry>
                                </LclInstrm>
                            </PmtTpInf>
                            <IntrBkSttlmAmt Ccy="NGN">1000.00</IntrBkSttlmAmt>
                            <IntrBkSttlmDt>2026-01-27</IntrBkSttlmDt>
                            <InstdAmt Ccy="NGN">1000.00</InstdAmt>
                            <DrctDbtTx>
                                <MndtRltdInf>
                                    <MndtId>MNDT-RCUR-00001</MndtId>
                                    <DtOfSgntr>2026-01-27</DtOfSgntr>
                                    <FrstColltnDt>2026-01-27</FrstColltnDt>
                                    <FnlColltnDt>2026-01-28</FnlColltnDt>
                                    <Frqcy><Tp>WEEK</Tp></Frqcy>
                                </MndtRltdInf>
                            </DrctDbtTx>
                            <Cdtr><Nm>KaYole</Nm></Cdtr>
                            <CdtrAcct><Id><IBAN>2210664433</IBAN></Id><Nm>KaYole</Nm></CdtrAcct>
                            <CdtrAgt>
                                <FinInstnId>
                                    <BICFI>999998</BICFI>
                                    <ClrSysMmbId><MmbId>999998</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </CdtrAgt>
                            <InstgAgt>
                                <FinInstnId>
                                    <BICFI>999998</BICFI>
                                    <ClrSysMmbId><MmbId>999998</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstgAgt>
                            <InstdAgt>
                                <FinInstnId>
                                    <BICFI>999997</BICFI>
                                    <ClrSysMmbId><MmbId>999997</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </InstdAgt>
                            <Dbtr><Nm>Musa</Nm></Dbtr>
                            <DbtrAcct><Id><IBAN>2110334983</IBAN></Id><Nm>Musa</Nm></DbtrAcct>
                            <DbtrAgt>
                                <FinInstnId>
                                    <BICFI>999997</BICFI>
                                    <ClrSysMmbId><MmbId>999997</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </DbtrAgt>
                            <RmtInf><Ustrd>Invoice INV-2025-001</Ustrd></RmtInf>
                        </DrctDbtTxInf>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <DebtorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22222222222</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </DebtorInfo>
                                    <CreditorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22222222222</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </CreditorInfo>
                                    <TransactionInfo>
                                        <TransactionLocation>01080652440N020900337921E</TransactionLocation>
                                        <NameEnquiryMsgId>99999820260127101157171722205219993</NameEnquiryMsgId>
                                        <ChannelCode>1</ChannelCode>
                                        <RiskRating>R000000000000000000B9</RiskRating>
                                        <FixedCollectionAmount>false</FixedCollectionAmount>
                                    </TransactionInfo>
                                </CustomData>
                            </Envlp>
                        </SplmtryData>
                    </FIToFICstmrDrctDbt>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pacs.003")
                .name("Direct Debit Transfer")
                .isoCode("pacs.003.001.11")
                .category("Direct Debit Operations")
                .rootElement("Document")
                .mainElement("FIToFICstmrDrctDbt")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pacs.003.001.11")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 11. pain.013: Creditor Payment Activation Request
    // ==========================================
    private static void registerPain013() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "GrpHdr.MsgId", "//GrpHdr/MsgId", "99905820260105102349998878725905163", "String", 35, true, false, "NPS_ID", "Message ID"),
                new IsoFieldDef("Creation DateTime", "GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2025-09-05T12:15:00.000+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp in UTC+1"),
                new IsoFieldDef("Initiating Party Name", "GrpHdr.InitgPty.Nm", "//GrpHdr/InitgPty/Nm", "Test Initiating Party", "String", 100, true, false, null, "Initiator Name"),
                new IsoFieldDef("Client ID", "GrpHdr.InitgPty.Id.OrgId.Othr.Id", "//GrpHdr/InitgPty/Id/OrgId/Othr/Id", "ClientID-123456", "String", 35, true, false, null, "Client Identifier"),
                new IsoFieldDef("Payment Information ID", "PmtInf.PmtInfId", "//PmtInf/PmtInfId", "GSFPMTINF035985837", "String", 35, true, false, null, "Payment Information ID"),
                new IsoFieldDef("Payment Method", "PmtInf.PmtMtd", "//PmtInf/PmtMtd", "TRF", "Enum", 3, true, false, null, "Payment Method (TRF)"),
                new IsoFieldDef("Requested Execution DateTime", "PmtInf.ReqdExctnDt.DtTm", "//PmtInf/ReqdExctnDt/DtTm", "2025-09-05T12:15:00.000+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Requested execution datetime"),
                new IsoFieldDef("Debtor Name", "PmtInf.Dbtr.Nm", "//PmtInf/Dbtr/Nm", "Test Account", "String", 100, true, false, null, "Debtor Name"),
                new IsoFieldDef("Debtor Account IBAN", "PmtInf.DbtrAcct.Id.IBAN", "//PmtInf/DbtrAcct/Id/IBAN", "3293827192", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Debtor Account Currency", "PmtInf.DbtrAcct.Ccy", "//PmtInf/DbtrAcct/Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Debtor Account Name", "PmtInf.DbtrAcct.Nm", "//PmtInf/DbtrAcct/Nm", "Test Account", "String", 100, true, false, null, "Debtor Account Name"),
                new IsoFieldDef("Debtor Agent BICFI", "PmtInf.DbtrAgt.FinInstnId.BICFI", "//PmtInf/DbtrAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Debtor Agent BICFI"),
                new IsoFieldDef("Debtor Agent Member ID", "PmtInf.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//PmtInf/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Debtor Member ID"),
                new IsoFieldDef("End-to-End ID", "PmtInf.CdtTrfTx.PmtId.EndToEndId", "//CdtTrfTx/PmtId/EndToEndId", "GSF035985837-E2E", "String", 35, true, false, "NPS_ID", "EndToEnd ID"),
                new IsoFieldDef("Instructed Amount", "PmtInf.CdtTrfTx.Amt.InstdAmt", "//CdtTrfTx/Amt/InstdAmt", "100.00", "Decimal", 18, true, false, "AMOUNT", "Instructed amount"),
                new IsoFieldDef("Instructed Amount Currency", "PmtInf.CdtTrfTx.Amt.InstdAmt@Ccy", "//CdtTrfTx/Amt/InstdAmt/@Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Creditor Agent BICFI", "PmtInf.CdtTrfTx.CdtrAgt.FinInstnId.BICFI", "//CdtTrfTx/CdtrAgt/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Creditor Agent BICFI"),
                new IsoFieldDef("Creditor Agent Member ID", "PmtInf.CdtTrfTx.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//CdtTrfTx/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Creditor Member ID"),
                new IsoFieldDef("Creditor Name", "PmtInf.CdtTrfTx.Cdtr.Nm", "//CdtTrfTx/Cdtr/Nm", "Client Name", "String", 100, true, false, null, "Creditor Name"),
                new IsoFieldDef("Creditor Account IBAN", "PmtInf.CdtTrfTx.CdtrAcct.Id.IBAN", "//CdtTrfTx/CdtrAcct/Id/IBAN", "1119384738", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Creditor Account Name", "PmtInf.CdtTrfTx.CdtrAcct.Nm", "//CdtTrfTx/CdtrAcct/Nm", "Client Name", "String", 100, true, false, null, "Creditor Account Name"),
                new IsoFieldDef("Payment Purpose", "PmtInf.CdtTrfTx.Purp.Prtry", "//CdtTrfTx/Purp/Prtry", "Invoice Funding", "String", 35, false, false, null, "Payment purpose"),
                new IsoFieldDef("Place and Name", "SplmtryData.PlcAndNm", "//SplmtryData/PlcAndNm", "AdditionalVerificationDetails", "String", 35, true, false, null, "Supplementary descriptor"),
                new IsoFieldDef("Debtor Account Designation", "SplmtryData.Envlp.CustomData.DebtorInfo.AccountDesignation", "//DebtorInfo/AccountDesignation", "1", "Numeric", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Debtor ID Type", "SplmtryData.Envlp.CustomData.DebtorInfo.IdType", "//DebtorInfo/IdType", "BVN", "String", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Debtor ID Value", "SplmtryData.Envlp.CustomData.DebtorInfo.IdValue", "//DebtorInfo/IdValue", "22222222222", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Debtor Account Tier", "SplmtryData.Envlp.CustomData.DebtorInfo.AccountTier", "//DebtorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Creditor Account Designation", "SplmtryData.Envlp.CustomData.CreditorInfo.AccountDesignation", "//CreditorInfo/AccountDesignation", "1", "Numeric", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Creditor ID Type", "SplmtryData.Envlp.CustomData.CreditorInfo.IdType", "//CreditorInfo/IdType", "BVN", "String", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Creditor ID Value", "SplmtryData.Envlp.CustomData.CreditorInfo.IdValue", "//CreditorInfo/IdValue", "22222222222", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Creditor Account Tier", "SplmtryData.Envlp.CustomData.CreditorInfo.AccountTier", "//CreditorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Transaction Location", "SplmtryData.Envlp.CustomData.TransactionInfo.TransactionLocation", "//TransactionInfo/TransactionLocation", "01080652440N020900337921E", "String", 30, true, false, null, "Location Coordinates"),
                new IsoFieldDef("Channel Code", "SplmtryData.Envlp.CustomData.TransactionInfo.ChannelCode", "//TransactionInfo/ChannelCode", "4", "String", 2, true, false, "CHANNEL_CODE", "Channel Code (1-11)"),
                new IsoFieldDef("Mandate Category", "SplmtryData.Envlp.CustomData.TransactionInfo.MandateCategory", "//TransactionInfo/MandateCategory", "0", "String", 4, true, false, null, "Mandate Category")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.013.001.11">
                    <CdtrPmtActvtnReq>
                        <GrpHdr>
                            <MsgId>99905820260105102349998878725905163</MsgId>
                            <CreDtTm>2025-09-05T12:15:00.000+01:00</CreDtTm>
                            <InitgPty>
                                <Nm>Test Initiating Party</Nm>
                                <Id><OrgId><Othr><Id>ClientID-123456</Id></Othr></OrgId></Id>
                            </InitgPty>
                        </GrpHdr>
                        <PmtInf>
                            <PmtInfId>GSFPMTINF035985837</PmtInfId>
                            <PmtMtd>TRF</PmtMtd>
                            <ReqdExctnDt><DtTm>2025-09-05T12:15:00.000+01:00</DtTm></ReqdExctnDt>
                            <Dbtr><Nm>Test Account</Nm></Dbtr>
                            <DbtrAcct>
                                <Id><IBAN>3293827192</IBAN></Id>
                                <Ccy>NGN</Ccy>
                                <Nm>Test Account</Nm>
                            </DbtrAcct>
                            <DbtrAgt>
                                <FinInstnId>
                                    <BICFI>999057</BICFI>
                                    <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </DbtrAgt>
                            <CdtTrfTx>
                                <PmtId><EndToEndId>GSF035985837-E2E</EndToEndId></PmtId>
                                <Amt><InstdAmt Ccy="NGN">100.00</InstdAmt></Amt>
                                <CdtrAgt>
                                    <FinInstnId>
                                        <BICFI>999058</BICFI>
                                        <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                    </FinInstnId>
                                </CdtrAgt>
                                <Cdtr><Nm>Client Name</Nm></Cdtr>
                                <CdtrAcct><Id><IBAN>1119384738</IBAN></Id><Nm>Client Name</Nm></CdtrAcct>
                                <Purp><Prtry>Invoice Funding</Prtry></Purp>
                            </CdtTrfTx>
                        </PmtInf>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <DebtorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22222222222</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </DebtorInfo>
                                    <DebtorMetadata>
                                        <BiometricData></BiometricData>
                                        <AdrLine>Marina</AdrLine>
                                        <PhneNb>08012345678</PhneNb>
                                        <EmailAdr>mt@nibss.com</EmailAdr>
                                    </DebtorMetadata>
                                    <CreditorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22222222222</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </CreditorInfo>
                                    <TransactionInfo>
                                        <TransactionLocation>01080652440N020900337921E</TransactionLocation>
                                        <ChannelCode>4</ChannelCode>
                                        <MandateCategory>0</MandateCategory>
                                    </TransactionInfo>
                                </CustomData>
                            </Envlp>
                        </SplmtryData>
                    </CdtrPmtActvtnReq>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pain.013")
                .name("Payment Activation Request")
                .isoCode("pain.013.001.11")
                .category("Payment Activation")
                .rootElement("Document")
                .mainElement("CdtrPmtActvtnReq")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pain.013.001.11")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 12. pain.014: Payment Activation Status Report
    // ==========================================
    private static void registerPain014() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "GrpHdr.MsgId", "//GrpHdr/MsgId", "99905820260105122349998878725905163", "String", 35, true, false, "NPS_ID", "Status Report Msg ID"),
                new IsoFieldDef("Creation Date Time", "GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-01-13T10:19:03.741+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp"),
                new IsoFieldDef("Initiating Party Name", "GrpHdr.InitgPty.Nm", "//GrpHdr/InitgPty/Nm", "Debtor Bank", "String", 100, true, false, null, "Initiating Party Name"),
                new IsoFieldDef("Creditor Name", "GrpHdr.Cdtr.Nm", "//GrpHdr/Cdtr/Nm", "CreditorCorp", "String", 100, true, false, null, "Creditor Name"),
                new IsoFieldDef("Creditor Account IBAN", "GrpHdr.CdtrAcct.Id.IBAN", "//GrpHdr/CdtrAcct/Id/IBAN", "5555544443", "String", 10, true, false, "NUBAN", "10-digit Creditor NUBAN"),
                new IsoFieldDef("Creditor Account Name", "GrpHdr.CdtrAcct.Nm", "//GrpHdr/CdtrAcct/Nm", "CreditorCorp", "String", 100, true, false, null, "Creditor Account Name"),
                new IsoFieldDef("Debtor Name", "GrpHdr.Dbtr.Nm", "//GrpHdr/Dbtr/Nm", "Debtor Customer", "String", 100, true, false, null, "Debtor Name"),
                new IsoFieldDef("Debtor Account IBAN", "GrpHdr.DbtrAcct.Id.IBAN", "//GrpHdr/DbtrAcct/Id/IBAN", "8888899999", "String", 10, true, false, "NUBAN", "10-digit Debtor NUBAN"),
                new IsoFieldDef("Debtor Account Name", "GrpHdr.DbtrAcct.Nm", "//GrpHdr/DbtrAcct/Nm", "Debtor Customer", "String", 100, true, false, null, "Debtor Account Name"),
                new IsoFieldDef("Forwarding Agent BICFI", "GrpHdr.FwdgAgt.FinInstnId.BICFI", "//GrpHdr/FwdgAgt/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Forwarding Agent BICFI"),
                new IsoFieldDef("Forwarding Agent Member ID", "GrpHdr.FwdgAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/FwdgAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Forwarding Agent Member ID"),
                new IsoFieldDef("Debtor Agent BICFI", "GrpHdr.DbtrAgt.FinInstnId.BICFI", "//GrpHdr/DbtrAgt/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Debtor Agent BICFI"),
                new IsoFieldDef("Debtor Agent Member ID", "GrpHdr.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Debtor Agent Member ID"),
                new IsoFieldDef("Creditor Agent BICFI", "GrpHdr.CdtrAgt.FinInstnId.BICFI", "//GrpHdr/CdtrAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Creditor Agent BICFI"),
                new IsoFieldDef("Creditor Agent Member ID", "GrpHdr.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Creditor Agent Member ID"),
                new IsoFieldDef("Original Message ID", "OrgnlGrpInfAndSts.OrgnlMsgId", "//OrgnlGrpInfAndSts/OrgnlMsgId", "99905820260105102349998878725905163", "String", 35, true, false, "NPS_ID", "Original pain.013 Msg ID"),
                new IsoFieldDef("Original Message Name ID", "OrgnlGrpInfAndSts.OrgnlMsgNmId", "//OrgnlGrpInfAndSts/OrgnlMsgNmId", "pain.013.001.11", "String", 35, true, false, null, "Original Message Name ID"),
                new IsoFieldDef("Original Creation Date Time", "OrgnlGrpInfAndSts.OrgnlCreDtTm", "//OrgnlGrpInfAndSts/OrgnlCreDtTm", "2026-01-05T10:27:26.737+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Original Creation DateTime"),
                new IsoFieldDef("Group Status", "OrgnlGrpInfAndSts.GrpSts", "//OrgnlGrpInfAndSts/GrpSts", "ACCP", "Code", 4, true, false, "GROUP_STATUS", "Status code (ACCP)"),
                new IsoFieldDef("Original Payment Info ID", "OrgnlPmtInfAndSts.OrgnlPmtInfId", "//OrgnlPmtInfAndSts/OrgnlPmtInfId", "GSFPMTINF035985837", "String", 35, true, false, null, "Original PmtInfId"),
                new IsoFieldDef("Original End-to-End ID", "OrgnlPmtInfAndSts.TxInfAndSts.OrgnlEndToEndId", "//OrgnlPmtInfAndSts/TxInfAndSts/OrgnlEndToEndId", "GSF035985837-E2E", "String", 35, true, false, null, "Original EndToEnd ID"),
                new IsoFieldDef("Transaction Status", "OrgnlPmtInfAndSts.TxInfAndSts.TxSts", "//TxInfAndSts/TxSts", "ACCP", "Code", 4, true, false, "GROUP_STATUS", "Transaction Status")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.014.001.11">
                    <CdtrPmtActvtnReqStsRpt>
                        <GrpHdr>
                            <MsgId>99905820260105122349998878725905163</MsgId>
                            <CreDtTm>2026-01-13T10:19:03.741+01:00</CreDtTm>
                            <InitgPty><Nm>Debtor Bank</Nm></InitgPty>
                            <Cdtr><Nm>CreditorCorp</Nm></Cdtr>
                            <CdtrAcct><Id><IBAN>5555544443</IBAN></Id><Nm>CreditorCorp</Nm></CdtrAcct>
                            <Dbtr><Nm>Debtor Customer</Nm></Dbtr>
                            <DbtrAcct><Id><IBAN>8888899999</IBAN></Id><Nm>Debtor Customer</Nm></DbtrAcct>
                            <FwdgAgt>
                                <FinInstnId>
                                    <BICFI>999058</BICFI>
                                    <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </FwdgAgt>
                            <DbtrAgt>
                                <FinInstnId>
                                    <BICFI>999058</BICFI>
                                    <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </DbtrAgt>
                            <CdtrAgt>
                                <FinInstnId>
                                    <BICFI>999057</BICFI>
                                    <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </CdtrAgt>
                        </GrpHdr>
                        <OrgnlGrpInfAndSts>
                            <OrgnlMsgId>99905820260105102349998878725905163</OrgnlMsgId>
                            <OrgnlMsgNmId>pain.013.001.11</OrgnlMsgNmId>
                            <OrgnlCreDtTm>2026-01-05T10:27:26.737+01:00</OrgnlCreDtTm>
                            <GrpSts>ACCP</GrpSts>
                        </OrgnlGrpInfAndSts>
                        <OrgnlPmtInfAndSts>
                            <OrgnlPmtInfId>GSFPMTINF035985837</OrgnlPmtInfId>
                            <TxInfAndSts>
                                <OrgnlEndToEndId>GSF035985837-E2E</OrgnlEndToEndId>
                                <TxSts>ACCP</TxSts>
                            </TxInfAndSts>
                        </OrgnlPmtInfAndSts>
                    </CdtrPmtActvtnReqStsRpt>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pain.014")
                .name("Payment Activation Status Report")
                .isoCode("pain.014.001.11")
                .category("Payment Activation")
                .rootElement("Document")
                .mainElement("CdtrPmtActvtnReqStsRpt")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pain.014.001.11")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 13. camt.060: Account Reporting Request
    // ==========================================
    private static void registerCamt060() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "AcctRptgReq.GrpHdr.MsgId", "//GrpHdr/MsgId", "99905720260302123735603795909182287", "String", 35, true, false, "NPS_ID", "Message ID"),
                new IsoFieldDef("Creation DateTime", "AcctRptgReq.GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-03-02T12:37:35.352+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp in UTC+1"),
                new IsoFieldDef("Sender Agent BICFI", "AcctRptgReq.GrpHdr.MsgSndr.Agt.FinInstnId.BICFI", "//MsgSndr/Agt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Sender BICFI"),
                new IsoFieldDef("Sender Agent Member ID", "AcctRptgReq.GrpHdr.MsgSndr.Agt.FinInstnId.ClrSysMmbId.MmbId", "//MsgSndr/Agt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Sender Member ID"),
                new IsoFieldDef("Reporting Request ID", "AcctRptgReq.RptgReq.Id", "//RptgReq/Id", "99905720260302123735603795909182287", "String", 35, true, false, "NPS_ID", "Reporting Request ID"),
                new IsoFieldDef("Requested Message Name ID", "AcctRptgReq.RptgReq.ReqdMsgNmId", "//RptgReq/ReqdMsgNmId", "camt.052.001.08", "String", 35, true, false, null, "Requested Message Type"),
                new IsoFieldDef("Account Number (IBAN)", "AcctRptgReq.RptgReq.Acct.Id.IBAN", "//RptgReq/Acct/Id/IBAN", "4488447166", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Account Currency", "AcctRptgReq.RptgReq.Acct.Ccy", "//RptgReq/Acct/Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Account Owner BICFI", "AcctRptgReq.RptgReq.AcctOwnr.Agt.FinInstnId.BICFI", "//AcctOwnr/Agt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Account Owner BICFI"),
                new IsoFieldDef("Account Owner Member ID", "AcctRptgReq.RptgReq.AcctOwnr.Agt.FinInstnId.ClrSysMmbId.MmbId", "//AcctOwnr/Agt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Account Owner Member ID"),
                new IsoFieldDef("Account Servicer BICFI", "AcctRptgReq.RptgReq.AcctSvcr.FinInstnId.BICFI", "//AcctSvcr/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Account Servicer BICFI"),
                new IsoFieldDef("Account Servicer Member ID", "AcctRptgReq.RptgReq.AcctSvcr.FinInstnId.ClrSysMmbId.MmbId", "//AcctSvcr/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Account Servicer Member ID"),
                new IsoFieldDef("Reporting Period From Date", "AcctRptgReq.RptgReq.RptgPrd.FrToDt.FrDt", "//RptgPrd/FrToDt/FrDt", "2026-02-24", "Date", 10, true, false, "DATE", "From Date"),
                new IsoFieldDef("Reporting Period To Date", "AcctRptgReq.RptgReq.RptgPrd.FrToDt.ToDt", "//RptgPrd/FrToDt/ToDt", "2026-03-02", "Date", 10, true, false, "DATE", "To Date"),
                new IsoFieldDef("Reporting Period Type", "AcctRptgReq.RptgReq.RptgPrd.Tp", "//RptgPrd/Tp", "ALLL", "String", 4, true, false, null, "Reporting Period Type"),
                new IsoFieldDef("Supplementary Place & Name", "AcctRptgReq.SplmtryData.PlcAndNm", "//SplmtryData/PlcAndNm", "AdditionalVerificationDetails", "String", 35, true, false, null, "Supplementary descriptor"),
                new IsoFieldDef("Creditor Account Designation", "AcctRptgReq.SplmtryData.Envlp.CustomData.CreditorInfo.AccountDesignation", "//CreditorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Creditor ID Type", "AcctRptgReq.SplmtryData.Envlp.CustomData.CreditorInfo.IdType", "//CreditorInfo/IdType", "BVN", "Enum", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Creditor ID Value", "AcctRptgReq.SplmtryData.Envlp.CustomData.CreditorInfo.IdValue", "//CreditorInfo/IdValue", "22112323460", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Creditor Account Tier", "AcctRptgReq.SplmtryData.Envlp.CustomData.CreditorInfo.AccountTier", "//CreditorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Transaction Location", "AcctRptgReq.SplmtryData.Envlp.CustomData.TransactionInfo.TransactionLocation", "//TransactionInfo/TransactionLocation", "01080652440N020900337921E", "String", 30, true, false, null, "Location Coordinates"),
                new IsoFieldDef("Channel Code", "AcctRptgReq.SplmtryData.Envlp.CustomData.TransactionInfo.ChannelCode", "//TransactionInfo/ChannelCode", "1", "Integer", 2, true, false, "CHANNEL_CODE", "Channel Code (1-11)"),
                new IsoFieldDef("Fixed Collection Amount", "AcctRptgReq.SplmtryData.Envlp.CustomData.TransactionInfo.FixedCollectionAmount", "//TransactionInfo/FixedCollectionAmount", "false", "Boolean", 5, true, false, null, "Fixed Collection Amount Indicator"),
                new IsoFieldDef("Mandate Code", "AcctRptgReq.SplmtryData.Envlp.CustomData.TransactionInfo.MandateCode", "//TransactionInfo/MandateCode", "MNDT-RCUR-13482", "String", 35, false, false, null, "Mandate Code")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.060.001.07">
                    <AcctRptgReq>
                        <GrpHdr>
                            <MsgId>99905720260302123735603795909182287</MsgId>
                            <CreDtTm>2026-03-02T12:37:35.352+01:00</CreDtTm>
                            <MsgSndr>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999057</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999057</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </MsgSndr>
                        </GrpHdr>
                        <RptgReq>
                            <Id>99905720260302123735603795909182287</Id>
                            <ReqdMsgNmId>camt.052.001.08</ReqdMsgNmId>
                            <Acct>
                                <Id>
                                    <IBAN>4488447166</IBAN>
                                </Id>
                                <Ccy>NGN</Ccy>
                            </Acct>
                            <AcctOwnr>
                                <Agt>
                                    <FinInstnId>
                                        <BICFI>999057</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999057</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Agt>
                            </AcctOwnr>
                            <AcctSvcr>
                                <FinInstnId>
                                    <BICFI>999058</BICFI>
                                    <ClrSysMmbId>
                                        <MmbId>999058</MmbId>
                                    </ClrSysMmbId>
                                </FinInstnId>
                            </AcctSvcr>
                            <RptgPrd>
                                <FrToDt>
                                    <FrDt>2026-02-24</FrDt>
                                    <ToDt>2026-03-02</ToDt>
                                </FrToDt>
                                <Tp>ALLL</Tp>
                            </RptgPrd>
                        </RptgReq>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <CreditorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22112323460</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </CreditorInfo>
                                    <TransactionInfo>
                                        <TransactionLocation>01080652440N020900337921E</TransactionLocation>
                                        <ChannelCode>1</ChannelCode>
                                        <FixedCollectionAmount>false</FixedCollectionAmount>
                                        <MandateCode>MNDT-RCUR-13482</MandateCode>
                                    </TransactionInfo>
                                </CustomData>
                            </Envlp>
                        </SplmtryData>
                    </AcctRptgReq>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("camt.060")
                .name("Account Reporting Request")
                .isoCode("camt.060.001.07")
                .category("Account Services & Statements")
                .rootElement("Document")
                .mainElement("AcctRptgReq")
                .namespace("urn:iso:std:iso:20022:tech:xsd:camt.060.001.07")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 14. camt.052: Bank To Customer Account Report
    // ==========================================
    private static void registerCamt052() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "BkToCstmrAcctRpt.GrpHdr.MsgId", "//GrpHdr/MsgId", "99905820260302123914844967272332044", "String", 35, true, false, "NPS_ID", "Message ID"),
                new IsoFieldDef("Creation DateTime", "BkToCstmrAcctRpt.GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-03-02T12:39:14.796+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp"),
                new IsoFieldDef("Message Recipient Name", "BkToCstmrAcctRpt.GrpHdr.MsgRcpt.Nm", "//GrpHdr/MsgRcpt/Nm", "Debtor Bank Name", "String", 100, false, false, null, "Message Recipient Name"),
                new IsoFieldDef("Message Recipient BIC", "BkToCstmrAcctRpt.GrpHdr.MsgRcpt.Id.OrgId.AnyBIC", "//GrpHdr/MsgRcpt/Id/OrgId/AnyBIC", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Recipient BIC"),
                new IsoFieldDef("Original Query Message ID", "BkToCstmrAcctRpt.GrpHdr.OrgnlBizQry.MsgId", "//OrgnlBizQry/MsgId", "99905820260302123735603795909182287", "String", 35, true, false, "NPS_ID", "Original query Msg ID"),
                new IsoFieldDef("Original Query Message Name ID", "BkToCstmrAcctRpt.GrpHdr.OrgnlBizQry.MsgNmId", "//OrgnlBizQry/MsgNmId", "camt.060.001.07", "String", 35, true, false, null, "Original Message Type"),
                new IsoFieldDef("Report ID", "BkToCstmrAcctRpt.Rpt.Id", "//Rpt/Id", "99905899905720260302123735604994726", "String", 35, true, false, "NPS_ID", "Report ID"),
                new IsoFieldDef("From DateTime", "BkToCstmrAcctRpt.Rpt.FrToDt.FrDtTm", "//Rpt/FrToDt/FrDtTm", "2026-02-24T09:03:07.000+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Report From DateTime"),
                new IsoFieldDef("To DateTime", "BkToCstmrAcctRpt.Rpt.FrToDt.ToDtTm", "//Rpt/FrToDt/ToDtTm", "2026-03-02T12:39:14.000+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Report To DateTime"),
                new IsoFieldDef("Account Number (IBAN)", "BkToCstmrAcctRpt.Rpt.Acct.Id.IBAN", "//Rpt/Acct/Id/IBAN", "4488447166", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Account Currency", "BkToCstmrAcctRpt.Rpt.Acct.Ccy", "//Rpt/Acct/Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Account Servicer BICFI", "BkToCstmrAcctRpt.Rpt.Acct.Svcr.FinInstnId.BICFI", "//Rpt/Acct/Svcr/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Servicer BICFI"),
                new IsoFieldDef("Account Servicer Member ID", "BkToCstmrAcctRpt.Rpt.Acct.Svcr.FinInstnId.ClrSysMmbId.MmbId", "//Rpt/Acct/Svcr/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Servicer Member ID"),
                new IsoFieldDef("Balance Type", "BkToCstmrAcctRpt.Rpt.Bal.Tp.CdOrPrtry.Prtry", "//Rpt/Bal/Tp/CdOrPrtry/Prtry", "CLRG", "String", 35, true, false, null, "Balance Type"),
                new IsoFieldDef("Balance Amount", "BkToCstmrAcctRpt.Rpt.Bal.Amt", "//Rpt/Bal/Amt", "500000.00", "Decimal", 18, true, false, "AMOUNT", "Balance amount"),
                new IsoFieldDef("Balance Currency", "BkToCstmrAcctRpt.Rpt.Bal.Amt@Ccy", "//Rpt/Bal/Amt/@Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Credit/Debit Indicator", "BkToCstmrAcctRpt.Rpt.Bal.CdtDbtInd", "//Rpt/Bal/CdtDbtInd", "CRDT", "Enum", 4, true, false, "CREDIT_DEBIT", "Credit/Debit (CRDT/DBIT)"),
                new IsoFieldDef("Balance DateTime", "BkToCstmrAcctRpt.Rpt.Bal.Dt.DtTm", "//Rpt/Bal/Dt/DtTm", "2026-02-04T10:21:33.000+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Balance DateTime"),
                new IsoFieldDef("Entry Amount", "BkToCstmrAcctRpt.Rpt.Ntry.Amt", "//Rpt/Ntry/Amt", "30000.00", "Decimal", 18, true, false, "AMOUNT", "Entry amount"),
                new IsoFieldDef("Entry Currency", "BkToCstmrAcctRpt.Rpt.Ntry.Amt@Ccy", "//Rpt/Ntry/Amt/@Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Entry currency code"),
                new IsoFieldDef("Entry Credit/Debit Indicator", "BkToCstmrAcctRpt.Rpt.Ntry.CdtDbtInd", "//Rpt/Ntry/CdtDbtInd", "CRDT", "Enum", 4, true, false, "CREDIT_DEBIT", "Entry Credit/Debit"),
                new IsoFieldDef("Entry Status", "BkToCstmrAcctRpt.Rpt.Ntry.Sts.Prtry", "//Rpt/Ntry/Sts/Prtry", "BOOK", "String", 35, true, false, null, "Entry status"),
                new IsoFieldDef("Booking Date", "BkToCstmrAcctRpt.Rpt.Ntry.BookgDt.Dt", "//Rpt/Ntry/BookgDt/Dt", "2026-03-02Z", "Date", 10, true, false, "DATE", "Booking date"),
                new IsoFieldDef("Value Date", "BkToCstmrAcctRpt.Rpt.Ntry.ValDt.Dt", "//Rpt/Ntry/ValDt/Dt", "2026-03-02Z", "Date", 10, true, false, "DATE", "Value date"),
                new IsoFieldDef("Account Servicer Reference", "BkToCstmrAcctRpt.Rpt.Ntry.AcctSvcrRef", "//Rpt/Ntry/AcctSvcrRef", "99905820260302123914844967272332044", "String", 35, false, false, "NPS_ID", "Account Servicer Reference"),
                new IsoFieldDef("Instructed Agent BICFI", "BkToCstmrAcctRpt.Rpt.Ntry.NtryDtls.TxDtls.RltdAgts.InstdAgt.FinInstnId.BICFI", "//NtryDtls/TxDtls/RltdAgts/InstdAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Instructed Agent BICFI")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.052.001.12">
                    <BkToCstmrAcctRpt>
                        <GrpHdr>
                            <MsgId>99905820260302123914844967272332044</MsgId>
                            <CreDtTm>2026-03-02T12:39:14.796+01:00</CreDtTm>
                            <MsgRcpt>
                                <Nm>Debtor Bank Name</Nm>
                                <Id>
                                    <OrgId>
                                        <AnyBIC>999057</AnyBIC>
                                    </OrgId>
                                </Id>
                            </MsgRcpt>
                            <OrgnlBizQry>
                                <MsgId>99905820260302123735603795909182287</MsgId>
                                <MsgNmId>camt.060.001.07</MsgNmId>
                                <CreDtTm>2026-03-02T11:37:35.242Z</CreDtTm>
                            </OrgnlBizQry>
                        </GrpHdr>
                        <Rpt>
                            <Id>99905899905720260302123735604994726</Id>
                            <FrToDt>
                                <FrDtTm>2026-02-24T09:03:07.000+01:00</FrDtTm>
                                <ToDtTm>2026-03-02T12:39:14.000+01:00</ToDtTm>
                            </FrToDt>
                            <Acct>
                                <Id>
                                    <IBAN>4488447166</IBAN>
                                </Id>
                                <Ccy>NGN</Ccy>
                                <Ownr>
                                    <Id>
                                        <OrgId>
                                            <Othr>
                                                <SchmeNm>
                                                    <Cd>999057</Cd>
                                                    <Prtry>999057</Prtry>
                                                </SchmeNm>
                                            </Othr>
                                        </OrgId>
                                    </Id>
                                </Ownr>
                                <Svcr>
                                    <FinInstnId>
                                        <BICFI>999058</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999058</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Svcr>
                            </Acct>
                            <Bal>
                                <Tp>
                                    <CdOrPrtry>
                                        <Prtry>CLRG</Prtry>
                                    </CdOrPrtry>
                                </Tp>
                                <Amt Ccy="NGN">500000.00</Amt>
                                <CdtDbtInd>CRDT</CdtDbtInd>
                                <Dt>
                                    <DtTm>2026-02-04T10:21:33.000+01:00</DtTm>
                                </Dt>
                            </Bal>
                            <Ntry>
                                <Amt Ccy="NGN">30000.00</Amt>
                                <CdtDbtInd>CRDT</CdtDbtInd>
                                <Sts>
                                    <Prtry>BOOK</Prtry>
                                </Sts>
                                <BookgDt>
                                    <Dt>2026-03-02Z</Dt>
                                </BookgDt>
                                <ValDt>
                                    <Dt>2026-03-02Z</Dt>
                                </ValDt>
                                <AcctSvcrRef>99905820260302123914844967272332044</AcctSvcrRef>
                                <BkTxCd>
                                    <Domn>
                                        <Cd>PMNT</Cd>
                                        <Fmly>
                                            <Cd>RCDT</Cd>
                                            <SubFmlyCd>ESCT</SubFmlyCd>
                                        </Fmly>
                                    </Domn>
                                </BkTxCd>
                                <NtryDtls>
                                    <TxDtls>
                                        <RltdAgts>
                                            <InstdAgt>
                                                <FinInstnId>
                                                    <BICFI>999057</BICFI>
                                                </FinInstnId>
                                            </InstdAgt>
                                        </RltdAgts>
                                    </TxDtls>
                                </NtryDtls>
                            </Ntry>
                        </Rpt>
                    </BkToCstmrAcctRpt>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("camt.052")
                .name("Bank To Customer Account Report")
                .isoCode("camt.052.001.12")
                .category("Account Services & Statements")
                .rootElement("Document")
                .mainElement("BkToCstmrAcctRpt")
                .namespace("urn:iso:std:iso:20022:tech:xsd:camt.052.001.12")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 15. camt.053: Bank To Customer Statement
    // ==========================================
    private static void registerCamt053() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "BkToCstmrStmt.GrpHdr.MsgId", "//GrpHdr/MsgId", "99905820260223092508470151175400802", "String", 35, true, false, "NPS_ID", "Message ID"),
                new IsoFieldDef("Creation DateTime", "BkToCstmrStmt.GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-03-02T11:40:55.308+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp"),
                new IsoFieldDef("Message Recipient Name", "BkToCstmrStmt.GrpHdr.MsgRcpt.Nm", "//GrpHdr/MsgRcpt/Nm", "Debtor Bank Name", "String", 100, false, false, null, "Message Recipient Name"),
                new IsoFieldDef("Message Recipient BIC", "BkToCstmrStmt.GrpHdr.MsgRcpt.Id.OrgId.AnyBIC", "//GrpHdr/MsgRcpt/Id/OrgId/AnyBIC", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Recipient BIC"),
                new IsoFieldDef("Original Query Message ID", "BkToCstmrStmt.GrpHdr.OrgnlBizQry.MsgId", "//OrgnlBizQry/MsgId", "99905820260302123735603795909182287", "String", 35, true, false, "NPS_ID", "Original query Msg ID"),
                new IsoFieldDef("Original Query Message Name ID", "BkToCstmrStmt.GrpHdr.OrgnlBizQry.MsgNmId", "//OrgnlBizQry/MsgNmId", "camt.060.001.07", "String", 35, true, false, null, "Original Message Type"),
                new IsoFieldDef("Statement ID", "BkToCstmrStmt.Stmt.Id", "//Stmt/Id", "99905899905720260302124055216285423", "String", 35, true, false, "NPS_ID", "Statement ID"),
                new IsoFieldDef("From DateTime", "BkToCstmrStmt.Stmt.FrToDt.FrDtTm", "//Stmt/FrToDt/FrDtTm", "2026-02-01T00:00:00.000+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Statement From DateTime"),
                new IsoFieldDef("To DateTime", "BkToCstmrStmt.Stmt.FrToDt.ToDtTm", "//Stmt/FrToDt/ToDtTm", "2026-02-27T05:59:59.000+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Statement To DateTime"),
                new IsoFieldDef("Account Number (IBAN)", "BkToCstmrStmt.Stmt.Acct.Id.IBAN", "//Stmt/Acct/Id/IBAN", "8887788778", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Account Currency", "BkToCstmrStmt.Stmt.Acct.Ccy", "//Stmt/Acct/Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Account Servicer BICFI", "BkToCstmrStmt.Stmt.Acct.Svcr.FinInstnId.BICFI", "//Stmt/Acct/Svcr/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Servicer BICFI"),
                new IsoFieldDef("Account Servicer Member ID", "BkToCstmrStmt.Stmt.Acct.Svcr.FinInstnId.ClrSysMmbId.MmbId", "//Stmt/Acct/Svcr/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Servicer Member ID"),
                new IsoFieldDef("Balance Type", "BkToCstmrStmt.Stmt.Bal.Tp.CdOrPrtry.Prtry", "//Stmt/Bal/Tp/CdOrPrtry/Prtry", "CLRG", "String", 35, true, false, null, "Balance Type"),
                new IsoFieldDef("Opening Balance Amount", "BkToCstmrStmt.Stmt.Bal.Amt", "//Stmt/Bal/Amt", "482000.00", "Decimal", 18, true, false, "AMOUNT", "Balance amount"),
                new IsoFieldDef("Balance Currency", "BkToCstmrStmt.Stmt.Bal.Amt@Ccy", "//Stmt/Bal/Amt/@Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Balance currency code"),
                new IsoFieldDef("Balance Credit/Debit Indicator", "BkToCstmrStmt.Stmt.Bal.CdtDbtInd", "//Stmt/Bal/CdtDbtInd", "CRDT", "Enum", 4, true, false, "CREDIT_DEBIT", "Credit/Debit (CRDT/DBIT)"),
                new IsoFieldDef("Entry Amount", "BkToCstmrStmt.Stmt.Ntry.Amt", "//Stmt/Ntry/Amt", "30000.00", "Decimal", 18, true, false, "AMOUNT", "Entry amount"),
                new IsoFieldDef("Entry Currency", "BkToCstmrStmt.Stmt.Ntry.Amt@Ccy", "//Stmt/Ntry/Amt/@Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Entry currency code"),
                new IsoFieldDef("Entry Credit/Debit Indicator", "BkToCstmrStmt.Stmt.Ntry.CdtDbtInd", "//Stmt/Ntry/CdtDbtInd", "CRDT", "Enum", 4, true, false, "CREDIT_DEBIT", "Entry Credit/Debit"),
                new IsoFieldDef("Entry Status Code", "BkToCstmrStmt.Stmt.Ntry.Sts.Cd", "//Stmt/Ntry/Sts/Cd", "BOOK", "String", 35, false, false, null, "Entry status code"),
                new IsoFieldDef("Booking Date", "BkToCstmrStmt.Stmt.Ntry.BookgDt.Dt", "//Stmt/Ntry/BookgDt/Dt", "2026-02-19Z", "Date", 10, true, false, "DATE", "Booking date"),
                new IsoFieldDef("Value Date", "BkToCstmrStmt.Stmt.Ntry.ValDt.Dt", "//Stmt/Ntry/ValDt/Dt", "2026-02-19Z", "Date", 10, true, false, "DATE", "Value date"),
                new IsoFieldDef("Account Servicer Reference", "BkToCstmrStmt.Stmt.Ntry.AcctSvcrRef", "//Stmt/Ntry/AcctSvcrRef", "99905820260223092508470151175400802", "String", 35, false, false, "NPS_ID", "Account Servicer Reference"),
                new IsoFieldDef("Instructed Agent BICFI", "BkToCstmrStmt.Stmt.Ntry.NtryDtls.TxDtls.RltdAgts.InstdAgt.FinInstnId.BICFI", "//NtryDtls/TxDtls/RltdAgts/InstdAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Instructed Agent BICFI")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:camt.053.001.12">
                    <BkToCstmrStmt>
                        <GrpHdr>
                            <MsgId>99905820260223092508470151175400802</MsgId>
                            <CreDtTm>2026-03-02T11:40:55.308+01:00</CreDtTm>
                            <MsgRcpt>
                                <Nm>Debtor Bank Name</Nm>
                                <Id>
                                    <OrgId>
                                        <AnyBIC>999057</AnyBIC>
                                    </OrgId>
                                </Id>
                            </MsgRcpt>
                            <OrgnlBizQry>
                                <MsgId>99905820260302123735603795909182287</MsgId>
                                <MsgNmId>camt.060.001.07</MsgNmId>
                                <CreDtTm>2026-02-13T13:32:00.000Z</CreDtTm>
                            </OrgnlBizQry>
                        </GrpHdr>
                        <Stmt>
                            <Id>99905899905720260302124055216285423</Id>
                            <FrToDt>
                                <FrDtTm>2026-02-01T00:00:00.000+01:00</FrDtTm>
                                <ToDtTm>2026-02-27T05:59:59.000+01:00</ToDtTm>
                            </FrToDt>
                            <Acct>
                                <Id>
                                    <IBAN>8887788778</IBAN>
                                </Id>
                                <Ccy>NGN</Ccy>
                                <Ownr>
                                    <Id>
                                        <OrgId>
                                            <Othr>
                                                <SchmeNm>
                                                    <Cd>999057</Cd>
                                                    <Prtry>999057</Prtry>
                                                </SchmeNm>
                                            </Othr>
                                        </OrgId>
                                    </Id>
                                </Ownr>
                                <Svcr>
                                    <FinInstnId>
                                        <BICFI>999058</BICFI>
                                        <ClrSysMmbId>
                                            <MmbId>999058</MmbId>
                                        </ClrSysMmbId>
                                    </FinInstnId>
                                </Svcr>
                            </Acct>
                            <Bal>
                                <Tp>
                                    <CdOrPrtry>
                                        <Cd>OPBD</Cd>
                                        <Prtry>CLRG</Prtry>
                                    </CdOrPrtry>
                                </Tp>
                                <Amt Ccy="NGN">482000.00</Amt>
                                <CdtDbtInd>CRDT</CdtDbtInd>
                                <Dt>
                                    <DtTm>2026-02-01T00:00:00.000+01:00</DtTm>
                                </Dt>
                            </Bal>
                            <Bal>
                                <Tp>
                                    <CdOrPrtry>
                                        <Cd>CLBD</Cd>
                                        <Prtry>CLRG</Prtry>
                                    </CdOrPrtry>
                                </Tp>
                                <Amt Ccy="NGN">500000.00</Amt>
                                <CdtDbtInd>CRDT</CdtDbtInd>
                                <Dt>
                                    <Dt>2026-02-27Z</Dt>
                                    <DtTm>2026-02-27T05:59:59.000+01:00</DtTm>
                                </Dt>
                            </Bal>
                            <Ntry>
                                <Amt Ccy="NGN">30000.00</Amt>
                                <CdtDbtInd>CRDT</CdtDbtInd>
                                <Sts>
                                    <Cd>BOOK</Cd>
                                    <Prtry>BOOK</Prtry>
                                </Sts>
                                <BookgDt>
                                    <Dt>2026-02-19Z</Dt>
                                </BookgDt>
                                <ValDt>
                                    <Dt>2026-02-19Z</Dt>
                                </ValDt>
                                <AcctSvcrRef>99905820260223092508470151175400802</AcctSvcrRef>
                                <BkTxCd>
                                    <Domn>
                                        <Cd>PMNT</Cd>
                                        <Fmly>
                                            <Cd>RCDT</Cd>
                                            <SubFmlyCd>ESCT</SubFmlyCd>
                                        </Fmly>
                                    </Domn>
                                </BkTxCd>
                                <NtryDtls>
                                    <TxDtls>
                                        <RltdAgts>
                                            <InstdAgt>
                                                <FinInstnId>
                                                    <BICFI>999057</BICFI>
                                                </FinInstnId>
                                            </InstdAgt>
                                        </RltdAgts>
                                    </TxDtls>
                                </NtryDtls>
                            </Ntry>
                        </Stmt>
                    </BkToCstmrStmt>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("camt.053")
                .name("Bank To Customer Statement")
                .isoCode("camt.053.001.12")
                .category("Account Services & Statements")
                .rootElement("Document")
                .mainElement("BkToCstmrStmt")
                .namespace("urn:iso:std:iso:20022:tech:xsd:camt.053.001.12")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 16. pain.001: Customer Credit Transfer Initiation
    // ==========================================
    private static void registerPain001() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "CstmrCdtTrfInitn.GrpHdr.MsgId", "//GrpHdr/MsgId", "99905720260225144722037299534778744", "String", 35, true, false, "NPS_ID", "Message ID"),
                new IsoFieldDef("Creation DateTime", "CstmrCdtTrfInitn.GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-02-25T14:47:22.637+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp"),
                new IsoFieldDef("Number of Transactions (Group)", "CstmrCdtTrfInitn.GrpHdr.NbOfTxs", "//GrpHdr/NbOfTxs", "1", "Numeric", 15, true, false, null, "Number of transactions"),
                new IsoFieldDef("Control Sum (Group)", "CstmrCdtTrfInitn.GrpHdr.CtrlSum", "//GrpHdr/CtrlSum", "120.51", "Decimal", 18, true, false, "AMOUNT", "Control Sum"),
                new IsoFieldDef("Initiating Party Name", "CstmrCdtTrfInitn.GrpHdr.InitgPty.Nm", "//GrpHdr/InitgPty/Nm", "Musa", "String", 100, true, false, null, "Initiator Name"),
                new IsoFieldDef("Initiating Party Scheme Code", "CstmrCdtTrfInitn.GrpHdr.InitgPty.Id.OrgId.Othr.SchmeNm.Cd", "//GrpHdr/InitgPty/Id/OrgId/Othr/SchmeNm/Cd", "999057", "String", 6, true, false, "MEMBER_ID", "Scheme Code"),
                new IsoFieldDef("Forwarding Agent BICFI", "CstmrCdtTrfInitn.GrpHdr.FwdgAgt.FinInstnId.BICFI", "//GrpHdr/FwdgAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Forwarding Agent BIC"),
                new IsoFieldDef("Payment Information ID", "CstmrCdtTrfInitn.PmtInf.PmtInfId", "//PmtInf/PmtInfId", "PMT-20251016-001-SINGLE", "String", 35, true, false, null, "PmtInf ID"),
                new IsoFieldDef("Payment Method", "CstmrCdtTrfInitn.PmtInf.PmtMtd", "//PmtInf/PmtMtd", "TRF", "String", 3, true, false, null, "Payment Method (TRF)"),
                new IsoFieldDef("Batch Booking", "CstmrCdtTrfInitn.PmtInf.BtchBookg", "//PmtInf/BtchBookg", "false", "Boolean", 5, true, false, null, "Batch booking flag"),
                new IsoFieldDef("Payment Number of Transactions", "CstmrCdtTrfInitn.PmtInf.NbOfTxs", "//PmtInf/NbOfTxs", "1", "Numeric", 15, true, false, null, "Transactions in payment"),
                new IsoFieldDef("Payment Control Sum", "CstmrCdtTrfInitn.PmtInf.CtrlSum", "//PmtInf/CtrlSum", "120.51", "Decimal", 18, true, false, "AMOUNT", "Payment Control Sum"),
                new IsoFieldDef("Required Execution Date", "CstmrCdtTrfInitn.PmtInf.ReqdExctnDt.Dt", "//PmtInf/ReqdExctnDt/Dt", "2026-02-17Z", "Date", 10, true, false, "DATE", "Execution Date"),
                new IsoFieldDef("Debtor Name", "CstmrCdtTrfInitn.PmtInf.Dbtr.Nm", "//PmtInf/Dbtr/Nm", "Musa", "String", 100, true, false, null, "Debtor Name"),
                new IsoFieldDef("Debtor Account Number (IBAN)", "CstmrCdtTrfInitn.PmtInf.DbtrAcct.Id.IBAN", "//PmtInf/DbtrAcct/Id/IBAN", "0177136558", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Debtor Account Name", "CstmrCdtTrfInitn.PmtInf.DbtrAcct.Nm", "//PmtInf/DbtrAcct/Nm", "Musa", "String", 100, true, false, null, "Debtor Account Name"),
                new IsoFieldDef("Debtor Agent BICFI", "CstmrCdtTrfInitn.PmtInf.DbtrAgt.FinInstnId.BICFI", "//PmtInf/DbtrAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, "INSTITUTION_CODE", "Debtor Agent BICFI"),
                new IsoFieldDef("Debtor Agent Member ID", "CstmrCdtTrfInitn.PmtInf.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//PmtInf/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Debtor Member ID"),
                new IsoFieldDef("Charge Bearer", "CstmrCdtTrfInitn.PmtInf.ChrgBr", "//PmtInf/ChrgBr", "SLEV", "String", 4, true, false, null, "Charge Bearer (SLEV)"),
                new IsoFieldDef("End-to-End ID", "CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.PmtId.EndToEndId", "//CdtTrfTxInf/PmtId/EndToEndId", "99905746102951471838787625821100514", "String", 35, true, false, "NPS_ID", "EndToEnd ID"),
                new IsoFieldDef("Instructed Amount", "CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.Amt.InstdAmt", "//CdtTrfTxInf/Amt/InstdAmt", "120.51", "Decimal", 18, true, false, "AMOUNT", "Instructed amount"),
                new IsoFieldDef("Instructed Amount Currency", "CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.Amt.InstdAmt.@Ccy", "//CdtTrfTxInf/Amt/InstdAmt/@Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Creditor Agent BICFI", "CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.CdtrAgt.FinInstnId.BICFI", "//CdtTrfTxInf/CdtrAgt/FinInstnId/BICFI", "999058", "String", 11, false, false, "INSTITUTION_CODE", "Creditor Agent BICFI"),
                new IsoFieldDef("Creditor Agent Member ID", "CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//CdtTrfTxInf/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Creditor Member ID"),
                new IsoFieldDef("Creditor Name", "CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.Cdtr.Nm", "//CdtTrfTxInf/Cdtr/Nm", "James", "String", 100, true, false, null, "Creditor Name"),
                new IsoFieldDef("Creditor Account Number (IBAN)", "CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.CdtrAcct.Id.IBAN", "//CdtTrfTxInf/CdtrAcct/Id/IBAN", "3157417712", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Creditor Account Name", "CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.CdtrAcct.Nm", "//CdtTrfTxInf/CdtrAcct/Nm", "James", "String", 100, true, false, null, "Creditor Account Name"),
                new IsoFieldDef("Remittance Information", "CstmrCdtTrfInitn.PmtInf.CdtTrfTxInf.RmtInf.Ustrd", "//CdtTrfTxInf/RmtInf/Ustrd", "Invoice 12345 (single)", "String", 140, true, false, null, "Remittance information"),
                new IsoFieldDef("Supplementary Place & Name", "CstmrCdtTrfInitn.SplmtryData.PlcAndNm", "//SplmtryData/PlcAndNm", "AdditionalVerificationDetails", "String", 35, true, false, null, "Supplementary descriptor"),
                new IsoFieldDef("Creditor Account Designation", "CstmrCdtTrfInitn.SplmtryData.Envlp.CustomData.CreditorInfo.AccountDesignation", "//CreditorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Creditor ID Type", "CstmrCdtTrfInitn.SplmtryData.Envlp.CustomData.CreditorInfo.IdType", "//CreditorInfo/IdType", "BVN", "String", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Creditor ID Value", "CstmrCdtTrfInitn.SplmtryData.Envlp.CustomData.CreditorInfo.IdValue", "//CreditorInfo/IdValue", "22298546518", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Creditor Account Tier", "CstmrCdtTrfInitn.SplmtryData.Envlp.CustomData.CreditorInfo.AccountTier", "//CreditorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Transaction Location", "CstmrCdtTrfInitn.SplmtryData.Envlp.CustomData.TransactionInfo.TransactionLocation", "//TransactionInfo/TransactionLocation", "013223231333", "String", 30, true, false, null, "Location Coordinates"),
                new IsoFieldDef("Channel Code", "CstmrCdtTrfInitn.SplmtryData.Envlp.CustomData.TransactionInfo.ChannelCode", "//TransactionInfo/ChannelCode", "2", "String", 2, true, false, "CHANNEL_CODE", "Channel Code (1-11)"),
                new IsoFieldDef("Fixed Collection Amount", "CstmrCdtTrfInitn.SplmtryData.Envlp.CustomData.TransactionInfo.FixedCollectionAmount", "//TransactionInfo/FixedCollectionAmount", "false", "Boolean", 5, true, false, null, "Fixed Collection Amount"),
                new IsoFieldDef("Mandate Code", "CstmrCdtTrfInitn.SplmtryData.Envlp.CustomData.TransactionInfo.MandateCode", "//TransactionInfo/MandateCode", "0000004/001/0000070986", "String", 35, false, false, null, "Mandate Code")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.001.001.12">
                    <CstmrCdtTrfInitn>
                        <GrpHdr>
                            <MsgId>99905720260225144722037299534778744</MsgId>
                            <CreDtTm>2026-02-25T14:47:22.637+01:00</CreDtTm>
                            <NbOfTxs>1</NbOfTxs>
                            <CtrlSum>120.51</CtrlSum>
                            <InitgPty>
                                <Nm>Musa</Nm>
                                <Id><OrgId><Othr><SchmeNm><Cd>999057</Cd></SchmeNm></Othr></OrgId></Id>
                            </InitgPty>
                            <FwdgAgt>
                                <FinInstnId>
                                    <BICFI>999057</BICFI>
                                </FinInstnId>
                            </FwdgAgt>
                        </GrpHdr>
                        <PmtInf>
                            <PmtInfId>PMT-20251016-001-SINGLE</PmtInfId>
                            <PmtMtd>TRF</PmtMtd>
                            <BtchBookg>false</BtchBookg>
                            <NbOfTxs>1</NbOfTxs>
                            <CtrlSum>120.51</CtrlSum>
                            <ReqdExctnDt><Dt>2026-02-17Z</Dt></ReqdExctnDt>
                            <Dbtr><Nm>Musa</Nm></Dbtr>
                            <DbtrAcct><Id><IBAN>0177136558</IBAN></Id><Nm>Musa</Nm></DbtrAcct>
                            <DbtrAgt>
                                <FinInstnId>
                                    <BICFI>999057</BICFI>
                                    <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </DbtrAgt>
                            <ChrgBr>SLEV</ChrgBr>
                            <CdtTrfTxInf>
                                <PmtId><EndToEndId>99905746102951471838787625821100514</EndToEndId></PmtId>
                                <Amt><InstdAmt Ccy="NGN">120.51</InstdAmt></Amt>
                                <CdtrAgt>
                                    <FinInstnId>
                                        <BICFI>999058</BICFI>
                                        <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                    </FinInstnId>
                                </CdtrAgt>
                                <Cdtr><Nm>James</Nm></Cdtr>
                                <CdtrAcct><Id><IBAN>3157417712</IBAN></Id><Nm>James</Nm></CdtrAcct>
                                <RmtInf><Ustrd>Invoice 12345 (single)</Ustrd></RmtInf>
                            </CdtTrfTxInf>
                        </PmtInf>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <CreditorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22298546518</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </CreditorInfo>
                                    <TransactionInfo>
                                        <TransactionLocation>013223231333</TransactionLocation>
                                        <ChannelCode>2</ChannelCode>
                                        <FixedCollectionAmount>false</FixedCollectionAmount>
                                        <MandateCode>0000004/001/0000070986</MandateCode>
                                    </TransactionInfo>
                                </CustomData>
                            </Envlp>
                        </SplmtryData>
                    </CstmrCdtTrfInitn>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pain.001")
                .name("Payment Initiation")
                .isoCode("pain.001.001.12")
                .category("Payment Initiation")
                .rootElement("Document")
                .mainElement("CstmrCdtTrfInitn")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pain.001.001.12")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 17. pain.002: Customer Payment Status Report
    // ==========================================
    private static void registerPain002() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "CstmrPmtStsRpt.GrpHdr.MsgId", "//GrpHdr/MsgId", "99999920260225192657029842136833211", "String", 35, true, false, "NPS_ID", "Status Report Msg ID"),
                new IsoFieldDef("Creation DateTime", "CstmrPmtStsRpt.GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-02-25T18:26:57.390+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp"),
                new IsoFieldDef("Initiating Party Name", "CstmrPmtStsRpt.GrpHdr.InitgPty.Nm", "//GrpHdr/InitgPty/Nm", "Musa", "String", 100, true, false, null, "Initiator Name"),
                new IsoFieldDef("Debtor Agent BICFI", "CstmrPmtStsRpt.GrpHdr.DbtrAgt.FinInstnId.BICFI", "//GrpHdr/DbtrAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, null, "Debtor Agent BICFI"),
                new IsoFieldDef("Debtor Agent Member ID", "CstmrPmtStsRpt.GrpHdr.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Debtor Member ID"),
                new IsoFieldDef("Original Message ID", "CstmrPmtStsRpt.OrgnlGrpInfAndSts.OrgnlMsgId", "//OrgnlGrpInfAndSts/OrgnlMsgId", "99905720260225192650869851166984847", "String", 35, true, false, "NPS_ID", "Original pain.001 Msg ID"),
                new IsoFieldDef("Original Message Name ID", "CstmrPmtStsRpt.OrgnlGrpInfAndSts.OrgnlMsgNmId", "//OrgnlGrpInfAndSts/OrgnlMsgNmId", "pain.001.001.12", "String", 35, true, false, null, "Original Message Name ID"),
                new IsoFieldDef("Group Status", "CstmrPmtStsRpt.OrgnlGrpInfAndSts.GrpSts", "//OrgnlGrpInfAndSts/GrpSts", "ACSC", "String", 4, true, false, "GROUP_STATUS", "Group status code"),
                new IsoFieldDef("Original Payment Information ID", "CstmrPmtStsRpt.OrgnlPmtInfAndSts.OrgnlPmtInfId", "//OrgnlPmtInfAndSts/OrgnlPmtInfId", "PMT-20251016-001-SINGLE", "String", 35, true, false, null, "Original PmtInf ID"),
                new IsoFieldDef("Status ID", "CstmrPmtStsRpt.OrgnlPmtInfAndSts.TxInfAndSts.StsId", "//TxInfAndSts/StsId", "99905774143804655117506058383208278", "String", 35, true, false, "NPS_ID", "Status ID"),
                new IsoFieldDef("Original End-to-End ID", "CstmrPmtStsRpt.OrgnlPmtInfAndSts.TxInfAndSts.OrgnlEndToEndId", "//TxInfAndSts/OrgnlEndToEndId", "99905774143804655117506058383208278", "String", 35, true, false, "NPS_ID", "Original EndToEnd ID"),
                new IsoFieldDef("Transaction Status", "CstmrPmtStsRpt.OrgnlPmtInfAndSts.TxInfAndSts.TxSts", "//TxInfAndSts/TxSts", "ACSC", "String", 4, true, false, "GROUP_STATUS", "Transaction Status"),
                new IsoFieldDef("Status Reason Code", "CstmrPmtStsRpt.OrgnlPmtInfAndSts.TxInfAndSts.StsRsnInf.Rsn.Cd", "//TxInfAndSts/StsRsnInf/Rsn/Cd", "000", "String", 4, false, false, "REASON_CODE", "Reason Code"),
                new IsoFieldDef("Additional Information", "CstmrPmtStsRpt.OrgnlPmtInfAndSts.TxInfAndSts.StsRsnInf.AddtlInf", "//TxInfAndSts/StsRsnInf/AddtlInf", "Accepted", "String", 140, false, false, null, "Additional Information")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.002.001.14">
                    <CstmrPmtStsRpt>
                        <GrpHdr>
                            <MsgId>99999920260225192657029842136833211</MsgId>
                            <CreDtTm>2026-02-25T18:26:57.390+01:00</CreDtTm>
                            <InitgPty><Nm>Musa</Nm></InitgPty>
                            <DbtrAgt>
                                <FinInstnId>
                                    <BICFI>999057</BICFI>
                                    <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </DbtrAgt>
                        </GrpHdr>
                        <OrgnlGrpInfAndSts>
                            <OrgnlMsgId>99905720260225192650869851166984847</OrgnlMsgId>
                            <OrgnlMsgNmId>pain.001.001.12</OrgnlMsgNmId>
                            <GrpSts>ACSC</GrpSts>
                        </OrgnlGrpInfAndSts>
                        <OrgnlPmtInfAndSts>
                            <OrgnlPmtInfId>PMT-20251016-001-SINGLE</OrgnlPmtInfId>
                            <TxInfAndSts>
                                <StsId>99905774143804655117506058383208278</StsId>
                                <OrgnlEndToEndId>99905774143804655117506058383208278</OrgnlEndToEndId>
                                <TxSts>ACSC</TxSts>
                                <StsRsnInf>
                                    <Rsn><Cd>000</Cd></Rsn>
                                    <AddtlInf>Accepted</AddtlInf>
                                </StsRsnInf>
                            </TxInfAndSts>
                        </OrgnlPmtInfAndSts>
                    </CstmrPmtStsRpt>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pain.002")
                .name("Customer Payment Status Report")
                .isoCode("pain.002.001.14")
                .category("Payment Initiation")
                .rootElement("Document")
                .mainElement("CstmrPmtStsRpt")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pain.002.001.14")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 18. pain.008: Customer Direct Debit Initiation
    // ==========================================
    private static void registerPain008() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "CstmrDrctDbtInitn.GrpHdr.MsgId", "//GrpHdr/MsgId", "99905720260312195134657916589823152", "String", 35, true, false, "NPS_ID", "Message ID"),
                new IsoFieldDef("Creation DateTime", "CstmrDrctDbtInitn.GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-03-12T19:51:35.192+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp"),
                new IsoFieldDef("Number of Transactions (Group)", "CstmrDrctDbtInitn.GrpHdr.NbOfTxs", "//GrpHdr/NbOfTxs", "1", "Numeric", 15, true, false, null, "Nb of transactions"),
                new IsoFieldDef("Control Sum (Group)", "CstmrDrctDbtInitn.GrpHdr.CtrlSum", "//GrpHdr/CtrlSum", "100.00", "Decimal", 18, true, false, "AMOUNT", "Control Sum"),
                new IsoFieldDef("Initiating Party Name", "CstmrDrctDbtInitn.GrpHdr.InitgPty.Nm", "//GrpHdr/InitgPty/Nm", "ACME BILLING LIMITED", "String", 100, true, false, null, "Initiator Name"),
                new IsoFieldDef("Forwarding Agent BICFI", "CstmrDrctDbtInitn.GrpHdr.FwdgAgt.FinInstnId.BICFI", "//GrpHdr/FwdgAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, null, "Forwarding Agent BIC"),
                new IsoFieldDef("Payment Information ID", "CstmrDrctDbtInitn.PmtInf.PmtInfId", "//PmtInf/PmtInfId", "026-071-67895-001-00022", "String", 35, true, false, null, "PmtInf ID"),
                new IsoFieldDef("Payment Method", "CstmrDrctDbtInitn.PmtInf.PmtMtd", "//PmtInf/PmtMtd", "DD", "String", 2, true, false, null, "Payment Method (DD)"),
                new IsoFieldDef("Payment Number of Transactions", "CstmrDrctDbtInitn.PmtInf.NbOfTxs", "//PmtInf/NbOfTxs", "1", "Numeric", 15, true, false, null, "Nb of transactions"),
                new IsoFieldDef("Payment Control Sum", "CstmrDrctDbtInitn.PmtInf.CtrlSum", "//PmtInf/CtrlSum", "100.00", "Decimal", 18, true, false, "AMOUNT", "Payment Control Sum"),
                new IsoFieldDef("Service Level Code", "CstmrDrctDbtInitn.PmtInf.PmtTpInf.SvcLvl.Cd", "//PmtInf/PmtTpInf/SvcLvl/Cd", "NURG", "String", 4, true, false, null, "Service Level Code"),
                new IsoFieldDef("Local Instrument Code", "CstmrDrctDbtInitn.PmtInf.PmtTpInf.LclInstrm.Prtry", "//PmtInf/PmtTpInf/LclInstrm/Prtry", "NPSDD", "String", 35, true, false, null, "Local Instrument Code"),
                new IsoFieldDef("Sequence Type", "CstmrDrctDbtInitn.PmtInf.PmtTpInf.SeqTp", "//PmtInf/PmtTpInf/SeqTp", "FRST", "String", 4, true, false, "SEQUENCE_TYPE", "Sequence Type"),
                new IsoFieldDef("Required Collection Date", "CstmrDrctDbtInitn.PmtInf.ReqdColltnDt", "//PmtInf/ReqdColltnDt", "2025-02-16Z", "Date", 10, true, false, "DATE", "Collection date"),
                new IsoFieldDef("Creditor Name", "CstmrDrctDbtInitn.PmtInf.Cdtr.Nm", "//PmtInf/Cdtr/Nm", "ACME BILLING LIMITED", "String", 100, true, false, null, "Creditor Name"),
                new IsoFieldDef("Creditor Account Number (IBAN)", "CstmrDrctDbtInitn.PmtInf.CdtrAcct.Id.IBAN", "//PmtInf/CdtrAcct/Id/IBAN", "3157417712", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Creditor Account Currency", "CstmrDrctDbtInitn.PmtInf.CdtrAcct.Ccy", "//PmtInf/CdtrAcct/Ccy", "NGN", "String", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Creditor Agent BICFI", "CstmrDrctDbtInitn.PmtInf.CdtrAgt.FinInstnId.BICFI", "//PmtInf/CdtrAgt/FinInstnId/BICFI", "999057", "String", 11, false, false, null, "Creditor Agent BICFI"),
                new IsoFieldDef("Creditor Agent Member ID", "CstmrDrctDbtInitn.PmtInf.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//PmtInf/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Creditor Member ID"),
                new IsoFieldDef("Instruction ID", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.PmtId.InstrId", "//DrctDbtTxInf/PmtId/InstrId", "99905799905820260312195134657916589", "String", 35, true, false, "NPS_ID", "Instruction ID"),
                new IsoFieldDef("End-to-End ID", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.PmtId.EndToEndId", "//DrctDbtTxInf/PmtId/EndToEndId", "99905720260312195134657916589823152", "String", 35, true, false, "NPS_ID", "EndToEnd ID"),
                new IsoFieldDef("Instructed Amount", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.InstdAmt", "//DrctDbtTxInf/InstdAmt", "100.00", "Decimal", 18, true, false, "AMOUNT", "Instructed amount"),
                new IsoFieldDef("Instructed Amount Currency", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.InstdAmt@Ccy", "//DrctDbtTxInf/InstdAmt/@Ccy", "NGN", "Currency Code", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Mandate ID", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DrctDbtTx.MndtRltdInf.MndtId", "//MndtRltdInf/MndtId", "0000004/001/0000070986", "String", 35, true, false, null, "Mandate ID"),
                new IsoFieldDef("Date of Signature", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DrctDbtTx.MndtRltdInf.DtOfSgntr", "//MndtRltdInf/DtOfSgntr", "2025-02-01Z", "Date", 10, true, false, "DATE", "Signature date"),
                new IsoFieldDef("First Collection Date", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DrctDbtTx.MndtRltdInf.FrstColltnDt", "//MndtRltdInf/FrstColltnDt", "2025-02-16Z", "Date", 10, true, false, "DATE", "First collection date"),
                new IsoFieldDef("Final Collection Date", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DrctDbtTx.MndtRltdInf.FnlColltnDt", "//MndtRltdInf/FnlColltnDt", "2025-12-31Z", "Date", 10, true, false, "DATE", "Final collection date"),
                new IsoFieldDef("Frequency Type", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DrctDbtTx.MndtRltdInf.Frqcy.Tp", "//MndtRltdInf/Frqcy/Tp", "MNTH", "Enum", 4, true, false, "FREQUENCY_TYPE", "Frequency Type"),
                new IsoFieldDef("Debtor Agent Member ID", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//DrctDbtTxInf/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999058", "String", 6, true, false, "MEMBER_ID", "Debtor Member ID"),
                new IsoFieldDef("Debtor Name", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.Dbtr.Nm", "//DrctDbtTxInf/Dbtr/Nm", "JOHN DOE", "String", 100, true, false, null, "Debtor Name"),
                new IsoFieldDef("Debtor Account Number (IBAN)", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DbtrAcct.Id.IBAN", "//DrctDbtTxInf/DbtrAcct/Id/IBAN", "0177136558", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Debtor Account Currency", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.DbtrAcct.Ccy", "//DrctDbtTxInf/DbtrAcct/Ccy", "NGN", "String", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Remittance Information", "CstmrDrctDbtInitn.PmtInf.DrctDbtTxInf.RmtInf.Ustrd", "//DrctDbtTxInf/RmtInf/Ustrd", "UTILITY BILL FEB-2025", "String", 140, true, false, null, "Remittance info"),
                new IsoFieldDef("Supplementary Place & Name", "CstmrDrctDbtInitn.SplmtryData.PlcAndNm", "//SplmtryData/PlcAndNm", "AdditionalVerificationDetails", "String", 35, true, false, null, "Supplementary descriptor"),
                new IsoFieldDef("Debtor Account Designation", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.DebtorInfo.AccountDesignation", "//DebtorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Debtor ID Type", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.DebtorInfo.IdType", "//DebtorInfo/IdType", "BVN", "String", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Debtor ID Value", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.DebtorInfo.IdValue", "//DebtorInfo/IdValue", "22222222222", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Debtor Account Tier", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.DebtorInfo.AccountTier", "//DebtorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Creditor Account Designation", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.CreditorInfo.AccountDesignation", "//CreditorInfo/AccountDesignation", "1", "Integer", 1, true, false, "ACCOUNT_DESIGNATION", "Account Designation (1-6)"),
                new IsoFieldDef("Creditor ID Type", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.CreditorInfo.IdType", "//CreditorInfo/IdType", "BVN", "String", 7, true, false, "ID_TYPE", "ID Type"),
                new IsoFieldDef("Creditor ID Value", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.CreditorInfo.IdValue", "//CreditorInfo/IdValue", "22222222222", "String", 35, true, false, "ID_VALUE", "11-digit BVN"),
                new IsoFieldDef("Creditor Account Tier", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.CreditorInfo.AccountTier", "//CreditorInfo/AccountTier", "1", "Integer", 1, true, false, "ACCOUNT_TIER", "Account Tier (1-3)"),
                new IsoFieldDef("Transaction Location", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.TransactionInfo.TransactionLocation", "//TransactionInfo/TransactionLocation", "013223231333", "String", 30, true, false, null, "Location Coordinates"),
                new IsoFieldDef("Name Enquiry Message ID", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.TransactionInfo.NameEnquiryMsgId", "//TransactionInfo/NameEnquiryMsgId", "99905720251104552022522202020202015", "String", 35, true, false, "NPS_ID", "Name Enquiry ID"),
                new IsoFieldDef("Channel Code", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.TransactionInfo.ChannelCode", "//TransactionInfo/ChannelCode", "4", "String", 2, true, false, "CHANNEL_CODE", "Channel Code (1-11)"),
                new IsoFieldDef("Fixed Collection Amount", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.TransactionInfo.FixedCollectionAmount", "//TransactionInfo/FixedCollectionAmount", "false", "Boolean", 5, true, false, null, "Fixed Collection Amount"),
                new IsoFieldDef("Mandate Reference Code", "CstmrDrctDbtInitn.SplmtryData.Envlp.CustomData.TransactionInfo.MandateCode", "//TransactionInfo/MandateCode", "0000004/001/0000070986", "String", 35, false, false, null, "Mandate Code")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pain.008.001.11">
                    <CstmrDrctDbtInitn>
                        <GrpHdr>
                            <MsgId>99905720260312195134657916589823152</MsgId>
                            <CreDtTm>2026-03-12T19:51:35.192+01:00</CreDtTm>
                            <NbOfTxs>1</NbOfTxs>
                            <CtrlSum>100.00</CtrlSum>
                            <InitgPty><Nm>ACME BILLING LIMITED</Nm></InitgPty>
                            <FwdgAgt>
                                <FinInstnId>
                                    <BICFI>999057</BICFI>
                                </FinInstnId>
                            </FwdgAgt>
                        </GrpHdr>
                        <PmtInf>
                            <PmtInfId>026-071-67895-001-00022</PmtInfId>
                            <PmtMtd>DD</PmtMtd>
                            <NbOfTxs>1</NbOfTxs>
                            <CtrlSum>100.00</CtrlSum>
                            <PmtTpInf>
                                <SvcLvl><Cd>NURG</Cd></SvcLvl>
                                <LclInstrm><Prtry>NPSDD</Prtry></LclInstrm>
                                <SeqTp>FRST</SeqTp>
                            </PmtTpInf>
                            <ReqdColltnDt>2025-02-16Z</ReqdColltnDt>
                            <Cdtr><Nm>ACME BILLING LIMITED</Nm></Cdtr>
                            <CdtrAcct><Id><IBAN>3157417712</IBAN></Id><Ccy>NGN</Ccy></CdtrAcct>
                            <CdtrAgt>
                                <FinInstnId>
                                    <BICFI>999057</BICFI>
                                    <ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId>
                                </FinInstnId>
                            </CdtrAgt>
                            <DrctDbtTxInf>
                                <PmtId>
                                    <InstrId>99905799905820260312195134657916589</InstrId>
                                    <EndToEndId>99905720260312195134657916589823152</EndToEndId>
                                </PmtId>
                                <InstdAmt Ccy="NGN">100.00</InstdAmt>
                                <DrctDbtTx>
                                    <MndtRltdInf>
                                        <MndtId>0000004/001/0000070986</MndtId>
                                        <DtOfSgntr>2025-02-01Z</DtOfSgntr>
                                        <FrstColltnDt>2025-02-16Z</FrstColltnDt>
                                        <FnlColltnDt>2025-12-31Z</FnlColltnDt>
                                        <Frqcy><Tp>MNTH</Tp></Frqcy>
                                    </MndtRltdInf>
                                </DrctDbtTx>
                                <DbtrAgt>
                                    <FinInstnId>
                                        <ClrSysMmbId><MmbId>999058</MmbId></ClrSysMmbId>
                                    </FinInstnId>
                                </DbtrAgt>
                                <Dbtr><Nm>JOHN DOE</Nm></Dbtr>
                                <DbtrAcct><Id><IBAN>0177136558</IBAN></Id><Ccy>NGN</Ccy></DbtrAcct>
                                <RmtInf><Ustrd>UTILITY BILL FEB-2025</Ustrd></RmtInf>
                            </DrctDbtTxInf>
                        </PmtInf>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                            <Envlp>
                                <CustomData>
                                    <DebtorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22222222222</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </DebtorInfo>
                                    <DebtorMetadata>
                                        <BiometricData></BiometricData>
                                    </DebtorMetadata>
                                    <CreditorInfo>
                                        <AccountDesignation>1</AccountDesignation>
                                        <IdType>BVN</IdType>
                                        <IdValue>22222222222</IdValue>
                                        <AccountTier>1</AccountTier>
                                    </CreditorInfo>
                                    <CreditorMetadata/>
                                    <TransactionInfo>
                                        <TransactionLocation>013223231333</TransactionLocation>
                                        <NameEnquiryMsgId>99905720251104552022522202020202015</NameEnquiryMsgId>
                                        <ChannelCode>4</ChannelCode>
                                        <FixedCollectionAmount>false</FixedCollectionAmount>
                                        <MandateCode>0000004/001/0000070986</MandateCode>
                                    </TransactionInfo>
                                </CustomData>
                            </Envlp>
                        </SplmtryData>
                    </CstmrDrctDbtInitn>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pain.008")
                .name("Direct Debit Initiation (Customer)")
                .isoCode("pain.008.001.11")
                .category("Direct Debit Operations")
                .rootElement("Document")
                .mainElement("CstmrDrctDbtInitn")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pain.008.001.11")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }

    // ==========================================
    // 19. pacs.004: Payment Return
    // ==========================================
    private static void registerPacs004() {
        List<IsoFieldDef> fields = Arrays.asList(
                new IsoFieldDef("Message ID", "PmtRtr.GrpHdr.MsgId", "//GrpHdr/MsgId", "99905720260512232654907247853431392", "String", 35, true, false, "NPS_ID", "Return Msg ID"),
                new IsoFieldDef("Creation DateTime", "PmtRtr.GrpHdr.CreDtTm", "//GrpHdr/CreDtTm", "2026-05-12T22:26:54.944+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Creation timestamp in UTC+1"),
                new IsoFieldDef("Settlement Method", "PmtRtr.GrpHdr.SttlmInf.SttlmMtd", "//GrpHdr/SttlmInf/SttlmMtd", "CLRG", "String", 4, true, false, "SETTLEMENT_METHOD", "Settlement Method"),
                new IsoFieldDef("Instructing Agent Member ID", "PmtRtr.GrpHdr.InstgAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/InstgAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Instructing Member ID"),
                new IsoFieldDef("Instructed Agent Member ID", "PmtRtr.GrpHdr.InstdAgt.FinInstnId.ClrSysMmbId.MmbId", "//GrpHdr/InstdAgt/FinInstnId/ClrSysMmbId/MmbId", "999998", "String", 6, true, false, "MEMBER_ID", "Instructed Member ID"),
                new IsoFieldDef("Original Message ID", "PmtRtr.OrgnlGrpInf.OrgnlMsgId", "//OrgnlGrpInf/OrgnlMsgId", "99999820260512232125820145868685089", "String", 35, true, false, "NPS_ID", "Original message ID"),
                new IsoFieldDef("Original Message Name", "PmtRtr.OrgnlGrpInf.OrgnlMsgNmId", "//OrgnlGrpInf/OrgnlMsgNmId", "pacs.008.001.12", "String", 35, true, false, null, "Original message name"),
                new IsoFieldDef("Original Creation DateTime", "PmtRtr.OrgnlGrpInf.OrgnlCreDtTm", "//OrgnlGrpInf/OrgnlCreDtTm", "2026-05-12T22:21:25.719+01:00", "DateTime", 35, true, false, "UTC1_DATETIME", "Original creation timestamp"),
                new IsoFieldDef("Return ID", "PmtRtr.TxInf.RtrId", "//TxInf/RtrId", "99905720260512232654907247853431392", "String", 35, true, false, "NPS_ID", "Return ID"),
                new IsoFieldDef("Original Instruction ID", "PmtRtr.TxInf.OrgnlInstrId", "//TxInf/OrgnlInstrId", "99999899905720260512232125864218515", "String", 35, true, false, "NPS_ID", "Original Instr ID"),
                new IsoFieldDef("Original End-to-End ID", "PmtRtr.TxInf.OrgnlEndToEndId", "//TxInf/OrgnlEndToEndId", "99999854223619654067102010472858250", "String", 35, true, false, "NPS_ID", "Original EndToEnd ID"),
                new IsoFieldDef("Original Transaction ID", "PmtRtr.TxInf.OrgnlTxId", "//TxInf/OrgnlTxId", "99999820260512232125820145868685089", "String", 35, true, false, "NPS_ID", "Original Tx ID"),
                new IsoFieldDef("Returned Interbank Settlement Amount", "PmtRtr.TxInf.RtrdIntrBkSttlmAmt", "//TxInf/RtrdIntrBkSttlmAmt", "60000.00", "Decimal", 18, true, false, "AMOUNT", "Returned settlement amount"),
                new IsoFieldDef("Returned Amount Currency", "PmtRtr.TxInf.RtrdIntrBkSttlmAmt.@Ccy", "//TxInf/RtrdIntrBkSttlmAmt/@Ccy", "NGN", "String", 3, true, false, "CURRENCY", "Currency code"),
                new IsoFieldDef("Interbank Settlement Date (Return)", "PmtRtr.TxInf.IntrBkSttlmDt", "//TxInf/IntrBkSttlmDt", "2026-05-12Z", "Date", 10, true, false, "DATE", "Return settlement date"),
                new IsoFieldDef("Charge Bearer", "PmtRtr.TxInf.ChrgBr", "//TxInf/ChrgBr", "SLEV", "String", 4, true, false, null, "Charge bearer (SLEV)"),
                new IsoFieldDef("Tx Instructing Agent Member ID", "PmtRtr.TxInf.InstgAgt.FinInstnId.ClrSysMmbId.MmbId", "//TxInf/InstgAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Tx Instructing Member ID"),
                new IsoFieldDef("Tx Instructed Agent Member ID", "PmtRtr.TxInf.InstdAgt.FinInstnId.ClrSysMmbId.MmbId", "//TxInf/InstdAgt/FinInstnId/ClrSysMmbId/MmbId", "999998", "String", 6, true, false, "MEMBER_ID", "Tx Instructed Member ID"),
                new IsoFieldDef("Return Reason Code", "PmtRtr.TxInf.RtrRsnInf.Rsn.Prtry", "//RtrRsnInf/Rsn/Prtry", "AC04", "String", 35, true, false, "REASON_CODE", "Return Reason Code"),
                new IsoFieldDef("Debtor Name", "PmtRtr.TxInf.OrgnlTxRef.Dbtr.Pty.Nm", "//OrgnlTxRef/Dbtr/Pty/Nm", "KaYole", "String", 100, true, false, null, "Debtor Name"),
                new IsoFieldDef("Debtor Account Number (IBAN)", "PmtRtr.TxInf.OrgnlTxRef.DbtrAcct.Id.IBAN", "//OrgnlTxRef/DbtrAcct/Id/IBAN", "0177136558", "String", 10, true, false, "NUBAN", "10-digit NUBAN"),
                new IsoFieldDef("Debtor Account Name", "PmtRtr.TxInf.OrgnlTxRef.DbtrAcct.Nm", "//OrgnlTxRef/DbtrAcct/Nm", "KaYole", "String", 100, true, false, null, "Debtor Account Name"),
                new IsoFieldDef("Debtor Agent Member ID", "PmtRtr.TxInf.OrgnlTxRef.DbtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//OrgnlTxRef/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999998", "String", 6, true, false, "MEMBER_ID", "Debtor Member ID"),
                new IsoFieldDef("Creditor Agent Member ID", "PmtRtr.TxInf.OrgnlTxRef.CdtrAgt.FinInstnId.ClrSysMmbId.MmbId", "//OrgnlTxRef/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId", "999057", "String", 6, true, false, "MEMBER_ID", "Creditor Member ID"),
                new IsoFieldDef("Creditor Name", "PmtRtr.TxInf.OrgnlTxRef.Cdtr.Pty.Nm", "//OrgnlTxRef/Cdtr/Pty/Nm", "Oge Best", "String", 100, true, false, null, "Creditor Name"),
                new IsoFieldDef("Creditor Account Number (IBAN)", "PmtRtr.TxInf.OrgnlTxRef.CdtrAcct.Id.IBAN", "//OrgnlTxRef/CdtrAcct/Id/IBAN", "2222222221", "String", 10, true, false, "NUBAN", "10-digit NUBAN")
        );

        String sampleXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.004.001.11">
                    <PmtRtr>
                        <GrpHdr>
                            <MsgId>99905720260512232654907247853431392</MsgId>
                            <CreDtTm>2026-05-12T22:26:54.944+01:00</CreDtTm>
                            <SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>
                            <InstgAgt><FinInstnId><ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId></FinInstnId></InstgAgt>
                            <InstdAgt><FinInstnId><ClrSysMmbId><MmbId>999998</MmbId></ClrSysMmbId></FinInstnId></InstdAgt>
                        </GrpHdr>
                        <OrgnlGrpInf>
                            <OrgnlMsgId>99999820260512232125820145868685089</OrgnlMsgId>
                            <OrgnlMsgNmId>pacs.008.001.12</OrgnlMsgNmId>
                            <OrgnlCreDtTm>2026-05-12T22:21:25.719+01:00</OrgnlCreDtTm>
                        </OrgnlGrpInf>
                        <TxInf>
                            <RtrId>99905720260512232654907247853431392</RtrId>
                            <OrgnlInstrId>99999899905720260512232125864218515</OrgnlInstrId>
                            <OrgnlEndToEndId>99999854223619654067102010472858250</OrgnlEndToEndId>
                            <OrgnlTxId>99999820260512232125820145868685089</OrgnlTxId>
                            <OrgnlIntrBkSttlmDt>2026-05-12Z</OrgnlIntrBkSttlmDt>
                            <RtrdIntrBkSttlmAmt Ccy="NGN">60000.00</RtrdIntrBkSttlmAmt>
                            <IntrBkSttlmDt>2026-05-12Z</IntrBkSttlmDt>
                            <ChrgBr>SLEV</ChrgBr>
                            <InstgAgt><FinInstnId><ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId></FinInstnId></InstgAgt>
                            <InstdAgt><FinInstnId><ClrSysMmbId><MmbId>999998</MmbId></ClrSysMmbId></FinInstnId></InstdAgt>
                            <RtrRsnInf>
                                <Rsn><Prtry>AC04</Prtry></Rsn>
                                <AddtlInf>Closed account number</AddtlInf>
                            </RtrRsnInf>
                            <OrgnlTxRef>
                                <IntrBkSttlmAmt Ccy="NGN">50.12</IntrBkSttlmAmt>
                                <PmtTpInf><ClrChanl>RTNS</ClrChanl><LclInstrm><Prtry>CTAA</Prtry></LclInstrm></PmtTpInf>
                                <Dbtr><Pty><Nm>KaYole</Nm></Pty></Dbtr>
                                <DbtrAcct><Id><IBAN>0177136558</IBAN></Id><Nm>KaYole</Nm></DbtrAcct>
                                <DbtrAgt><FinInstnId><ClrSysMmbId><MmbId>999998</MmbId></ClrSysMmbId></FinInstnId></DbtrAgt>
                                <CdtrAgt><FinInstnId><ClrSysMmbId><MmbId>999057</MmbId></ClrSysMmbId></FinInstnId></CdtrAgt>
                                <Cdtr><Pty><Nm>Oge Best</Nm></Pty></Cdtr>
                                <CdtrAcct><Id><IBAN>2222222221</IBAN></Id></CdtrAcct>
                            </OrgnlTxRef>
                        </TxInf>
                        <SplmtryData>
                            <PlcAndNm>AdditionalVerificationDetails</PlcAndNm>
                        </SplmtryData>
                    </PmtRtr>
                </Document>
                """.trim();

        register(IsoMessageDefinition.builder()
                .key("pacs.004")
                .name("Payment Return")
                .isoCode("pacs.004.001.11")
                .category("Credit Transfer & Returns")
                .rootElement("Document")
                .mainElement("PmtRtr")
                .namespace("urn:iso:std:iso:20022:tech:xsd:pacs.004.001.11")
                .fields(fields)
                .sampleXml(sampleXml)
                .build());
    }
}
