package org.example.signer.xml;

import org.example.signer.dto.BankStatementDto;
import org.example.signer.model.BankStatement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BankStatementXmlGenerator {

    public static BankStatement generate(BankStatementDto requestDto, String msgId, String stmtId) {
        BankStatement doc = new BankStatement();
        BankStatement.BkToCstmrStmt bkStmt = new BankStatement.BkToCstmrStmt();

        // --- Group Header ---
        BankStatement.GrpHdr grpHdr = new BankStatement.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = ZonedDateTime.now(ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);

        BankStatement.MsgRcpt msgRcpt = new BankStatement.MsgRcpt();
        msgRcpt.setNm(requestDto.getMessageRecipientName() != null ? requestDto.getMessageRecipientName() : "Debtor Bank Name");
        BankStatement.MsgRcptId rcptId = new BankStatement.MsgRcptId();
        BankStatement.OrgId orgId = new BankStatement.OrgId();
        orgId.setAnyBIC(requestDto.getMessageRecipientBIC() != null ? requestDto.getMessageRecipientBIC() : "MSGRCPT");
        rcptId.setOrgId(orgId);
        msgRcpt.setId(rcptId);
        grpHdr.setMsgRcpt(msgRcpt);

        BankStatement.OrgnlBizQry bizQry = new BankStatement.OrgnlBizQry();
        bizQry.setMsgId(requestDto.getOriginalQueryMsgId() != null ? requestDto.getOriginalQueryMsgId() : "99905820260213292033011112202634446");
        bizQry.setMsgNmId(requestDto.getOriginalQueryMsgNmId() != null ? requestDto.getOriginalQueryMsgNmId() : "camt.060.001.07");
        bizQry.setCreDtTm(requestDto.getOriginalQueryCreDtTm() != null ? requestDto.getOriginalQueryCreDtTm() : "2026-02-13T13:32:00.000Z");
        grpHdr.setOrgnlBizQry(bizQry);

        bkStmt.setGrpHdr(grpHdr);

        // --- Stmt ---
        BankStatement.Stmt stmt = new BankStatement.Stmt();
        stmt.setId(stmtId);

        BankStatement.FrToDt frToDt = new BankStatement.FrToDt();
        frToDt.setFrDtTm(requestDto.getFromDateTime() != null ? requestDto.getFromDateTime() : "2025-04-20T00:00:00.000Z");
        frToDt.setToDtTm(requestDto.getToDateTime() != null ? requestDto.getToDateTime() : "2025-04-20T23:59:59.000Z");
        stmt.setFrToDt(frToDt);

        BankStatement.Acct acct = new BankStatement.Acct();
        BankStatement.AcctId acctId = new BankStatement.AcctId();
        acctId.setIban(requestDto.getAccountNumber() != null ? requestDto.getAccountNumber() : "8887788778");
        acct.setId(acctId);
        acct.setCcy(requestDto.getCurrency() != null ? requestDto.getCurrency() : "NGN");

        BankStatement.Ownr ownr = new BankStatement.Ownr();
        BankStatement.OwnrId ownrId = new BankStatement.OwnrId();
        BankStatement.OwnrOrgId ownrOrgId = new BankStatement.OwnrOrgId();
        BankStatement.Othr ownrOthr = new BankStatement.Othr();
        BankStatement.SchmeNm schmeNm = new BankStatement.SchmeNm();
        String schemeCode = requestDto.getSchemeCode() != null ? requestDto.getSchemeCode() : "999057";
        schmeNm.setCd(schemeCode);
        schmeNm.setPrtry(schemeCode);
        ownrOthr.setSchmeNm(schmeNm);
        ownrOrgId.setOthr(ownrOthr);
        ownrId.setOrgId(ownrOrgId);
        ownr.setId(ownrId);
        acct.setOwnr(ownr);

        BankStatement.Svcr svcr = new BankStatement.Svcr();
        BankStatement.FinInstnId svcrFinInstnId = new BankStatement.FinInstnId();
        String svcrMmbId = requestDto.getAccountServicerMemberId() != null ? requestDto.getAccountServicerMemberId() : "999058";
        svcrFinInstnId.setBicfi(requestDto.getAccountServicerBIC() != null ? requestDto.getAccountServicerBIC() : "XYZBNGNLXXX");
        BankStatement.ClrSysMmbId svcrClrSysMmbId = new BankStatement.ClrSysMmbId();
        svcrClrSysMmbId.setMmbId(svcrMmbId);
        svcrFinInstnId.setClrSysMmbId(svcrClrSysMmbId);
        svcr.setFinInstnId(svcrFinInstnId);
        acct.setSvcr(svcr);
        stmt.setAcct(acct);

        // --- Balances ---
        List<BankStatement.Bal> balances = new ArrayList<>();
        
        // Opening Balance
        BankStatement.Bal opBal = new BankStatement.Bal();
        BankStatement.BalTp opBalTp = new BankStatement.BalTp();
        BankStatement.CdOrPrtry opCdOrPrtry = new BankStatement.CdOrPrtry();
        opCdOrPrtry.setCd(requestDto.getOpeningBalanceCode() != null ? requestDto.getOpeningBalanceCode() : "OPBD");
        opCdOrPrtry.setPrtry(requestDto.getOpeningBalanceType() != null ? requestDto.getOpeningBalanceType() : "CLRG");
        opBalTp.setCdOrPrtry(opCdOrPrtry);
        opBal.setTp(opBalTp);

        BankStatement.Amount opAmt = new BankStatement.Amount();
        opAmt.setCcy(acct.getCcy());
        opAmt.setValue(requestDto.getOpeningBalanceAmount() != null ? requestDto.getOpeningBalanceAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("482000.00"));
        opBal.setAmt(opAmt);
        opBal.setCdtDbtInd(requestDto.getOpeningBalanceCdtDbtInd() != null ? requestDto.getOpeningBalanceCdtDbtInd() : "CRDT");

        BankStatement.BalDt opBalDt = new BankStatement.BalDt();
        opBalDt.setDtTm("2025-04-20T10:21:33.000Z");
        opBal.setDt(opBalDt);
        balances.add(opBal);

        // Closing Balance
        BankStatement.Bal clBal = new BankStatement.Bal();
        BankStatement.BalTp clBalTp = new BankStatement.BalTp();
        BankStatement.CdOrPrtry clCdOrPrtry = new BankStatement.CdOrPrtry();
        clCdOrPrtry.setCd(requestDto.getClosingBalanceCode() != null ? requestDto.getClosingBalanceCode() : "CLBD");
        clCdOrPrtry.setPrtry(requestDto.getClosingBalanceType() != null ? requestDto.getClosingBalanceType() : "CLRG");
        clBalTp.setCdOrPrtry(clCdOrPrtry);
        clBal.setTp(clBalTp);

        BankStatement.Amount clAmt = new BankStatement.Amount();
        clAmt.setCcy(acct.getCcy());
        clAmt.setValue(requestDto.getClosingBalanceAmount() != null ? requestDto.getClosingBalanceAmount().setScale(2, RoundingMode.HALF_UP) : new BigDecimal("500000.00"));
        clBal.setAmt(clAmt);
        clBal.setCdtDbtInd(requestDto.getClosingBalanceCdtDbtInd() != null ? requestDto.getClosingBalanceCdtDbtInd() : "CRDT");

        BankStatement.BalDt clBalDt = new BankStatement.BalDt();
        clBalDt.setDt("2025-04-20Z");
        clBalDt.setDtTm("2025-04-20T10:21:33.000Z");
        clBal.setDt(clBalDt);
        balances.add(clBal);

        stmt.setBal(balances);

        // --- Entries ---
        List<BankStatement.Ntry> entries = new ArrayList<>();
        BankStatement.Ntry ntry1 = new BankStatement.Ntry();
        BankStatement.Amount ntry1Amt = new BankStatement.Amount();
        ntry1Amt.setCcy(acct.getCcy());
        ntry1Amt.setValue(new BigDecimal("30000.00"));
        ntry1.setAmt(ntry1Amt);
        ntry1.setCdtDbtInd("CRDT");
        BankStatement.Sts ntry1Sts = new BankStatement.Sts();
        ntry1Sts.setCd("BOOK");
        ntry1Sts.setPrtry("BOOK");
        ntry1.setSts(ntry1Sts);
        BankStatement.EntryDt bookgDt = new BankStatement.EntryDt();
        bookgDt.setDt("2025-04-20Z");
        ntry1.setBookgDt(bookgDt);
        BankStatement.EntryDt valDt = new BankStatement.EntryDt();
        valDt.setDt("2025-04-20Z");
        ntry1.setValDt(valDt);
        ntry1.setAcctSvcrRef(msgId);
        BankStatement.BkTxCd bkTxCd1 = new BankStatement.BkTxCd();
        BankStatement.Domn domn1 = new BankStatement.Domn();
        domn1.setCd("PMNT");
        BankStatement.Fmly fmly1 = new BankStatement.Fmly();
        fmly1.setCd("RCDT");
        fmly1.setSubFmlyCd("ESCT");
        domn1.setFmly(fmly1);
        bkTxCd1.setDomn(domn1);
        ntry1.setBkTxCd(bkTxCd1);
        entries.add(ntry1);

        stmt.setNtry(entries);
        bkStmt.setStmt(stmt);
        doc.setBkToCstmrStmt(bkStmt);
        return doc;
    }
}
