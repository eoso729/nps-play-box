package org.example.signer.xml;

import org.example.signer.dto.CustomerPaymentStatusReportDto;
import org.example.signer.model.CustomerPaymentStatusReport;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomerPaymentStatusReportXmlGenerator {

    public static CustomerPaymentStatusReport generate(CustomerPaymentStatusReportDto requestDto, String msgId, String stsId) {
        CustomerPaymentStatusReport doc = new CustomerPaymentStatusReport();
        CustomerPaymentStatusReport.CstmrPmtStsRpt rpt = new CustomerPaymentStatusReport.CstmrPmtStsRpt();

        // --- Group Header ---
        CustomerPaymentStatusReport.GrpHdr grpHdr = new CustomerPaymentStatusReport.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = ZonedDateTime.now(ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);

        CustomerPaymentStatusReport.Party initgPty = new CustomerPaymentStatusReport.Party();
        initgPty.setNm(requestDto.getInitiatingPartyName() != null ? requestDto.getInitiatingPartyName() : "Musa");
        grpHdr.setInitgPty(initgPty);

        CustomerPaymentStatusReport.Agent dbtrAgt = new CustomerPaymentStatusReport.Agent();
        CustomerPaymentStatusReport.FinInstnId finInstnId = new CustomerPaymentStatusReport.FinInstnId();
        String mmbId = requestDto.getDebtorAgentMemberId() != null && !requestDto.getDebtorAgentMemberId().trim().isEmpty()
                ? requestDto.getDebtorAgentMemberId().trim() : "999057";
        String bic = requestDto.getDebtorAgentBIC() != null && !requestDto.getDebtorAgentBIC().trim().isEmpty()
                ? requestDto.getDebtorAgentBIC().trim() : mmbId;
        finInstnId.setBicfi(bic);
        CustomerPaymentStatusReport.ClrSysMmbId clrSysMmbId = new CustomerPaymentStatusReport.ClrSysMmbId();
        clrSysMmbId.setMmbId(mmbId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        dbtrAgt.setFinInstnId(finInstnId);
        grpHdr.setDbtrAgt(dbtrAgt);
        rpt.setGrpHdr(grpHdr);

        // --- Original Group Info & Status ---
        CustomerPaymentStatusReport.OrgnlGrpInfAndSts orgnlGrpInf = new CustomerPaymentStatusReport.OrgnlGrpInfAndSts();
        orgnlGrpInf.setOrgnlMsgId(requestDto.getOriginalMsgId() != null ? requestDto.getOriginalMsgId() : "99905720260225192650869851166984847");
        orgnlGrpInf.setOrgnlMsgNmId(requestDto.getOriginalMsgNmId() != null ? requestDto.getOriginalMsgNmId() : "pain.001.001.12");
        orgnlGrpInf.setGrpSts(requestDto.getGroupStatus() != null ? requestDto.getGroupStatus() : "ACSC");
        rpt.setOrgnlGrpInfAndSts(orgnlGrpInf);

        // --- Original Payment Info & Status ---
        CustomerPaymentStatusReport.OrgnlPmtInfAndSts orgnlPmtInf = new CustomerPaymentStatusReport.OrgnlPmtInfAndSts();
        orgnlPmtInf.setOrgnlPmtInfId(requestDto.getOriginalPmtInfId() != null ? requestDto.getOriginalPmtInfId() : orgnlGrpInf.getOrgnlMsgId());

        CustomerPaymentStatusReport.TxInfAndSts txInf = new CustomerPaymentStatusReport.TxInfAndSts();
        txInf.setStsId(requestDto.getStatusId() != null ? requestDto.getStatusId() : stsId);
        txInf.setOrgnlEndToEndId(requestDto.getOriginalEndToEndId() != null ? requestDto.getOriginalEndToEndId() : txInf.getStsId());
        txInf.setTxSts(requestDto.getTransactionStatus() != null ? requestDto.getTransactionStatus() : orgnlGrpInf.getGrpSts());

        if (requestDto.getStatusCode() != null || requestDto.getAdditionalInformation() != null) {
            CustomerPaymentStatusReport.StsRsnInf stsRsnInf = new CustomerPaymentStatusReport.StsRsnInf();
            if (requestDto.getStatusCode() != null) {
                CustomerPaymentStatusReport.Rsn rsn = new CustomerPaymentStatusReport.Rsn();
                rsn.setCd(requestDto.getStatusCode());
                stsRsnInf.setRsn(rsn);
            }
            if (requestDto.getAdditionalInformation() != null) {
                stsRsnInf.setAddtlInf(requestDto.getAdditionalInformation());
            }
            txInf.setStsRsnInf(stsRsnInf);
        }
        orgnlPmtInf.setTxInfAndSts(txInf);
        rpt.setOrgnlPmtInfAndSts(orgnlPmtInf);

        doc.setCstmrPmtStsRpt(rpt);
        return doc;
    }
}
