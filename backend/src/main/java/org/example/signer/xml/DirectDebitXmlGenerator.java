package org.example.signer.xml;

import org.example.signer.dto.DirectDebitRequestDto;
import org.example.signer.model.DirectDebit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DirectDebitXmlGenerator {

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("100.00");

    public static DirectDebit generate(DirectDebitRequestDto requestDto, String msgId, String endToEndId, String instrId) {
        BigDecimal amount = (requestDto.getAmount() != null && requestDto.getAmount().compareTo(BigDecimal.ZERO) > 0)
                ? requestDto.getAmount().setScale(2, RoundingMode.HALF_UP)
                : DEFAULT_AMOUNT;

        DirectDebit doc = new DirectDebit();
        DirectDebit.CstmrDrctDbtInitn cstmrDrctDbtInitn = new DirectDebit.CstmrDrctDbtInitn();

        // --- Group Header ---
        DirectDebit.GrpHdr grpHdr = new DirectDebit.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = java.time.ZonedDateTime.now(ZoneId.of("Africa/Lagos"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);
        grpHdr.setNbOfTxs(1);
        grpHdr.setCtrlSum(amount);

        // Initiating Party
        DirectDebit.InitgPty initgPty = new DirectDebit.InitgPty();
        initgPty.setNm(requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "ACME BILLING LIMITED");
        grpHdr.setInitgPty(initgPty);

        // Forwarding Agent
        DirectDebit.FwdgAgt fwdgAgt = new DirectDebit.FwdgAgt();
        DirectDebit.FinInstnId fwdgFinInstnId = new DirectDebit.FinInstnId();
        fwdgFinInstnId.setBicfi("FWDGAGT");
        fwdgAgt.setFinInstnId(fwdgFinInstnId);
        grpHdr.setFwdgAgt(fwdgAgt);

        cstmrDrctDbtInitn.setGrpHdr(grpHdr);

        // --- Payment Info ---
        DirectDebit.PmtInf pmtInf = new DirectDebit.PmtInf();
        pmtInf.setPmtInfId("026-071-67895-001-00022"); // Example fixed value
        pmtInf.setPmtMtd("DD");
        pmtInf.setNbOfTxs(1);
        pmtInf.setCtrlSum(amount);

        // Payment Type Information
        DirectDebit.PmtTpInf pmtTpInf = new DirectDebit.PmtTpInf();
        DirectDebit.SvcLvl svcLvl = new DirectDebit.SvcLvl();
        svcLvl.setCd("NURG");
        pmtTpInf.setSvcLvl(svcLvl);
        DirectDebit.LclInstrm lclInstrm = new DirectDebit.LclInstrm();
        lclInstrm.setPrtry("NPSDD");
        pmtTpInf.setLclInstrm(lclInstrm);
        pmtTpInf.setSeqTp("FRST");
        pmtInf.setPmtTpInf(pmtTpInf);

        // Requested Collection Date
        pmtInf.setReqdColltnDt("2025-02-16Z"); // Example fixed value

        // Creditor
        DirectDebit.Cdtr cdtr = new DirectDebit.Cdtr();
        cdtr.setNm(requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "ACME BILLING LIMITED");
        pmtInf.setCdtr(cdtr);

        // Creditor Account
        DirectDebit.CdtrAcct cdtrAcct = new DirectDebit.CdtrAcct();
        DirectDebit.AcctId cdtrAcctId = new DirectDebit.AcctId();
        cdtrAcctId.setIban(requestDto.getCreditorIban() != null ? requestDto.getCreditorIban() : "3157417712");
        cdtrAcct.setId(cdtrAcctId);
        cdtrAcct.setCcy("NGN");
        pmtInf.setCdtrAcct(cdtrAcct);

        // Creditor Agent
        DirectDebit.CdtrAgt cdtrAgt = new DirectDebit.CdtrAgt();
        DirectDebit.FinInstnId cdtrFinInstnId = new DirectDebit.FinInstnId();
        cdtrFinInstnId.setBicfi("AA123456");
        DirectDebit.ClrSysMmbId cdtrClrSysMmbId = new DirectDebit.ClrSysMmbId();
        cdtrClrSysMmbId.setMmbId(requestDto.getCreditorId() != null ? requestDto.getCreditorId() : "000058");
        cdtrFinInstnId.setClrSysMmbId(cdtrClrSysMmbId);
        cdtrAgt.setFinInstnId(cdtrFinInstnId);
        pmtInf.setCdtrAgt(cdtrAgt);

        // --- Direct Debit Transaction Information ---
        DirectDebit.DrctDbtTxInf drctDbtTxInf = new DirectDebit.DrctDbtTxInf();

        // Payment ID
        DirectDebit.PmtId pmtId = new DirectDebit.PmtId();
        pmtId.setInstrId(instrId);
        pmtId.setEndToEndId(endToEndId);
        drctDbtTxInf.setPmtId(pmtId);

        // Instructed Amount
        DirectDebit.InstdAmt instdAmt = new DirectDebit.InstdAmt();
        instdAmt.setCcy("NGN");
        instdAmt.setValue(amount);
        drctDbtTxInf.setInstdAmt(instdAmt);

        // Direct Debit Transaction Details
        DirectDebit.DrctDbtTx drctDbtTx = new DirectDebit.DrctDbtTx();
        DirectDebit.MndtRltdInf mndtRltdInf = new DirectDebit.MndtRltdInf();
        mndtRltdInf.setMndtId(requestDto.getMandateId() != null ? requestDto.getMandateId() : "0000004/001/0000070986");
        mndtRltdInf.setDtOfSgntr(requestDto.getDtOfSgntr() != null ? requestDto.getDtOfSgntr() : "2025-02-01Z");
        mndtRltdInf.setFrstColltnDt(requestDto.getFrstColltnDt() != null ? requestDto.getFrstColltnDt() : "2026-04-16Z");
        mndtRltdInf.setFnlColltnDt(requestDto.getFnlColltnDt() != null ? requestDto.getFnlColltnDt() : "2026-12-31Z");
        DirectDebit.Frqcy frqcy = new DirectDebit.Frqcy();
        frqcy.setTp(requestDto.getFreqTp() != null ? requestDto.getFreqTp() : "MNTH");
        mndtRltdInf.setFrqcy(frqcy);
        drctDbtTx.setMndtRltdInf(mndtRltdInf);
        drctDbtTxInf.setDrctDbtTx(drctDbtTx);

        // Debtor Agent
        DirectDebit.DbtrAgt dbtrAgt = new DirectDebit.DbtrAgt();
        DirectDebit.FinInstnId dbtrFinInstnId = new DirectDebit.FinInstnId();
        DirectDebit.ClrSysMmbId dbtrClrSysMmbId = new DirectDebit.ClrSysMmbId();
        dbtrClrSysMmbId.setMmbId(requestDto.getDebtorId() != null ? requestDto.getDebtorId() : "999997");
        dbtrFinInstnId.setClrSysMmbId(dbtrClrSysMmbId);
        dbtrAgt.setFinInstnId(dbtrFinInstnId);
        drctDbtTxInf.setDbtrAgt(dbtrAgt);

        // Debtor
        DirectDebit.Dbtr dbtr = new DirectDebit.Dbtr();
        dbtr.setNm(requestDto.getDebtorName() != null ? requestDto.getDebtorName() : "JOHN DOE");
        drctDbtTxInf.setDbtr(dbtr);

        // Debtor Account
        DirectDebit.DbtrAcct dbtrAcct = new DirectDebit.DbtrAcct();
        DirectDebit.AcctId dbtrAcctId = new DirectDebit.AcctId();
        dbtrAcctId.setIban(requestDto.getDebtorIban() != null ? requestDto.getDebtorIban() : "0123456789");
        dbtrAcct.setId(dbtrAcctId);
        dbtrAcct.setCcy("NGN");
        drctDbtTxInf.setDbtrAcct(dbtrAcct);

        // Remittance Info
        DirectDebit.RmtInf rmtInf = new DirectDebit.RmtInf();
        rmtInf.setUstrd(requestDto.getRemittanceInfo() != null ? requestDto.getRemittanceInfo() : "UTILITY BILL FEB-2025");
        drctDbtTxInf.setRmtInf(rmtInf);

        pmtInf.setDrctDbtTxInf(drctDbtTxInf);
        cstmrDrctDbtInitn.setPmtInf(pmtInf);

        // --- Supplementary Data ---
        DirectDebit.SplmtryData splmtryData = new DirectDebit.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        DirectDebit.Envlp envlp = new DirectDebit.Envlp();
        DirectDebit.CustomData customData = new DirectDebit.CustomData();

        // Debtor Info
        DirectDebit.DebtorInfo debtorInfo = new DirectDebit.DebtorInfo();
        debtorInfo.setAccountDesignation("1");
        debtorInfo.setIdType("BVN");
        debtorInfo.setIdValue("22222222222");
        debtorInfo.setAccountTier("1");
        customData.setDebtorInfo(debtorInfo);

        // Debtor Metadata
        DirectDebit.DebtorMetadata debtorMetadata = new DirectDebit.DebtorMetadata();
        debtorMetadata.setBiometricData("");
        customData.setDebtorMetadata(debtorMetadata);

        // Creditor Info
        DirectDebit.CreditorInfo creditorInfo = new DirectDebit.CreditorInfo();
        creditorInfo.setAccountDesignation("1");
        creditorInfo.setIdType("BVN");
        creditorInfo.setIdValue("22222222222");
        creditorInfo.setAccountTier("1");
        customData.setCreditorInfo(creditorInfo);

        // Creditor Metadata
        customData.setCreditorMetadata(new DirectDebit.CreditorMetadata());

        // Transaction Info
        DirectDebit.TransactionInfo transactionInfo = new DirectDebit.TransactionInfo();
        transactionInfo.setTransactionLocation("013223231333");
        transactionInfo.setNameEnquiryMsgId(requestDto.getNameEnquiryMsgId() != null ? requestDto.getNameEnquiryMsgId() : "99905820251104552022522202020202015");
        transactionInfo.setChannelCode("4");
        transactionInfo.setFixedCollectionAmount(false);
        transactionInfo.setMandateCode(requestDto.getMandateId() != null ? requestDto.getMandateId() : "0000004/001/0000070986");
        customData.setTransactionInfo(transactionInfo);

        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);
        cstmrDrctDbtInitn.setSplmtryData(splmtryData);

        doc.setCstmrDrctDbtInitn(cstmrDrctDbtInitn);

        return doc;
    }
}
