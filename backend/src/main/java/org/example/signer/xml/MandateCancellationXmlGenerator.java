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
        LocalDateTime now = LocalDateTime.now();
        String creDtTm = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        grpHdr.setCreDtTm(creDtTm);
        req.setGrpHdr(grpHdr);

        // --- Underlying Cancellation Details ---
        MandateCancellation.UndrlygCxlDtls details = new MandateCancellation.UndrlygCxlDtls();

        // Original Message Info
        MandateCancellation.OrgnlMsgInf orgnlMsgInf = new MandateCancellation.OrgnlMsgInf();
        orgnlMsgInf.setMsgId(requestDto.getOriginalMsgId() != null ? requestDto.getOriginalMsgId() : "99999820260331160816119597368459797");
        orgnlMsgInf.setMsgNmId("pain.009.001.08");
        orgnlMsgInf.setCreDtTm(requestDto.getOriginalCreDtTm() != null ? requestDto.getOriginalCreDtTm() : "2026-03-31T16:08:16");
        details.setOrgnlMsgInf(orgnlMsgInf);

        // Cancellation Reason
        MandateCancellation.CxlRsn cxlRsn = new MandateCancellation.CxlRsn();
        MandateCancellation.Rsn rsn = new MandateCancellation.Rsn();
        rsn.setCd(requestDto.getCancellationReasonCode() != null ? requestDto.getCancellationReasonCode() : "AC04");
        rsn.setPrtry(requestDto.getCancellationReasonDescription() != null ? requestDto.getCancellationReasonDescription() : "Cancel");
        cxlRsn.setRsn(rsn);
        details.setCxlRsn(cxlRsn);

        // Original Mandate Container
        MandateCancellation.OrgnlMndtContainer mndtContainer = new MandateCancellation.OrgnlMndtContainer();
        String mndtId = requestDto.getOriginalMandateId() != null ? requestDto.getOriginalMandateId() : "MNDT-RCUR-00061";
        mndtContainer.setOrgnlMndtId(mndtId);

        // Original Mandate Details
        MandateCancellation.OrgnlMndtDetails mndtDetails = new MandateCancellation.OrgnlMndtDetails();
        
        MandateCancellation.Ocrncs ocrncs = new MandateCancellation.Ocrncs();
        ocrncs.setSeqTp(requestDto.getSequenceType() != null ? requestDto.getSequenceType() : "RCUR");
        MandateCancellation.Frqcy frqcy = new MandateCancellation.Frqcy();
        frqcy.setTp(requestDto.getFrequencyType() != null ? requestDto.getFrequencyType() : "MNTH");
        ocrncs.setFrqcy(frqcy);
        ocrncs.setFrstColltnDt(requestDto.getFirstCollectionDate() != null ? requestDto.getFirstCollectionDate() : "2026-04-01");
        ocrncs.setFnlColltnDt(requestDto.getFinalCollectionDate() != null ? requestDto.getFinalCollectionDate() : "2026-04-30");
        mndtDetails.setOcrncs(ocrncs);
        
        mndtDetails.setTrckgInd(requestDto.getTrackingIndicator() != null ? requestDto.getTrackingIndicator() : "false");

        // Creditor Info
        MandateCancellation.Party cdtr = new MandateCancellation.Party();
        String cdrNm = requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "Tester";
        cdtr.setNm(cdrNm);
        mndtDetails.setCdtr(cdtr);

        MandateCancellation.CashAccount cdtrAcct = new MandateCancellation.CashAccount();
        MandateCancellation.AccountId cdtrAcctId = new MandateCancellation.AccountId();
        cdtrAcctId.setIban(requestDto.getCreditorAccountNumber() != null ? requestDto.getCreditorAccountNumber() : "0987654320");
        cdtrAcct.setId(cdtrAcctId);
        cdtrAcct.setNm(cdrNm);
        mndtDetails.setCdtrAcct(cdtrAcct);

        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        String cdtrBic = requestDto.getCreditorAgentBIC() != null ? requestDto.getCreditorAgentBIC() : "999998";
        String cdtrMmbId = requestDto.getCreditorAgentMemberId() != null ? requestDto.getCreditorAgentMemberId() : srcId;

        MandateCancellation.Agent cdtrAgt = new MandateCancellation.Agent();
        cdtrAgt.setFinInstnId(createFinInstnId(cdtrBic, cdtrMmbId));
        mndtDetails.setCdtrAgt(cdtrAgt);

        // Debtor Info
        MandateCancellation.Party dbtr = new MandateCancellation.Party();
        String dbtNm = requestDto.getDebtorName() != null ? requestDto.getDebtorName() : "Ponmile Joy";
        dbtr.setNm(dbtNm);
        mndtDetails.setDbtr(dbtr);

        MandateCancellation.CashAccount dbtrAcct = new MandateCancellation.CashAccount();
        MandateCancellation.AccountId dbtrAcctId = new MandateCancellation.AccountId();
        dbtrAcctId.setIban(requestDto.getDebtorAccountNumber() != null ? requestDto.getDebtorAccountNumber() : "3157417712");
        dbtrAcct.setId(dbtrAcctId);
        dbtrAcct.setNm(dbtNm);
        mndtDetails.setDbtrAcct(dbtrAcct);

        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";
        String dbtrBic = requestDto.getDebtorAgentBIC() != null ? requestDto.getDebtorAgentBIC() : "999997";
        String dbtrMmbId = requestDto.getDebtorAgentMemberId() != null ? requestDto.getDebtorAgentMemberId() : destId;

        MandateCancellation.Agent dbtrAgt = new MandateCancellation.Agent();
        dbtrAgt.setFinInstnId(createFinInstnId(dbtrBic, dbtrMmbId));
        mndtDetails.setDbtrAgt(dbtrAgt);

        mndtContainer.setOrgnlMndt(mndtDetails);
        details.setOrgnlMndt(mndtContainer);
        
        req.setUndrlygCxlDtls(details);
        doc.setMndtCxlReq(req);
        return doc;
    }

    private static MandateCancellation.FinInstnId createFinInstnId(String bic, String memberId) {
        MandateCancellation.FinInstnId finInstnId = new MandateCancellation.FinInstnId();
        finInstnId.setBicfi(bic);
        MandateCancellation.ClrSysMmbId clrSysMmbId = new MandateCancellation.ClrSysMmbId();
        clrSysMmbId.setMmbId(memberId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        return finInstnId;
    }
}
