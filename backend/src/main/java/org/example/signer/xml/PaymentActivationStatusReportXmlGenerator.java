package org.example.signer.xml;

import org.example.signer.dto.PaymentActivationStatusReportDto;
import org.example.signer.model.PaymentActivationStatusReport;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentActivationStatusReportXmlGenerator {

    public static PaymentActivationStatusReport generate(PaymentActivationStatusReportDto requestDto, String msgId) {
        PaymentActivationStatusReport doc = new PaymentActivationStatusReport();
        PaymentActivationStatusReport.CdtrPmtActvtnReqStsRpt rpt = new PaymentActivationStatusReport.CdtrPmtActvtnReqStsRpt();

        // --- Group Header ---
        PaymentActivationStatusReport.GrpHdr grpHdr = new PaymentActivationStatusReport.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = ZonedDateTime.now(ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);

        PaymentActivationStatusReport.Party initgPty = new PaymentActivationStatusReport.Party();
        initgPty.setNm(requestDto.getInitiatingPartyName() != null ? requestDto.getInitiatingPartyName() : "Debtor Bank");
        grpHdr.setInitgPty(initgPty);

        // Creditor
        PaymentActivationStatusReport.Party cdtr = new PaymentActivationStatusReport.Party();
        cdtr.setNm(requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "CreditorCorp");
        grpHdr.setCdtr(cdtr);

        PaymentActivationStatusReport.CashAccount cdtrAcct = new PaymentActivationStatusReport.CashAccount();
        PaymentActivationStatusReport.AccountId cdtrAccId = new PaymentActivationStatusReport.AccountId();
        cdtrAccId.setIban(requestDto.getCreditorAccountNumber() != null ? requestDto.getCreditorAccountNumber() : "5555544443");
        cdtrAcct.setId(cdtrAccId);
        cdtrAcct.setNm(requestDto.getCreditorAccountName() != null ? requestDto.getCreditorAccountName() : cdtr.getNm());
        grpHdr.setCdtrAcct(cdtrAcct);

        // Debtor
        PaymentActivationStatusReport.Party dbtr = new PaymentActivationStatusReport.Party();
        dbtr.setNm(requestDto.getDebtorName() != null ? requestDto.getDebtorName() : "Debtor Customer");
        grpHdr.setDbtr(dbtr);

        PaymentActivationStatusReport.CashAccount dbtrAcct = new PaymentActivationStatusReport.CashAccount();
        PaymentActivationStatusReport.AccountId dbtrAccId = new PaymentActivationStatusReport.AccountId();
        dbtrAccId.setIban(requestDto.getDebtorAccountNumber() != null ? requestDto.getDebtorAccountNumber() : "8888899999");
        dbtrAcct.setId(dbtrAccId);
        dbtrAcct.setNm(requestDto.getDebtorAccountName() != null ? requestDto.getDebtorAccountName() : dbtr.getNm());
        grpHdr.setDbtrAcct(dbtrAcct);

        // Agents
        String srcId = requestDto.getSourceId() != null && !requestDto.getSourceId().trim().isEmpty()
                ? requestDto.getSourceId().trim() : null;
        String destId = requestDto.getDestinationId() != null && !requestDto.getDestinationId().trim().isEmpty()
                ? requestDto.getDestinationId().trim() : null;

        String fwdgMmbId = requestDto.getForwardingAgentMemberId() != null && !requestDto.getForwardingAgentMemberId().trim().isEmpty()
                ? requestDto.getForwardingAgentMemberId().trim()
                : (srcId != null ? srcId : "999057");
        String fwdgBic = requestDto.getForwardingAgentBIC() != null && !requestDto.getForwardingAgentBIC().trim().isEmpty()
                ? requestDto.getForwardingAgentBIC().trim() : fwdgMmbId;
        grpHdr.setFwdgAgt(createAgent(fwdgMmbId, fwdgBic));

        String dbtrMmbId = requestDto.getDebtorAgentMemberId() != null && !requestDto.getDebtorAgentMemberId().trim().isEmpty()
                ? requestDto.getDebtorAgentMemberId().trim()
                : (srcId != null ? srcId : "999058");
        String dbtrBic = requestDto.getDebtorAgentBIC() != null && !requestDto.getDebtorAgentBIC().trim().isEmpty()
                ? requestDto.getDebtorAgentBIC().trim() : dbtrMmbId;
        grpHdr.setDbtrAgt(createAgent(dbtrMmbId, dbtrBic));

        String cdtrMmbId = requestDto.getCreditorAgentMemberId() != null && !requestDto.getCreditorAgentMemberId().trim().isEmpty()
                ? requestDto.getCreditorAgentMemberId().trim()
                : (destId != null ? destId : "999057");
        String cdtrBic = requestDto.getCreditorAgentBIC() != null && !requestDto.getCreditorAgentBIC().trim().isEmpty()
                ? requestDto.getCreditorAgentBIC().trim() : cdtrMmbId;
        grpHdr.setCdtrAgt(createAgent(cdtrMmbId, cdtrBic));

        rpt.setGrpHdr(grpHdr);

        // --- Original Group Info & Status ---
        PaymentActivationStatusReport.OrgnlGrpInfAndSts orgnlGrpInf = new PaymentActivationStatusReport.OrgnlGrpInfAndSts();
        orgnlGrpInf.setOrgnlMsgId(requestDto.getOriginalMsgId() != null ? requestDto.getOriginalMsgId() : "99999820260129214037117325354651516");
        orgnlGrpInf.setOrgnlMsgNmId(requestDto.getOriginalMsgNmId() != null ? requestDto.getOriginalMsgNmId() : "pain.013.001.11");
        orgnlGrpInf.setOrgnlCreDtTm(requestDto.getOriginalCreDtTm() != null ? requestDto.getOriginalCreDtTm() : "2026-01-29T21:40:37.000Z");
        orgnlGrpInf.setGrpSts(requestDto.getGroupStatus() != null ? requestDto.getGroupStatus() : "ACCP");
        rpt.setOrgnlGrpInfAndSts(orgnlGrpInf);

        // --- Original Payment Info & Status ---
        PaymentActivationStatusReport.OrgnlPmtInfAndSts orgnlPmtInf = new PaymentActivationStatusReport.OrgnlPmtInfAndSts();
        orgnlPmtInf.setOrgnlPmtInfId(requestDto.getOriginalPmtInfId() != null ? requestDto.getOriginalPmtInfId() : orgnlGrpInf.getOrgnlMsgId());
        
        PaymentActivationStatusReport.TxInfAndSts txInf = new PaymentActivationStatusReport.TxInfAndSts();
        txInf.setOrgnlEndToEndId(requestDto.getOriginalEndToEndId() != null ? requestDto.getOriginalEndToEndId() : orgnlGrpInf.getOrgnlMsgId());
        txInf.setTxSts(requestDto.getTransactionStatus() != null ? requestDto.getTransactionStatus() : orgnlGrpInf.getGrpSts());
        orgnlPmtInf.setTxInfAndSts(txInf);
        rpt.setOrgnlPmtInfAndSts(orgnlPmtInf);

        doc.setCdtrPmtActvtnReqStsRpt(rpt);
        return doc;
    }

    private static PaymentActivationStatusReport.Agent createAgent(String mmbId, String bicfi) {
        PaymentActivationStatusReport.Agent agt = new PaymentActivationStatusReport.Agent();
        PaymentActivationStatusReport.FinInstnId finInstnId = new PaymentActivationStatusReport.FinInstnId();
        if (bicfi != null && !bicfi.trim().isEmpty()) {
            finInstnId.setBicfi(bicfi.trim());
        }
        PaymentActivationStatusReport.ClrSysMmbId clrSysMmbId = new PaymentActivationStatusReport.ClrSysMmbId();
        clrSysMmbId.setMmbId(mmbId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        agt.setFinInstnId(finInstnId);
        return agt;
    }
}
