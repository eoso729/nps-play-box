package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.004.001.13")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class PaymentReturn {

    @XmlElement(name = "PmtRtr")
    private PmtRtr pmtRtr;

    // ─── Root ────────────────────────────────────────────────────────────────

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PmtRtr {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;
        @XmlElement(name = "OrgnlGrpInf")
        private OrgnlGrpInf orgnlGrpInf;
        @XmlElement(name = "TxInf")
        private TxInf txInf;
        @XmlElement(name = "SplmtryData")
        private SplmtryData splmtryData;
    }

    // ─── Group Header ────────────────────────────────────────────────────────

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class GrpHdr {
        @XmlElement(name = "MsgId")
        private String msgId;
        @XmlElement(name = "CreDtTm")
        private String creDtTm;
        @XmlElement(name = "SttlmInf")
        private SttlmInf sttlmInf;
        @XmlElement(name = "InstgAgt")
        private InstgAgt instgAgt;
        @XmlElement(name = "InstdAgt")
        private InstdAgt instdAgt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SttlmInf {
        @XmlElement(name = "SttlmMtd")
        private String sttlmMtd;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class InstgAgt {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class InstdAgt {
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

    // ─── Original Group Info ─────────────────────────────────────────────────

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlGrpInf {
        @XmlElement(name = "OrgnlMsgId")
        private String orgnlMsgId;
        @XmlElement(name = "OrgnlMsgNmId")
        private String orgnlMsgNmId;
        @XmlElement(name = "OrgnlCreDtTm")
        private String orgnlCreDtTm;
    }

    // ─── Transaction Information ──────────────────────────────────────────────

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class TxInf {
        @XmlElement(name = "RtrId")
        private String rtrId;
        @XmlElement(name = "OrgnlInstrId")
        private String orgnlInstrId;
        @XmlElement(name = "OrgnlEndToEndId")
        private String orgnlEndToEndId;
        @XmlElement(name = "OrgnlTxId")
        private String orgnlTxId;
        @XmlElement(name = "OrgnlIntrBkSttlmDt")
        private String orgnlIntrBkSttlmDt;
        @XmlElement(name = "RtrdIntrBkSttlmAmt")
        private RtrdIntrBkSttlmAmt rtrdIntrBkSttlmAmt;
        @XmlElement(name = "IntrBkSttlmDt")
        private String intrBkSttlmDt;
        @XmlElement(name = "ChrgBr")
        private String chrgBr;
        @XmlElement(name = "InstgAgt")
        private InstgAgt instgAgt;
        @XmlElement(name = "InstdAgt")
        private InstdAgt instdAgt;
        @XmlElement(name = "RtrRsnInf")
        private RtrRsnInf rtrRsnInf;
        @XmlElement(name = "OrgnlTxRef")
        private OrgnlTxRef orgnlTxRef;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class RtrdIntrBkSttlmAmt {
        @XmlAttribute(name = "Ccy")
        private String ccy;
        @XmlValue
        private BigDecimal value;
    }

    // ─── Return Reason ────────────────────────────────────────────────────────

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class RtrRsnInf {
        @XmlElement(name = "Rsn")
        private Rsn rsn;
        @XmlElement(name = "AddtlInf")
        private String addtlInf;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Rsn {
        @XmlElement(name = "Prtry")
        private String prtry;
    }

    // ─── Original Transaction Reference ──────────────────────────────────────

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlTxRef {
        @XmlElement(name = "IntrBkSttlmAmt")
        private String intrBkSttlmAmt;
        @XmlElement(name = "PmtTpInf")
        private PmtTpInf pmtTpInf;
        @XmlElement(name = "Dbtr")
        private PartyChoice dbtr;
        @XmlElement(name = "DbtrAcct")
        private DbtrAcct dbtrAcct;
        @XmlElement(name = "DbtrAgt")
        private DbtrAgt dbtrAgt;
        @XmlElement(name = "CdtrAgt")
        private CdtrAgt cdtrAgt;
        @XmlElement(name = "Cdtr")
        private PartyChoice cdtr;
        @XmlElement(name = "CdtrAcct")
        private CdtrAcct cdtrAcct;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PmtTpInf {
        @XmlElement(name = "ClrChanl")
        private String clrChanl;
        @XmlElement(name = "LclInstrm")
        private LclInstrm lclInstrm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class LclInstrm {
        @XmlElement(name = "Prtry")
        private String prtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PartyChoice {
        @XmlElement(name = "Pty")
        private Pty pty;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Pty {
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
    public static class CdtrAcct {
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
    public static class DbtrAgt {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdtrAgt {
        @XmlElement(name = "FinInstnId")
        private FinInstnId finInstnId;
    }

    // ─── Supplementary Data ───────────────────────────────────────────────────

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SplmtryData {
        @XmlElement(name = "PlcAndNm")
        private String plcAndNm;
    }
}
