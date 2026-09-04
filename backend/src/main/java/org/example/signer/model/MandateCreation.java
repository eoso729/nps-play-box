package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pain.009.001.08")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MandateCreation {

    @XmlElement(name = "MndtInitnReq")
    private MndtInitnReq mndtInitnReq;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class MndtInitnReq {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;
        @XmlElement(name = "Mndt")
        private Mndt mndt;
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
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Mndt {
        @XmlElement(name = "MndtId")
        private String mndtId;
        @XmlElement(name = "Ocrncs")
        private Ocrncs ocrncs;
        @XmlElement(name = "TrckgInd")
        private Boolean trckgInd;
        @XmlElement(name = "ColltnAmt")
        private ColltnAmt colltnAmt;
        @XmlElement(name = "Cdtr")
        private Cdtr cdtr;
        @XmlElement(name = "CdtrAcct")
        private CdtrAcct cdtrAcct;
        @XmlElement(name = "CdtrAgt")
        private CdtrAgt cdtrAgt;
        @XmlElement(name = "Dbtr")
        private Dbtr dbtr;
        @XmlElement(name = "DbtrAcct")
        private DbtrAcct dbtrAcct;
        @XmlElement(name = "DbtrAgt")
        private DbtrAgt dbtrAgt;
        @XmlElement(name = "RfrdDoc")
        private RfrdDoc rfrdDoc;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Ocrncs {
        @XmlElement(name = "SeqTp")
        private String seqTp;
        @XmlElement(name = "Frqcy")
        private Frqcy frqcy;
        @XmlElement(name = "FrstColltnDt")
        private String frstColltnDt;
        @XmlElement(name = "FnlColltnDt")
        private String fnlColltnDt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Frqcy {
        @XmlElement(name = "Tp")
        private String tp;
    }

    public static class BigDecimal2DecimalAdapter extends XmlAdapter<String, BigDecimal> {
        @Override
        public BigDecimal unmarshal(String v) {
            return (v != null && !v.trim().isEmpty()) ? new BigDecimal(v.trim()) : null;
        }

        @Override
        public String marshal(BigDecimal v) {
            return v != null ? v.setScale(2, RoundingMode.HALF_UP).toPlainString() : null;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class ColltnAmt {
        @XmlAttribute(name = "Ccy")
        private String ccy;
        @XmlValue
        @XmlJavaTypeAdapter(BigDecimal2DecimalAdapter.class)
        private BigDecimal value;
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
    public static class CdtrAgt {
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
    public static class RfrdDoc {
        @XmlElement(name = "Tp")
        private Tp tp;
        @XmlElement(name = "Nb")
        private String nb;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Tp {
        @XmlElement(name = "CdOrPrtry")
        private CdOrPrtry cdOrPrtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdOrPrtry {
        @XmlElement(name = "Cd")
        private String cd;
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
        @XmlElement(name = "FixedCollectionAmount")
        private Boolean fixedCollectionAmount;
    }
}
