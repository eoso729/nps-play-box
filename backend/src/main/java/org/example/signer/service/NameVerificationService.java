package org.example.signer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.Utils.CurlSender;
import org.example.signer.Utils.Encrypter;
import org.example.signer.Utils.Signer;
import org.example.signer.Utils.XmlUtils;
import org.example.signer.dto.NameVerificationRequestDto;
import org.example.signer.model.NameVerification;
import org.example.signer.xml.NameVerificationXmlGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class NameVerificationService {

    @Value("${app.keys.private-path}")
    private String privateKeyPath;

    @Value("${app.keys.public-path}")
    private String publicKeyPath;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Map<String, String> executeNameVerification(NameVerificationRequestDto requestDto) throws Exception {
        // Generate MsgId
        String newMsgId = generateMsgId(requestDto.getSourceId());

        // Generate JAXB object
        NameVerification nameVerification = NameVerificationXmlGenerator.generate(requestDto, newMsgId);

        // Marshal JAXB object to Document
        Document doc = XmlUtils.marshalToDocument(nameVerification);
        XmlUtils.writeDocumentToFile(doc, "name_verification_generated.xml");

        // Load keys
        PrivateKey privateKey = Signer.loadPrivateKey(privateKeyPath);
        PublicKey publicKey = Signer.loadPublicKey(publicKeyPath);

        // Sign the document
        Signer.sign(doc, privateKey);
        XmlUtils.writeDocumentToFile(doc, "name_verification_signed.xml");

        // Encrypt the IdVrfctnReq block
        Encrypter.encrypt(doc, publicKey, "IdVrfctnReq");
        XmlUtils.writeDocumentToFile(doc, "name_verification_encrypted.xml");

        String xmlContentToSend = XmlUtils.documentToString(doc);

        // Send request
        String url = "http://10.8.8.132:8022/nps/acmt"; // Assuming acmt for name verification
        String forwardedIp = "10.8.8.132";
        CurlSender.CurlResult response = CurlSender.send(xmlContentToSend, url, forwardedIp);

        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            throw new IOException("HTTP request failed with status code " + response.getStatusCode() + ". Response body: " + response.getBody());
        }

        Map<String, String> detailsedResposne = new HashMap<>();
        detailsedResposne.put("response", response.getBody());
        detailsedResposne.put("messageId", newMsgId);


        return detailsedResposne;
    }

    public Map<String, String> executeNameVerificationAcmt023(NameVerificationRequestDto requestDto) throws Exception {
        // Generate MsgId
        String newMsgId = generateMsgId(requestDto.getSourceId());

        // Generate JAXB object
        NameVerification nameVerification = NameVerificationXmlGenerator.generate(requestDto, newMsgId);

        // Marshal JAXB object to Document
        Document doc = XmlUtils.marshalToDocument(nameVerification);
        XmlUtils.writeDocumentToFile(doc, "name_verification_generated.xml");

        // Load keys
        PrivateKey privateKey = Signer.loadPrivateKey(privateKeyPath);
        PublicKey publicKey = Signer.loadPublicKey(publicKeyPath);

        // Sign the document
        Signer.sign(doc, privateKey);
        XmlUtils.writeDocumentToFile(doc, "name_verification_signed.xml");

        // Encrypt the IdVrfctnReq block
        Encrypter.encrypt(doc, publicKey, "IdVrfctnReq");
        XmlUtils.writeDocumentToFile(doc, "name_verification_encrypted.xml");

        String xmlContentToSend = XmlUtils.documentToString(doc);

        // Step 1: Get OAuth token
        String accessToken = getOAuthToken();

        // Step 2: Send the signed and encrypted XML document with Bearer token
        String url = "https://apitest.nibss-plc.com.ng:1443/nibss-inst/acmt/023";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/xml")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(xmlContentToSend))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP request failed with status code " + response.statusCode() + ". Response body: " + response.body());
        }

        Map<String, String> detailedResponse = new HashMap<>();
        detailedResponse.put("responseCode", String.valueOf(response.statusCode()));
        detailedResponse.put("response", response.body());
        detailedResponse.put("messageId", newMsgId);

        return detailedResponse;
    }

    @Value("${AZURE_CLIENT_ID:99b61e01-efcd-4ab8-902e-ec3650518242}")
    private String clientId;

    @Value("${AZURE_CLIENT_SECRET:}")
    private String clientSecret;

    private String getOAuthToken() throws IOException, InterruptedException {
        String tokenUrl = "https://apitest.nibss-plc.com.ng:1443/reset";

        // Build form data
        String formData = "client_Id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(clientId + "/.default", StandardCharsets.UTF_8)
                + "&grant_type=" + URLEncoder.encode("client_credentials", StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OAuth token request failed with status code " + response.statusCode() + ". Response: " + response.body());
        }

        // Parse JSON response to extract access_token
        JsonNode jsonNode = OBJECT_MAPPER.readTree(response.body());
        String accessToken = jsonNode.get("access_token").asText();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Failed to extract access_token from OAuth response");
        }

        return accessToken;
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
}
