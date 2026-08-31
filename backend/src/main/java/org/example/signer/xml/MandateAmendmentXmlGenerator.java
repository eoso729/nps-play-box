package org.example.signer.xml;

import org.example.signer.dto.MandateAmendmentRequestDto;
import org.example.signer.model.MandateAmendment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MandateAmendmentXmlGenerator {

    public static MandateAmendment generate(MandateAmendmentRequestDto requestDto, String msgId) {
        MandateAmendment doc = new MandateAmendment();
        MandateAmendment.MndtAmdmntReq mndtAmdmntReq = new MandateAmendment.MndtAmdmntReq();
        
        // --- Group Header ---
        MandateAmendment.GrpHdr grpHdr = new MandateAmendment.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = java.time.ZonedDateTime.now(java.time.ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);
        
        MandateAmendment.InitgPty initgPty = new MandateAmendment.InitgPty();
        initgPty.setNm(requestDto.getInitiatingPartyName() != null ? requestDto.getInitiatingPartyName() : "ABC Tech Pvt Ltd");
        grpHdr.setInitgPty(initgPty);
        mndtAmdmntReq.setGrpHdr(grpHdr);
        
        // --- Underlying Amendment Details ---
        List<MandateAmendment.UndrlygAmdmntDtls> undrlygAmdmntDtlsList = new ArrayList<>();
        MandateAmendment.UndrlygAmdmntDtls undrlygAmdmntDtls = new MandateAmendment.UndrlygAmdmntDtls();
        
        // Original Message Info
        MandateAmendment.OrgnlMsgInf orgnlMsgInf = new MandateAmendment.OrgnlMsgInf();
        orgnlMsgInf.setMsgId(requestDto.getOrgnlMsgId() != null ? requestDto.getOrgnlMsgId() : "99999820260331160816119597368459797");
        orgnlMsgInf.setMsgNmId(requestDto.getOrgnlMsgNmId() != null ? requestDto.getOrgnlMsgNmId() : "pain.009.001.08");
        orgnlMsgInf.setCreDtTm(requestDto.getOrgnlCreDtTm() != null ? requestDto.getOrgnlCreDtTm() : "2026-03-31T16:08:16");
        undrlygAmdmntDtls.setOrgnlMsgInf(orgnlMsgInf);
        
        // Amendment Reason
        MandateAmendment.AmdmntRsn amdmntRsn = new MandateAmendment.AmdmntRsn();
        MandateAmendment.RsnDetail rsnDetail = new MandateAmendment.RsnDetail();
        rsnDetail.setCd(requestDto.getAmdmntRsnCode() != null ? requestDto.getAmdmntRsnCode() : "AC04");
        rsnDetail.setPrtry(requestDto.getAmdmntRsnProprietary() != null ? requestDto.getAmdmntRsnProprietary() : "Debtor Info update");
        amdmntRsn.setRsn(rsnDetail);
        undrlygAmdmntDtls.setAmdmntRsn(amdmntRsn);
        
        // New Mandate
        MandateAmendment.Mndt mndt = new MandateAmendment.Mndt();
        String mndtId = requestDto.getOrgnlMndtId() != null ? requestDto.getOrgnlMndtId() : "MNDT-RCUR-00061";
        mndt.setMndtId(mndtId);
        
        // Occurrences
        MandateAmendment.Ocrncs ocrncs = new MandateAmendment.Ocrncs();
        ocrncs.setSeqTp(requestDto.getSequenceType() != null ? requestDto.getSequenceType() : "RCUR");
        MandateAmendment.Frqcy frqcy = new MandateAmendment.Frqcy();
        frqcy.setTp(requestDto.getFrequencyType() != null ? requestDto.getFrequencyType() : "WEEK");
        ocrncs.setFrqcy(frqcy);
        ocrncs.setFrstColltnDt(requestDto.getFirstCollectionDate() != null ? requestDto.getFirstCollectionDate() : "2026-04-01");
        ocrncs.setFnlColltnDt(requestDto.getFinalCollectionDate() != null ? requestDto.getFinalCollectionDate() : "2026-04-30");
        mndt.setOcrncs(ocrncs);
        
        // Tracking Indicator
        mndt.setTrckgInd(requestDto.getTrackingIndicator() != null ? requestDto.getTrackingIndicator() : false);
        
        // Creditor
        MandateAmendment.Cdtr cdtr = new MandateAmendment.Cdtr();
        String cdrNm = requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "ABC Tech Pvt Ltd";
        cdtr.setNm(cdrNm);
        mndt.setCdtr(cdtr);
        
        // Creditor Account
        MandateAmendment.CdtrAcct cdtrAcct = new MandateAmendment.CdtrAcct();
        MandateAmendment.AcctId cdtrAcctId = new MandateAmendment.AcctId();
        cdtrAcctId.setIban(requestDto.getCreditorAccountNumber() != null ? requestDto.getCreditorAccountNumber() : "3232444422");
        cdtrAcct.setId(cdtrAcctId);
        cdtrAcct.setNm(cdrNm);
        mndt.setCdtrAcct(cdtrAcct);
        
        // Creditor Agent
        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        String cdtrBic = requestDto.getCreditorAgentBIC() != null ? requestDto.getCreditorAgentBIC() : "AA123456";
        String cdtrMmbId = requestDto.getCreditorAgentMemberId() != null ? requestDto.getCreditorAgentMemberId() : srcId;
        
        MandateAmendment.CdtrAgt cdtrAgt = new MandateAmendment.CdtrAgt();
        cdtrAgt.setFinInstnId(createFinInstnId(cdtrBic, cdtrMmbId));
        mndt.setCdtrAgt(cdtrAgt);
        
        // Debtor
        MandateAmendment.Dbtr dbtr = new MandateAmendment.Dbtr();
        String dbtNm = requestDto.getDebtorName() != null ? requestDto.getDebtorName() : "Mr. Fred";
        dbtr.setNm(dbtNm);
        mndt.setDbtr(dbtr);
        
        // Debtor Account
        MandateAmendment.DbtrAcct dbtrAcct = new MandateAmendment.DbtrAcct();
        MandateAmendment.AcctId dbtrAcctId = new MandateAmendment.AcctId();
        dbtrAcctId.setIban(requestDto.getDebtorAccountNumber() != null ? requestDto.getDebtorAccountNumber() : "4343211111");
        dbtrAcct.setId(dbtrAcctId);
        dbtrAcct.setNm(dbtNm);
        mndt.setDbtrAcct(dbtrAcct);
        
        // Debtor Agent
        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";
        String dbtrBic = requestDto.getDebtorAgentBIC() != null ? requestDto.getDebtorAgentBIC() : "BB123456";
        String dbtrMmbId = requestDto.getDebtorAgentMemberId() != null ? requestDto.getDebtorAgentMemberId() : destId;
        
        MandateAmendment.DbtrAgt dbtrAgt = new MandateAmendment.DbtrAgt();
        dbtrAgt.setFinInstnId(createFinInstnId(dbtrBic, dbtrMmbId));
        mndt.setDbtrAgt(dbtrAgt);
        
        undrlygAmdmntDtls.setMndt(mndt);
        
        // Original Mandate
        MandateAmendment.OrgnlMndt orgnlMndt = new MandateAmendment.OrgnlMndt();
        orgnlMndt.setOrgnlMndtId(mndtId);
        undrlygAmdmntDtls.setOrgnlMndt(orgnlMndt);
        
        undrlygAmdmntDtlsList.add(undrlygAmdmntDtls);
        mndtAmdmntReq.setUndrlygAmdmntDtls(undrlygAmdmntDtlsList);
        
        doc.setMndtAmdmntReq(mndtAmdmntReq);
        return doc;
    }

    private static MandateAmendment.FinInstnId createFinInstnId(String bic, String memberId) {
        MandateAmendment.FinInstnId finInstnId = new MandateAmendment.FinInstnId();
        finInstnId.setBicfi(bic);
        MandateAmendment.ClrSysMmbId clrSysMmbId = new MandateAmendment.ClrSysMmbId();
        clrSysMmbId.setMmbId(memberId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        return finInstnId;
    }
}
