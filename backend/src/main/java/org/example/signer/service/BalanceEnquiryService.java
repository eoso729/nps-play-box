package org.example.signer.service;

import org.example.signer.Utils.CurlSender;
import org.example.signer.Utils.Encrypter;
import org.example.signer.Utils.Signer;
import org.example.signer.Utils.XmlUtils;
import org.example.signer.dto.AccountReportingRequestDto;
import org.example.signer.model.BalanceEnquiry;
import org.example.signer.xml.BalanceEnquiryXmlGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class BalanceEnquiryService {

    @Value("${app.keys.private-path}")
    private String privateKeyPath;

    @Value("${app.keys.public-path}")
    private String publicKeyPath;

    public Map<String, String> executeBalanceEnquiry(AccountReportingRequestDto requestDto) throws Exception {

        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";

        String newMsgId = generateMsgId(srcId);
        String rptgReqId = generateRptgReqId(srcId, destId);

        // Generate JAXB object
        BalanceEnquiry balanceEnquiry = BalanceEnquiryXmlGenerator.generate(requestDto, newMsgId, rptgReqId);

        // Marshal JAXB object to Document
        Document doc = XmlUtils.marshalToDocument(balanceEnquiry);
        XmlUtils.writeDocumentToFile(doc, "balance_enquiry_generated.xml");

        // Load keys
        PrivateKey privateKey = Signer.loadPrivateKey(privateKeyPath);
        PublicKey publicKey = Signer.loadPublicKey(publicKeyPath);

        // Sign the document
        Signer.sign(doc, privateKey);
        XmlUtils.writeDocumentToFile(doc, "balance_enquiry_signed.xml");

        // Encrypt the sensitive data in the signed document
        Encrypter.encrypt(doc, publicKey, "AcctRptgReq");
        XmlUtils.writeDocumentToFile(doc, "balance_enquiry_encrypted.xml");

        String xmlContentToSend = XmlUtils.documentToString(doc);

        // Send the signed and encrypted XML document
        String url = "http://10.8.8.132:8022/nps/camt";
        String forwardedIp = "10.8.8.132";
        CurlSender.CurlResult response = CurlSender.send(xmlContentToSend, url, privateKey, forwardedIp);

        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            throw new IOException("HTTP request failed with status code " + response.getStatusCode() + ". Response body: " + response.getBody());
        }

        Map<String, String> detailedResponse = new HashMap<>();
        detailedResponse.put("response", response.getBody());
        detailedResponse.put("messageId", newMsgId);

        return detailedResponse;
    }

    private String generateMsgId(String sourceId) {
        Random random = new Random();
        StringBuilder randomDigits = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            randomDigits.append(random.nextInt(10));
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter msgIdFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String msgIdTimestamp = now.format(msgIdFormatter);

        return sourceId + msgIdTimestamp + randomDigits.toString();
    }

    private String generateRptgReqId(String sourceId, String destinationId) {
        Random random = new Random();
        StringBuilder randomDigits = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            randomDigits.append(random.nextInt(10));
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);

        return sourceId + destinationId + timestamp + randomDigits.toString();
    }
}
