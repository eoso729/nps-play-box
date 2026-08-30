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
        grpHdr.setNbOfTxs(1); // Fixed value
        grpHdr.setCtrlSum(amount); // Use dynamic amount
        
        // Initiating Party
        PaymentInitiation.InitgPty initgPty = new PaymentInitiation.InitgPty();
        initgPty.setNm("Musa"); // Fixed value
        PaymentInitiation.Id id = new PaymentInitiation.Id();
        PaymentInitiation.OrgId orgId = new PaymentInitiation.OrgId();
        PaymentInitiation.Othr othr = new PaymentInitiation.Othr();
        PaymentInitiation.SchmeNm schmeNm = new PaymentInitiation.SchmeNm();
        schmeNm.setCd(requestDto.getInitiatorId());
        othr.setSchmeNm(schmeNm);
        orgId.setOthr(othr);
        id.setOrgId(orgId);
        initgPty.setId(id);
        grpHdr.setInitgPty(initgPty);
        
        // Forwarding Agent
        PaymentInitiation.FwdgAgt fwdgAgt = new PaymentInitiation.FwdgAgt();
        PaymentInitiation.FinInstnId fwdgFinInstnId = new PaymentInitiation.FinInstnId();
        fwdgFinInstnId.setBicfi("FWDGAGT"); // Fixed value
        fwdgAgt.setFinInstnId(fwdgFinInstnId);
        grpHdr.setFwdgAgt(fwdgAgt);
        
        // --- Payment Info ---
        PaymentInitiation.PmtInf pmtInf = new PaymentInitiation.PmtInf();
        pmtInf.setPmtInfId("PMT-20251016-001-SINGLE"); // Fixed value
        pmtInf.setPmtMtd("TRF"); // Fixed value
        pmtInf.setBtchBookg(false); // Fixed value
        pmtInf.setNbOfTxs(1); // Fixed value
        pmtInf.setCtrlSum(amount); // Use dynamic amount
        
        // Requested Execution Date
        PaymentInitiation.ReqdExctnDt reqdExctnDtObj = new PaymentInitiation.ReqdExctnDt();
        reqdExctnDtObj.setDt(reqdExctnDt + "Z");
        pmtInf.setReqdExctnDt(reqdExctnDtObj);
        
        // Debtor
        PaymentInitiation.Dbtr dbtr = new PaymentInitiation.Dbtr();
        dbtr.setNm("Musa"); // Fixed value
        pmtInf.setDbtr(dbtr);
        
        // Debtor Account
        PaymentInitiation.DbtrAcct dbtrAcct = new PaymentInitiation.DbtrAcct();
        PaymentInitiation.AcctId dbtrAcctId = new PaymentInitiation.AcctId();
        dbtrAcctId.setIban("0177136558"); // Debtor account
        dbtrAcct.setId(dbtrAcctId);
        dbtrAcct.setNm("Musa"); // Fixed value
        pmtInf.setDbtrAcct(dbtrAcct);
        
        // Debtor Agent
        PaymentInitiation.DbtrAgt dbtrAgt = new PaymentInitiation.DbtrAgt();
        PaymentInitiation.FinInstnId dbtrFinInstnId = new PaymentInitiation.FinInstnId();
        dbtrFinInstnId.setBicfi(requestDto.getDebtorId());
        PaymentInitiation.ClrSysMmbId dbtrClrSysMmbId = new PaymentInitiation.ClrSysMmbId();
        dbtrClrSysMmbId.setMmbId(requestDto.getDebtorId());
        dbtrFinInstnId.setClrSysMmbId(dbtrClrSysMmbId);
        dbtrAgt.setFinInstnId(dbtrFinInstnId);
        pmtInf.setDbtrAgt(dbtrAgt);
        
        // Charge Bearer
        pmtInf.setChrgBr("SLEV"); // Fixed value
        
        // --- Credit Transfer Transaction Info ---
        PaymentInitiation.CdtTrfTxInf cdtTrfTxInf = new PaymentInitiation.CdtTrfTxInf();
        
        // Payment ID
        PaymentInitiation.PmtId pmtId = new PaymentInitiation.PmtId();
        pmtId.setEndToEndId(endToEndId);
        cdtTrfTxInf.setPmtId(pmtId);
        
        // Amount
        PaymentInitiation.Amt amt = new PaymentInitiation.Amt();
        PaymentInitiation.InstdAmt instdAmt = new PaymentInitiation.InstdAmt();
        instdAmt.setCcy("NGN"); // Fixed value
        instdAmt.setValue(amount); // Use dynamic amount
        amt.setInstdAmt(instdAmt);
        cdtTrfTxInf.setAmt(amt);
        
        // Creditor Agent
        PaymentInitiation.CdtrAgt cdtrAgt = new PaymentInitiation.CdtrAgt();
        PaymentInitiation.FinInstnId cdtrFinInstnId = new PaymentInitiation.FinInstnId();
        cdtrFinInstnId.setBicfi(requestDto.getCreditorId());
        PaymentInitiation.ClrSysMmbId cdtrClrSysMmbId = new PaymentInitiation.ClrSysMmbId();
        cdtrClrSysMmbId.setMmbId(requestDto.getCreditorId());
        cdtrFinInstnId.setClrSysMmbId(cdtrClrSysMmbId);
        cdtrAgt.setFinInstnId(cdtrFinInstnId);
        cdtTrfTxInf.setCdtrAgt(cdtrAgt);
        
        // Creditor
        PaymentInitiation.Cdtr cdtr = new PaymentInitiation.Cdtr();
        cdtr.setNm("James"); // Fixed value
        cdtTrfTxInf.setCdtr(cdtr);
        
        // Creditor Account
        PaymentInitiation.CdtrAcct cdtrAcct = new PaymentInitiation.CdtrAcct();
        PaymentInitiation.AcctId cdtrAcctId = new PaymentInitiation.AcctId();
//        cdtrAcctId.setIban("0693712114"); // Fixed value
        cdtrAcctId.setIban("3157417712"); // Fixed value
        cdtrAcct.setId(cdtrAcctId);
        cdtrAcct.setNm("James"); // Fixed value
        cdtTrfTxInf.setCdtrAcct(cdtrAcct);
        
        // Remittance Info
        PaymentInitiation.RmtInf rmtInf = new PaymentInitiation.RmtInf();
        rmtInf.setUstrd("Invoice 12345 (single)"); // Fixed value
        cdtTrfTxInf.setRmtInf(rmtInf);
        
        pmtInf.setCdtTrfTxInf(cdtTrfTxInf);
        
        // --- Supplementary Data ---
        PaymentInitiation.SplmtryData splmtryData = new PaymentInitiation.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails"); // Fixed value
        PaymentInitiation.Envlp envlp = new PaymentInitiation.Envlp();
        PaymentInitiation.CustomData customData = new PaymentInitiation.CustomData();
        
        // Creditor Info - Fixed values
        PaymentInitiation.CreditorInfo creditorInfo = new PaymentInitiation.CreditorInfo();
        creditorInfo.setAccountDesignation("1");
        creditorInfo.setIdType("BVN");
        creditorInfo.setIdValue("22298546518");
        creditorInfo.setAccountTier("1");
        customData.setCreditorInfo(creditorInfo);
        
        // Transaction Info - Fixed values
        PaymentInitiation.TransactionInfo transactionInfo = new PaymentInitiation.TransactionInfo();
        transactionInfo.setTransactionLocation("013223231333");
        transactionInfo.setChannelCode("2");
        transactionInfo.setFixedCollectionAmount(false);
        transactionInfo.setMandateCode("0000004/001/0000070986");
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
