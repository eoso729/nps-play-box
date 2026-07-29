package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.12")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Transfer {

    @XmlElement(name = "FIToFICstmrCdtTrf")
    private FIToFICstmrCdtTrf fiToFICstmrCdtTrf;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class FIToFICstmrCdtTrf {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;
        @XmlElement(name = "CdtTrfTxInf")
        private CdtTrfTxInf cdtTrfTxInf;
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
        @XmlElement(name = "BtchBookg")
        private boolean btchBookg;
        @XmlElement(name = "NbOfTxs")
        private int nbOfTxs;
        @XmlElement(name = "SttlmInf")
        private SttlmInf sttlmInf;
        @XmlElement(name = "InstgAgt")
        private Agt instgAgt;
        @XmlElement(name = "InstdAgt")
        private Agt instdAgt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SttlmInf {
        @XmlElement(name = "SttlmMtd")
        private String sttlmMtd;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Agt {
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
    public static class CdtTrfTxInf {
        @XmlElement(name = "PmtId")
        private PmtId pmtId;
        @XmlElement(name = "PmtTpInf")
        private PmtTpInf pmtTpInf;
        @XmlElement(name = "IntrBkSttlmAmt")
        private IntrBkSttlmAmt intrBkSttlmAmt;
        @XmlElement(name = "IntrBkSttlmDt")
        private String intrBkSttlmDt;
        @XmlElement(name = "ChrgBr")
        private String chrgBr;
        @XmlElement(name = "InstgAgt")
        private Agt instgAgt;
        @XmlElement(name = "InstdAgt")
        private Agt instdAgt;
        @XmlElement(name = "Dbtr")
        private Dbtr dbtr;
        @XmlElement(name = "DbtrAcct")
        private DbtrAcct dbtrAcct;
        @XmlElement(name = "DbtrAgt")
        private Agt dbtrAgt;
        @XmlElement(name = "CdtrAgt")
        private Agt cdtrAgt;
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
        @XmlElement(name = "InstrId")
        private String instrId;
        @XmlElement(name = "EndToEndId")
        private String endToEndId;
        @XmlElement(name = "TxId")
        private String txId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PmtTpInf {
        @XmlElement(name = "ClrChanl")
        private String clrChanl;
        @XmlElement(name = "SvcLvl")
        private SvcLvl svcLvl;
        @XmlElement(name = "LclInstrm")
        private LclInstrm lclInstrm;
        @XmlElement(name = "CtgyPurp")
        private CtgyPurp ctgyPurp;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SvcLvl {
        @XmlElement(name = "Prtry")
        private String prtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class LclInstrm {
        @XmlElement(name = "Prtry")
        private String prtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CtgyPurp {
        @XmlElement(name = "Prtry")
        private String prtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class IntrBkSttlmAmt {
        @XmlAttribute(name = "Ccy")
        private String ccy;
        @XmlValue
        private BigDecimal value;
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
    public static class AcctId {
        @XmlElement(name = "IBAN")
        private String iban;
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
    public static class RmtInf {
        @XmlElement(name = "Ustrd")
        private String ustrd;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SplmtryData {
        @XmlElement(name = "PlcAndNm")
        private String plcAndNm;
        @XmlElement(name = "TestVariable")
        private String testVariable;
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
        @XmlElement(name = "NameEnquiryMsgId")
        private String nameEnquiryMsgId;
        @XmlElement(name = "ChannelCode")
        private String channelCode;
        @XmlElement(name = "RiskRating")
        private String riskRating;
    }
}