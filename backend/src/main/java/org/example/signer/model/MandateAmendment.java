package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.util.List;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pain.010.001.08")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class MandateAmendment {

    @XmlElement(name = "MndtAmdmntReq")
    private MndtAmdmntReq mndtAmdmntReq;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class MndtAmdmntReq {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;
        @XmlElement(name = "UndrlygAmdmntDtls")
        private List<UndrlygAmdmntDtls> undrlygAmdmntDtls;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class GrpHdr {
        @XmlElement(name = "MsgId")
        private String msgId;
        @XmlElement(name = "CreDtTm")
        private String creDtTm;
        @XmlElement(name = "InitgPty")
        private InitgPty initgPty;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class InitgPty {
        @XmlElement(name = "Nm")
        private String nm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class UndrlygAmdmntDtls {
        @XmlElement(name = "OrgnlMsgInf")
        private OrgnlMsgInf orgnlMsgInf;
        @XmlElement(name = "AmdmntRsn")
        private AmdmntRsn amdmntRsn;
        @XmlElement(name = "Mndt")
        private Mndt mndt;
        @XmlElement(name = "OrgnlMndt")
        private OrgnlMndt orgnlMndt;
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
    public static class AmdmntRsn {
        @XmlElement(name = "Rsn")
        private RsnDetail rsn;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class RsnDetail {
        @XmlElement(name = "Cd")
        private String cd;
        @XmlElement(name = "Prtry")
        private String prtry;
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
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlMndt {
        @XmlElement(name = "OrgnlMndtId")
        private String orgnlMndtId;
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
}
