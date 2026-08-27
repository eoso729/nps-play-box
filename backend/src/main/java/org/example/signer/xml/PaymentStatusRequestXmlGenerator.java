package org.example.signer.xml;

import org.example.signer.dto.PaymentStatusRequestDto;
import org.example.signer.model.PaymentStatusRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PaymentStatusRequestXmlGenerator {

    public static PaymentStatusRequest generate(PaymentStatusRequestDto requestDto, String msgId) {
        PaymentStatusRequest doc = new PaymentStatusRequest();
        PaymentStatusRequest.FIToFIPmtStsReq req = new PaymentStatusRequest.FIToFIPmtStsReq();

        // --- Group Header ---
        PaymentStatusRequest.GrpHdr grpHdr = new PaymentStatusRequest.GrpHdr();
        grpHdr.setMsgId(msgId);
        String creDtTm = ZonedDateTime.now(ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        grpHdr.setCreDtTm(creDtTm);

        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999057";
        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999012";

        grpHdr.setInstgAgt(createAgt(srcId, false));
        req.setGrpHdr(grpHdr);

        // --- Original Group Info ---
        PaymentStatusRequest.OrgnlGrpInf orgnlGrpInf = new PaymentStatusRequest.OrgnlGrpInf();
        orgnlGrpInf.setOrgnlMsgId(requestDto.getOriginalMsgId() != null ? requestDto.getOriginalMsgId() : "99905820250802112346977904433112345");
        orgnlGrpInf.setOrgnlMsgNmId(requestDto.getOriginalMsgNmId() != null ? requestDto.getOriginalMsgNmId() : "pacs.008.001.12");
        orgnlGrpInf.setOrgnlCreDtTm(requestDto.getOriginalCreDtTm() != null ? requestDto.getOriginalCreDtTm() : "2025-02-25T00:02:35.072Z");
        req.setOrgnlGrpInf(orgnlGrpInf);

        // --- Transaction Info ---
        PaymentStatusRequest.TxInf txInf = new PaymentStatusRequest.TxInf();
        txInf.setStsReqId(msgId);
        txInf.setOrgnlTxId(requestDto.getOriginalTxId() != null ? requestDto.getOriginalTxId() : orgnlGrpInf.getOrgnlMsgId());
        txInf.setInstgAgt(createAgt(srcId, true));
        txInf.setInstdAgt(createAgt(destId, true));

        PaymentStatusRequest.OrgnlTxRef orgnlTxRef = new PaymentStatusRequest.OrgnlTxRef();
        orgnlTxRef.setIntrBkSttlmDt(requestDto.getSettlementDate() != null ? requestDto.getSettlementDate() : "2025-02-25");
        txInf.setOrgnlTxRef(orgnlTxRef);
        req.setTxInf(txInf);

        doc.setFiToFIPmtStsReq(req);
        return doc;
    }

    private static PaymentStatusRequest.Agt createAgt(String mmbId, boolean includeBicfi) {
        PaymentStatusRequest.Agt agt = new PaymentStatusRequest.Agt();
        PaymentStatusRequest.FinInstnId finInstnId = new PaymentStatusRequest.FinInstnId();
        if (includeBicfi) {
            finInstnId.setBicfi(mmbId);
        }
        PaymentStatusRequest.ClrSysMmbId clrSysMmbId = new PaymentStatusRequest.ClrSysMmbId();
        clrSysMmbId.setMmbId(mmbId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        agt.setFinInstnId(finInstnId);
        return agt;
    }
}
