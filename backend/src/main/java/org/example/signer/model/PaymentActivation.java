package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pain.013.001.11")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class PaymentActivation {

    @XmlElement(name = "CdtrPmtActvtnReq")
    private CdtrPmtActvtnReq cdtrPmtActvtnReq;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdtrPmtActvtnReq {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;
        @XmlElement(name = "PmtInf")
        private PmtInf pmtInf;
        @XmlElement(name = "SplmtryData")
        private SplmtryData splmtryData;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class GrpHdr {
        @XmlElement(name = "MsgId")
        private String msgId;
        @XmlElement(name = "CreDtTm")
        private String creDtTm;
        @XmlElement(name = "InitgPty")
        private InitgPty initgPty;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class InitgPty {
        @XmlElement(name = "Nm")
        private String nm;
        @XmlElement(name = "Id")
        private PartyId id;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PartyId {
        @XmlElement(name = "OrgId")
        private OrgId orgId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgId {
        @XmlElement(name = "Othr")
        private Othr othr;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Othr {
        @XmlElement(name = "Id")
        private String id;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PmtInf {
        @XmlElement(name = "PmtInfId")
        private String pmtInfId;
        @XmlElement(name = "PmtMtd")
        private String pmtMtd;
        @XmlElement(name = "ReqdExctnDt")
        private ReqdExctnDt reqdExctnDt;
        @XmlElement(name = "Dbtr")
        private Dbtr dbtr;
        @XmlElement(name = "DbtrAcct")
        private DbtrAcct dbtrAcct;
        @XmlElement(name = "DbtrAgt")
        private DbtrAgt dbtrAgt;
        @XmlElement(name = "CdtTrfTx")
        private CdtTrfTx cdtTrfTx;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class ReqdExctnDt {
        @XmlElement(name = "DtTm")
        private String dtTm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Dbtr {
        @XmlElement(name = "Nm")
        private String nm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class DbtrAcct {
        @XmlElement(name = "Id")
        private AcctId id;
        @XmlElement(name = "Ccy")
        private String ccy;
        @XmlElement(name = "Nm")
        private String nm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class AcctId {
        @XmlElement(name = "IBAN")
        private String iban;
        @XmlElement(name = "Othr")
        private Othr othr;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class DbtrAgt {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class FinInstnId {
        @XmlElement(name = "BICFI")
        private String bicfi;
        @XmlElement(name = "ClrSysMmbId")
        private ClrSysMmbId clrSysMmbId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class ClrSysMmbId {
        @XmlElement(name = "MmbId")
        private String mmbId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdtTrfTx {
        @XmlElement(name = "PmtId")
        private PmtId pmtId;
        @XmlElement(name = "Amt")
        private Amt amt;
        @XmlElement(name = "CdtrAgt")
        private CdtrAgt cdtrAgt;
        @XmlElement(name = "Cdtr")
        private Cdtr cdtr;
        @XmlElement(name = "CdtrAcct")
        private CdtrAcct cdtrAcct;
        @XmlElement(name = "Purp")
        private Purp purp;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PmtId {
        @XmlElement(name = "EndToEndId")
        private String endToEndId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Amt {
        @XmlElement(name = "InstdAmt")
        private InstdAmt instdAmt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class InstdAmt {
        @XmlAttribute(name = "Ccy")
        private String ccy;
        @XmlValue
        private BigDecimal value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdtrAgt {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Cdtr {
        @XmlElement(name = "Nm")
        private String nm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdtrAcct {
        @XmlElement(name = "Id")
        private AcctId id;
        @XmlElement(name = "Nm")
        private String nm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Purp {
        @XmlElement(name = "Prtry")
        private String prtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SplmtryData {
        @XmlElement(name = "PlcAndNm")
        private String plcAndNm;
        @XmlElement(name = "Envlp")
        private Envlp envlp;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Envlp {
        @XmlElement(name = "CustomData")
        private CustomData customData;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CustomData {
        @XmlElement(name = "DebtorInfo")
        private DebtorInfo debtorInfo;
        @XmlElement(name = "DebtorMetadata")
        private DebtorMetadata debtorMetadata;
        @XmlElement(name = "CreditorInfo")
        private CreditorInfo creditorInfo;
        @XmlElement(name = "TransactionInfo")
        private TransactionInfo transactionInfo;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class DebtorInfo {
        @XmlElement(name = "AccountDesignation")
        private String accountDesignation;
        @XmlElement(name = "IdType")
        private String idType;
        @XmlElement(name = "IdValue")
        private String idValue;
        @XmlElement(name = "AccountTier")
        private String accountTier;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class DebtorMetadata {
        @XmlElement(name = "BiometricData")
        private String biometricData;
        @XmlElement(name = "AdrLine")
        private String adrLine;
        @XmlElement(name = "PhneNb")
        private String phneNb;
        @XmlElement(name = "EmailAdr")
        private String emailAdr;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CreditorInfo {
        @XmlElement(name = "AccountDesignation")
        private String accountDesignation;
        @XmlElement(name = "IdType")
        private String idType;
        @XmlElement(name = "IdValue")
        private String idValue;
        @XmlElement(name = "AccountTier")
        private String accountTier;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class TransactionInfo {
        @XmlElement(name = "TransactionLocation")
        private String transactionLocation;
        @XmlElement(name = "ChannelCode")
        private String channelCode;
        @XmlElement(name = "MandateCategory")
        private String mandateCategory;
    }
}
