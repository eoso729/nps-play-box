package org.example.signer.xml;

import org.example.signer.dto.TransferResponseDto;
import org.example.signer.model.TransferResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TransferResponseXmlGenerator {

    public static TransferResponse generate(TransferResponseDto requestDto) {
        TransferResponse doc = new TransferResponse();
        TransferResponse.FIToFIPmtStsRpt fiToFIPmtStsRpt = new TransferResponse.FIToFIPmtStsRpt();

        String srcId = requestDto.getSendingInstitutionId() != null && !requestDto.getSendingInstitutionId().isEmpty()
                ? requestDto.getSendingInstitutionId() : "090004";
        String destId = requestDto.getReceivingInstitutionId() != null && !requestDto.getReceivingInstitutionId().isEmpty()
                ? requestDto.getReceivingInstitutionId() : "100022";

        // --- Group Header ---
        TransferResponse.GrpHdr grpHdr = new TransferResponse.GrpHdr();
        grpHdr.setMsgId(generateMsgId(srcId));
        grpHdr.setCreDtTm(ZonedDateTime.now(ZoneId.of("Africa/Lagos")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));
        grpHdr.setInstgAgt(createAgt(srcId));
        grpHdr.setInstdAgt(createAgt(destId));

        // --- Original Group Info and Status ---
        TransferResponse.OrgnlGrpInfAndSts orgnlGrpInfAndSts = new TransferResponse.OrgnlGrpInfAndSts();
        String origMsgId = requestDto.getOriginalMsgId() != null ? requestDto.getOriginalMsgId() : generateMsgId(destId);
        orgnlGrpInfAndSts.setOrgnlMsgId(origMsgId);
        orgnlGrpInfAndSts.setOrgnlMsgNmId(requestDto.getOriginalMsgNmId() != null ? requestDto.getOriginalMsgNmId() : "pacs.008.001.12");
        orgnlGrpInfAndSts.setOrgnlCreDtTm(requestDto.getOriginalCreDtTm() != null ? requestDto.getOriginalCreDtTm() :
                ZonedDateTime.now(ZoneId.of("Africa/Lagos")).minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));
        orgnlGrpInfAndSts.setGrpSts(requestDto.getGroupStatus() != null ? requestDto.getGroupStatus() : "ACSC");

        // --- Transaction Info and Status ---
        TransferResponse.TxInfAndSts txInfAndSts = new TransferResponse.TxInfAndSts();
        txInfAndSts.setStsId(requestDto.getStatusId() != null ? requestDto.getStatusId() : "AUTH");
        txInfAndSts.setOrgnlInstrId(requestDto.getOriginalInstrId() != null ? requestDto.getOriginalInstrId() : generateId(destId + srcId, 9));
        txInfAndSts.setOrgnlEndToEndId(requestDto.getOriginalEndToEndId() != null ? requestDto.getOriginalEndToEndId() : generateId(destId, 15));
        txInfAndSts.setOrgnlTxId(requestDto.getOriginalTxId() != null ? requestDto.getOriginalTxId() : origMsgId);
        txInfAndSts.setInstgAgt(createAgt(srcId));
        txInfAndSts.setInstdAgt(createAgt(destId));
        TransferResponse.OrgnlTxRef orgnlTxRef = new TransferResponse.OrgnlTxRef();
        String sttlmDt = requestDto.getSettlementDate() != null ? requestDto.getSettlementDate() :
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "Z";
        orgnlTxRef.setIntrBkSttlmDt(sttlmDt.endsWith("Z") ? sttlmDt : sttlmDt + "Z");
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


    private static String generateId(String prefix, int randomLength) {
        Random random = new Random();
        StringBuilder randomDigits = new StringBuilder();
        for (int i = 0; i < randomLength; i++) {
            randomDigits.append(random.nextInt(10));
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter msgIdFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String msgIdTimestamp = now.format(msgIdFormatter);

        return prefix + msgIdTimestamp + randomDigits.toString();
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
