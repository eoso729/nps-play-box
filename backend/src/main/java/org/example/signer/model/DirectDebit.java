package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pain.008.001.11")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class DirectDebit {

    @XmlElement(name = "CstmrDrctDbtInitn")
    private CstmrDrctDbtInitn cstmrDrctDbtInitn;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CstmrDrctDbtInitn {
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
        @XmlElement(name = "NbOfTxs")
        private Integer nbOfTxs;
        @XmlElement(name = "CtrlSum")
        private BigDecimal ctrlSum;
        @XmlElement(name = "PmtTpInf")
        private PmtTpInf pmtTpInf;
        @XmlElement(name = "ReqdColltnDt")
        private String reqdColltnDt;
        @XmlElement(name = "Cdtr")
        private Cdtr cdtr;
        @XmlElement(name = "CdtrAcct")
        private CdtrAcct cdtrAcct;
        @XmlElement(name = "CdtrAgt")
        private CdtrAgt cdtrAgt;
        @XmlElement(name = "DrctDbtTxInf")
        private DrctDbtTxInf drctDbtTxInf;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PmtTpInf {
        @XmlElement(name = "SvcLvl")
        private SvcLvl svcLvl;
        @XmlElement(name = "LclInstrm")
        private LclInstrm lclInstrm;
        @XmlElement(name = "SeqTp")
        private String seqTp;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SvcLvl {
        @XmlElement(name = "Cd")
        private String cd;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class LclInstrm {
        @XmlElement(name = "Prtry")
        private String prtry;
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
        @XmlElement(name = "Ccy")
        private String ccy;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdtrAgt {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class DrctDbtTxInf {
        @XmlElement(name = "PmtId")
        private PmtId pmtId;
        @XmlElement(name = "InstdAmt")
        private InstdAmt instdAmt;
        @XmlElement(name = "DrctDbtTx")
        private DrctDbtTx drctDbtTx;
        @XmlElement(name = "DbtrAgt")
        private DbtrAgt dbtrAgt;
        @XmlElement(name = "Dbtr")
        private Dbtr dbtr;
        @XmlElement(name = "DbtrAcct")
        private DbtrAcct dbtrAcct;
        @XmlElement(name = "RmtInf")
        private RmtInf rmtInf;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PmtId {
        @XmlElement(name = "InstrId")
        private String instrId;
        @XmlElement(name = "EndToEndId")
        private String endToEndId;
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
    public static class DrctDbtTx {
        @XmlElement(name = "MndtRltdInf")
        private MndtRltdInf mndtRltdInf;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class MndtRltdInf {
        @XmlElement(name = "MndtId")
        private String mndtId;
        @XmlElement(name = "DtOfSgntr")
        private String dtOfSgntr;
        @XmlElement(name = "FrstColltnDt")
        private String frstColltnDt;
        @XmlElement(name = "FnlColltnDt")
        private String fnlColltnDt;
        @XmlElement(name = "Frqcy")
        private Frqcy frqcy;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Frqcy {
        @XmlElement(name = "Tp")
        private String tp;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class DbtrAgt {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
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
        @XmlElement(name = "DebtorInfo")
        private DebtorInfo debtorInfo;
        @XmlElement(name = "DebtorMetadata")
        private DebtorMetadata debtorMetadata;
        @XmlElement(name = "CreditorInfo")
        private CreditorInfo creditorInfo;
        @XmlElement(name = "CreditorMetadata")
        private CreditorMetadata creditorMetadata;
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
    public static class CreditorMetadata {
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class TransactionInfo {
        @XmlElement(name = "TransactionLocation")
        private String transactionLocation;
        @XmlElement(name = "NameEnquiryMsgId")
        private String nameEnquiryMsgId;
        @XmlElement(name = "ChannelCode")
        private String channelCode;
        @XmlElement(name = "FixedCollectionAmount")
        private Boolean fixedCollectionAmount;
        @XmlElement(name = "MandateCode")
        private String mandateCode;
    }
}
