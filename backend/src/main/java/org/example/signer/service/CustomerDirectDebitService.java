package org.example.signer.service;

import org.example.signer.Utils.CurlSender;
import org.example.signer.Utils.Encrypter;
import org.example.signer.Utils.Signer;
import org.example.signer.Utils.XmlUtils;
import org.example.signer.dto.CustomerDirectDebitRequestDto;
import org.example.signer.model.CustomerDirectDebit;
import org.example.signer.xml.CustomerDirectDebitXmlGenerator;
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
public class CustomerDirectDebitService {

    @Value("${app.keys.private-path}")
    private String privateKeyPath;

    @Value("${app.keys.public-path}")
    private String publicKeyPath;

    public Map<String, String> executeCustomerDirectDebit(CustomerDirectDebitRequestDto requestDto) throws Exception {

        String newMsgId = generateMsgId(requestDto.getSourceId());

        // Generate JAXB object
        CustomerDirectDebit customerDirectDebit = CustomerDirectDebitXmlGenerator.generate(requestDto, newMsgId);

        // Marshal JAXB object to Document
        Document doc = XmlUtils.marshalToDocument(customerDirectDebit);
        XmlUtils.writeDocumentToFile(doc, "customer_direct_debit_generated.xml");

        // Load private key for signing
        PrivateKey privateKey = Signer.loadPrivateKey(privateKeyPath);
        PublicKey publicKey = Signer.loadPublicKey(publicKeyPath);

        // Sign the document
        Signer.sign(doc, privateKey);
        XmlUtils.writeDocumentToFile(doc, "customer_direct_debit_signed.xml");

        // Encrypt the sensitive data in the signed document
        Encrypter.encrypt(doc, publicKey, "FIToFICstmrDrctDbt");
        XmlUtils.writeDocumentToFile(doc, "customer_direct_debit_encrypted.xml");

        String xmlContentToSend = XmlUtils.documentToString(doc);

        // Send the signed XML document
        String url = "http://10.8.8.132:8022/nps/pacs"; // pacs endpoint
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
        if (sourceId == null) sourceId = "999998";
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
}
