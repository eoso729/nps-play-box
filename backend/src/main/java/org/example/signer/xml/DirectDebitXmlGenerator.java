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
        String initName = requestDto.getInitiatingPartyName() != null && !requestDto.getInitiatingPartyName().trim().isEmpty()
                ? requestDto.getInitiatingPartyName().trim()
                : (requestDto.getCreditorName() != null ? requestDto.getCreditorName().trim() : "ACME BILLING LIMITED");
        initgPty.setNm(initName);
        grpHdr.setInitgPty(initgPty);

        // Forwarding Agent (optional)
        if (requestDto.getForwardingAgentBIC() != null && !requestDto.getForwardingAgentBIC().trim().isEmpty()) {
            DirectDebit.FwdgAgt fwdgAgt = new DirectDebit.FwdgAgt();
            DirectDebit.FinInstnId fwdgFinInstnId = new DirectDebit.FinInstnId();
            fwdgFinInstnId.setBicfi(requestDto.getForwardingAgentBIC().trim());
            fwdgAgt.setFinInstnId(fwdgFinInstnId);
            grpHdr.setFwdgAgt(fwdgAgt);
        }

        cstmrDrctDbtInitn.setGrpHdr(grpHdr);

        // --- Payment Info ---
        DirectDebit.PmtInf pmtInf = new DirectDebit.PmtInf();
        String pmtInfId = requestDto.getPaymentInformationId() != null && !requestDto.getPaymentInformationId().trim().isEmpty()
                ? requestDto.getPaymentInformationId().trim()
                : "026-071-67895-001-00022";
        if (pmtInfId.length() > 35) pmtInfId = pmtInfId.substring(0, 35);
        pmtInf.setPmtInfId(pmtInfId);
        pmtInf.setPmtMtd("DD");
        pmtInf.setNbOfTxs(1);
        pmtInf.setCtrlSum(amount);

        // Payment Type Information
        DirectDebit.PmtTpInf pmtTpInf = new DirectDebit.PmtTpInf();
        DirectDebit.SvcLvl svcLvl = new DirectDebit.SvcLvl();
        svcLvl.setCd(requestDto.getServiceLevelCode() != null && !requestDto.getServiceLevelCode().trim().isEmpty()
                ? requestDto.getServiceLevelCode().trim() : "NURG");
        pmtTpInf.setSvcLvl(svcLvl);
        DirectDebit.LclInstrm lclInstrm = new DirectDebit.LclInstrm();
        lclInstrm.setPrtry(requestDto.getLocalInstrumentCode() != null && !requestDto.getLocalInstrumentCode().trim().isEmpty()
                ? requestDto.getLocalInstrumentCode().trim() : "NPSDD");
        pmtTpInf.setLclInstrm(lclInstrm);
        pmtTpInf.setSeqTp(requestDto.getSequenceType() != null && !requestDto.getSequenceType().trim().isEmpty()
                ? requestDto.getSequenceType().trim() : "FRST");
        pmtInf.setPmtTpInf(pmtTpInf);

        // Requested Collection Date
        String reqdColltnDt = requestDto.getRequestedCollectionDate() != null && !requestDto.getRequestedCollectionDate().trim().isEmpty()
                ? requestDto.getRequestedCollectionDate().trim()
                : (requestDto.getFrstColltnDt() != null ? requestDto.getFrstColltnDt().trim() : "2025-02-16Z");
        pmtInf.setReqdColltnDt(reqdColltnDt);

        // Creditor
        DirectDebit.Cdtr cdtr = new DirectDebit.Cdtr();
        String cdtrName = requestDto.getCreditorName() != null && !requestDto.getCreditorName().trim().isEmpty()
                ? requestDto.getCreditorName().trim() : "ACME BILLING LIMITED";
        cdtr.setNm(cdtrName);
        pmtInf.setCdtr(cdtr);

        // Creditor Account
        DirectDebit.CdtrAcct cdtrAcct = new DirectDebit.CdtrAcct();
        DirectDebit.AcctId cdtrAcctId = new DirectDebit.AcctId();
        cdtrAcctId.setIban(requestDto.getCreditorIban() != null ? requestDto.getCreditorIban().trim() : "3157417712");
        cdtrAcct.setId(cdtrAcctId);
        String ccy = requestDto.getCurrency() != null && !requestDto.getCurrency().trim().isEmpty()
                ? requestDto.getCurrency().trim() : "NGN";
        cdtrAcct.setCcy(ccy);
        pmtInf.setCdtrAcct(cdtrAcct);

        // Creditor Agent
        DirectDebit.CdtrAgt cdtrAgt = new DirectDebit.CdtrAgt();
        DirectDebit.FinInstnId cdtrFinInstnId = new DirectDebit.FinInstnId();
        String srcInst = requestDto.getCreditorId() != null && !requestDto.getCreditorId().trim().isEmpty()
                ? requestDto.getCreditorId().trim()
                : (requestDto.getInitiatorId() != null ? requestDto.getInitiatorId().trim() : "999057");
        String cdtrBic = requestDto.getCreditorAgentBIC() != null && !requestDto.getCreditorAgentBIC().trim().isEmpty()
                ? requestDto.getCreditorAgentBIC().trim() : srcInst;
        cdtrFinInstnId.setBicfi(cdtrBic);
        DirectDebit.ClrSysMmbId cdtrClrSysMmbId = new DirectDebit.ClrSysMmbId();
        cdtrClrSysMmbId.setMmbId(srcInst);
        cdtrFinInstnId.setClrSysMmbId(cdtrClrSysMmbId);
        cdtrAgt.setFinInstnId(cdtrFinInstnId);
        pmtInf.setCdtrAgt(cdtrAgt);

        // --- Direct Debit Transaction Information ---
        DirectDebit.DrctDbtTxInf drctDbtTxInf = new DirectDebit.DrctDbtTxInf();

        // Payment ID
        DirectDebit.PmtId pmtId = new DirectDebit.PmtId();
        String pmtInstrId = requestDto.getInstructionId() != null && !requestDto.getInstructionId().trim().isEmpty()
                ? requestDto.getInstructionId().trim() : instrId;
        pmtId.setInstrId(pmtInstrId);
        String pmtEndToEndId = requestDto.getEndToEndId() != null && !requestDto.getEndToEndId().trim().isEmpty()
                ? requestDto.getEndToEndId().trim() : endToEndId;
        pmtId.setEndToEndId(pmtEndToEndId);
        drctDbtTxInf.setPmtId(pmtId);

        // Instructed Amount
        DirectDebit.InstdAmt instdAmt = new DirectDebit.InstdAmt();
        instdAmt.setCcy(ccy);
        instdAmt.setValue(amount);
        drctDbtTxInf.setInstdAmt(instdAmt);

        // Direct Debit Transaction Details
        DirectDebit.DrctDbtTx drctDbtTx = new DirectDebit.DrctDbtTx();
        DirectDebit.MndtRltdInf mndtRltdInf = new DirectDebit.MndtRltdInf();
        mndtRltdInf.setMndtId(requestDto.getMandateId() != null ? requestDto.getMandateId().trim() : "0000004/001/0000070986");
        mndtRltdInf.setDtOfSgntr(requestDto.getDtOfSgntr() != null ? requestDto.getDtOfSgntr().trim() : "2025-02-01Z");
        if (requestDto.getFrstColltnDt() != null && !requestDto.getFrstColltnDt().trim().isEmpty()) {
            mndtRltdInf.setFrstColltnDt(requestDto.getFrstColltnDt().trim());
        }
        if (requestDto.getFnlColltnDt() != null && !requestDto.getFnlColltnDt().trim().isEmpty()) {
            mndtRltdInf.setFnlColltnDt(requestDto.getFnlColltnDt().trim());
        }
        DirectDebit.Frqcy frqcy = new DirectDebit.Frqcy();
        frqcy.setTp(requestDto.getFreqTp() != null ? requestDto.getFreqTp().trim() : "MNTH");
        mndtRltdInf.setFrqcy(frqcy);
        drctDbtTx.setMndtRltdInf(mndtRltdInf);
        drctDbtTxInf.setDrctDbtTx(drctDbtTx);

        // Debtor Agent
        DirectDebit.DbtrAgt dbtrAgt = new DirectDebit.DbtrAgt();
        DirectDebit.FinInstnId dbtrFinInstnId = new DirectDebit.FinInstnId();
        DirectDebit.ClrSysMmbId dbtrClrSysMmbId = new DirectDebit.ClrSysMmbId();
        String dstInst = requestDto.getDebtorId() != null && !requestDto.getDebtorId().trim().isEmpty()
                ? requestDto.getDebtorId().trim() : "999058";
        dbtrClrSysMmbId.setMmbId(dstInst);
        dbtrFinInstnId.setClrSysMmbId(dbtrClrSysMmbId);
        dbtrAgt.setFinInstnId(dbtrFinInstnId);
        drctDbtTxInf.setDbtrAgt(dbtrAgt);

        // Debtor
        DirectDebit.Dbtr dbtr = new DirectDebit.Dbtr();
        dbtr.setNm(requestDto.getDebtorName() != null ? requestDto.getDebtorName().trim() : "JOHN DOE");
        drctDbtTxInf.setDbtr(dbtr);

        // Debtor Account
        DirectDebit.DbtrAcct dbtrAcct = new DirectDebit.DbtrAcct();
        DirectDebit.AcctId dbtrAcctId = new DirectDebit.AcctId();
        if (requestDto.getDebtorIban() != null && !requestDto.getDebtorIban().trim().isEmpty()) {
            dbtrAcctId.setIban(requestDto.getDebtorIban().trim());
        }
        if (requestDto.getOtherAccountIdentifier() != null && !requestDto.getOtherAccountIdentifier().trim().isEmpty()) {
            DirectDebit.Othr othr = new DirectDebit.Othr();
            othr.setId(requestDto.getOtherAccountIdentifier().trim());
            dbtrAcctId.setOthr(othr);
        }
        if (dbtrAcctId.getIban() == null && dbtrAcctId.getOthr() == null) {
            dbtrAcctId.setIban("0177136558");
        }
        dbtrAcct.setId(dbtrAcctId);
        dbtrAcct.setCcy(ccy);
        drctDbtTxInf.setDbtrAcct(dbtrAcct);

        // Remittance Info
        if (requestDto.getRemittanceInfo() != null && !requestDto.getRemittanceInfo().trim().isEmpty()) {
            DirectDebit.RmtInf rmtInf = new DirectDebit.RmtInf();
            rmtInf.setUstrd(requestDto.getRemittanceInfo().trim());
            drctDbtTxInf.setRmtInf(rmtInf);
        }

        pmtInf.setDrctDbtTxInf(drctDbtTxInf);
        cstmrDrctDbtInitn.setPmtInf(pmtInf);

        // --- Supplementary Data ---
        DirectDebit.SplmtryData splmtryData = new DirectDebit.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        DirectDebit.Envlp envlp = new DirectDebit.Envlp();
        DirectDebit.CustomData customData = new DirectDebit.CustomData();

        // Debtor Info
        DirectDebit.DebtorInfo debtorInfo = new DirectDebit.DebtorInfo();
        debtorInfo.setAccountDesignation(requestDto.getDebtorAccountDesignation() != null ? requestDto.getDebtorAccountDesignation().trim() : "1");
        debtorInfo.setIdType(requestDto.getDebtorIdType() != null ? requestDto.getDebtorIdType().trim() : "BVN");
        debtorInfo.setIdValue(requestDto.getDebtorIdValue() != null ? requestDto.getDebtorIdValue().trim() : "22222222222");
        debtorInfo.setAccountTier(requestDto.getDebtorAccountTier() != null ? requestDto.getDebtorAccountTier().trim() : "1");
        customData.setDebtorInfo(debtorInfo);

        // Debtor Metadata
        DirectDebit.DebtorMetadata debtorMetadata = new DirectDebit.DebtorMetadata();
        debtorMetadata.setBiometricData(requestDto.getDebtorBiometricData() != null ? requestDto.getDebtorBiometricData().trim() : "");
        customData.setDebtorMetadata(debtorMetadata);

        // Creditor Info
        DirectDebit.CreditorInfo creditorInfo = new DirectDebit.CreditorInfo();
        creditorInfo.setAccountDesignation(requestDto.getCreditorAccountDesignation() != null ? requestDto.getCreditorAccountDesignation().trim() : "1");
        creditorInfo.setIdType(requestDto.getCreditorIdType() != null ? requestDto.getCreditorIdType().trim() : "BVN");
        creditorInfo.setIdValue(requestDto.getCreditorIdValue() != null ? requestDto.getCreditorIdValue().trim() : "22222222222");
        creditorInfo.setAccountTier(requestDto.getCreditorAccountTier() != null ? requestDto.getCreditorAccountTier().trim() : "1");
        customData.setCreditorInfo(creditorInfo);

        // Creditor Metadata
        customData.setCreditorMetadata(new DirectDebit.CreditorMetadata());

        // Transaction Info
        DirectDebit.TransactionInfo transactionInfo = new DirectDebit.TransactionInfo();
        transactionInfo.setTransactionLocation(requestDto.getTransactionLocation() != null ? requestDto.getTransactionLocation().trim() : "013223231333");
        transactionInfo.setNameEnquiryMsgId(requestDto.getNameEnquiryMsgId() != null ? requestDto.getNameEnquiryMsgId().trim() : (srcInst + "20251104552022522202020201500"));
        transactionInfo.setChannelCode(requestDto.getChannelCode() != null ? requestDto.getChannelCode().trim() : "4");
        transactionInfo.setFixedCollectionAmount(requestDto.getFixedCollectionAmount() != null ? requestDto.getFixedCollectionAmount() : false);
        transactionInfo.setMandateCode(requestDto.getMandateCode() != null ? requestDto.getMandateCode().trim() : (requestDto.getMandateId() != null ? requestDto.getMandateId().trim() : "0000004/001/0000070986"));
        customData.setTransactionInfo(transactionInfo);

        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);
        cstmrDrctDbtInitn.setSplmtryData(splmtryData);

        doc.setCstmrDrctDbtInitn(cstmrDrctDbtInitn);

        return doc;
    }
}
