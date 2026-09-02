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
        java.time.ZonedDateTime nowWat = java.time.ZonedDateTime.now(java.time.ZoneId.of("Africa/Lagos"));
        String creDtTm = nowWat.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);

        PaymentActivation.InitgPty initgPty = new PaymentActivation.InitgPty();
        String sourceId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        initgPty.setNm(requestDto.getSourceName() != null ? requestDto.getSourceName() : "Tester Jack");
        
        PaymentActivation.PartyId partyId = new PaymentActivation.PartyId();
        PaymentActivation.OrgId orgId = new PaymentActivation.OrgId();
        PaymentActivation.Othr initgOthr = new PaymentActivation.Othr();
        initgOthr.setId(requestDto.getClientId() != null && !requestDto.getClientId().trim().isEmpty()
                ? requestDto.getClientId().trim() : sourceId);
        orgId.setOthr(initgOthr);
        partyId.setOrgId(orgId);
        initgPty.setId(partyId);
        grpHdr.setInitgPty(initgPty);
        req.setGrpHdr(grpHdr);

        // --- Payment Information ---
        PaymentActivation.PmtInf pmtInf = new PaymentActivation.PmtInf();
        pmtInf.setPmtInfId(requestDto.getPaymentInformationId() != null && !requestDto.getPaymentInformationId().trim().isEmpty()
                ? requestDto.getPaymentInformationId().trim() : msgId);
        pmtInf.setPmtMtd("TRF");

        PaymentActivation.ReqdExctnDt reqdExctnDt = new PaymentActivation.ReqdExctnDt();
        String execDt = requestDto.getRequestedExecutionDate();
        if (execDt != null && !execDt.trim().isEmpty()) {
            reqdExctnDt.setDtTm(execDt.contains("T") ? execDt : execDt + "T00:00:00+01:00");
        } else {
            reqdExctnDt.setDtTm(nowWat.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));
        }
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
        dbtrOthr.setId(requestDto.getOtherAccountId() != null && !requestDto.getOtherAccountId().trim().isEmpty()
                ? requestDto.getOtherAccountId().trim() : destId);
        dbtrAcctId.setOthr(dbtrOthr);
        dbtrAcct.setId(dbtrAcctId);
        
        String currency = requestDto.getCurrency() != null && !requestDto.getCurrency().trim().isEmpty()
                ? requestDto.getCurrency().trim() : "NGN";
        dbtrAcct.setCcy(currency);
        String dbtAcctNm = requestDto.getDebtorAccountName() != null && !requestDto.getDebtorAccountName().trim().isEmpty()
                ? requestDto.getDebtorAccountName().trim() : dbtNm;
        dbtrAcct.setNm(dbtAcctNm);
        pmtInf.setDbtrAcct(dbtrAcct);

        PaymentActivation.DbtrAgt dbtrAgt = new PaymentActivation.DbtrAgt();
        String dbtrMmbId = requestDto.getDebtorAgentMemberId() != null && !requestDto.getDebtorAgentMemberId().trim().isEmpty()
                ? requestDto.getDebtorAgentMemberId().trim() : destId;
        String dbtrBic = requestDto.getDebtorAgentBIC() != null && !requestDto.getDebtorAgentBIC().trim().isEmpty()
                ? requestDto.getDebtorAgentBIC().trim() : dbtrMmbId;
        dbtrAgt.setFinInstnId(createFinInstnId(dbtrBic, dbtrMmbId));
        pmtInf.setDbtrAgt(dbtrAgt);

        // --- Credit Transfer Transaction ---
        PaymentActivation.CdtTrfTx cdtTrfTx = new PaymentActivation.CdtTrfTx();
        PaymentActivation.PmtId pmtId = new PaymentActivation.PmtId();
        String finalE2e = requestDto.getEndToEndId() != null && !requestDto.getEndToEndId().trim().isEmpty()
                ? requestDto.getEndToEndId().trim() : endToEndId;
        pmtId.setEndToEndId(finalE2e);
        cdtTrfTx.setPmtId(pmtId);

        PaymentActivation.Amt amt = new PaymentActivation.Amt();
        PaymentActivation.InstdAmt instdAmt = new PaymentActivation.InstdAmt();
        instdAmt.setCcy(currency);
        instdAmt.setValue(requestDto.getAmount() != null ? requestDto.getAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("2500.00"));
        amt.setInstdAmt(instdAmt);
        cdtTrfTx.setAmt(amt);

        PaymentActivation.CdtrAgt cdtrAgt = new PaymentActivation.CdtrAgt();
        String cdtrMmbId = requestDto.getCreditorAgentMemberId() != null && !requestDto.getCreditorAgentMemberId().trim().isEmpty()
                ? requestDto.getCreditorAgentMemberId().trim() : sourceId;
        String cdtrBic = requestDto.getCreditorAgentBIC() != null && !requestDto.getCreditorAgentBIC().trim().isEmpty()
                ? requestDto.getCreditorAgentBIC().trim() : cdtrMmbId;
        cdtrAgt.setFinInstnId(createFinInstnId(cdtrBic, cdtrMmbId));
        cdtTrfTx.setCdtrAgt(cdtrAgt);

        PaymentActivation.Cdtr cdtr = new PaymentActivation.Cdtr();
        String cdrNm = requestDto.getCreditorName() != null ? requestDto.getCreditorName() : "Tester Jack";
        cdtr.setNm(cdrNm);
        cdtTrfTx.setCdtr(cdtr);

        PaymentActivation.CdtrAcct cdtrAcct = new PaymentActivation.CdtrAcct();
        PaymentActivation.AcctId cdtrAcctId = new PaymentActivation.AcctId();
        cdtrAcctId.setIban(requestDto.getCreditorAccountNumber() != null ? requestDto.getCreditorAccountNumber() : "3157417733");
        
        PaymentActivation.Othr cdtrOthr = new PaymentActivation.Othr();
        cdtrOthr.setId(requestDto.getOtherCreditorAccountId() != null && !requestDto.getOtherCreditorAccountId().trim().isEmpty()
                ? requestDto.getOtherCreditorAccountId().trim() : sourceId);
        cdtrAcctId.setOthr(cdtrOthr);
        cdtrAcct.setId(cdtrAcctId);
        String cdrAcctNm = requestDto.getCreditorAccountName() != null && !requestDto.getCreditorAccountName().trim().isEmpty()
                ? requestDto.getCreditorAccountName().trim() : cdrNm;
        cdtrAcct.setNm(cdrAcctNm);
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
        debtorInfo.setAccountDesignation(requestDto.getDebtorAccountDesignation() != null && !requestDto.getDebtorAccountDesignation().trim().isEmpty()
                ? requestDto.getDebtorAccountDesignation().trim() : "1");
        debtorInfo.setIdType(requestDto.getDebtorIdType() != null && !requestDto.getDebtorIdType().trim().isEmpty()
                ? requestDto.getDebtorIdType().trim() : "BVN");
        debtorInfo.setIdValue(requestDto.getDebtorIdValue() != null && !requestDto.getDebtorIdValue().trim().isEmpty()
                ? requestDto.getDebtorIdValue().trim() : "11111111145");
        debtorInfo.setAccountTier(requestDto.getDebtorAccountTier() != null && !requestDto.getDebtorAccountTier().trim().isEmpty()
                ? requestDto.getDebtorAccountTier().trim() : "1");
        customData.setDebtorInfo(debtorInfo);

        PaymentActivation.DebtorMetadata debtorMetadata = new PaymentActivation.DebtorMetadata();
        debtorMetadata.setBiometricData(requestDto.getDebtorBiometricData() != null ? requestDto.getDebtorBiometricData() : "");
        debtorMetadata.setAdrLine(requestDto.getDebtorAddressLine() != null && !requestDto.getDebtorAddressLine().trim().isEmpty()
                ? requestDto.getDebtorAddressLine().trim() : "Marina");
        debtorMetadata.setPhneNb(requestDto.getDebtorPhoneNumber() != null && !requestDto.getDebtorPhoneNumber().trim().isEmpty()
                ? requestDto.getDebtorPhoneNumber().trim() : "08012345678");
        debtorMetadata.setEmailAdr(requestDto.getDebtorEmailAddress() != null && !requestDto.getDebtorEmailAddress().trim().isEmpty()
                ? requestDto.getDebtorEmailAddress().trim() : "mt@nibss.com");
        customData.setDebtorMetadata(debtorMetadata);

        PaymentActivation.CreditorInfo creditorInfo = new PaymentActivation.CreditorInfo();
        creditorInfo.setAccountDesignation(requestDto.getCreditorAccountDesignation() != null && !requestDto.getCreditorAccountDesignation().trim().isEmpty()
                ? requestDto.getCreditorAccountDesignation().trim() : "1");
        creditorInfo.setIdType(requestDto.getCreditorIdType() != null && !requestDto.getCreditorIdType().trim().isEmpty()
                ? requestDto.getCreditorIdType().trim() : "BVN");
        creditorInfo.setIdValue(requestDto.getCreditorIdValue() != null && !requestDto.getCreditorIdValue().trim().isEmpty()
                ? requestDto.getCreditorIdValue().trim() : "11111111145");
        creditorInfo.setAccountTier(requestDto.getCreditorAccountTier() != null && !requestDto.getCreditorAccountTier().trim().isEmpty()
                ? requestDto.getCreditorAccountTier().trim() : "1");
        customData.setCreditorInfo(creditorInfo);

        PaymentActivation.TransactionInfo transactionInfo = new PaymentActivation.TransactionInfo();
        transactionInfo.setTransactionLocation(requestDto.getTransactionLocation() != null && !requestDto.getTransactionLocation().trim().isEmpty()
                ? requestDto.getTransactionLocation().trim() : "01080652440N020900337921E");
        transactionInfo.setChannelCode(requestDto.getChannelCode() != null && !requestDto.getChannelCode().trim().isEmpty()
                ? requestDto.getChannelCode().trim() : "1");
        transactionInfo.setMandateCategory(requestDto.getMandateCategory() != null && !requestDto.getMandateCategory().trim().isEmpty()
                ? requestDto.getMandateCategory().trim() : "0");
        customData.setTransactionInfo(transactionInfo);

        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);
        req.setSplmtryData(splmtryData);

        doc.setCdtrPmtActvtnReq(req);
        return doc;
    }

    private static PaymentActivation.FinInstnId createFinInstnId(String bic, String memberId) {
        PaymentActivation.FinInstnId finInstnId = new PaymentActivation.FinInstnId();
        if (bic != null && !bic.trim().isEmpty()) {
            finInstnId.setBicfi(bic.trim());
        }
        PaymentActivation.ClrSysMmbId clrSysMmbId = new PaymentActivation.ClrSysMmbId();
        clrSysMmbId.setMmbId(memberId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        return finInstnId;
    }
}
