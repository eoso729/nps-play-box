package org.example.signer.xml;

import org.example.signer.dto.MandateAcceptanceReportDto;
import org.example.signer.model.MandateAcceptanceReport;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MandateAcceptanceXmlGenerator {

    public static MandateAcceptanceReport generate(MandateAcceptanceReportDto requestDto, String msgId) {
        MandateAcceptanceReport doc = new MandateAcceptanceReport();
        MandateAcceptanceReport.MndtAccptncRpt rpt = new MandateAcceptanceReport.MndtAccptncRpt();

        // --- Group Header ---
        MandateAcceptanceReport.GrpHdr grpHdr = new MandateAcceptanceReport.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = ZonedDateTime.now(ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);
        rpt.setGrpHdr(grpHdr);

        // --- Underlying Acceptance Details ---
        MandateAcceptanceReport.UndrlygAccptncDtls details = new MandateAcceptanceReport.UndrlygAccptncDtls();

        // Original Message Info
        MandateAcceptanceReport.OrgnlMsgInf orgnlMsgInf = new MandateAcceptanceReport.OrgnlMsgInf();
        orgnlMsgInf.setMsgId(requestDto.getOriginalMsgId() != null ? requestDto.getOriginalMsgId() : "99905820251211112346125578725905163");
        orgnlMsgInf.setMsgNmId(requestDto.getOriginalMsgNmId() != null ? requestDto.getOriginalMsgNmId() : "pain.009.001.08");
        orgnlMsgInf.setCreDtTm(requestDto.getOriginalCreDtTm() != null ? requestDto.getOriginalCreDtTm() : "2025-12-11T16:19:15.342Z");
        details.setOrgnlMsgInf(orgnlMsgInf);

        // Acceptance Result
        MandateAcceptanceReport.AccptncRslt accptncRslt = new MandateAcceptanceReport.AccptncRslt();
        accptncRslt.setAccptd(requestDto.getAccepted() != null ? requestDto.getAccepted() : "true");
        details.setAccptncRslt(accptncRslt);

        // Mandate Info
        MandateAcceptanceReport.OrgnlMndtOuter outer = new MandateAcceptanceReport.OrgnlMndtOuter();
        outer.setOrgnlMndtId(requestDto.getOriginalMandateId() != null ? requestDto.getOriginalMandateId() : "MNDT-RCUR-00001");

        MandateAcceptanceReport.OrgnlMndtInner inner = new MandateAcceptanceReport.OrgnlMndtInner();
        
        // Occurrences
        MandateAcceptanceReport.Ocrncs ocrncs = new MandateAcceptanceReport.Ocrncs();
        ocrncs.setSeqTp(requestDto.getSequenceType() != null ? requestDto.getSequenceType() : "RCUR");
        MandateAcceptanceReport.Frequency frqcy = new MandateAcceptanceReport.Frequency();
        frqcy.setTp(requestDto.getFrequencyType() != null ? requestDto.getFrequencyType() : "WEEK");
        ocrncs.setFrqcy(frqcy);
        ocrncs.setFrstColltnDt(requestDto.getFirstCollectionDate() != null ? requestDto.getFirstCollectionDate() : "2025-09-08");
        ocrncs.setFnlColltnDt(requestDto.getFinalCollectionDate() != null ? requestDto.getFinalCollectionDate() : "2025-12-31");
        inner.setOcrncs(ocrncs);

        inner.setTrckgInd(requestDto.getTrackingIndicator() != null ? requestDto.getTrackingIndicator() : "false");

        // Creditor
        MandateAcceptanceReport.Party cdtr = new MandateAcceptanceReport.Party();
        cdtr.setNm(requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "CreditorCorp");
        inner.setCdtr(cdtr);

        MandateAcceptanceReport.CashAccount cdtrAcct = new MandateAcceptanceReport.CashAccount();
        MandateAcceptanceReport.AccountId cdtrAccId = new MandateAcceptanceReport.AccountId();
        cdtrAccId.setIban(requestDto.getCreditorAccountNumber() != null ? requestDto.getCreditorAccountNumber() : "5555544443");
        cdtrAcct.setId(cdtrAccId);
        cdtrAcct.setNm(requestDto.getCreditorAccountName() != null ? requestDto.getCreditorAccountName() : cdtr.getNm());
        inner.setCdtrAcct(cdtrAcct);

        MandateAcceptanceReport.Agent cdtrAgt = new MandateAcceptanceReport.Agent();
        MandateAcceptanceReport.FinInstnId cdtrFinInstnId = new MandateAcceptanceReport.FinInstnId();
        String cdtrMmbId = requestDto.getCreditorAgentMemberId() != null ? requestDto.getCreditorAgentMemberId() : "999058";
        cdtrFinInstnId.setBicfi(requestDto.getCreditorAgentBIC() != null ? requestDto.getCreditorAgentBIC() : cdtrMmbId);
        MandateAcceptanceReport.ClrSysMmbId cdtrClrSysMmbId = new MandateAcceptanceReport.ClrSysMmbId();
        cdtrClrSysMmbId.setMmbId(cdtrMmbId);
        cdtrFinInstnId.setClrSysMmbId(cdtrClrSysMmbId);
        cdtrAgt.setFinInstnId(cdtrFinInstnId);
        inner.setCdtrAgt(cdtrAgt);

        // Debtor
        MandateAcceptanceReport.Party dbtr = new MandateAcceptanceReport.Party();
        dbtr.setNm(requestDto.getDebtorName() != null ? requestDto.getDebtorName() : "Debtor Customer");
        inner.setDbtr(dbtr);

        MandateAcceptanceReport.CashAccount dbtrAcct = new MandateAcceptanceReport.CashAccount();
        MandateAcceptanceReport.AccountId dbtrAccId = new MandateAcceptanceReport.AccountId();
        dbtrAccId.setIban(requestDto.getDebtorAccountNumber() != null ? requestDto.getDebtorAccountNumber() : "8888899999");
        dbtrAcct.setId(dbtrAccId);
        dbtrAcct.setNm(requestDto.getDebtorAccountName() != null ? requestDto.getDebtorAccountName() : dbtr.getNm());
        inner.setDbtrAcct(dbtrAcct);

        MandateAcceptanceReport.Agent dbtrAgt = new MandateAcceptanceReport.Agent();
        MandateAcceptanceReport.FinInstnId dbtrFinInstnId = new MandateAcceptanceReport.FinInstnId();
        String dbtrMmbId = requestDto.getDebtorAgentMemberId() != null ? requestDto.getDebtorAgentMemberId() : "999057";
        dbtrFinInstnId.setBicfi(requestDto.getDebtorAgentBIC() != null ? requestDto.getDebtorAgentBIC() : dbtrMmbId);
        MandateAcceptanceReport.ClrSysMmbId dbtrClrSysMmbId = new MandateAcceptanceReport.ClrSysMmbId();
        dbtrClrSysMmbId.setMmbId(dbtrMmbId);
        dbtrFinInstnId.setClrSysMmbId(dbtrClrSysMmbId);
        dbtrAgt.setFinInstnId(dbtrFinInstnId);
        inner.setDbtrAgt(dbtrAgt);

        outer.setOrgnlMndt(inner);
        details.setOrgnlMndt(outer);
        rpt.setUndrlygAccptncDtls(details);

        doc.setMndtAccptncRpt(rpt);
        return doc;
    }
}
