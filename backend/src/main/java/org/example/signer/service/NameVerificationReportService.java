package org.example.signer.service;

import org.example.signer.Utils.CurlSender;
import org.example.signer.Utils.Encrypter;
import org.example.signer.Utils.Signer;
import org.example.signer.Utils.XmlUtils;
import org.example.signer.dto.NameVerificationReportDto;
import org.example.signer.model.NameVerificationReport;
import org.example.signer.xml.NameVerificationReportXmlGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

@Service
public class NameVerificationReportService {

    @Value("${app.keys.private-path}")
    private String privateKeyPath;

    @Value("${app.keys.public-path}")
    private String publicKeyPath;

    public String executeNameVerificationReport(NameVerificationReportDto requestDto) throws Exception {
        // Generate JAXB object
        NameVerificationReport report = NameVerificationReportXmlGenerator.generate(requestDto);

        // Marshal JAXB object to Document
        Document doc = XmlUtils.marshalToDocument(report);
        XmlUtils.writeDocumentToFile(doc, "name_verification_report_generated.xml");

        // Load keys
        PrivateKey privateKey = Signer.loadPrivateKey(privateKeyPath);
        PublicKey publicKey = Signer.loadPublicKey(publicKeyPath);

        // Sign the document
        Signer.sign(doc, privateKey);
        XmlUtils.writeDocumentToFile(doc, "name_verification_report_signed.xml");

        // Encrypt the IdVrfctnRpt block
        Encrypter.encrypt(doc, publicKey, "IdVrfctnRpt");
        XmlUtils.writeDocumentToFile(doc, "name_verification_report_encrypted.xml");

        String xmlContentToSend = XmlUtils.documentToString(doc);

        // Send request
        String url = "http://10.8.8.132:8022/nps/acmt"; // Assuming same endpoint for reports
        String forwardedIp = "10.8.8.132";
        CurlSender.CurlResult response = CurlSender.send(xmlContentToSend, url, forwardedIp);

        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            throw new IOException("HTTP request failed with status code " + response.getStatusCode() + ". Response body: " + response.getBody());
        }

        return response.getBody();
    }
}
