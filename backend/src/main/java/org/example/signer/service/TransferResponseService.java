package org.example.signer.service;

import org.example.signer.Utils.CurlSender;
import org.example.signer.Utils.Signer;
import org.example.signer.Utils.XmlUtils;
import org.example.signer.dto.TransferResponseDto;
import org.example.signer.model.TransferResponse;
import org.example.signer.xml.TransferResponseXmlGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.io.IOException;
import java.security.PrivateKey;

@Service
public class TransferResponseService {

    @Value("${app.keys.private-path}")
    private String privateKeyPath;

    public String executeTransferResponse(TransferResponseDto requestDto) throws Exception {
        // Generate JAXB object
        TransferResponse response = TransferResponseXmlGenerator.generate(requestDto);

        // Marshal JAXB object to Document
        Document doc = XmlUtils.marshalToDocument(response);
        XmlUtils.writeDocumentToFile(doc, "transfer_response_generated.xml");

        // Load private key
        PrivateKey privateKey = Signer.loadPrivateKey(privateKeyPath);

        // Sign the document
        Signer.sign(doc, privateKey);
        XmlUtils.writeDocumentToFile(doc, "transfer_response_signed.xml");

        String xmlContentToSend = XmlUtils.documentToString(doc);

        // Send request
        String url = "http://10.8.8.132:8022/nps/pacs"; // Assuming same endpoint for responses
        String forwardedIp = "10.8.8.132";
        // Note: The response to a pacs.002 is not expected to be encrypted, so no private key is passed for decryption.
        CurlSender.CurlResult curlResponse = CurlSender.send(xmlContentToSend, url, forwardedIp);

        if (curlResponse.getStatusCode() < 200 || curlResponse.getStatusCode() >= 300) {
            throw new IOException("HTTP request failed with status code " + curlResponse.getStatusCode() + ". Response body: " + curlResponse.getBody());
        }

        return curlResponse.getBody();
    }
}
