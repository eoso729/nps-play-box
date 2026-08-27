package org.example.signer.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.028.001.06")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class PaymentStatusRequest {

    @XmlElement(name = "FIToFIPmtStsReq")
    private FIToFIPmtStsReq fiToFIPmtStsReq;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class FIToFIPmtStsReq {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;

        @XmlElement(name = "OrgnlGrpInf")
        private OrgnlGrpInf orgnlGrpInf;

        @XmlElement(name = "TxInf")
        private TxInf txInf;
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
    }

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

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class TxInf {
        @XmlElement(name = "StsReqId")
        private String stsReqId;

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
    public static class OrgnlTxRef {
        @XmlElement(name = "IntrBkSttlmDt")
        private String intrBkSttlmDt;
    }
}
