package org.example.signer.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pain.014.001.11")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class PaymentActivationStatusReport {

    @XmlElement(name = "CdtrPmtActvtnReqStsRpt")
    private CdtrPmtActvtnReqStsRpt cdtrPmtActvtnReqStsRpt;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdtrPmtActvtnReqStsRpt {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;

        @XmlElement(name = "OrgnlGrpInfAndSts")
        private OrgnlGrpInfAndSts orgnlGrpInfAndSts;

        @XmlElement(name = "OrgnlPmtInfAndSts")
        private OrgnlPmtInfAndSts orgnlPmtInfAndSts;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class GrpHdr {
        @XmlElement(name = "MsgId")
        private String msgId;

        @XmlElement(name = "CreDtTm")
        private String creDtTm;

        @XmlElement(name = "InitgPty")
        private Party initgPty;

        @XmlElement(name = "Cdtr")
        private Party cdtr;

        @XmlElement(name = "CdtrAcct")
        private CashAccount cdtrAcct;

        @XmlElement(name = "Dbtr")
        private Party dbtr;

        @XmlElement(name = "DbtrAcct")
        private CashAccount dbtrAcct;

        @XmlElement(name = "FwdgAgt")
        private Agent fwdgAgt;

        @XmlElement(name = "DbtrAgt")
        private Agent dbtrAgt;

        @XmlElement(name = "CdtrAgt")
        private Agent cdtrAgt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlGrpInfAndSts {
        @XmlElement(name = "OrgnlMsgId")
        private String orgnlMsgId;

        @XmlElement(name = "OrgnlMsgNmId")
        private String orgnlMsgNmId;

        @XmlElement(name = "OrgnlCreDtTm")
        private String orgnlCreDtTm;

        @XmlElement(name = "GrpSts")
        private String grpSts;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlPmtInfAndSts {
        @XmlElement(name = "OrgnlPmtInfId")
        private String orgnlPmtInfId;

        @XmlElement(name = "TxInfAndSts")
        private TxInfAndSts txInfAndSts;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class TxInfAndSts {
        @XmlElement(name = "OrgnlEndToEndId")
        private String orgnlEndToEndId;

        @XmlElement(name = "TxSts")
        private String txSts;
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
}
