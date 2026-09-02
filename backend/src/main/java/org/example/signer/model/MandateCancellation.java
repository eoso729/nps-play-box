package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pain.011.001.08")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MandateCancellation {

    @XmlElement(name = "MndtCxlReq")
    private MndtCxlReq mndtCxlReq;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class MndtCxlReq {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;
        @XmlElement(name = "UndrlygCxlDtls")
        private UndrlygCxlDtls undrlygCxlDtls;
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
    public static class UndrlygCxlDtls {
        @XmlElement(name = "OrgnlMsgInf")
        private OrgnlMsgInf orgnlMsgInf;
        @XmlElement(name = "CxlRsn")
        private CxlRsn cxlRsn;
        @XmlElement(name = "OrgnlMndt")
        private OrgnlMndtDetails orgnlMndt;
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
    public static class CxlRsn {
        @XmlElement(name = "Rsn")
        private Rsn rsn;
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
    public static class OrgnlMndtDetails {
        @XmlElement(name = "OrgnlMndtId")
        private String orgnlMndtId;

        @XmlElement(name = "OrgnlMndt")
        private InnerOrgnlMndt orgnlMndt;

        // Convenience getters for backward compatibility
        public Party getDbtr() { return orgnlMndt != null ? orgnlMndt.getDbtr() : null; }
        public CashAccount getDbtrAcct() { return orgnlMndt != null ? orgnlMndt.getDbtrAcct() : null; }
        public Agent getDbtrAgt() { return orgnlMndt != null ? orgnlMndt.getDbtrAgt() : null; }
        public Agent getCdtrAgt() { return orgnlMndt != null ? orgnlMndt.getCdtrAgt() : null; }
        public Party getCdtr() { return orgnlMndt != null ? orgnlMndt.getCdtr() : null; }
        public CashAccount getCdtrAcct() { return orgnlMndt != null ? orgnlMndt.getCdtrAcct() : null; }
        public Ocrncs getOcrncs() { return orgnlMndt != null ? orgnlMndt.getOcrncs() : null; }
        public String getTrckgInd() { return orgnlMndt != null ? orgnlMndt.getTrckgInd() : null; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class InnerOrgnlMndt {
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
