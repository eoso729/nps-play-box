package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:acmt.024.001.04")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class NameVerificationReport {

    @XmlElement(name = "IdVrfctnRpt")
    private IdVrfctnRpt idVrfctnRpt;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class IdVrfctnRpt {
        @XmlElement(name = "Assgnmt")
        private Assgnmt assgnmt;
        @XmlElement(name = "OrgnlAssgnmt")
        private OrgnlAssgnmt orgnlAssgnmt;
        @XmlElement(name = "Rpt")
        private Rpt rpt;
        @XmlElement(name = "SplmtryData")
        private SplmtryData splmtryData;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Assgnmt {
        @XmlElement(name = "MsgId")
        private String msgId;
        @XmlElement(name = "CreDtTm")
        private String creDtTm;
        @XmlElement(name = "Assgnr")
        private Assgnr assgnr;
        @XmlElement(name = "Assgne")
        private Assgne assgne;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Assgnr {
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
    public static class Assgne {
        @XmlElement(name = "Pty")
        private Pty pty;
        @XmlElement(name = "Agt")
        private Agt agt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Pty {
        @XmlElement(name = "Nm")
        private String nm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlAssgnmt {
        @XmlElement(name = "MsgId")
        private String msgId;
        @XmlElement(name = "CreDtTm")
        private String creDtTm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Rpt {
        @XmlElement(name = "OrgnlId")
        private String orgnlId;
        @XmlElement(name = "Vrfctn")
        private boolean vrfctn;
        @XmlElement(name = "Rsn")
        private Rsn rsn;
        @XmlElement(name = "OrgnlPtyAndAcctId")
        private OrgnlPtyAndAcctId orgnlPtyAndAcctId;
        @XmlElement(name = "UpdtdPtyAndAcctId")
        private UpdtdPtyAndAcctId updtdPtyAndAcctId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Rsn {
        @XmlElement(name = "Cd")
        private String cd;
        @XmlElement(name = "Prtry")
        private String prtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlPtyAndAcctId {
        @XmlElement(name = "Acct")
        private Acct acct;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Acct {
        @XmlElement(name = "Id")
        private AcctId id;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class AcctId {
        @XmlElement(name = "IBAN")
        private String iban;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class UpdtdPtyAndAcctId {
        @XmlElement(name = "Pty")
        private Pty pty;
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
        @XmlElement(name = "RiskRating")
        private String riskRating;
    }
}
