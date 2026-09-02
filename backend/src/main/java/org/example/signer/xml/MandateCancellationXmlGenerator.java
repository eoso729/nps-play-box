package org.example.signer.xml;

import org.example.signer.dto.MandateCancellationRequestDto;
import org.example.signer.model.MandateCancellation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MandateCancellationXmlGenerator {

    public static MandateCancellation generate(MandateCancellationRequestDto requestDto, String msgId) {
        MandateCancellation doc = new MandateCancellation();
        MandateCancellation.MndtCxlReq req = new MandateCancellation.MndtCxlReq();

        // --- Group Header ---
        MandateCancellation.GrpHdr grpHdr = new MandateCancellation.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = java.time.ZonedDateTime.now(java.time.ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);
        req.setGrpHdr(grpHdr);

        // --- Underlying Cancellation Details ---
        MandateCancellation.UndrlygCxlDtls details = new MandateCancellation.UndrlygCxlDtls();

        // Original Message Info
        MandateCancellation.OrgnlMsgInf orgnlMsgInf = new MandateCancellation.OrgnlMsgInf();
        orgnlMsgInf.setMsgId(requestDto.getOriginalMsgId() != null ? requestDto.getOriginalMsgId() : "99999820260331160816119597368459797");
        orgnlMsgInf.setMsgNmId(requestDto.getOriginalMsgNmId() != null ? requestDto.getOriginalMsgNmId() : "pain.009.001.08");
        orgnlMsgInf.setCreDtTm(requestDto.getOriginalCreDtTm() != null ? requestDto.getOriginalCreDtTm() : "2026-03-31T16:08:16");
        details.setOrgnlMsgInf(orgnlMsgInf);

        // Cancellation Reason
        MandateCancellation.CxlRsn cxlRsn = new MandateCancellation.CxlRsn();
        MandateCancellation.Rsn rsn = new MandateCancellation.Rsn();
        rsn.setCd(requestDto.getCancellationReasonCode() != null ? requestDto.getCancellationReasonCode() : "AC04");
        rsn.setPrtry(requestDto.getCancellationReasonDescription() != null ? requestDto.getCancellationReasonDescription() : "Cancel");
        cxlRsn.setRsn(rsn);
        details.setCxlRsn(cxlRsn);

        // Original Mandate Details (Outer)
        MandateCancellation.OrgnlMndtDetails mndtDetails = new MandateCancellation.OrgnlMndtDetails();
        String mndtId = requestDto.getOriginalMandateId() != null ? requestDto.getOriginalMandateId() : "MNDT-RCUR-00061";
        mndtDetails.setOrgnlMndtId(mndtId);
        
        // Original Mandate Details (Inner)
        MandateCancellation.InnerOrgnlMndt innerMndt = new MandateCancellation.InnerOrgnlMndt();
        
        MandateCancellation.Ocrncs ocrncs = new MandateCancellation.Ocrncs();
        ocrncs.setSeqTp(requestDto.getSequenceType() != null ? requestDto.getSequenceType() : "RCUR");
        MandateCancellation.Frqcy frqcy = new MandateCancellation.Frqcy();
        frqcy.setTp(requestDto.getFrequencyType() != null ? requestDto.getFrequencyType() : "MNTH");
        ocrncs.setFrqcy(frqcy);
        ocrncs.setFrstColltnDt(requestDto.getFirstCollectionDate() != null ? requestDto.getFirstCollectionDate() : "2026-04-01");
        ocrncs.setFnlColltnDt(requestDto.getFinalCollectionDate() != null ? requestDto.getFinalCollectionDate() : "2026-04-30");
        innerMndt.setOcrncs(ocrncs);
        
        innerMndt.setTrckgInd(requestDto.getTrackingIndicator() != null ? requestDto.getTrackingIndicator() : "false");

        // Creditor Info
        MandateCancellation.Party cdtr = new MandateCancellation.Party();
        String cdrNm = requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "Tester";
        cdtr.setNm(cdrNm);
        innerMndt.setCdtr(cdtr);

        MandateCancellation.CashAccount cdtrAcct = new MandateCancellation.CashAccount();
        MandateCancellation.AccountId cdtrAcctId = new MandateCancellation.AccountId();
        cdtrAcctId.setIban(requestDto.getCreditorAccountNumber() != null ? requestDto.getCreditorAccountNumber() : "0987654320");
        cdtrAcct.setId(cdtrAcctId);
        String cdrAcctNm = requestDto.getCreditorAccountName() != null && !requestDto.getCreditorAccountName().trim().isEmpty()
                ? requestDto.getCreditorAccountName().trim() : cdrNm;
        cdtrAcct.setNm(cdrAcctNm);
        innerMndt.setCdtrAcct(cdtrAcct);

        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        String cdtrMmbId = requestDto.getCreditorAgentMemberId() != null ? requestDto.getCreditorAgentMemberId() : srcId;
        String cdtrBic = requestDto.getCreditorAgentBIC() != null && !requestDto.getCreditorAgentBIC().trim().isEmpty()
                ? requestDto.getCreditorAgentBIC().trim() : cdtrMmbId;

        MandateCancellation.Agent cdtrAgt = new MandateCancellation.Agent();
        cdtrAgt.setFinInstnId(createFinInstnId(cdtrBic, cdtrMmbId));
        innerMndt.setCdtrAgt(cdtrAgt);

        // Debtor Info
        MandateCancellation.Party dbtr = new MandateCancellation.Party();
        String dbtNm = requestDto.getDebtorName() != null ? requestDto.getDebtorName() : "Ponmile Joy";
        dbtr.setNm(dbtNm);
        innerMndt.setDbtr(dbtr);

        MandateCancellation.CashAccount dbtrAcct = new MandateCancellation.CashAccount();
        MandateCancellation.AccountId dbtrAcctId = new MandateCancellation.AccountId();
        dbtrAcctId.setIban(requestDto.getDebtorAccountNumber() != null ? requestDto.getDebtorAccountNumber() : "3157417712");
        dbtrAcct.setId(dbtrAcctId);
        String dbtAcctNm = requestDto.getDebtorAccountName() != null && !requestDto.getDebtorAccountName().trim().isEmpty()
                ? requestDto.getDebtorAccountName().trim() : dbtNm;
        dbtrAcct.setNm(dbtAcctNm);
        innerMndt.setDbtrAcct(dbtrAcct);

        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";
        String dbtrMmbId = requestDto.getDebtorAgentMemberId() != null ? requestDto.getDebtorAgentMemberId() : destId;
        String dbtrBic = requestDto.getDebtorAgentBIC() != null && !requestDto.getDebtorAgentBIC().trim().isEmpty()
                ? requestDto.getDebtorAgentBIC().trim() : dbtrMmbId;

        MandateCancellation.Agent dbtrAgt = new MandateCancellation.Agent();
        dbtrAgt.setFinInstnId(createFinInstnId(dbtrBic, dbtrMmbId));
        innerMndt.setDbtrAgt(dbtrAgt);

        mndtDetails.setOrgnlMndt(innerMndt);
        details.setOrgnlMndt(mndtDetails);
        
        req.setUndrlygCxlDtls(details);
        doc.setMndtCxlReq(req);
        return doc;
    }

    private static MandateCancellation.FinInstnId createFinInstnId(String bic, String memberId) {
        MandateCancellation.FinInstnId finInstnId = new MandateCancellation.FinInstnId();
        if (bic != null && !bic.trim().isEmpty()) {
            finInstnId.setBicfi(bic.trim());
        }
        MandateCancellation.ClrSysMmbId clrSysMmbId = new MandateCancellation.ClrSysMmbId();
        clrSysMmbId.setMmbId(memberId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        return finInstnId;
    }
}
