package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.060.001.07")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class BalanceEnquiry {

    @XmlElement(name = "AcctRptgReq")
    private AcctRptgReq acctRptgReq;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class AcctRptgReq {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;
        @XmlElement(name = "RptgReq")
        private RptgReq rptgReq;
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
        @XmlElement(name = "MsgSndr")
        private MsgSndr msgSndr;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class MsgSndr {
        @XmlElement(name = "Agt")
        private Agt agt;
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
    public static class RptgReq {
        @XmlElement(name = "Id")
        private String id;
        @XmlElement(name = "ReqdMsgNmId")
        private String reqdMsgNmId;
        @XmlElement(name = "Acct")
        private Acct acct;
        @XmlElement(name = "AcctOwnr")
        private AcctOwnr acctOwnr;
        @XmlElement(name = "AcctSvcr")
        private AcctSvcr acctSvcr;
        @XmlElement(name = "RptgPrd")
        private RptgPrd rptgPrd;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Acct {
        @XmlElement(name = "Id")
        private AcctId id;
        @XmlElement(name = "Ccy")
        private String ccy;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class AcctId {
        @XmlElement(name = "IBAN")
        private String iban;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class AcctOwnr {
        @XmlElement(name = "Agt")
        private Agt agt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class AcctSvcr {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class RptgPrd {
        @XmlElement(name = "FrToDt")
        private FrToDt frToDt;
        @XmlElement(name = "Tp")
        private String tp;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class FrToDt {
        @XmlElement(name = "FrDt")
        private String frDt;
        @XmlElement(name = "ToDt")
        private String toDt;
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
