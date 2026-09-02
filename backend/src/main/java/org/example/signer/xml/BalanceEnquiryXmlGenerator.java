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
        
        String senderMmbId = requestDto.getSourceId() != null ? requestDto.getSourceId() :
                (requestDto.getMessageSenderMemberId() != null ? requestDto.getMessageSenderMemberId() : "999998");
        String senderBic = requestDto.getMessageSenderBIC() != null && !requestDto.getMessageSenderBIC().trim().isEmpty()
                ? requestDto.getMessageSenderBIC().trim() : senderMmbId;

        String ownerMmbId = requestDto.getAccountOwnerMemberId() != null ? requestDto.getAccountOwnerMemberId() : senderMmbId;
        String ownerBic = requestDto.getAccountOwnerBIC() != null && !requestDto.getAccountOwnerBIC().trim().isEmpty()
                ? requestDto.getAccountOwnerBIC().trim() : ownerMmbId;

        String servicerMmbId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() :
                (requestDto.getAccountServicerMemberId() != null ? requestDto.getAccountServicerMemberId() : "999997");
        String servicerBic = requestDto.getAccountServicerBIC() != null && !requestDto.getAccountServicerBIC().trim().isEmpty()
                ? requestDto.getAccountServicerBIC().trim() : servicerMmbId;

        // Message Sender
        BalanceEnquiry.MsgSndr msgSndr = new BalanceEnquiry.MsgSndr();
        msgSndr.setAgt(createAgt(senderBic, senderMmbId));
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
        acctOwnr.setAgt(createAgt(ownerBic, ownerMmbId));
        rptgReq.setAcctOwnr(acctOwnr);
        
        // Account Servicer
        BalanceEnquiry.AcctSvcr acctSvcr = new BalanceEnquiry.AcctSvcr();
        acctSvcr.setFinInstnId(createFinInstnId(servicerBic, servicerMmbId));
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

    private static BalanceEnquiry.Agt createAgt(String bic, String memberId) {
        BalanceEnquiry.Agt agt = new BalanceEnquiry.Agt();
        agt.setFinInstnId(createFinInstnId(bic, memberId));
        return agt;
    }

    private static BalanceEnquiry.FinInstnId createFinInstnId(String bic, String memberId) {
        BalanceEnquiry.FinInstnId finInstnId = new BalanceEnquiry.FinInstnId();
        if (bic != null && !bic.trim().isEmpty()) {
            finInstnId.setBicfi(bic.trim());
        }
        BalanceEnquiry.ClrSysMmbId clrSysMmbId = new BalanceEnquiry.ClrSysMmbId();
        clrSysMmbId.setMmbId(memberId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        return finInstnId;
    }
}
