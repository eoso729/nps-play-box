package org.example.signer.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pain.002.001.12")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class CustomerPaymentStatusReport {

    @XmlElement(name = "CstmrPmtStsRpt")
    private CstmrPmtStsRpt cstmrPmtStsRpt;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CstmrPmtStsRpt {
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

        @XmlElement(name = "DbtrAgt")
        private Agent dbtrAgt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Party {
        @XmlElement(name = "Nm")
        private String nm;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlGrpInfAndSts {
        @XmlElement(name = "OrgnlMsgId")
        private String orgnlMsgId;

        @XmlElement(name = "OrgnlMsgNmId")
        private String orgnlMsgNmId;

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
        @XmlElement(name = "StsId")
        private String stsId;

        @XmlElement(name = "OrgnlEndToEndId")
        private String orgnlEndToEndId;

        @XmlElement(name = "TxSts")
        private String txSts;

        @XmlElement(name = "StsRsnInf")
        private StsRsnInf stsRsnInf;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class StsRsnInf {
        @XmlElement(name = "Rsn")
        private Rsn rsn;

        @XmlElement(name = "AddtlInf")
        private String addtlInf;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Rsn {
        @XmlElement(name = "Cd")
        private String cd;
    }
}
