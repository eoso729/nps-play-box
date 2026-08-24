package org.example.signer.xml;

import org.example.signer.dto.PaymentActivationRequestDto;
import org.example.signer.model.PaymentActivation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentActivationXmlGenerator {

    public static PaymentActivation generate(PaymentActivationRequestDto requestDto, String msgId, String endToEndId) {
        PaymentActivation doc = new PaymentActivation();
        PaymentActivation.CdtrPmtActvtnReq req = new PaymentActivation.CdtrPmtActvtnReq();

        // --- Group Header ---
        PaymentActivation.GrpHdr grpHdr = new PaymentActivation.GrpHdr();
        grpHdr.setMsgId(msgId);
        LocalDateTime now = LocalDateTime.now();
        String creDtTm = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        grpHdr.setCreDtTm(creDtTm);

        PaymentActivation.InitgPty initgPty = new PaymentActivation.InitgPty();
        String sourceId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        initgPty.setNm(requestDto.getSourceName() != null ? requestDto.getSourceName() : "Tester Jack");
        
        PaymentActivation.PartyId partyId = new PaymentActivation.PartyId();
        PaymentActivation.OrgId orgId = new PaymentActivation.OrgId();
        PaymentActivation.Othr initgOthr = new PaymentActivation.Othr();
        initgOthr.setId(sourceId);
        orgId.setOthr(initgOthr);
        partyId.setOrgId(orgId);
        initgPty.setId(partyId);
        grpHdr.setInitgPty(initgPty);
        req.setGrpHdr(grpHdr);

        // --- Payment Information ---
        PaymentActivation.PmtInf pmtInf = new PaymentActivation.PmtInf();
        pmtInf.setPmtInfId(msgId);
        pmtInf.setPmtMtd("TRF");

        PaymentActivation.ReqdExctnDt reqdExctnDt = new PaymentActivation.ReqdExctnDt();
        reqdExctnDt.setDtTm(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        pmtInf.setReqdExctnDt(reqdExctnDt);

        PaymentActivation.Dbtr dbtr = new PaymentActivation.Dbtr();
        String dbtNm = requestDto.getDebtorName() != null ? requestDto.getDebtorName() : "Ponmile Joy";
        dbtr.setNm(dbtNm);
        pmtInf.setDbtr(dbtr);

        PaymentActivation.DbtrAcct dbtrAcct = new PaymentActivation.DbtrAcct();
        PaymentActivation.AcctId dbtrAcctId = new PaymentActivation.AcctId();
        dbtrAcctId.setIban(requestDto.getDebtorAccountNumber() != null ? requestDto.getDebtorAccountNumber() : "3157417712");
        
        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";
        PaymentActivation.Othr dbtrOthr = new PaymentActivation.Othr();
        dbtrOthr.setId(destId);
        dbtrAcctId.setOthr(dbtrOthr);
        dbtrAcct.setId(dbtrAcctId);
        
        String currency = "NGN";
        dbtrAcct.setCcy(currency);
        dbtrAcct.setNm(dbtNm);
        pmtInf.setDbtrAcct(dbtrAcct);

        PaymentActivation.DbtrAgt dbtrAgt = new PaymentActivation.DbtrAgt();
        dbtrAgt.setFinInstnId(createFinInstnId(destId));
        pmtInf.setDbtrAgt(dbtrAgt);

        // --- Credit Transfer Transaction ---
        PaymentActivation.CdtTrfTx cdtTrfTx = new PaymentActivation.CdtTrfTx();
        PaymentActivation.PmtId pmtId = new PaymentActivation.PmtId();
        pmtId.setEndToEndId(endToEndId);
        cdtTrfTx.setPmtId(pmtId);

        PaymentActivation.Amt amt = new PaymentActivation.Amt();
        PaymentActivation.InstdAmt instdAmt = new PaymentActivation.InstdAmt();
        instdAmt.setCcy(currency);
        instdAmt.setValue(requestDto.getAmount() != null ? requestDto.getAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("2500.00"));
        amt.setInstdAmt(instdAmt);
        cdtTrfTx.setAmt(amt);

        PaymentActivation.CdtrAgt cdtrAgt = new PaymentActivation.CdtrAgt();
        cdtrAgt.setFinInstnId(createFinInstnId(sourceId));
        cdtTrfTx.setCdtrAgt(cdtrAgt);

        PaymentActivation.Cdtr cdtr = new PaymentActivation.Cdtr();
        String cdrNm = requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "Tester Jack";
        cdtr.setNm(cdrNm);
        cdtTrfTx.setCdtr(cdtr);

        PaymentActivation.CdtrAcct cdtrAcct = new PaymentActivation.CdtrAcct();
        PaymentActivation.AcctId cdtrAcctId = new PaymentActivation.AcctId();
        cdtrAcctId.setIban(requestDto.getCreditorAccountNumber() != null ? requestDto.getCreditorAccountNumber() : "3157417733");
        
        PaymentActivation.Othr cdtrOthr = new PaymentActivation.Othr();
        cdtrOthr.setId(sourceId);
        cdtrAcctId.setOthr(cdtrOthr);
        cdtrAcct.setId(cdtrAcctId);
        cdtrAcct.setNm(cdrNm);
        cdtTrfTx.setCdtrAcct(cdtrAcct);

        PaymentActivation.Purp purp = new PaymentActivation.Purp();
        purp.setPrtry(requestDto.getPurpose() != null ? requestDto.getPurpose() : "Test 013");
        cdtTrfTx.setPurp(purp);

        pmtInf.setCdtTrfTx(cdtTrfTx);
        req.setPmtInf(pmtInf);

        // --- Supplementary Data ---
        PaymentActivation.SplmtryData splmtryData = new PaymentActivation.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        PaymentActivation.Envlp envlp = new PaymentActivation.Envlp();
        PaymentActivation.CustomData customData = new PaymentActivation.CustomData();

        PaymentActivation.DebtorInfo debtorInfo = new PaymentActivation.DebtorInfo();
        debtorInfo.setAccountDesignation("1");
        debtorInfo.setIdType("BVN");
        debtorInfo.setIdValue("11111111145");
        debtorInfo.setAccountTier("1");
        customData.setDebtorInfo(debtorInfo);

        PaymentActivation.DebtorMetadata debtorMetadata = new PaymentActivation.DebtorMetadata();
        debtorMetadata.setBiometricData("");
        debtorMetadata.setAdrLine("Marina");
        debtorMetadata.setPhneNb("08012345678");
        debtorMetadata.setEmailAdr("mt@nibss.com");
        customData.setDebtorMetadata(debtorMetadata);

        PaymentActivation.CreditorInfo creditorInfo = new PaymentActivation.CreditorInfo();
        creditorInfo.setAccountDesignation("1");
        creditorInfo.setIdType("BVN");
        creditorInfo.setIdValue("11111111145");
        creditorInfo.setAccountTier("1");
        customData.setCreditorInfo(creditorInfo);

        PaymentActivation.TransactionInfo transactionInfo = new PaymentActivation.TransactionInfo();
        transactionInfo.setTransactionLocation("01080652440N020900337921E");
        transactionInfo.setChannelCode("1");
        transactionInfo.setMandateCategory("0");
        customData.setTransactionInfo(transactionInfo);

        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);
        req.setSplmtryData(splmtryData);

        doc.setCdtrPmtActvtnReq(req);
        return doc;
    }

    private static PaymentActivation.FinInstnId createFinInstnId(String memberId) {
        PaymentActivation.FinInstnId finInstnId = new PaymentActivation.FinInstnId();
        finInstnId.setBicfi(memberId);
        PaymentActivation.ClrSysMmbId clrSysMmbId = new PaymentActivation.ClrSysMmbId();
        clrSysMmbId.setMmbId(memberId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        return finInstnId;
    }
}
