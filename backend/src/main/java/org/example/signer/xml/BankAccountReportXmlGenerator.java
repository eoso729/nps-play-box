package org.example.signer.xml;

import org.example.signer.dto.BankAccountReportDto;
import org.example.signer.model.BankAccountReport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BankAccountReportXmlGenerator {

    public static BankAccountReport generate(BankAccountReportDto requestDto, String msgId, String rptId) {
        BankAccountReport doc = new BankAccountReport();
        BankAccountReport.BkToCstmrAcctRpt bkRpt = new BankAccountReport.BkToCstmrAcctRpt();

        // --- Group Header ---
        BankAccountReport.GrpHdr grpHdr = new BankAccountReport.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = ZonedDateTime.now(ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);

        BankAccountReport.MsgRcpt msgRcpt = new BankAccountReport.MsgRcpt();
        msgRcpt.setNm(requestDto.getMessageRecipientName() != null ? requestDto.getMessageRecipientName() : "Debtor Bank Name");
        BankAccountReport.MsgRcptId rcptId = new BankAccountReport.MsgRcptId();
        String rcptBic = requestDto.getMessageRecipientBIC() != null ? requestDto.getMessageRecipientBIC() :
                (requestDto.getSchemeCode() != null ? requestDto.getSchemeCode() : "999057");
        BankAccountReport.OrgId orgId = new BankAccountReport.OrgId();
        orgId.setAnyBIC(rcptBic);
        rcptId.setOrgId(orgId);
        msgRcpt.setId(rcptId);
        grpHdr.setMsgRcpt(msgRcpt);

        BankAccountReport.OrgnlBizQry bizQry = new BankAccountReport.OrgnlBizQry();
        bizQry.setMsgId(requestDto.getOriginalQueryMsgId() != null ? requestDto.getOriginalQueryMsgId() : "99905820260302123735603795909182287");
        bizQry.setMsgNmId(requestDto.getOriginalQueryMsgNmId() != null ? requestDto.getOriginalQueryMsgNmId() : "camt.060.001.07");
        bizQry.setCreDtTm(requestDto.getOriginalQueryCreDtTm() != null ? requestDto.getOriginalQueryCreDtTm() : "2026-03-02T11:37:35.242Z");
        grpHdr.setOrgnlBizQry(bizQry);

        bkRpt.setGrpHdr(grpHdr);

        // --- Report ---
        BankAccountReport.Rpt rpt = new BankAccountReport.Rpt();
        rpt.setId(rptId);

        BankAccountReport.FrToDt frToDt = new BankAccountReport.FrToDt();
        frToDt.setFrDtTm(requestDto.getFromDateTime() != null ? requestDto.getFromDateTime() : "2026-02-01T00:00:00.000Z");
        frToDt.setToDtTm(requestDto.getToDateTime() != null ? requestDto.getToDateTime() : "2026-02-28T23:59:59.000Z");
        rpt.setFrToDt(frToDt);

        BankAccountReport.Acct acct = new BankAccountReport.Acct();
        BankAccountReport.AcctId acctId = new BankAccountReport.AcctId();
        acctId.setIban(requestDto.getAccountNumber() != null ? requestDto.getAccountNumber() : "4488447166");
        acct.setId(acctId);
        acct.setCcy(requestDto.getCurrency() != null ? requestDto.getCurrency() : "NGN");

        BankAccountReport.Ownr ownr = new BankAccountReport.Ownr();
        BankAccountReport.OwnrId ownrId = new BankAccountReport.OwnrId();
        BankAccountReport.OwnrOrgId ownrOrgId = new BankAccountReport.OwnrOrgId();
        BankAccountReport.Othr ownrOthr = new BankAccountReport.Othr();
        BankAccountReport.SchmeNm schmeNm = new BankAccountReport.SchmeNm();
        String schemeCode = requestDto.getSchemeCode() != null ? requestDto.getSchemeCode() : "999057";
        schmeNm.setCd(schemeCode);
        schmeNm.setPrtry(requestDto.getProprietaryScheme() != null ? requestDto.getProprietaryScheme() : schemeCode);
        ownrOthr.setSchmeNm(schmeNm);
        ownrOrgId.setOthr(ownrOthr);
        ownrId.setOrgId(ownrOrgId);
        ownr.setId(ownrId);
        acct.setOwnr(ownr);

        BankAccountReport.Svcr svcr = new BankAccountReport.Svcr();
        BankAccountReport.FinInstnId svcrFinInstnId = new BankAccountReport.FinInstnId();
        String svcrMmbId = requestDto.getAccountServicerMemberId() != null ? requestDto.getAccountServicerMemberId() : "999058";
        svcrFinInstnId.setBicfi(requestDto.getAccountServicerBIC() != null ? requestDto.getAccountServicerBIC() : svcrMmbId);
        BankAccountReport.ClrSysMmbId svcrClrSysMmbId = new BankAccountReport.ClrSysMmbId();
        svcrClrSysMmbId.setMmbId(svcrMmbId);
        svcrFinInstnId.setClrSysMmbId(svcrClrSysMmbId);
        svcr.setFinInstnId(svcrFinInstnId);
        acct.setSvcr(svcr);
        rpt.setAcct(acct);

        // --- Balance ---
        BankAccountReport.Bal bal = new BankAccountReport.Bal();
        BankAccountReport.BalTp balTp = new BankAccountReport.BalTp();
        BankAccountReport.CdOrPrtry cdOrPrtry = new BankAccountReport.CdOrPrtry();
        cdOrPrtry.setPrtry(requestDto.getBalanceType() != null ? requestDto.getBalanceType() : "CLRG");
        balTp.setCdOrPrtry(cdOrPrtry);
        bal.setTp(balTp);

        BankAccountReport.Amount balAmt = new BankAccountReport.Amount();
        balAmt.setCcy(acct.getCcy());
        balAmt.setValue(requestDto.getBalanceAmount() != null ? requestDto.getBalanceAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("500000.00"));
        bal.setAmt(balAmt);
        bal.setCdtDbtInd(requestDto.getCreditDebitIndicator() != null ? requestDto.getCreditDebitIndicator() : "CRDT");

        BankAccountReport.BalDt balDt = new BankAccountReport.BalDt();
        balDt.setDtTm(requestDto.getBalanceDateTime() != null ? requestDto.getBalanceDateTime() : creDtTm);
        bal.setDt(balDt);
        rpt.setBal(bal);

        // --- Entries ---
        List<BankAccountReport.Ntry> entries = new ArrayList<>();
        BankAccountReport.Ntry ntry1 = new BankAccountReport.Ntry();
        BankAccountReport.Amount ntry1Amt = new BankAccountReport.Amount();
        ntry1Amt.setCcy(acct.getCcy());
        BigDecimal entryVal = requestDto.getEntryAmount() != null ? requestDto.getEntryAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("30000.00");
        ntry1Amt.setValue(entryVal);
        ntry1.setAmt(ntry1Amt);
        ntry1.setCdtDbtInd(requestDto.getEntryCreditDebitIndicator() != null ? requestDto.getEntryCreditDebitIndicator() : "CRDT");
        BankAccountReport.Sts ntry1Sts = new BankAccountReport.Sts();
        ntry1Sts.setPrtry(requestDto.getEntryStatus() != null ? requestDto.getEntryStatus() : "BOOK");
        ntry1.setSts(ntry1Sts);
        BankAccountReport.EntryDt bookgDt = new BankAccountReport.EntryDt();
        bookgDt.setDt(requestDto.getEntryBookingDate() != null ? requestDto.getEntryBookingDate() : "2026-03-02Z");
        ntry1.setBookgDt(bookgDt);
        BankAccountReport.EntryDt valDt = new BankAccountReport.EntryDt();
        valDt.setDt(requestDto.getEntryValueDate() != null ? requestDto.getEntryValueDate() : "2026-03-02Z");
        ntry1.setValDt(valDt);
        ntry1.setAcctSvcrRef(requestDto.getAccountServicerReference() != null ? requestDto.getAccountServicerReference() : msgId);
        BankAccountReport.BkTxCd bkTxCd1 = new BankAccountReport.BkTxCd();
        BankAccountReport.Domn domn1 = new BankAccountReport.Domn();
        domn1.setCd(requestDto.getDomainCode() != null ? requestDto.getDomainCode() : "PMNT");
        BankAccountReport.Fmly fmly1 = new BankAccountReport.Fmly();
        fmly1.setCd(requestDto.getFamilyCode() != null ? requestDto.getFamilyCode() : "RCDT");
        fmly1.setSubFmlyCd(requestDto.getSubFamilyCode() != null ? requestDto.getSubFamilyCode() : "ESCT");
        domn1.setFmly(fmly1);
        bkTxCd1.setDomn(domn1);
        ntry1.setBkTxCd(bkTxCd1);

        if (requestDto.getInstructedAgentBIC() != null && !requestDto.getInstructedAgentBIC().trim().isEmpty()) {
            BankAccountReport.NtryDtls ntryDtls = new BankAccountReport.NtryDtls();
            BankAccountReport.TxDtls txDtls = new BankAccountReport.TxDtls();
            BankAccountReport.RltdAgts rltdAgts = new BankAccountReport.RltdAgts();
            BankAccountReport.InstdAgt instdAgt = new BankAccountReport.InstdAgt();
            BankAccountReport.FinInstnId finInstnId = new BankAccountReport.FinInstnId();
            finInstnId.setBicfi(requestDto.getInstructedAgentBIC().trim());
            instdAgt.setFinInstnId(finInstnId);
            rltdAgts.setInstdAgt(instdAgt);
            txDtls.setRltdAgts(rltdAgts);
            ntryDtls.setTxDtls(txDtls);
            ntry1.setNtryDtls(ntryDtls);
        }

        entries.add(ntry1);
        rpt.setNtry(entries);
        bkRpt.setRpt(rpt);
        doc.setBkToCstmrAcctRpt(bkRpt);
        return doc;
    }
}
