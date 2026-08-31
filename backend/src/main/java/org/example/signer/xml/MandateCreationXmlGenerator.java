package org.example.signer.xml;

import org.example.signer.dto.MandateCreationRequestDto;
import org.example.signer.model.MandateCreation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MandateCreationXmlGenerator {

    public static MandateCreation generate(MandateCreationRequestDto requestDto, String msgId, String mandateId) {
        MandateCreation doc = new MandateCreation();
        MandateCreation.MndtInitnReq mndtInitnReq = new MandateCreation.MndtInitnReq();
        
        // --- Group Header ---
        MandateCreation.GrpHdr grpHdr = new MandateCreation.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = java.time.ZonedDateTime.now(java.time.ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);
        mndtInitnReq.setGrpHdr(grpHdr);
        
        // --- Mandate ---
        MandateCreation.Mndt mndt = new MandateCreation.Mndt();
        mndt.setMndtId(mandateId);
        
        // --- Occurrences ---
        MandateCreation.Ocrncs ocrncs = new MandateCreation.Ocrncs();
        ocrncs.setSeqTp(requestDto.getSequenceType() != null ? requestDto.getSequenceType() : "RCUR");
        MandateCreation.Frqcy frqcy = new MandateCreation.Frqcy();
        frqcy.setTp(requestDto.getFrequencyType() != null ? requestDto.getFrequencyType() : "WEEK");
        ocrncs.setFrqcy(frqcy);
        ocrncs.setFrstColltnDt(requestDto.getFirstCollectionDate() != null ? requestDto.getFirstCollectionDate() : "2025-01-25");
        ocrncs.setFnlColltnDt(requestDto.getFinalCollectionDate() != null ? requestDto.getFinalCollectionDate() : "2026-01-25");
        mndt.setOcrncs(ocrncs);
        
        // --- Tracking Indicator ---
        mndt.setTrckgInd(requestDto.getTrackingIndicator() != null ? requestDto.getTrackingIndicator() : false);
        
        // --- Collection Amount ---
        MandateCreation.ColltnAmt colltnAmt = new MandateCreation.ColltnAmt();
        colltnAmt.setCcy(requestDto.getCurrency() != null ? requestDto.getCurrency() : "NGN");
        colltnAmt.setValue(requestDto.getCollectionAmount() != null ? requestDto.getCollectionAmount() : new BigDecimal("50000.00"));
        mndt.setColltnAmt(colltnAmt);
        
        // --- Creditor ---
        MandateCreation.Cdtr cdtr = new MandateCreation.Cdtr();
        String cdrNm = requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "CreditorCorp";
        cdtr.setNm(cdrNm);
        mndt.setCdtr(cdtr);
        
        // --- Creditor Account ---
        MandateCreation.CdtrAcct cdtrAcct = new MandateCreation.CdtrAcct();
        MandateCreation.AcctId cdtrAcctId = new MandateCreation.AcctId();
        cdtrAcctId.setIban(requestDto.getCreditorAccountNumber() != null ? requestDto.getCreditorAccountNumber() : "3829837329");
        cdtrAcct.setId(cdtrAcctId);
        cdtrAcct.setNm(cdrNm);
        mndt.setCdtrAcct(cdtrAcct);
        
        // --- Creditor Agent ---
        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        String cdtrBic = requestDto.getCreditorAgentBIC() != null ? requestDto.getCreditorAgentBIC() : "XYZBANK";
        String cdtrMmbId = requestDto.getCreditorAgentMemberId() != null ? requestDto.getCreditorAgentMemberId() : srcId;
        
        MandateCreation.CdtrAgt cdtrAgt = new MandateCreation.CdtrAgt();
        cdtrAgt.setFinInstnId(createFinInstnId(cdtrBic, cdtrMmbId));
        mndt.setCdtrAgt(cdtrAgt);
        
        // --- Debtor ---
        MandateCreation.Dbtr dbtr = new MandateCreation.Dbtr();
        String dbtNm = requestDto.getDebtorName() != null ? requestDto.getDebtorName() : 
                      (requestDto.getDestinationName() != null ? requestDto.getDestinationName() : "Debtor Customer");
        dbtr.setNm(dbtNm);
        mndt.setDbtr(dbtr);
        
        // --- Debtor Account ---
        MandateCreation.DbtrAcct dbtrAcct = new MandateCreation.DbtrAcct();
        MandateCreation.AcctId dbtrAcctId = new MandateCreation.AcctId();
        dbtrAcctId.setIban(requestDto.getDebtorAccountNumber() != null ? requestDto.getDebtorAccountNumber() : 
                         (requestDto.getDestinationAccountNumber() != null ? requestDto.getDestinationAccountNumber() : "3829736273"));
        dbtrAcct.setId(dbtrAcctId);
        dbtrAcct.setNm(dbtNm);
        mndt.setDbtrAcct(dbtrAcct);
        
        // --- Debtor Agent ---
        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";
        String dbtrBic = requestDto.getDebtorAgentBIC() != null ? requestDto.getDebtorAgentBIC() : "ABCBANK";
        String dbtrMmbId = requestDto.getDebtorAgentMemberId() != null ? requestDto.getDebtorAgentMemberId() : destId;
        
        MandateCreation.DbtrAgt dbtrAgt = new MandateCreation.DbtrAgt();
        dbtrAgt.setFinInstnId(createFinInstnId(dbtrBic, dbtrMmbId));
        mndt.setDbtrAgt(dbtrAgt);
        
        // --- Referenced Document ---
        MandateCreation.RfrdDoc rfrdDoc = new MandateCreation.RfrdDoc();
        MandateCreation.Tp tp = new MandateCreation.Tp();
        MandateCreation.CdOrPrtry cdOrPrtry = new MandateCreation.CdOrPrtry();
        cdOrPrtry.setCd(requestDto.getReferenceDocumentType() != null ? requestDto.getReferenceDocumentType() : "INV");
        tp.setCdOrPrtry(cdOrPrtry);
        rfrdDoc.setTp(tp);
        rfrdDoc.setNb(requestDto.getReferenceDocumentNumber() != null ? requestDto.getReferenceDocumentNumber() : "INV-2025-001");
        mndt.setRfrdDoc(rfrdDoc);
        mndtInitnReq.setMndt(mndt);
        
        // --- Supplementary Data ---
        MandateCreation.SplmtryData splmtryData = new MandateCreation.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        MandateCreation.Envlp envlp = new MandateCreation.Envlp();
        MandateCreation.CustomData customData = new MandateCreation.CustomData();
        
        MandateCreation.DebtorInfo debtorInfo = new MandateCreation.DebtorInfo();
        debtorInfo.setAccountDesignation(requestDto.getDebtorAccountDesignation() != null ? requestDto.getDebtorAccountDesignation() : "1");
        debtorInfo.setIdType(requestDto.getDebtorIdType() != null ? requestDto.getDebtorIdType() : "BVN");
        debtorInfo.setIdValue(requestDto.getDebtorIdValue() != null ? requestDto.getDebtorIdValue() : "22222222222");
        debtorInfo.setAccountTier(requestDto.getDebtorAccountTier() != null ? requestDto.getDebtorAccountTier() : "1");
        customData.setDebtorInfo(debtorInfo);
        
        MandateCreation.DebtorMetadata debtorMetadata = new MandateCreation.DebtorMetadata();
        debtorMetadata.setBiometricData(requestDto.getDebtorBiometricData() != null ? requestDto.getDebtorBiometricData() : "a");
        debtorMetadata.setAdrLine(requestDto.getDebtorAdrLine() != null ? requestDto.getDebtorAdrLine() : "d");
        debtorMetadata.setPhneNb(requestDto.getDebtorPhneNb() != null ? requestDto.getDebtorPhneNb() : "09038472264");
        debtorMetadata.setEmailAdr(requestDto.getDebtorEmailAdr() != null ? requestDto.getDebtorEmailAdr() : "mt@nibss.com");
        customData.setDebtorMetadata(debtorMetadata);
        
        MandateCreation.CreditorInfo creditorInfo = new MandateCreation.CreditorInfo();
        creditorInfo.setAccountDesignation(requestDto.getCreditorAccountDesignation() != null ? requestDto.getCreditorAccountDesignation() : "1");
        creditorInfo.setIdType(requestDto.getCreditorIdType() != null ? requestDto.getCreditorIdType() : "BVN");
        creditorInfo.setIdValue(requestDto.getCreditorIdValue() != null ? requestDto.getCreditorIdValue() : "22222222222");
        creditorInfo.setAccountTier(requestDto.getCreditorAccountTier() != null ? requestDto.getCreditorAccountTier() : "1");
        customData.setCreditorInfo(creditorInfo);
        
        MandateCreation.TransactionInfo transactionInfo = new MandateCreation.TransactionInfo();
        transactionInfo.setTransactionLocation(requestDto.getTransactionLocation() != null ? requestDto.getTransactionLocation() : "01080652440N020900337921E");
        transactionInfo.setChannelCode(requestDto.getChannelCode() != null ? requestDto.getChannelCode() : "4");
        transactionInfo.setMandateCategory(requestDto.getMandateCategory() != null ? requestDto.getMandateCategory() : "0");
        transactionInfo.setFixedCollectionAmount(requestDto.getFixedCollectionAmount() != null ? requestDto.getFixedCollectionAmount() : false);
        customData.setTransactionInfo(transactionInfo);
        
        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);
        mndtInitnReq.setSplmtryData(splmtryData);
        
        doc.setMndtInitnReq(mndtInitnReq);
        return doc;
    }

    private static MandateCreation.FinInstnId createFinInstnId(String bic, String memberId) {
        MandateCreation.FinInstnId finInstnId = new MandateCreation.FinInstnId();
        finInstnId.setBicfi(bic);
        MandateCreation.ClrSysMmbId clrSysMmbId = new MandateCreation.ClrSysMmbId();
        clrSysMmbId.setMmbId(memberId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        return finInstnId;
    }
}
