package org.example.signer.xml;

import org.example.signer.dto.NameVerificationReportDto;
import org.example.signer.model.NameVerificationReport;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class NameVerificationReportXmlGenerator {

    public static NameVerificationReport generate(NameVerificationReportDto requestDto) {
        NameVerificationReport doc = new NameVerificationReport();
        NameVerificationReport.IdVrfctnRpt idVrfctnRpt = new NameVerificationReport.IdVrfctnRpt();

        // --- Assignment ---
        NameVerificationReport.Assgnmt assgnmt = new NameVerificationReport.Assgnmt();
        assgnmt.setMsgId(generateMsgId(requestDto.getSendingInstitutionId()));
        assgnmt.setCreDtTm(ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));

        NameVerificationReport.Assgnr assgnr = new NameVerificationReport.Assgnr();
        assgnr.setAgt(createAgt(requestDto.getSendingInstitutionId()));
        assgnmt.setAssgnr(assgnr);

        NameVerificationReport.Assgne assgne = new NameVerificationReport.Assgne();
        NameVerificationReport.Pty assgnePty = new NameVerificationReport.Pty();
        assgnePty.setNm(requestDto.getReceiverName());
        assgne.setPty(assgnePty);
        assgne.setAgt(createAgt(requestDto.getReceivingInstitutionId()));
        assgnmt.setAssgne(assgne);

        // --- Original Assignment ---
        NameVerificationReport.OrgnlAssgnmt orgnlAssgnmt = new NameVerificationReport.OrgnlAssgnmt();
        orgnlAssgnmt.setMsgId(requestDto.getOriginalMsgId());
        orgnlAssgnmt.setCreDtTm(requestDto.getOriginalCreDtTm());

        // --- Report ---
        NameVerificationReport.Rpt rpt = new NameVerificationReport.Rpt();
        rpt.setOrgnlId(requestDto.getOriginalMsgId());
        rpt.setVrfctn(requestDto.isVerificationResponse());

        NameVerificationReport.OrgnlPtyAndAcctId orgnlPtyAndAcctId = new NameVerificationReport.OrgnlPtyAndAcctId();
        NameVerificationReport.Acct orgnlAcct = new NameVerificationReport.Acct();
        NameVerificationReport.AcctId orgnlAcctId = new NameVerificationReport.AcctId();
        orgnlAcctId.setIban(requestDto.getVerifiedAccountNumber());
        orgnlAcct.setId(orgnlAcctId);
        orgnlPtyAndAcctId.setAcct(orgnlAcct);
        rpt.setOrgnlPtyAndAcctId(orgnlPtyAndAcctId);

        NameVerificationReport.UpdtdPtyAndAcctId updtdPtyAndAcctId = new NameVerificationReport.UpdtdPtyAndAcctId();
        NameVerificationReport.Pty updtdPty = new NameVerificationReport.Pty();
        updtdPty.setNm(requestDto.getVerifiedAccountName());
        updtdPtyAndAcctId.setPty(updtdPty);
        rpt.setUpdtdPtyAndAcctId(updtdPtyAndAcctId);

        // --- Supplementary Data ---
        NameVerificationReport.SplmtryData splmtryData = new NameVerificationReport.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        NameVerificationReport.Envlp envlp = new NameVerificationReport.Envlp();
        NameVerificationReport.CustomData customData = new NameVerificationReport.CustomData();

        NameVerificationReport.CreditorInfo creditorInfo = new NameVerificationReport.CreditorInfo();
        creditorInfo.setAccountDesignation(requestDto.getCreditorAccountDesignation());
        creditorInfo.setIdType(requestDto.getCreditorIdType());
        creditorInfo.setIdValue(requestDto.getCreditorIdValue());
        creditorInfo.setAccountTier(requestDto.getCreditorAccountTier());
        customData.setCreditorInfo(creditorInfo);

        NameVerificationReport.TransactionInfo transactionInfo = new NameVerificationReport.TransactionInfo();
        transactionInfo.setRiskRating(requestDto.getTransactionRiskRating());
        customData.setTransactionInfo(transactionInfo);

        envlp.setCustomData(customData);
        splmtryData.setEnvlp(envlp);

        // --- Assemble Document ---
        idVrfctnRpt.setAssgnmt(assgnmt);
        idVrfctnRpt.setOrgnlAssgnmt(orgnlAssgnmt);
        idVrfctnRpt.setRpt(rpt);
        idVrfctnRpt.setSplmtryData(splmtryData);
        doc.setIdVrfctnRpt(idVrfctnRpt);

        return doc;
    }

    private static NameVerificationReport.Agt createAgt(String mmbId) {
        NameVerificationReport.Agt agt = new NameVerificationReport.Agt();
        NameVerificationReport.FinInstnId finInstnId = new NameVerificationReport.FinInstnId();
        NameVerificationReport.ClrSysMmbId clrSysMmbId = new NameVerificationReport.ClrSysMmbId();
        clrSysMmbId.setMmbId(mmbId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        agt.setFinInstnId(finInstnId);
        return agt;
    }

    private static String generateMsgId(String institutionId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        StringBuilder randomDigits = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 15; i++) {
            randomDigits.append(random.nextInt(10));
        }
        return institutionId + timestamp + randomDigits.toString();
    }
}
