package org.example.signer.model;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:camt.052.001.12")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class BankAccountReport {

    @XmlElement(name = "BkToCstmrAcctRpt")
    private BkToCstmrAcctRpt bkToCstmrAcctRpt;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class BkToCstmrAcctRpt {
        @XmlElement(name = "GrpHdr")
        private GrpHdr grpHdr;

        @XmlElement(name = "Rpt")
        private Rpt rpt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class GrpHdr {
        @XmlElement(name = "MsgId")
        private String msgId;

        @XmlElement(name = "CreDtTm")
        private String creDtTm;

        @XmlElement(name = "MsgRcpt")
        private MsgRcpt msgRcpt;

        @XmlElement(name = "OrgnlBizQry")
        private OrgnlBizQry orgnlBizQry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class MsgRcpt {
        @XmlElement(name = "Nm")
        private String nm;

        @XmlElement(name = "Id")
        private MsgRcptId id;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class MsgRcptId {
        @XmlElement(name = "OrgId")
        private OrgId orgId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgId {
        @XmlElement(name = "AnyBIC")
        private String anyBIC;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OrgnlBizQry {
        @XmlElement(name = "MsgId")
        private String msgId;

        @XmlElement(name = "MsgNmId")
        private String msgNmId;

        @XmlElement(name = "CreDtTm")
        private String creDtTm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Rpt {
        @XmlElement(name = "Id")
        private String id;

        @XmlElement(name = "FrToDt")
        private FrToDt frToDt;

        @XmlElement(name = "Acct")
        private Acct acct;

        @XmlElement(name = "Bal")
        private Bal bal;

        @XmlElement(name = "Ntry")
        private List<Ntry> ntry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class FrToDt {
        @XmlElement(name = "FrDtTm")
        private String frDtTm;

        @XmlElement(name = "ToDtTm")
        private String toDtTm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Acct {
        @XmlElement(name = "Id")
        private AcctId id;

        @XmlElement(name = "Ccy")
        private String ccy;

        @XmlElement(name = "Ownr")
        private Ownr ownr;

        @XmlElement(name = "Svcr")
        private Svcr svcr;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class AcctId {
        @XmlElement(name = "IBAN")
        private String iban;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Ownr {
        @XmlElement(name = "Id")
        private OwnrId id;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OwnrId {
        @XmlElement(name = "OrgId")
        private OwnrOrgId orgId;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class OwnrOrgId {
        @XmlElement(name = "Othr")
        private Othr othr;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Othr {
        @XmlElement(name = "SchmeNm")
        private SchmeNm schmeNm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class SchmeNm {
        @XmlElement(name = "Cd")
        private String cd;

        @XmlElement(name = "Prtry")
        private String prtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Svcr {
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
    public static class Bal {
        @XmlElement(name = "Tp")
        private BalTp tp;

        @XmlElement(name = "Amt")
        private Amount amt;

        @XmlElement(name = "CdtDbtInd")
        private String cdtDbtInd;

        @XmlElement(name = "Dt")
        private BalDt dt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class BalTp {
        @XmlElement(name = "CdOrPrtry")
        private CdOrPrtry cdOrPrtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class CdOrPrtry {
        @XmlElement(name = "Cd")
        private String cd;

        @XmlElement(name = "Prtry")
        private String prtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Amount {
        @XmlAttribute(name = "Ccy")
        private String ccy;

        @XmlValue
        private BigDecimal value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class BalDt {
        @XmlElement(name = "DtTm")
        private String dtTm;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Ntry {
        @XmlElement(name = "Amt")
        private Amount amt;

        @XmlElement(name = "CdtDbtInd")
        private String cdtDbtInd;

        @XmlElement(name = "Sts")
        private Sts sts;

        @XmlElement(name = "BookgDt")
        private EntryDt bookgDt;

        @XmlElement(name = "ValDt")
        private EntryDt valDt;

        @XmlElement(name = "AcctSvcrRef")
        private String acctSvcrRef;

        @XmlElement(name = "BkTxCd")
        private BkTxCd bkTxCd;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Sts {
        @XmlElement(name = "Prtry")
        private String prtry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class EntryDt {
        @XmlElement(name = "Dt")
        private String dt;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class BkTxCd {
        @XmlElement(name = "Domn")
        private Domn domn;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Domn {
        @XmlElement(name = "Cd")
        private String cd;

        @XmlElement(name = "Fmly")
        private Fmly fmly;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    public static class Fmly {
        @XmlElement(name = "Cd")
        private String cd;

        @XmlElement(name = "SubFmlyCd")
        private String subFmlyCd;
    }
}
