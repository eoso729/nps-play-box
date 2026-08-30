package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.002.001.12")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class TransferResponse {

    @XmlElement(name = "FIToFIPmtStsRpt")
    private FIToFIPmtStsRpt fiToFIPmtStsRpt;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class FIToFIPmtStsRpt {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;
        @XmlElement(name = "OrgnlGrpInfAndSts")
        private OrgnlGrpInfAndSts orgnlGrpInfAndSts;
        @XmlElement(name = "TxInfAndSts")
        private TxInfAndSts txInfAndSts;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class GrpHdr {
        @XmlElement(name = "MsgId")
        private String msgId;
        @XmlElement(name = "CreDtTm")
        private String creDtTm;
        @XmlElement(name = "InstgAgt")
        private Agt instgAgt;
        @XmlElement(name = "InstdAgt")
        private Agt instdAgt;
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
    public static class TxInfAndSts {
        @XmlElement(name = "StsId")
        private String stsId;
        @XmlElement(name = "OrgnlInstrId")
        private String orgnlInstrId;
        @XmlElement(name = "OrgnlEndToEndId")
        private String orgnlEndToEndId;
        @XmlElement(name = "OrgnlTxId")
        private String orgnlTxId;
        @XmlElement(name = "InstgAgt")
        private Agt instgAgt;
        @XmlElement(name = "InstdAgt")
        private Agt instdAgt;
        @XmlElement(name = "OrgnlTxRef")
        private OrgnlTxRef orgnlTxRef;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlTxRef {
        @XmlElement(name = "IntrBkSttlmDt")
        private String intrBkSttlmDt;
    }
}
