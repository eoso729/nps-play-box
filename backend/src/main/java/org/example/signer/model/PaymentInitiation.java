package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pain.001.001.12")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class PaymentInitiation {

    @XmlElement(name = "CstmrCdtTrfInitn")
    private CstmrCdtTrfInitn cstmrCdtTrfInitn;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CstmrCdtTrfInitn {
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
        @XmlElement(name = "NbOfTxs")
        private Integer nbOfTxs;
        @XmlElement(name = "CtrlSum")
        private BigDecimal ctrlSum;
        @XmlElement(name = "InitgPty")
        private InitgPty initgPty;
        @XmlElement(name = "FwdgAgt")
        private FwdgAgt fwdgAgt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class InitgPty {
        @XmlElement(name = "Nm")
        private String nm;
        @XmlElement(name = "Id")
        private Id id;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Id {
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
        @XmlElement(name = "SchmeNm")
        private SchmeNm schmeNm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SchmeNm {
        @XmlElement(name = "Cd")
        private String cd;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class FwdgAgt {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PmtInf {
        @XmlElement(name = "PmtInfId")
        private String pmtInfId;
        @XmlElement(name = "PmtMtd")
        private String pmtMtd;
        @XmlElement(name = "BtchBookg")
        private Boolean btchBookg;
        @XmlElement(name = "NbOfTxs")
        private Integer nbOfTxs;
        @XmlElement(name = "CtrlSum")
        private BigDecimal ctrlSum;
        @XmlElement(name = "ReqdExctnDt")
        private ReqdExctnDt reqdExctnDt;
        @XmlElement(name = "Dbtr")
        private Dbtr dbtr;
        @XmlElement(name = "DbtrAcct")
        private DbtrAcct dbtrAcct;
        @XmlElement(name = "DbtrAgt")
        private DbtrAgt dbtrAgt;
        @XmlElement(name = "ChrgBr")
        private String chrgBr;
        @XmlElement(name = "CdtTrfTxInf")
        private CdtTrfTxInf cdtTrfTxInf;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class ReqdExctnDt {
        @XmlElement(name = "Dt")
        private String dt;
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
        @XmlElement(name = "Nm")
        private String nm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class DbtrAgt {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdtTrfTxInf {
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
        @XmlElement(name = "RmtInf")
        private RmtInf rmtInf;
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
    public static class AcctId {
        @XmlElement(name = "IBAN")
        private String iban;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class RmtInf {
        @XmlElement(name = "Ustrd")
        private String ustrd;
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
        @XmlElement(name = "CreditorInfo")
        private CreditorInfo creditorInfo;
        @XmlElement(name = "TransactionInfo")
        private TransactionInfo transactionInfo;
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
        @XmlElement(name = "FixedCollectionAmount")
        private Boolean fixedCollectionAmount;
        @XmlElement(name = "MandateCode")
        private String mandateCode;
    }
}
