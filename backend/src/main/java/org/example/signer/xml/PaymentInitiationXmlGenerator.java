package org.example.signer.xml;

import org.example.signer.dto.PaymentInitiationRequestDto;
import org.example.signer.model.PaymentInitiation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class PaymentInitiationXmlGenerator {

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("120.51");

    public static PaymentInitiation generate(PaymentInitiationRequestDto requestDto, String msgId, String endToEndId, String reqdExctnDt) {
        // Use provided amount or default
        BigDecimal amount = (requestDto.getAmount() != null && requestDto.getAmount().compareTo(BigDecimal.ZERO) > 0) 
                ? requestDto.getAmount().setScale(2, RoundingMode.HALF_UP) 
                : DEFAULT_AMOUNT;
        
        PaymentInitiation doc = new PaymentInitiation();
        PaymentInitiation.CstmrCdtTrfInitn cstmrCdtTrfInitn = new PaymentInitiation.CstmrCdtTrfInitn();
        
        // --- Group Header ---
        PaymentInitiation.GrpHdr grpHdr = new PaymentInitiation.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = java.time.ZonedDateTime.now(ZoneId.of("Africa/Lagos"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);
        grpHdr.setNbOfTxs(1);
        grpHdr.setCtrlSum(amount);
        
        // Initiating Party
        PaymentInitiation.InitgPty initgPty = new PaymentInitiation.InitgPty();
        String initName = requestDto.getInitiatingPartyName() != null && !requestDto.getInitiatingPartyName().trim().isEmpty()
                ? requestDto.getInitiatingPartyName().trim() : "Musa";
        initgPty.setNm(initName);
        PaymentInitiation.Id id = new PaymentInitiation.Id();
        PaymentInitiation.OrgId orgId = new PaymentInitiation.OrgId();
        PaymentInitiation.Othr othr = new PaymentInitiation.Othr();
        PaymentInitiation.SchmeNm schmeNm = new PaymentInitiation.SchmeNm();
        String schemeCode = requestDto.getSchemeCode() != null && !requestDto.getSchemeCode().trim().isEmpty()
                ? requestDto.getSchemeCode().trim()
                : (requestDto.getInitiatorId() != null && !requestDto.getInitiatorId().trim().isEmpty() ? requestDto.getInitiatorId().trim() : "999057");
        schmeNm.setCd(schemeCode);
        othr.setSchmeNm(schmeNm);
        orgId.setOthr(othr);
        id.setOrgId(orgId);
        initgPty.setId(id);
        grpHdr.setInitgPty(initgPty);
        
        // Forwarding Agent (Optional)
        if (requestDto.getForwardingAgentBIC() != null && !requestDto.getForwardingAgentBIC().trim().isEmpty()) {
            PaymentInitiation.FwdgAgt fwdgAgt = new PaymentInitiation.FwdgAgt();
            PaymentInitiation.FinInstnId fwdgFinInstnId = new PaymentInitiation.FinInstnId();
            fwdgFinInstnId.setBicfi(requestDto.getForwardingAgentBIC().trim());
            fwdgAgt.setFinInstnId(fwdgFinInstnId);
            grpHdr.setFwdgAgt(fwdgAgt);
        }
        
        // --- Payment Info ---
        PaymentInitiation.PmtInf pmtInf = new PaymentInitiation.PmtInf();
        String pmtInfId;
        if (requestDto.getPaymentInformationId() != null && !requestDto.getPaymentInformationId().trim().isEmpty()) {
            pmtInfId = requestDto.getPaymentInformationId().trim();
        } else if (msgId != null && msgId.length() > 31) {
            pmtInfId = "PMT-" + msgId.substring(msgId.length() - 31);
        } else if (msgId != null) {
            pmtInfId = "PMT-" + msgId;
        } else {
            pmtInfId = "PMT-20251016-001-SINGLE";
        }
        if (pmtInfId.length() > 35) {
            pmtInfId = pmtInfId.substring(0, 35);
        }
        pmtInf.setPmtInfId(pmtInfId);
        pmtInf.setPmtMtd("TRF");
        pmtInf.setBtchBookg(requestDto.getBatchBooking() != null ? requestDto.getBatchBooking() : false);
        pmtInf.setNbOfTxs(1);
        pmtInf.setCtrlSum(amount);
        
        // Requested Execution Date
        PaymentInitiation.ReqdExctnDt reqdExctnDtObj = new PaymentInitiation.ReqdExctnDt();
        String exctnDt = requestDto.getRequestedExecutionDate() != null && !requestDto.getRequestedExecutionDate().trim().isEmpty()
                ? requestDto.getRequestedExecutionDate().trim() : reqdExctnDt;
        if (exctnDt != null && !exctnDt.endsWith("Z")) {
            exctnDt = exctnDt + "Z";
        }
        reqdExctnDtObj.setDt(exctnDt);
        pmtInf.setReqdExctnDt(reqdExctnDtObj);
        
        // Debtor
        PaymentInitiation.Dbtr dbtr = new PaymentInitiation.Dbtr();
        String debtorName = requestDto.getDebtorName() != null && !requestDto.getDebtorName().trim().isEmpty()
                ? requestDto.getDebtorName().trim() : "Musa";
        dbtr.setNm(debtorName);
        pmtInf.setDbtr(dbtr);
        
        // Debtor Account
        PaymentInitiation.DbtrAcct dbtrAcct = new PaymentInitiation.DbtrAcct();
        PaymentInitiation.AcctId dbtrAcctId = new PaymentInitiation.AcctId();
        String debtorAcctNum = requestDto.getDebtorAccountNumber() != null && !requestDto.getDebtorAccountNumber().trim().isEmpty()
                ? requestDto.getDebtorAccountNumber().trim() : "0177136558";
        dbtrAcctId.setIban(debtorAcctNum);
        dbtrAcct.setId(dbtrAcctId);
        String debtorAcctName = requestDto.getDebtorAccountName() != null && !requestDto.getDebtorAccountName().trim().isEmpty()
                ? requestDto.getDebtorAccountName().trim() : debtorName;
        dbtrAcct.setNm(debtorAcctName);
        pmtInf.setDbtrAcct(dbtrAcct);
        
        // Debtor Agent
        PaymentInitiation.DbtrAgt dbtrAgt = new PaymentInitiation.DbtrAgt();
        PaymentInitiation.FinInstnId dbtrFinInstnId = new PaymentInitiation.FinInstnId();
        String dbtrMmbId = requestDto.getDebtorAgentMemberId() != null && !requestDto.getDebtorAgentMemberId().trim().isEmpty()
                ? requestDto.getDebtorAgentMemberId().trim()
                : (requestDto.getDebtorId() != null && !requestDto.getDebtorId().trim().isEmpty() ? requestDto.getDebtorId().trim() : "999057");
        String dbtrBic = requestDto.getDebtorAgentBIC() != null && !requestDto.getDebtorAgentBIC().trim().isEmpty()
                ? requestDto.getDebtorAgentBIC().trim() : dbtrMmbId;
        dbtrFinInstnId.setBicfi(dbtrBic);
        PaymentInitiation.ClrSysMmbId dbtrClrSysMmbId = new PaymentInitiation.ClrSysMmbId();
        dbtrClrSysMmbId.setMmbId(dbtrMmbId);
        dbtrFinInstnId.setClrSysMmbId(dbtrClrSysMmbId);
        dbtrAgt.setFinInstnId(dbtrFinInstnId);
        pmtInf.setDbtrAgt(dbtrAgt);
        
        // Charge Bearer
        String chrgBr = requestDto.getChargeBearer() != null && !requestDto.getChargeBearer().trim().isEmpty()
                ? requestDto.getChargeBearer().trim() : "SLEV";
        pmtInf.setChrgBr(chrgBr);
        
        // --- Credit Transfer Transaction Info ---
        PaymentInitiation.CdtTrfTxInf cdtTrfTxInf = new PaymentInitiation.CdtTrfTxInf();
        
        // Payment ID
        PaymentInitiation.PmtId pmtId = new PaymentInitiation.PmtId();
        String actualEndToEndId = requestDto.getEndToEndId() != null && !requestDto.getEndToEndId().trim().isEmpty()
                ? requestDto.getEndToEndId().trim() : endToEndId;
        pmtId.setEndToEndId(actualEndToEndId);
        cdtTrfTxInf.setPmtId(pmtId);
        
        // Amount
        PaymentInitiation.Amt amt = new PaymentInitiation.Amt();
        PaymentInitiation.InstdAmt instdAmt = new PaymentInitiation.InstdAmt();
        String ccy = requestDto.getCurrency() != null && !requestDto.getCurrency().trim().isEmpty()
                ? requestDto.getCurrency().trim() : "NGN";
        instdAmt.setCcy(ccy);
        instdAmt.setValue(amount);
        amt.setInstdAmt(instdAmt);
        cdtTrfTxInf.setAmt(amt);
        
        // Creditor Agent
        PaymentInitiation.CdtrAgt cdtrAgt = new PaymentInitiation.CdtrAgt();
        PaymentInitiation.FinInstnId cdtrFinInstnId = new PaymentInitiation.FinInstnId();
        String cdtrMmbId = requestDto.getCreditorAgentMemberId() != null && !requestDto.getCreditorAgentMemberId().trim().isEmpty()
                ? requestDto.getCreditorAgentMemberId().trim()
                : (requestDto.getCreditorId() != null && !requestDto.getCreditorId().trim().isEmpty() ? requestDto.getCreditorId().trim() : "999058");
        String cdtrBic = requestDto.getCreditorAgentBIC() != null && !requestDto.getCreditorAgentBIC().trim().isEmpty()
                ? requestDto.getCreditorAgentBIC().trim() : cdtrMmbId;
        cdtrFinInstnId.setBicfi(cdtrBic);
        PaymentInitiation.ClrSysMmbId cdtrClrSysMmbId = new PaymentInitiation.ClrSysMmbId();
        cdtrClrSysMmbId.setMmbId(cdtrMmbId);
        cdtrFinInstnId.setClrSysMmbId(cdtrClrSysMmbId);
        cdtrAgt.setFinInstnId(cdtrFinInstnId);
        cdtTrfTxInf.setCdtrAgt(cdtrAgt);
        
        // Creditor
        PaymentInitiation.Cdtr cdtr = new PaymentInitiation.Cdtr();
        String creditorName = requestDto.getCreditorName() != null && !requestDto.getCreditorName().trim().isEmpty()
                ? requestDto.getCreditorName().trim() : "James";
        cdtr.setNm(creditorName);
        cdtTrfTxInf.setCdtr(cdtr);
        
        // Creditor Account
        PaymentInitiation.CdtrAcct cdtrAcct = new PaymentInitiation.CdtrAcct();
        PaymentInitiation.AcctId cdtrAcctId = new PaymentInitiation.AcctId();
        String creditorAcctNum = requestDto.getCreditorAccountNumber() != null && !requestDto.getCreditorAccountNumber().trim().isEmpty()
                ? requestDto.getCreditorAccountNumber().trim() : "3157417712";
        cdtrAcctId.setIban(creditorAcctNum);
        cdtrAcct.setId(cdtrAcctId);
        String creditorAcctName = requestDto.getCreditorAccountName() != null && !requestDto.getCreditorAccountName().trim().isEmpty()
                ? requestDto.getCreditorAccountName().trim() : creditorName;
        cdtrAcct.setNm(creditorAcctName);
        cdtTrfTxInf.setCdtrAcct(cdtrAcct);
        
        // Remittance Info
        PaymentInitiation.RmtInf rmtInf = new PaymentInitiation.RmtInf();
        String rmtInfo = requestDto.getRemittanceInformation() != null && !requestDto.getRemittanceInformation().trim().isEmpty()
                ? requestDto.getRemittanceInformation().trim() : "Invoice 12345 (single)";
        rmtInf.setUstrd(rmtInfo);
        cdtTrfTxInf.setRmtInf(rmtInf);
        
        pmtInf.setCdtTrfTxInf(cdtTrfTxInf);
        
        // --- Supplementary Data ---
        PaymentInitiation.SplmtryData splmtryData = new PaymentInitiation.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        PaymentInitiation.Envlp envlp = new PaymentInitiation.Envlp();
        PaymentInitiation.CustomData customData = new PaymentInitiation.CustomData();
        
        PaymentInitiation.CreditorInfo creditorInfo = new PaymentInitiation.CreditorInfo();
        creditorInfo.setAccountDesignation(requestDto.getAccountDesignation() != null && !requestDto.getAccountDesignation().trim().isEmpty()
                ? requestDto.getAccountDesignation().trim() : "1");
        creditorInfo.setIdType(requestDto.getIdType() != null && !requestDto.getIdType().trim().isEmpty()
                ? requestDto.getIdType().trim() : "BVN");
        creditorInfo.setIdValue(requestDto.getIdValue() != null && !requestDto.getIdValue().trim().isEmpty()
                ? requestDto.getIdValue().trim() : "22298546518");
        creditorInfo.setAccountTier(requestDto.getAccountTier() != null && !requestDto.getAccountTier().trim().isEmpty()
                ? requestDto.getAccountTier().trim() : "1");
        customData.setCreditorInfo(creditorInfo);
        
        PaymentInitiation.TransactionInfo transactionInfo = new PaymentInitiation.TransactionInfo();
        transactionInfo.setTransactionLocation(requestDto.getTransactionLocation() != null && !requestDto.getTransactionLocation().trim().isEmpty()
                ? requestDto.getTransactionLocation().trim() : "013223231333");
        transactionInfo.setChannelCode(requestDto.getChannelCode() != null && !requestDto.getChannelCode().trim().isEmpty()
                ? requestDto.getChannelCode().trim() : "2");
        transactionInfo.setFixedCollectionAmount(requestDto.getFixedCollectionAmount() != null ? requestDto.getFixedCollectionAmount() : false);
        if (requestDto.getMandateCode() != null && !requestDto.getMandateCode().trim().isEmpty()) {
            transactionInfo.setMandateCode(requestDto.getMandateCode().trim());
        } else {
            transactionInfo.setMandateCode("0000004/001/0000070986");
        }
        customData.setTransactionInfo(transactionInfo);
        
        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);
        
        // --- Assemble Document ---
        cstmrCdtTrfInitn.setGrpHdr(grpHdr);
        cstmrCdtTrfInitn.setPmtInf(pmtInf);
        cstmrCdtTrfInitn.setSplmtryData(splmtryData);
        doc.setCstmrCdtTrfInitn(cstmrCdtTrfInitn);
        
        return doc;
    }
}
