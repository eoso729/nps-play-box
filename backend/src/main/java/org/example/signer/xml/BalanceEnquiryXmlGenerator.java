package org.example.signer.xml;

import org.example.signer.dto.AccountReportingRequestDto;
import org.example.signer.model.BalanceEnquiry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BalanceEnquiryXmlGenerator {

    public static BalanceEnquiry generate(AccountReportingRequestDto requestDto, String msgId, String rptgReqId) {
        BalanceEnquiry doc = new BalanceEnquiry();
        BalanceEnquiry.AcctRptgReq acctRptgReq = new BalanceEnquiry.AcctRptgReq();
        
        // --- Group Header ---
        BalanceEnquiry.GrpHdr grpHdr = new BalanceEnquiry.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = java.time.ZonedDateTime.now(java.time.ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);
        
        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";

        // Message Sender
        BalanceEnquiry.MsgSndr msgSndr = new BalanceEnquiry.MsgSndr();
        BalanceEnquiry.Agt msgSndrAgt = new BalanceEnquiry.Agt();
        BalanceEnquiry.FinInstnId msgSndrFinInstnId = new BalanceEnquiry.FinInstnId();
        msgSndrFinInstnId.setBicfi(srcId);
        BalanceEnquiry.ClrSysMmbId msgSndrClrSysMmbId = new BalanceEnquiry.ClrSysMmbId();
        msgSndrClrSysMmbId.setMmbId(srcId);
        msgSndrFinInstnId.setClrSysMmbId(msgSndrClrSysMmbId);
        msgSndrAgt.setFinInstnId(msgSndrFinInstnId);
        msgSndr.setAgt(msgSndrAgt);
        grpHdr.setMsgSndr(msgSndr);
        
        // --- Reporting Request ---
        BalanceEnquiry.RptgReq rptgReq = new BalanceEnquiry.RptgReq();
        rptgReq.setId(requestDto.getReportingRequestId() != null ? requestDto.getReportingRequestId() : rptgReqId);
        rptgReq.setReqdMsgNmId(requestDto.getRequestedMessageType() != null ? requestDto.getRequestedMessageType() : "STATEMENT");

        // Account
        BalanceEnquiry.Acct acct = new BalanceEnquiry.Acct();
        BalanceEnquiry.AcctId acctId = new BalanceEnquiry.AcctId();
        acctId.setIban(requestDto.getAccountNumber() != null ? requestDto.getAccountNumber() : "3157417712");
        acct.setId(acctId);
        acct.setCcy(requestDto.getCurrency() != null ? requestDto.getCurrency() : "NGN");
        rptgReq.setAcct(acct);
        
        // Account Owner
        BalanceEnquiry.AcctOwnr acctOwnr = new BalanceEnquiry.AcctOwnr();
        BalanceEnquiry.Agt acctOwnrAgt = new BalanceEnquiry.Agt();
        BalanceEnquiry.FinInstnId acctOwnrFinInstnId = new BalanceEnquiry.FinInstnId();
        acctOwnrFinInstnId.setBicfi(srcId);
        BalanceEnquiry.ClrSysMmbId acctOwnrClrSysMmbId = new BalanceEnquiry.ClrSysMmbId();
        acctOwnrClrSysMmbId.setMmbId(srcId);
        acctOwnrFinInstnId.setClrSysMmbId(acctOwnrClrSysMmbId);
        acctOwnrAgt.setFinInstnId(acctOwnrFinInstnId);
        acctOwnr.setAgt(acctOwnrAgt);
        rptgReq.setAcctOwnr(acctOwnr);
        
        // Account Servicer
        BalanceEnquiry.AcctSvcr acctSvcr = new BalanceEnquiry.AcctSvcr();
        BalanceEnquiry.FinInstnId acctSvcrFinInstnId = new BalanceEnquiry.FinInstnId();
        acctSvcrFinInstnId.setBicfi(destId);
        BalanceEnquiry.ClrSysMmbId acctSvcrClrSysMmbId = new BalanceEnquiry.ClrSysMmbId();
        acctSvcrClrSysMmbId.setMmbId(destId);
        acctSvcrFinInstnId.setClrSysMmbId(acctSvcrClrSysMmbId);
        acctSvcr.setFinInstnId(acctSvcrFinInstnId);
        rptgReq.setAcctSvcr(acctSvcr);

        // Reporting Period
        BalanceEnquiry.RptgPrd rptgPrd = new BalanceEnquiry.RptgPrd();
        BalanceEnquiry.FrToDt frToDt = new BalanceEnquiry.FrToDt();
        frToDt.setFrDt(requestDto.getFromDate() != null ? requestDto.getFromDate() : "2026-03-01");
        frToDt.setToDt(requestDto.getToDate() != null ? requestDto.getToDate() : "2026-03-09");
        rptgPrd.setFrToDt(frToDt);
        rptgPrd.setTp(requestDto.getReportingPeriodType() != null ? requestDto.getReportingPeriodType() : "ALLL");
        rptgReq.setRptgPrd(rptgPrd);
        
        // --- Supplementary Data ---
        BalanceEnquiry.SplmtryData splmtryData = new BalanceEnquiry.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        BalanceEnquiry.Envlp envlp = new BalanceEnquiry.Envlp();
        BalanceEnquiry.CustomData customData = new BalanceEnquiry.CustomData();
        
        // Creditor Info
        BalanceEnquiry.CreditorInfo creditorInfo = new BalanceEnquiry.CreditorInfo();
        creditorInfo.setAccountDesignation(requestDto.getAccountDesignation() != null ? requestDto.getAccountDesignation() : "1");
        creditorInfo.setIdType(requestDto.getIdType() != null ? requestDto.getIdType() : "BVN");
        creditorInfo.setIdValue(requestDto.getIdValue() != null ? requestDto.getIdValue() : "11111111145");
        creditorInfo.setAccountTier(requestDto.getAccountTier() != null ? requestDto.getAccountTier() : "3");
        customData.setCreditorInfo(creditorInfo);
        
        // Transaction Info
        BalanceEnquiry.TransactionInfo transactionInfo = new BalanceEnquiry.TransactionInfo();
        transactionInfo.setTransactionLocation(requestDto.getTransactionLocation() != null ? requestDto.getTransactionLocation() : "013223231333");
        transactionInfo.setChannelCode(requestDto.getChannelCode() != null ? requestDto.getChannelCode() : "2");
        transactionInfo.setFixedCollectionAmount(requestDto.getFixedCollectionAmount() != null ? Boolean.valueOf(requestDto.getFixedCollectionAmount()) : false);
        transactionInfo.setMandateCode(requestDto.getMandateCode() != null ? requestDto.getMandateCode() : "MNDT-RCUR-13482");
        customData.setTransactionInfo(transactionInfo);
        
        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);
        
        // --- Assemble Document ---
        acctRptgReq.setGrpHdr(grpHdr);
        acctRptgReq.setRptgReq(rptgReq);
        acctRptgReq.setSplmtryData(splmtryData);
        doc.setAcctRptgReq(acctRptgReq);
        
        return doc;
    }
}
