package org.example.signer.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pain.012.001.08")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MandateAcceptanceReport {

    @XmlElement(name = "MndtAccptncRpt")
    private MndtAccptncRpt mndtAccptncRpt;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class MndtAccptncRpt {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;

        @XmlElement(name = "UndrlygAccptncDtls")
        private UndrlygAccptncDtls undrlygAccptncDtls;
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
    public static class UndrlygAccptncDtls {
        @XmlElement(name = "OrgnlMsgInf")
        private OrgnlMsgInf orgnlMsgInf;

        @XmlElement(name = "AccptncRslt")
        private AccptncRslt accptncRslt;

        @XmlElement(name = "OrgnlMndt")
        private OrgnlMndtOuter orgnlMndt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlMsgInf {
        @XmlElement(name = "MsgId")
        private String msgId;

        @XmlElement(name = "MsgNmId")
        private String msgNmId;

        @XmlElement(name = "CreDtTm")
        private String creDtTm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class AccptncRslt {
        @XmlElement(name = "Accptd")
        private String accptd;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlMndtOuter {
        @XmlElement(name = "OrgnlMndtId")
        private String orgnlMndtId;

        @XmlElement(name = "OrgnlMndt")
        private OrgnlMndtInner orgnlMndt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlMndtInner {
        @XmlElement(name = "Ocrncs")
        private Ocrncs ocrncs;

        @XmlElement(name = "TrckgInd")
        private String trckgInd;

        @XmlElement(name = "Cdtr")
        private Party cdtr;

        @XmlElement(name = "CdtrAcct")
        private CashAccount cdtrAcct;

        @XmlElement(name = "CdtrAgt")
        private Agent cdtrAgt;

        @XmlElement(name = "Dbtr")
        private Party dbtr;

        @XmlElement(name = "DbtrAcct")
        private CashAccount dbtrAcct;

        @XmlElement(name = "DbtrAgt")
        private Agent dbtrAgt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Ocrncs {
        @XmlElement(name = "SeqTp")
        private String seqTp;

        @XmlElement(name = "Frqcy")
        private Frequency frqcy;

        @XmlElement(name = "FrstColltnDt")
        private String frstColltnDt;

        @XmlElement(name = "FnlColltnDt")
        private String fnlColltnDt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Frequency {
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
}
