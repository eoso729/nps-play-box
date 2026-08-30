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
        assgnmt.setCreDtTm(ZonedDateTime.now(ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));

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

        String vrfctnVal = requestDto.getVerificationResponse();
        boolean isSuccess = vrfctnVal == null || vrfctnVal.trim().isEmpty() || "true".equalsIgnoreCase(vrfctnVal);
        rpt.setVrfctn(isSuccess ? "true" : "false");

        if (!isSuccess) {
            String code = (requestDto.getReasonCode() != null && !requestDto.getReasonCode().trim().isEmpty())
                    ? requestDto.getReasonCode().trim() : "33";
            String reasonText = (requestDto.getReasonProprietary() != null && !requestDto.getReasonProprietary().trim().isEmpty())
                    ? requestDto.getReasonProprietary().trim() : "Account number mismatch";

            NameVerificationReport.Rsn rsn = new NameVerificationReport.Rsn();
            rsn.setCd(code);
            rsn.setPrtry(reasonText);
            rpt.setRsn(rsn);
        } else {
            rpt.setRsn(null);
        }

        NameVerificationReport.OrgnlPtyAndAcctId orgnlPtyAndAcctId = new NameVerificationReport.OrgnlPtyAndAcctId();
        NameVerificationReport.Acct orgnlAcct = new NameVerificationReport.Acct();
        NameVerificationReport.AcctId orgnlAcctId = new NameVerificationReport.AcctId();
        orgnlAcctId.setIban(requestDto.getVerifiedAccountNumber());
        orgnlAcct.setId(orgnlAcctId);
        orgnlPtyAndAcctId.setAcct(orgnlAcct);
        rpt.setOrgnlPtyAndAcctId(orgnlPtyAndAcctId);

        if (isSuccess && requestDto.getVerifiedAccountName() != null && !requestDto.getVerifiedAccountName().trim().isEmpty()) {
            NameVerificationReport.UpdtdPtyAndAcctId updtdPtyAndAcctId = new NameVerificationReport.UpdtdPtyAndAcctId();
            NameVerificationReport.Pty updtdPty = new NameVerificationReport.Pty();
            updtdPty.setNm(requestDto.getVerifiedAccountName().trim());
            updtdPtyAndAcctId.setPty(updtdPty);
            rpt.setUpdtdPtyAndAcctId(updtdPtyAndAcctId);
        }

        // --- Supplementary Data ---
        NameVerificationReport.SplmtryData splmtryData = new NameVerificationReport.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        NameVerificationReport.Envlp envlp = new NameVerificationReport.Envlp();
        NameVerificationReport.CustomData customData = new NameVerificationReport.CustomData();

        NameVerificationReport.CreditorInfo creditorInfo = new NameVerificationReport.CreditorInfo();
        creditorInfo.setAccountDesignation(requestDto.getCreditorAccountDesignation() != null ? requestDto.getCreditorAccountDesignation() : "1");
        creditorInfo.setIdType(requestDto.getCreditorIdType() != null ? requestDto.getCreditorIdType() : "BVN");
        creditorInfo.setIdValue(requestDto.getCreditorIdValue() != null ? requestDto.getCreditorIdValue() : "22112323460");
        creditorInfo.setAccountTier(requestDto.getCreditorAccountTier() != null ? requestDto.getCreditorAccountTier() : "1");
        customData.setCreditorInfo(creditorInfo);

        NameVerificationReport.TransactionInfo transactionInfo = new NameVerificationReport.TransactionInfo();
        transactionInfo.setRiskRating(requestDto.getTransactionRiskRating() != null ? requestDto.getTransactionRiskRating() : "1");
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
        if (mmbId != null && !mmbId.isEmpty()) {
            finInstnId.setBicfi(mmbId);
            NameVerificationReport.ClrSysMmbId clrSysMmbId = new NameVerificationReport.ClrSysMmbId();
            clrSysMmbId.setMmbId(mmbId);
            finInstnId.setClrSysMmbId(clrSysMmbId);
        }
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
