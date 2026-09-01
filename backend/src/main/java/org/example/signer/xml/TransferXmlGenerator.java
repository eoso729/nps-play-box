package org.example.signer.xml;

import org.example.signer.dto.TransferRequestDto;
import org.example.signer.model.Transfer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TransferXmlGenerator {

    public static Transfer generate(TransferRequestDto requestDto, String msgId) {
        Transfer doc = new Transfer();
        Transfer.FIToFICstmrCdtTrf fiToFICstmrCdtTrf = new Transfer.FIToFICstmrCdtTrf();
        Transfer.GrpHdr grpHdr = new Transfer.GrpHdr();
        Transfer.CdtTrfTxInf cdtTrfTxInf = new Transfer.CdtTrfTxInf();

        // --- Timestamps ---
        LocalDateTime now = LocalDateTime.now();
        String creDtTm = java.time.ZonedDateTime.now(java.time.ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        String intrBkSttlmDt = now.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // --- Group Header ---
        grpHdr.setMsgId(msgId);
        grpHdr.setCreDtTm(creDtTm);
        grpHdr.setBtchBookg(false);
        grpHdr.setNbOfTxs(1);
        Transfer.SttlmInf sttlmInf = new Transfer.SttlmInf();
        sttlmInf.setSttlmMtd(requestDto.getSettlementMethod() != null ? requestDto.getSettlementMethod() : "CLRG");
        grpHdr.setSttlmInf(sttlmInf);
        
        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";
        
        grpHdr.setInstgAgt(createAgt(srcId));
        grpHdr.setInstdAgt(createAgt(destId));

        // --- Credit Transfer Transaction Info ---
        Transfer.PmtId pmtId = new Transfer.PmtId();
        String instrId = requestDto.getInstructionId() != null ? requestDto.getInstructionId() : generateId(srcId + destId, 9);
        pmtId.setInstrId(instrId);
        pmtId.setEndToEndId(requestDto.getEndToEndId() != null ? requestDto.getEndToEndId() : instrId);
        pmtId.setTxId(msgId);
        cdtTrfTxInf.setPmtId(pmtId);

        Transfer.PmtTpInf pmtTpInf = new Transfer.PmtTpInf();
        pmtTpInf.setClrChanl(requestDto.getClearingChannel() != null ? requestDto.getClearingChannel() : "RTNS");
        Transfer.SvcLvl svcLvl = new Transfer.SvcLvl();
        svcLvl.setPrtry(requestDto.getServiceLevel() != null ? requestDto.getServiceLevel() : "0100");
        pmtTpInf.setSvcLvl(svcLvl);
        Transfer.LclInstrm lclInstrm = new Transfer.LclInstrm();
        lclInstrm.setPrtry(requestDto.getLocalInstrument() != null ? requestDto.getLocalInstrument() : "CTAA");
        pmtTpInf.setLclInstrm(lclInstrm);
        Transfer.CtgyPurp ctgyPurp = new Transfer.CtgyPurp();
        ctgyPurp.setPrtry(requestDto.getCategoryPurpose() != null ? requestDto.getCategoryPurpose() : "001");
        pmtTpInf.setCtgyPurp(ctgyPurp);
        cdtTrfTxInf.setPmtTpInf(pmtTpInf);

        Transfer.IntrBkSttlmAmt intrBkSttlmAmt = new Transfer.IntrBkSttlmAmt();
        intrBkSttlmAmt.setCcy(requestDto.getCurrency() != null ? requestDto.getCurrency() : "NGN");
        BigDecimal amtVal = requestDto.getAmount() != null ? requestDto.getAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("0.00");
        intrBkSttlmAmt.setValue(amtVal);
        cdtTrfTxInf.setIntrBkSttlmAmt(intrBkSttlmAmt);

        cdtTrfTxInf.setIntrBkSttlmDt(intrBkSttlmDt);
        cdtTrfTxInf.setChrgBr(requestDto.getChargeBearer() != null ? requestDto.getChargeBearer() : "SLEV");
        cdtTrfTxInf.setInstgAgt(createAgt(srcId));
        cdtTrfTxInf.setInstdAgt(createAgt(destId));

        Transfer.Dbtr dbtr = new Transfer.Dbtr();
        dbtr.setNm(requestDto.getSenderName() != null ? requestDto.getSenderName() : "Sender");
        cdtTrfTxInf.setDbtr(dbtr);

        Transfer.DbtrAcct dbtrAcct = new Transfer.DbtrAcct();
        Transfer.AcctId dbtrAcctId = new Transfer.AcctId();
        dbtrAcctId.setIban(requestDto.getSenderAccountNumber() != null ? requestDto.getSenderAccountNumber() : "0000000000");
        dbtrAcct.setId(dbtrAcctId);
        dbtrAcct.setNm(requestDto.getSenderAccountName() != null ? requestDto.getSenderAccountName() : dbtr.getNm());
        cdtTrfTxInf.setDbtrAcct(dbtrAcct);

        cdtTrfTxInf.setDbtrAgt(createAgt(srcId));
        cdtTrfTxInf.setCdtrAgt(createAgt(destId));

        Transfer.Cdtr cdtr = new Transfer.Cdtr();
        cdtr.setNm(requestDto.getBeneficiaryName() != null ? requestDto.getBeneficiaryName() : "Beneficiary");
        cdtTrfTxInf.setCdtr(cdtr);

        Transfer.CdtrAcct cdtrAcct = new Transfer.CdtrAcct();
        Transfer.AcctId cdtrAcctId = new Transfer.AcctId();
        cdtrAcctId.setIban(requestDto.getBeneficiaryAccountNumber() != null ? requestDto.getBeneficiaryAccountNumber() : "0000000000");
        cdtrAcct.setId(cdtrAcctId);
        cdtrAcct.setNm(requestDto.getBeneficiaryAccountName() != null ? requestDto.getBeneficiaryAccountName() : cdtr.getNm());
        cdtTrfTxInf.setCdtrAcct(cdtrAcct);

        Transfer.RmtInf rmtInf = new Transfer.RmtInf();
        rmtInf.setUstrd(requestDto.getNarration() != null ? requestDto.getNarration() : "Transfer");
        cdtTrfTxInf.setRmtInf(rmtInf);

        // --- Supplementary Data ---
        Transfer.SplmtryData splmtryData = new Transfer.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        Transfer.Envlp envlp = new Transfer.Envlp();
        Transfer.CustomData customData = new Transfer.CustomData();
        
        Transfer.DebtorInfo debtorInfo = new Transfer.DebtorInfo();
        debtorInfo.setAccountDesignation(requestDto.getDebtorAccountDesignation() != null ? requestDto.getDebtorAccountDesignation() : "1");
        debtorInfo.setIdType(requestDto.getDebtorIdType() != null ? requestDto.getDebtorIdType() : "BVN");
        debtorInfo.setIdValue(requestDto.getDebtorIdValue() != null ? requestDto.getDebtorIdValue() : "22112323440");
        debtorInfo.setAccountTier(requestDto.getDebtorAccountTier() != null ? requestDto.getDebtorAccountTier() : "1");
        customData.setDebtorInfo(debtorInfo);

        Transfer.CreditorInfo creditorInfo = new Transfer.CreditorInfo();
        creditorInfo.setAccountDesignation(requestDto.getCreditorAccountDesignation() != null ? requestDto.getCreditorAccountDesignation() : "1");
        creditorInfo.setIdType(requestDto.getCreditorIdType() != null ? requestDto.getCreditorIdType() : "BVN");
        creditorInfo.setIdValue(requestDto.getCreditorIdValue() != null ? requestDto.getCreditorIdValue() : 
                              (requestDto.getCreditorBvn() != null ? requestDto.getCreditorBvn() : "22112323460"));
        creditorInfo.setAccountTier(requestDto.getCreditorAccountTier() != null ? requestDto.getCreditorAccountTier() : "1");
        customData.setCreditorInfo(creditorInfo);

        Transfer.TransactionInfo transactionInfo = new Transfer.TransactionInfo();
        transactionInfo.setTransactionLocation(requestDto.getTransactionLocation() != null ? requestDto.getTransactionLocation() : "01080652440N020900337921E");
        transactionInfo.setNameEnquiryMsgId(requestDto.getNameEnquiryMsgId() != null && !requestDto.getNameEnquiryMsgId().trim().isEmpty() ? 
                requestDto.getNameEnquiryMsgId() : generateId(srcId, 15));
        transactionInfo.setChannelCode(requestDto.getChannelCode() != null ? requestDto.getChannelCode() : "1");
        transactionInfo.setRiskRating(requestDto.getRiskRating() != null ? requestDto.getRiskRating() : "R000000000000000000B9");
        customData.setTransactionInfo(transactionInfo);

        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);

        // --- Assemble Document ---
        fiToFICstmrCdtTrf.setGrpHdr(grpHdr);
        fiToFICstmrCdtTrf.setCdtTrfTxInf(cdtTrfTxInf);
        fiToFICstmrCdtTrf.setSplmtryData(splmtryData);
        doc.setFiToFICstmrCdtTrf(fiToFICstmrCdtTrf);

        return doc;
    }

    private static Transfer.Agt createAgt(String mmbId) {
        Transfer.Agt agt = new Transfer.Agt();
        Transfer.FinInstnId finInstnId = new Transfer.FinInstnId();
        Transfer.ClrSysMmbId clrSysMmbId = new Transfer.ClrSysMmbId();
        clrSysMmbId.setMmbId(mmbId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        agt.setFinInstnId(finInstnId);
        return agt;
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
