package org.example.signer.xml;

import org.example.signer.dto.NameVerificationRequestDto;
import org.example.signer.model.NameVerification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NameVerificationXmlGenerator {

    public static NameVerification generate(NameVerificationRequestDto requestDto, String msgId) {
        NameVerification doc = new NameVerification();
        NameVerification.IdVrfctnReq idVrfctnReq = new NameVerification.IdVrfctnReq();
        NameVerification.Assgnmt assgnmt = new NameVerification.Assgnmt();
        NameVerification.Cretr cretr = new NameVerification.Cretr();
        NameVerification.Pty cretrPty = new NameVerification.Pty();
        NameVerification.Assgnr assgnr = new NameVerification.Assgnr();
        NameVerification.Pty assgnrPty = new NameVerification.Pty();
        NameVerification.Agt assgnrAgt = new NameVerification.Agt();
        NameVerification.FinInstnId assgnrFinInstnId = new NameVerification.FinInstnId();
        NameVerification.ClrSysMmbId assgnrClrSysMmbId = new NameVerification.ClrSysMmbId();
        NameVerification.Assgne assgne = new NameVerification.Assgne();
        NameVerification.Agt assgneAgt = new NameVerification.Agt();
        NameVerification.FinInstnId assgneFinInstnId = new NameVerification.FinInstnId();
        NameVerification.ClrSysMmbId assgneClrSysMmbId = new NameVerification.ClrSysMmbId();
        NameVerification.Vrfctn vrfctn = new NameVerification.Vrfctn();
        NameVerification.PtyAndAcctId ptyAndAcctId = new NameVerification.PtyAndAcctId();
        NameVerification.Pty vrfctnPty = new NameVerification.Pty();
        NameVerification.Acct acct = new NameVerification.Acct();
        NameVerification.AcctId acctId = new NameVerification.AcctId();

        String formattedDateTime = java.time.ZonedDateTime.now(java.time.ZoneId.of("Africa/Lagos"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));

        cretrPty.setNm(requestDto.getSendingPartyName());
        cretr.setPty(cretrPty);

        assgnrPty.setNm(requestDto.getSendingPartyName());
        assgnrClrSysMmbId.setMmbId(requestDto.getSourceId());
        assgnrFinInstnId.setClrSysMmbId(assgnrClrSysMmbId);
        assgnrFinInstnId.setBicfi(requestDto.getSourceId());
        assgnrAgt.setFinInstnId(assgnrFinInstnId);
        assgnr.setPty(assgnrPty);
        assgnr.setAgt(assgnrAgt);

        assgneClrSysMmbId.setMmbId(requestDto.getBeneficiaryId());
        assgneFinInstnId.setClrSysMmbId(assgneClrSysMmbId);
        assgneFinInstnId.setBicfi(requestDto.getBeneficiaryId());
        assgneAgt.setFinInstnId(assgneFinInstnId);
        assgne.setAgt(assgneAgt);

        assgnmt.setMsgId(msgId);
        assgnmt.setCreDtTm(formattedDateTime);
        assgnmt.setCretr(cretr);
        assgnmt.setAssgnr(assgnr);
        assgnmt.setAssgne(assgne);

        vrfctnPty.setNm(requestDto.getPartyToBeVerifiedName());
        acctId.setIban(requestDto.getPartyToBeVerifiedAccountNumber());
        acct.setId(acctId);
        ptyAndAcctId.setPty(vrfctnPty);
        ptyAndAcctId.setAcct(acct);

        vrfctn.setId(msgId);
        vrfctn.setPtyAndAcctId(ptyAndAcctId);

        idVrfctnReq.setAssgnmt(assgnmt);
        idVrfctnReq.setVrfctn(vrfctn);

        doc.setIdVrfctnReq(idVrfctnReq);

        return doc;
    }
}
