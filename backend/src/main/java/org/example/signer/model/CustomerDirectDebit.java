package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.003.001.11")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class CustomerDirectDebit {

    @XmlElement(name = "FIToFICstmrDrctDbt")
    private FIToFICstmrDrctDbt fiToFICstmrDrctDbt;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class FIToFICstmrDrctDbt {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;
        @XmlElement(name = "DrctDbtTxInf")
        private DrctDbtTxInf drctDbtTxInf;
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
        @XmlElement(name = "InstgAgt")
        private Agent instgAgt;
        @XmlElement(name = "InstdAgt")
        private Agent instdAgt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class DrctDbtTxInf {
        @XmlElement(name = "PmtId")
        private PmtId pmtId;
        @XmlElement(name = "IntrBkSttlmAmt")
        private Amount intrBkSttlmAmt;
        @XmlElement(name = "IntrBkSttlmDt")
        private String intrBkSttlmDt;
        @XmlElement(name = "InstdAmt")
        private Amount instdAmt;
        @XmlElement(name = "DrctDbtTx")
        private DrctDbtTx drctDbtTx;
        @XmlElement(name = "Cdtr")
        private Party cdtr;
        @XmlElement(name = "CdtrAcct")
        private CashAccount cdtrAcct;
        @XmlElement(name = "CdtrAgt")
        private Agent cdtrAgt;
        @XmlElement(name = "InstgAgt")
        private Agent instgAgt;
        @XmlElement(name = "InstdAgt")
        private Agent instdAgt;
        @XmlElement(name = "Dbtr")
        private Party dbtr;
        @XmlElement(name = "DbtrAcct")
        private CashAccount dbtrAcct;
        @XmlElement(name = "DbtrAgt")
        private Agent dbtrAgt;
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
    public static class Amount {
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
    public static class Party {
        @XmlElement(name = "Nm")
        private String nm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CashAccount {
        @XmlElement(name = "Id")
        private AccountId id;
        @XmlElement(name = "Nm")
        private String nm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class AccountId {
        @XmlElement(name = "IBAN")
        private String iban;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Agent {
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
        private String creditorMetadata; // Blank in sample
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
    public static class TransactionInfo {
        @XmlElement(name = "TransactionLocation")
        private String transactionLocation;
        @XmlElement(name = "NameEnquiryMsgId")
        private String nameEnquiryMsgId;
        @XmlElement(name = "ChannelCode")
        private String channelCode;
        @XmlElement(name = "RiskRating")
        private String riskRating;
        @XmlElement(name = "FixedCollectionAmount")
        private String fixedCollectionAmount;
    }
}
