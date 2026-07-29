package org.example.signer.xml;

import org.example.signer.dto.TransferResponseDto;
import org.example.signer.model.TransferResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TransferResponseXmlGenerator {

    public static TransferResponse generate(TransferResponseDto requestDto) {
        TransferResponse doc = new TransferResponse();
        TransferResponse.FIToFIPmtStsRpt fiToFIPmtStsRpt = new TransferResponse.FIToFIPmtStsRpt();

        // --- Group Header ---
        TransferResponse.GrpHdr grpHdr = new TransferResponse.GrpHdr();
        grpHdr.setMsgId(generateMsgId(requestDto.getSendingInstitutionId()));
        grpHdr.setCreDtTm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")));
        grpHdr.setInstgAgt(createAgt(requestDto.getSendingInstitutionId()));
        grpHdr.setInstdAgt(createAgt(requestDto.getReceivingInstitutionId()));

        // --- Original Group Info and Status ---
        TransferResponse.OrgnlGrpInfAndSts orgnlGrpInfAndSts = new TransferResponse.OrgnlGrpInfAndSts();
        orgnlGrpInfAndSts.setOrgnlMsgId(requestDto.getOriginalMsgId());
        orgnlGrpInfAndSts.setOrgnlMsgNmId("pacs.008.001.12");
        orgnlGrpInfAndSts.setOrgnlCreDtTm(requestDto.getOriginalCreDtTm());
        orgnlGrpInfAndSts.setGrpSts(requestDto.getGroupStatus());

        // --- Transaction Info and Status ---
        TransferResponse.TxInfAndSts txInfAndSts = new TransferResponse.TxInfAndSts();
        txInfAndSts.setInstgAgt(createAgt(requestDto.getSendingInstitutionId()));
        txInfAndSts.setInstdAgt(createAgt(requestDto.getReceivingInstitutionId()));
        TransferResponse.OrgnlTxRef orgnlTxRef = new TransferResponse.OrgnlTxRef();
        orgnlTxRef.setIntrBkSttlmDt(requestDto.getSettlementDate());
        txInfAndSts.setOrgnlTxRef(orgnlTxRef);

        // --- Assemble Document ---
        fiToFIPmtStsRpt.setGrpHdr(grpHdr);
        fiToFIPmtStsRpt.setOrgnlGrpInfAndSts(orgnlGrpInfAndSts);
        fiToFIPmtStsRpt.setTxInfAndSts(txInfAndSts);
        doc.setFiToFIPmtStsRpt(fiToFIPmtStsRpt);

        return doc;
    }

    private static TransferResponse.Agt createAgt(String mmbId) {
        TransferResponse.Agt agt = new TransferResponse.Agt();
        TransferResponse.FinInstnId finInstnId = new TransferResponse.FinInstnId();
        TransferResponse.ClrSysMmbId clrSysMmbId = new TransferResponse.ClrSysMmbId();
        clrSysMmbId.setMmbId(mmbId);
        finInstnId.setClrSysMmbId(clrSysMmbId);
        agt.setFinInstnId(finInstnId);
        return agt;
    }


    private static String generateMsgId(String institutionId) {
        Random random = new Random();
        StringBuilder randomDigits = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            randomDigits.append(random.nextInt(10));
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter msgIdFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String msgIdTimestamp = now.format(msgIdFormatter);

        return institutionId + msgIdTimestamp + randomDigits.toString();
    }
}
