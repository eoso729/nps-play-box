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
        String fwdgMmbId = requestDto.getForwardingAgentMemberId() != null ? requestDto.getForwardingAgentMemberId() : "999057";
        grpHdr.setFwdgAgt(createAgent(fwdgMmbId, requestDto.getForwardingAgentBIC()));

        String dbtrMmbId = requestDto.getDebtorAgentMemberId() != null ? requestDto.getDebtorAgentMemberId() : "999058";
        grpHdr.setDbtrAgt(createAgent(dbtrMmbId, requestDto.getDebtorAgentBIC()));

        String cdtrMmbId = requestDto.getCreditorAgentMemberId() != null ? requestDto.getCreditorAgentMemberId() : "999057";
        grpHdr.setCdtrAgt(createAgent(cdtrMmbId, requestDto.getCreditorAgentBIC()));

        rpt.setGrpHdr(grpHdr);

        // --- Original Group Info & Status ---
        PaymentActivationStatusReport.OrgnlGrpInfAndSts orgnlGrpInf = new PaymentActivationStatusReport.OrgnlGrpInfAndSts();
        orgnlGrpInf.setOrgnlMsgId(requestDto.getOriginalMsgId() != null ? requestDto.getOriginalMsgId() : "99905820260105102349998878725905163");
        orgnlGrpInf.setOrgnlMsgNmId(requestDto.getOriginalMsgNmId() != null ? requestDto.getOriginalMsgNmId() : "pain.013.001.11");
        orgnlGrpInf.setOrgnlCreDtTm(requestDto.getOriginalCreDtTm() != null ? requestDto.getOriginalCreDtTm() : "2026-01-05T10:27:26.737+01:00");
        orgnlGrpInf.setGrpSts(requestDto.getGroupStatus() != null ? requestDto.getGroupStatus() : "ACCP");
        rpt.setOrgnlGrpInfAndSts(orgnlGrpInf);

        // --- Original Payment Info & Status ---
        PaymentActivationStatusReport.OrgnlPmtInfAndSts orgnlPmtInf = new PaymentActivationStatusReport.OrgnlPmtInfAndSts();
        orgnlPmtInf.setOrgnlPmtInfId(requestDto.getOriginalPmtInfId() != null ? requestDto.getOriginalPmtInfId() : "GSFPMTINF035985837");
        
        PaymentActivationStatusReport.TxInfAndSts txInf = new PaymentActivationStatusReport.TxInfAndSts();
        txInf.setOrgnlEndToEndId(requestDto.getOriginalEndToEndId() != null ? requestDto.getOriginalEndToEndId() : "GSF035985837-E2E");
        txInf.setTxSts(requestDto.getTransactionStatus() != null ? requestDto.getTransactionStatus() : orgnlGrpInf.getGrpSts());
        orgnlPmtInf.setTxInfAndSts(txInf);
        rpt.setOrgnlPmtInfAndSts(orgnlPmtInf);

        doc.setCdtrPmtActvtnReqStsRpt(rpt);
        return doc;
    }

    private static PaymentActivationStatusReport.Agent createAgent(String mmbId, String bicfi) {
        PaymentActivationStatusReport.Agent agt = new PaymentActivationStatusReport.Agent();
        PaymentActivationStatusReport.FinInstnId finInstnId = new PaymentActivationStatusReport.FinInstnId();
        finInstnId.setBicfi(bicfi != null ? bicfi : mmbId);
        PaymentActivationStatusReport.ClrSysMmbId clrSysMmbId = new PaymentActivationStatusReport.ClrSysMmbId();
        clrSysMmbId.setMmbId(mmbId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        agt.setFinInstnId(finInstnId);
        return agt;
    }
}
