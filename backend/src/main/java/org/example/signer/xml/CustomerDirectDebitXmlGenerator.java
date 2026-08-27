package org.example.signer.xml;

import org.example.signer.dto.CustomerDirectDebitRequestDto;
import org.example.signer.model.CustomerDirectDebit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class CustomerDirectDebitXmlGenerator {

    public static CustomerDirectDebit generate(CustomerDirectDebitRequestDto requestDto, String msgId) {
        CustomerDirectDebit doc = new CustomerDirectDebit();
        CustomerDirectDebit.FIToFICstmrDrctDbt req = new CustomerDirectDebit.FIToFICstmrDrctDbt();

        // --- Group Header ---
        CustomerDirectDebit.GrpHdr grpHdr = new CustomerDirectDebit.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = java.time.ZonedDateTime.now(java.time.ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);
        grpHdr.setNbOfTxs(1);
        BigDecimal amount = requestDto.getAmount() != null ? requestDto.getAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("2200.00");
        grpHdr.setCtrlSum(amount);

        String instgAgtId = requestDto.getInstructingBankMemberId() != null ? requestDto.getInstructingBankMemberId() : "999998";
        String instdAgtId = requestDto.getDebtorBankMemberId() != null ? requestDto.getDebtorBankMemberId() : "999999";

        CustomerDirectDebit.Agent instgAgt = createAgent(instgAgtId);
        grpHdr.setInstgAgt(instgAgt);
        CustomerDirectDebit.Agent instdAgt = createAgent(instdAgtId);
        grpHdr.setInstdAgt(instdAgt);
        req.setGrpHdr(grpHdr);

        // --- Transaction Info ---
        CustomerDirectDebit.DrctDbtTxInf txInf = new CustomerDirectDebit.DrctDbtTxInf();
        
        CustomerDirectDebit.PmtId pmtId = new CustomerDirectDebit.PmtId();
        pmtId.setInstrId(generateId(requestDto.getSourceId() + requestDto.getDestinationId(), 9));
        pmtId.setEndToEndId(generateId(requestDto.getSourceId(), 15));
        pmtId.setTxId(requestDto.getTransactionId() != null ? requestDto.getTransactionId() : msgId);
        txInf.setPmtId(pmtId);

        String currency = requestDto.getCurrency() != null ? requestDto.getCurrency() : "NGN";
        
        CustomerDirectDebit.Amount sttlmAmt = new CustomerDirectDebit.Amount();
        sttlmAmt.setCcy(currency);
        sttlmAmt.setValue(amount);
        txInf.setIntrBkSttlmAmt(sttlmAmt);
        
        txInf.setIntrBkSttlmDt(requestDto.getSettlementDate() != null ? requestDto.getSettlementDate() : "2026-01-27");
        
        CustomerDirectDebit.Amount instdAmt = new CustomerDirectDebit.Amount();
        instdAmt.setCcy(currency);
        instdAmt.setValue(amount);
        txInf.setInstdAmt(instdAmt);

        // Direct Debit Transaction Details
        CustomerDirectDebit.DrctDbtTx drctDbtTx = new CustomerDirectDebit.DrctDbtTx();
        CustomerDirectDebit.MndtRltdInf mndtRltdInf = new CustomerDirectDebit.MndtRltdInf();
        mndtRltdInf.setMndtId(requestDto.getMandateId() != null ? requestDto.getMandateId() : "MNDT-RCUR-00035");
        mndtRltdInf.setDtOfSgntr(requestDto.getDateOfSignature() != null ? requestDto.getDateOfSignature() : "2026-01-27");
        mndtRltdInf.setFrstColltnDt(requestDto.getFirstCollectionDate() != null ? requestDto.getFirstCollectionDate() : "2026-01-27");
        mndtRltdInf.setFnlColltnDt(requestDto.getFinalCollectionDate() != null ? requestDto.getFinalCollectionDate() : "2026-01-28");
        CustomerDirectDebit.Frqcy frqcy = new CustomerDirectDebit.Frqcy();
        frqcy.setTp(requestDto.getFrequencyType() != null ? requestDto.getFrequencyType() : "WEEK");
        mndtRltdInf.setFrqcy(frqcy);
        drctDbtTx.setMndtRltdInf(mndtRltdInf);
        txInf.setDrctDbtTx(drctDbtTx);

        // Creditor
        CustomerDirectDebit.Party cdtr = new CustomerDirectDebit.Party();
        String cdrNm = requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "Netflix";
        cdtr.setNm(cdrNm);
        txInf.setCdtr(cdtr);

        CustomerDirectDebit.CashAccount cdtrAcct = new CustomerDirectDebit.CashAccount();
        CustomerDirectDebit.AccountId cdtrAcctId = new CustomerDirectDebit.AccountId();
        cdtrAcctId.setIban(requestDto.getCreditorAccountNumber() != null ? requestDto.getCreditorAccountNumber() : "0987654320");
        cdtrAcct.setId(cdtrAcctId);
        cdtrAcct.setNm(cdrNm);
        txInf.setCdtrAcct(cdtrAcct);

        txInf.setCdtrAgt(createAgent(instgAgtId));
        txInf.setInstgAgt(createAgent(instgAgtId));
        txInf.setInstdAgt(createAgent(instdAgtId));

        // Debtor
        CustomerDirectDebit.Party dbtr = new CustomerDirectDebit.Party();
        String dbtNm = requestDto.getDebtorName() != null ? requestDto.getDebtorName() : "Ponmile Joy";
        dbtr.setNm(dbtNm);
        txInf.setDbtr(dbtr);

        CustomerDirectDebit.CashAccount dbtrAcct = new CustomerDirectDebit.CashAccount();
        CustomerDirectDebit.AccountId dbtrAcctId = new CustomerDirectDebit.AccountId();
        dbtrAcctId.setIban(requestDto.getDebtorAccountNumber() != null ? requestDto.getDebtorAccountNumber() : "3157417712");
        dbtrAcct.setId(dbtrAcctId);
        dbtrAcct.setNm(dbtNm);
        txInf.setDbtrAcct(dbtrAcct);

        txInf.setDbtrAgt(createAgent(instdAgtId));

        // Remittance
        CustomerDirectDebit.RmtInf rmtInf = new CustomerDirectDebit.RmtInf();
        rmtInf.setUstrd(requestDto.getNarration() != null ? requestDto.getNarration() : "Invoice INV-2025-001");
        txInf.setRmtInf(rmtInf);

        req.setDrctDbtTxInf(txInf);

        // --- Supplementary Data ---
        CustomerDirectDebit.SplmtryData splmtryData = new CustomerDirectDebit.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        CustomerDirectDebit.Envlp envlp = new CustomerDirectDebit.Envlp();
        CustomerDirectDebit.CustomData customData = new CustomerDirectDebit.CustomData();

        CustomerDirectDebit.DebtorInfo debtorInfo = new CustomerDirectDebit.DebtorInfo();
        debtorInfo.setAccountDesignation(requestDto.getDebtorAccountDesignation() != null ? requestDto.getDebtorAccountDesignation() : "1");
        debtorInfo.setIdType(requestDto.getDebtorIdType() != null ? requestDto.getDebtorIdType() : "BVN");
        debtorInfo.setIdValue(requestDto.getDebtorIdValue() != null ? requestDto.getDebtorIdValue() : "11111111145");
        debtorInfo.setAccountTier(requestDto.getDebtorAccountTier() != null ? requestDto.getDebtorAccountTier() : "1");
        customData.setDebtorInfo(debtorInfo);

        CustomerDirectDebit.DebtorMetadata debtorMetadata = new CustomerDirectDebit.DebtorMetadata();
        debtorMetadata.setBiometricData(requestDto.getDebtorBiometricData() != null ? requestDto.getDebtorBiometricData() : "");
        customData.setDebtorMetadata(debtorMetadata);

        CustomerDirectDebit.CreditorInfo creditorInfo = new CustomerDirectDebit.CreditorInfo();
        creditorInfo.setAccountDesignation(requestDto.getCreditorAccountDesignation() != null ? requestDto.getCreditorAccountDesignation() : "1");
        creditorInfo.setIdType(requestDto.getCreditorIdType() != null ? requestDto.getCreditorIdType() : "BVN");
        creditorInfo.setIdValue(requestDto.getCreditorIdValue() != null ? requestDto.getCreditorIdValue() : "11111111145");
        creditorInfo.setAccountTier(requestDto.getCreditorAccountTier() != null ? requestDto.getCreditorAccountTier() : "1");
        customData.setCreditorInfo(creditorInfo);

        customData.setCreditorMetadata("");

        CustomerDirectDebit.TransactionInfo transactionInfo = new CustomerDirectDebit.TransactionInfo();
        transactionInfo.setTransactionLocation(requestDto.getTransactionLocation() != null ? requestDto.getTransactionLocation() : "01080652440N020900337921E");
        transactionInfo.setNameEnquiryMsgId(requestDto.getNameEnquiryMsgId() != null ? requestDto.getNameEnquiryMsgId() : "99999820260127101157171722205219993");
        transactionInfo.setChannelCode(requestDto.getChannelCode() != null ? requestDto.getChannelCode() : "1");
        transactionInfo.setRiskRating(requestDto.getRiskRating() != null ? requestDto.getRiskRating() : "R000000000000000000B9");
        transactionInfo.setFixedCollectionAmount(requestDto.getFixedCollectionAmount() != null ? requestDto.getFixedCollectionAmount() : "Yes");
        customData.setTransactionInfo(transactionInfo);

        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);
        req.setSplmtryData(splmtryData);

        doc.setFiToFICstmrDrctDbt(req);
        return doc;
    }

    private static CustomerDirectDebit.Agent createAgent(String memberId) {
        CustomerDirectDebit.Agent agent = new CustomerDirectDebit.Agent();
        CustomerDirectDebit.FinInstnId finInstnId = new CustomerDirectDebit.FinInstnId();
        finInstnId.setBicfi(memberId);
        CustomerDirectDebit.ClrSysMmbId clrSysMmbId = new CustomerDirectDebit.ClrSysMmbId();
        clrSysMmbId.setMmbId(memberId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        agent.setFinInstnId(finInstnId);
        return agent;
    }

    private static String generateId(String prefix, int randomLength) {
        Random random = new Random();
        StringBuilder randomDigits = new StringBuilder();
        for (int i = 0; i < randomLength; i++) {
            randomDigits.append(random.nextInt(10));
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter msgIdFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String msgIdTimestamp = now.format(msgIdFormatter);

        return prefix + msgIdTimestamp + randomDigits.toString();
    }
}
