package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:acmt.023.001.03")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class NameVerification {

    @XmlElement(name = "IdVrfctnReq")
    private IdVrfctnReq idVrfctnReq;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class IdVrfctnReq {

        @XmlElement(name = "Assgnmt")
        private Assgnmt assgnmt;

        @XmlElement(name = "Vrfctn")
        private Vrfctn vrfctn;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Assgnmt {

        @XmlElement(name = "MsgId")
        private String msgId;

        @XmlElement(name = "CreDtTm")
        private String creDtTm;

        @XmlElement(name = "Cretr")
        private Cretr cretr;

        @XmlElement(name = "Assgnr")
        private Assgnr assgnr;

        @XmlElement(name = "Assgne")
        private Assgne assgne;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Cretr {

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
    public static class Assgnr {

        @XmlElement(name = "Pty")
        private Pty pty;

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

        @XmlElement(name = "Agt")
        private Agt agt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Vrfctn {

        @XmlElement(name = "Id")
        private String id;

        @XmlElement(name = "PtyAndAcctId")
        private PtyAndAcctId ptyAndAcctId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class PtyAndAcctId {

        @XmlElement(name = "Pty")
        private Pty pty;

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
}
