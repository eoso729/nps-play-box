package org.example.signer.xml;

import org.example.signer.dto.PaymentReturnRequestDto;
import org.example.signer.model.PaymentReturn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentReturnXmlGenerator {

    public static PaymentReturn generate(PaymentReturnRequestDto dto, String msgId) {

        PaymentReturn doc = new PaymentReturn();
        PaymentReturn.PmtRtr pmtRtr = new PaymentReturn.PmtRtr();

        String sourceId      = dto.getSourceId()      != null ? dto.getSourceId()      : "999057";
        String destinationId = dto.getDestinationId() != null ? dto.getDestinationId() : "999998";
        String bicfi         = dto.getBicfi()         != null ? dto.getBicfi()         : sourceId;
        String currency      = dto.getCurrency()      != null ? dto.getCurrency()       : "NGN";

        String creDtTm = ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        LocalDateTime now = LocalDateTime.now();

        // ── Group Header ────────────────────────────────────────────────────
        PaymentReturn.GrpHdr grpHdr = new PaymentReturn.GrpHdr();
        grpHdr.setMsgId(msgId);
        grpHdr.setCreDtTm(creDtTm);

        PaymentReturn.SttlmInf sttlmInf = new PaymentReturn.SttlmInf();
        sttlmInf.setSttlmMtd("CLRG");
        grpHdr.setSttlmInf(sttlmInf);

        grpHdr.setInstgAgt(buildInstgAgt(bicfi, sourceId));
        grpHdr.setInstdAgt(buildInstdAgt(destinationId));
        pmtRtr.setGrpHdr(grpHdr);

        // ── Original Group Info ─────────────────────────────────────────────
        PaymentReturn.OrgnlGrpInf orgnlGrpInf = new PaymentReturn.OrgnlGrpInf();
        orgnlGrpInf.setOrgnlMsgId(
                dto.getOriginalMsgId() != null ? dto.getOriginalMsgId() : "");
        orgnlGrpInf.setOrgnlMsgNmId(
                dto.getOriginalMsgNameId() != null ? dto.getOriginalMsgNameId() : "pacs.008.001.12");
        orgnlGrpInf.setOrgnlCreDtTm(
                dto.getOriginalCreDtTm() != null ? dto.getOriginalCreDtTm() : creDtTm);
        pmtRtr.setOrgnlGrpInf(orgnlGrpInf);

        // ── Transaction Information ─────────────────────────────────────────
        PaymentReturn.TxInf txInf = new PaymentReturn.TxInf();
        txInf.setRtrId(msgId);
        txInf.setOrgnlInstrId(
                dto.getOriginalInstrId() != null ? dto.getOriginalInstrId() : "");
        txInf.setOrgnlEndToEndId(
                dto.getOriginalEndToEndId() != null ? dto.getOriginalEndToEndId() : "");
        txInf.setOrgnlTxId(
                dto.getOriginalTxId() != null ? dto.getOriginalTxId() : "");

        // Original interbank settlement date
        String orgnlSttlmDt = dto.getOriginalIntrBkSttlmDt() != null
                ? dto.getOriginalIntrBkSttlmDt()
                : now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "Z";
        txInf.setOrgnlIntrBkSttlmDt(orgnlSttlmDt);

        // Returned settlement amount
        PaymentReturn.RtrdIntrBkSttlmAmt rtrdAmt = new PaymentReturn.RtrdIntrBkSttlmAmt();
        rtrdAmt.setCcy(currency);
        BigDecimal retAmt = dto.getReturnedAmount() != null ? dto.getReturnedAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("0.00");
        rtrdAmt.setValue(retAmt);
        txInf.setRtrdIntrBkSttlmAmt(rtrdAmt);

        // Settlement date for the return
        String sttlmDt = dto.getIntrBkSttlmDt() != null
                ? dto.getIntrBkSttlmDt()
                : now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "Z";
        txInf.setIntrBkSttlmDt(sttlmDt);

        txInf.setChrgBr(dto.getChargeBrr() != null ? dto.getChargeBrr() : "SLEV");

        txInf.setInstgAgt(buildInstgAgt(bicfi, sourceId));
        txInf.setInstdAgt(buildInstdAgt(destinationId));

        // Return Reason
        PaymentReturn.RtrRsnInf rtrRsnInf = new PaymentReturn.RtrRsnInf();
        PaymentReturn.Rsn rsn = new PaymentReturn.Rsn();
        rsn.setPrtry(dto.getReturnReasonCode() != null ? dto.getReturnReasonCode() : "AC04");
        rtrRsnInf.setRsn(rsn);
        rtrRsnInf.setAddtlInf(dto.getReturnReasonInfo());
        txInf.setRtrRsnInf(rtrRsnInf);

        // Original Transaction Reference
        PaymentReturn.OrgnlTxRef orgnlTxRef = new PaymentReturn.OrgnlTxRef();

        String origAmt = dto.getOriginalIntrBkSttlmAmt() != null
                ? dto.getOriginalIntrBkSttlmAmt().toPlainString()
                : "0.00";
        orgnlTxRef.setIntrBkSttlmAmt(origAmt);

        PaymentReturn.PmtTpInf pmtTpInf = new PaymentReturn.PmtTpInf();
        pmtTpInf.setClrChanl(dto.getClearingChannel() != null ? dto.getClearingChannel() : "RTNS");
        PaymentReturn.LclInstrm lclInstrm = new PaymentReturn.LclInstrm();
        lclInstrm.setPrtry(dto.getLocalInstrument() != null ? dto.getLocalInstrument() : "CTAA");
        pmtTpInf.setLclInstrm(lclInstrm);
        orgnlTxRef.setPmtTpInf(pmtTpInf);

        // Debtor
        PaymentReturn.PartyChoice dbtrParty = new PaymentReturn.PartyChoice();
        PaymentReturn.Pty dbtrPty = new PaymentReturn.Pty();
        dbtrPty.setNm(dto.getDebtorName() != null ? dto.getDebtorName() : "");
        dbtrParty.setPty(dbtrPty);
        orgnlTxRef.setDbtr(dbtrParty);

        PaymentReturn.DbtrAcct dbtrAcct = new PaymentReturn.DbtrAcct();
        PaymentReturn.AcctId dbtrAcctId = new PaymentReturn.AcctId();
        dbtrAcctId.setIban(dto.getDebtorAccountNumber() != null ? dto.getDebtorAccountNumber() : "");
        dbtrAcct.setId(dbtrAcctId);
        dbtrAcct.setNm(dto.getDebtorAccountName() != null ? dto.getDebtorAccountName() : dto.getDebtorName());
        orgnlTxRef.setDbtrAcct(dbtrAcct);

        PaymentReturn.DbtrAgt dbtrAgt = new PaymentReturn.DbtrAgt();
        dbtrAgt.setFinInstnId(buildFinInstnId(null, dto.getDebtorAgentMmbId() != null ? dto.getDebtorAgentMmbId() : destinationId));
        orgnlTxRef.setDbtrAgt(dbtrAgt);

        // Creditor
        PaymentReturn.CdtrAgt cdtrAgt = new PaymentReturn.CdtrAgt();
        cdtrAgt.setFinInstnId(buildFinInstnId(null, dto.getCreditorAgentMmbId() != null ? dto.getCreditorAgentMmbId() : sourceId));
        orgnlTxRef.setCdtrAgt(cdtrAgt);

        PaymentReturn.PartyChoice cdtrParty = new PaymentReturn.PartyChoice();
        PaymentReturn.Pty cdtrPty = new PaymentReturn.Pty();
        cdtrPty.setNm(dto.getCreditorName() != null ? dto.getCreditorName() : "");
        cdtrParty.setPty(cdtrPty);
        orgnlTxRef.setCdtr(cdtrParty);

        PaymentReturn.CdtrAcct cdtrAcct = new PaymentReturn.CdtrAcct();
        PaymentReturn.AcctId cdtrAcctId = new PaymentReturn.AcctId();
        cdtrAcctId.setIban(dto.getCreditorAccountNumber() != null ? dto.getCreditorAccountNumber() : "");
        cdtrAcct.setId(cdtrAcctId);
        orgnlTxRef.setCdtrAcct(cdtrAcct);

        txInf.setOrgnlTxRef(orgnlTxRef);
        pmtRtr.setTxInf(txInf);

        // ── Supplementary Data ──────────────────────────────────────────────
        PaymentReturn.SplmtryData splmtryData = new PaymentReturn.SplmtryData();
        splmtryData.setPlcAndNm("AdditionalVerificationDetails");
        pmtRtr.setSplmtryData(splmtryData);

        doc.setPmtRtr(pmtRtr);
        return doc;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static PaymentReturn.InstgAgt buildInstgAgt(String bicfi, String mmbId) {
        PaymentReturn.InstgAgt instgAgt = new PaymentReturn.InstgAgt();
        instgAgt.setFinInstnId(buildFinInstnId(bicfi, mmbId));
        return instgAgt;
    }

    private static PaymentReturn.InstdAgt buildInstdAgt(String mmbId) {
        PaymentReturn.InstdAgt instdAgt = new PaymentReturn.InstdAgt();
        instdAgt.setFinInstnId(buildFinInstnId(null, mmbId));
        return instdAgt;
    }

    private static PaymentReturn.FinInstnId buildFinInstnId(String bicfi, String mmbId) {
        PaymentReturn.FinInstnId finInstnId = new PaymentReturn.FinInstnId();
        if (bicfi != null) {
            finInstnId.setBicfi(bicfi);
        }
        PaymentReturn.ClrSysMmbId clrSysMmbId = new PaymentReturn.ClrSysMmbId();
        clrSysMmbId.setMmbId(mmbId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        return finInstnId;
    }
}
