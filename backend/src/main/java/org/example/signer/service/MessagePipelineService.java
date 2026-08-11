package org.example.signer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.Utils.CurlSender;
import org.example.signer.Utils.Encrypter;
import org.example.signer.Utils.Signer;
import org.example.signer.Utils.XmlUtils;
import org.example.signer.dto.*;
import org.example.signer.dto.response.MessageSendResponseDto;
import org.example.signer.dto.response.ServicePushResult;
import org.example.signer.dto.response.XmlGenerationResponseDto;
import org.example.signer.model.*;
import org.example.signer.xml.*;
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
import java.util.Random;

@Slf4j
@Service
public class MessagePipelineService {

    @Value("${app.keys.private-path}")
    private String privateKeyPath;

    @Value("${app.keys.public-path}")
    private String publicKeyPath;

    @Value("${AZURE_CLIENT_ID:99b61e01-efcd-4ab8-902e-ec3650518242}")
    private String clientId;

    @Value("${AZURE_CLIENT_SECRET:}")
    private String clientSecret;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ==========================================
    // GROUP A: GENERATE ONLY (NO NETWORK SEND)
    // ==========================================

    public XmlGenerationResponseDto generatePaymentActivationPain013(PaymentActivationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        String endToEndId = generateEndToEndId(requestDto.getSourceId());
        PaymentActivation model = PaymentActivationXmlGenerator.generate(requestDto, msgId, endToEndId);
        return buildXmlGenerationResponse("pain.013", msgId, model);
    }

    public XmlGenerationResponseDto generatePaymentInitiationPain001(PaymentInitiationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getInitiatorId());
        String endToEndId = generateEndToEndId(requestDto.getInitiatorId());
        String reqdExctnDt = generateReqdExctnDt();
        PaymentInitiation model = PaymentInitiationXmlGenerator.generate(requestDto, msgId, endToEndId, reqdExctnDt);
        return buildXmlGenerationResponse("pain.001", msgId, model);
    }

    public XmlGenerationResponseDto generateMandateCreationPain009(MandateCreationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        String mandateId = generateMandateId();
        MandateCreation model = MandateCreationXmlGenerator.generate(requestDto, msgId, mandateId);
        return buildXmlGenerationResponse("pain.009", msgId, model);
    }

    public XmlGenerationResponseDto generateMandateAmendmentPain010(MandateAmendmentRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        MandateAmendment model = MandateAmendmentXmlGenerator.generate(requestDto, msgId);
        return buildXmlGenerationResponse("pain.010", msgId, model);
    }

    public XmlGenerationResponseDto generateMandateCancellationPain011(MandateCancellationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        MandateCancellation model = MandateCancellationXmlGenerator.generate(requestDto, msgId);
        return buildXmlGenerationResponse("pain.011", msgId, model);
    }

    public XmlGenerationResponseDto generateDirectDebitPain008(DirectDebitRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getInitiatorId());
        String endToEndId = generateEndToEndId(requestDto.getInitiatorId());
        String instrId = generateInstrId(requestDto.getInitiatorId(), requestDto.getCreditorId());
        DirectDebit model = DirectDebitXmlGenerator.generate(requestDto, msgId, endToEndId, instrId);
        return buildXmlGenerationResponse("pain.008", msgId, model);
    }

    public XmlGenerationResponseDto generateCustomerDirectDebitPacs003(CustomerDirectDebitRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        CustomerDirectDebit model = CustomerDirectDebitXmlGenerator.generate(requestDto, msgId);
        return buildXmlGenerationResponse("pacs.003", msgId, model);
    }

    public XmlGenerationResponseDto generateTransferPacs008(TransferRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        Transfer model = TransferXmlGenerator.generate(requestDto, msgId);
        return buildXmlGenerationResponse("pacs.008", msgId, model);
    }

    public XmlGenerationResponseDto generatePaymentReturnPacs004(PaymentReturnRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        PaymentReturn model = PaymentReturnXmlGenerator.generate(requestDto, msgId);
        return buildXmlGenerationResponse("pacs.004", msgId, model);
    }

    public XmlGenerationResponseDto generateNameVerificationAcmt023(NameVerificationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        NameVerification model = NameVerificationXmlGenerator.generate(requestDto, msgId);
        return buildXmlGenerationResponse("acmt.023", msgId, model);
    }

    public XmlGenerationResponseDto generateBalanceEnquiryCamt060(AccountReportingRequestDto requestDto) throws Exception {
        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";
        String msgId = generateMsgId(srcId);
        String rptgReqId = generateRptgReqId(srcId, destId);
        BalanceEnquiry model = BalanceEnquiryXmlGenerator.generate(requestDto, msgId, rptgReqId);
        return buildXmlGenerationResponse("camt.060", msgId, model);
    }

    // ==========================================
    // GROUP B: SEND FULL PIPELINE
    // ==========================================

    public MessageSendResponseDto sendPaymentActivationPain013(PaymentActivationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        String endToEndId = generateEndToEndId(requestDto.getSourceId());
        PaymentActivation model = PaymentActivationXmlGenerator.generate(requestDto, msgId, endToEndId);

        return executeFullPipeline("pain.013", msgId, model, "CdtrPmtActvtnReq",
                "http://10.8.8.132:8022/nps/pain", "10.8.8.132", false, null);
    }

    public MessageSendResponseDto sendPaymentInitiationPain001(PaymentInitiationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getInitiatorId());
        String endToEndId = generateEndToEndId(requestDto.getInitiatorId());
        String reqdExctnDt = generateReqdExctnDt();
        PaymentInitiation model = PaymentInitiationXmlGenerator.generate(requestDto, msgId, endToEndId, reqdExctnDt);

        return executeFullPipeline("pain.001", msgId, model, "CstmrCdtTrfInitn",
                "http://10.89.137.138:9095/nibss-inst/pain/001", null, true, "http://10.89.137.138:9095/reset");
    }

    public MessageSendResponseDto sendMandateCreationPain009(MandateCreationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        String mandateId = generateMandateId();
        MandateCreation model = MandateCreationXmlGenerator.generate(requestDto, msgId, mandateId);

        return executeFullPipeline("pain.009", msgId, model, "MndtInitnReq",
                "http://10.8.8.132:8022/nps/pain", "10.8.8.132", false, null);
    }

    public MessageSendResponseDto sendMandateAmendmentPain010(MandateAmendmentRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        MandateAmendment model = MandateAmendmentXmlGenerator.generate(requestDto, msgId);

        return executeFullPipeline("pain.010", msgId, model, "MndtAmdmntReq",
                "http://10.8.8.132:8022/nps/pain", "10.8.8.132", false, null);
    }

    public MessageSendResponseDto sendMandateCancellationPain011(MandateCancellationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        MandateCancellation model = MandateCancellationXmlGenerator.generate(requestDto, msgId);

        return executeFullPipeline("pain.011", msgId, model, "MndtCxlReq",
                "http://10.8.8.132:8022/nps/pain", "10.8.8.132", false, null);
    }

    public MessageSendResponseDto sendDirectDebitPain008(DirectDebitRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getInitiatorId());
        String endToEndId = generateEndToEndId(requestDto.getInitiatorId());
        String instrId = generateInstrId(requestDto.getInitiatorId(), requestDto.getCreditorId());
        DirectDebit model = DirectDebitXmlGenerator.generate(requestDto, msgId, endToEndId, instrId);

        return executeFullPipeline("pain.008", msgId, model, "CstmrDrctDbtInitn",
                "http://10.89.137.138:9095/nibss-inst/pain/008", null, true, "http://10.89.137.138:9095/reset");
    }

    public MessageSendResponseDto sendCustomerDirectDebitPacs003(CustomerDirectDebitRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        CustomerDirectDebit model = CustomerDirectDebitXmlGenerator.generate(requestDto, msgId);

        return executeFullPipeline("pacs.003", msgId, model, "FIToFICstmrDrctDbt",
                "http://10.8.8.132:8022/nps/pacs", "10.8.8.132", false, null);
    }

    public MessageSendResponseDto sendTransferPacs008(TransferRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        Transfer model = TransferXmlGenerator.generate(requestDto, msgId);

        return executeFullPipeline("pacs.008", msgId, model, "FIToFICstmrCdtTrf",
                "http://10.8.8.132:8022/nps/pacs", "10.8.8.132", false, null);
    }

    public MessageSendResponseDto sendPaymentReturnPacs004(PaymentReturnRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        PaymentReturn model = PaymentReturnXmlGenerator.generate(requestDto, msgId);

        return executeFullPipeline("pacs.004", msgId, model, "PmtRtr",
                "http://10.8.8.132:8022/nps/pacs", "10.8.8.132", false, null);
    }

    public MessageSendResponseDto sendNameVerificationAcmt023(NameVerificationRequestDto requestDto) throws Exception {
        String msgId = generateMsgId(requestDto.getSourceId());
        NameVerification model = NameVerificationXmlGenerator.generate(requestDto, msgId);

        return executeFullPipeline("acmt.023", msgId, model, "IdVrfctnReq",
                "http://10.8.8.132:8022/nps/acmt", "10.8.8.132", false, null);
    }

    public MessageSendResponseDto sendBalanceEnquiryCamt060(AccountReportingRequestDto requestDto) throws Exception {
        String srcId = requestDto.getSourceId() != null ? requestDto.getSourceId() : "999998";
        String destId = requestDto.getDestinationId() != null ? requestDto.getDestinationId() : "999997";
        String msgId = generateMsgId(srcId);
        String rptgReqId = generateRptgReqId(srcId, destId);
        BalanceEnquiry model = BalanceEnquiryXmlGenerator.generate(requestDto, msgId, rptgReqId);

        return executeFullPipeline("camt.060", msgId, model, "AcctRptgReq",
                "http://10.8.8.132:8022/nps/camt", "10.8.8.132", false, null);
    }

    // ==========================================
    // HELPER PIPELINE METHODS
    // ==========================================

    private XmlGenerationResponseDto buildXmlGenerationResponse(String messageType, String msgId, Object jaxbModel) throws Exception {
        Document doc = XmlUtils.marshalToDocument(jaxbModel);
        String plainXml = XmlUtils.documentToString(doc);

        PrivateKey privateKey = Signer.loadPrivateKey(privateKeyPath);
        Signer.sign(doc, privateKey);
        String signedXml = XmlUtils.documentToString(doc);

        return XmlGenerationResponseDto.builder()
                .messageType(messageType)
                .messageId(msgId)
                .plainXml(plainXml)
                .signedXml(signedXml)
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    private MessageSendResponseDto executeFullPipeline(
            String messageType,
            String msgId,
            Object jaxbModel,
            String encryptElement,
            String targetUrl,
            String forwardedIp,
            boolean useOAuth,
            String tokenUrl) throws Exception {

        Document doc = XmlUtils.marshalToDocument(jaxbModel);
        String plainXml = XmlUtils.documentToString(doc);

        PrivateKey privateKey = Signer.loadPrivateKey(privateKeyPath);
        PublicKey publicKey = Signer.loadPublicKey(publicKeyPath);

        Signer.sign(doc, privateKey);
        String signedXml = XmlUtils.documentToString(doc);

        if (encryptElement != null && !encryptElement.isEmpty()) {
            Encrypter.encrypt(doc, publicKey, encryptElement);
        }
        String xmlContentToSend = XmlUtils.documentToString(doc);

        long startTime = System.currentTimeMillis();
        ServicePushResult servicePushResult;

        try {
            int statusCode;
            String rawResponseBody;

            if (useOAuth) {
                String token = getOAuthToken(tokenUrl);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .header("Content-Type", "application/xml")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString(xmlContentToSend))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                statusCode = response.statusCode();
                rawResponseBody = response.body();
            } else {
                CurlSender.CurlResult curlResult = CurlSender.send(xmlContentToSend, targetUrl, privateKey, forwardedIp);
                statusCode = curlResult.getStatusCode();
                rawResponseBody = curlResult.getBody();
            }

            long executionTimeMs = System.currentTimeMillis() - startTime;
            servicePushResult = ServicePushResult.builder()
                    .statusCode(statusCode)
                    .rawResponseBody(rawResponseBody)
                    .executionTimeMs(executionTimeMs)
                    .success(statusCode >= 200 && statusCode < 300)
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

        } catch (Exception e) {
            log.error("Error executing network send for {}", messageType, e);
            long executionTimeMs = System.currentTimeMillis() - startTime;
            servicePushResult = ServicePushResult.builder()
                    .statusCode(503)
                    .rawResponseBody("Dispatch Error: " + e.getMessage())
                    .executionTimeMs(executionTimeMs)
                    .success(false)
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();
        }

        return MessageSendResponseDto.builder()
                .messageType(messageType)
                .messageId(msgId)
                .plainXml(plainXml)
                .signedXml(signedXml)
                .serviceResponse(servicePushResult)
                .build();
    }

    private String getOAuthToken(String tokenUrl) throws IOException, InterruptedException {
        if (tokenUrl == null || tokenUrl.isEmpty()) {
            tokenUrl = "https://apitest.nibss-plc.com.ng:1443/reset";
        }
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

        JsonNode jsonNode = OBJECT_MAPPER.readTree(response.body());
        String accessToken = jsonNode.get("access_token") != null ? jsonNode.get("access_token").asText() : null;

        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Failed to extract access_token from OAuth response");
        }

        return accessToken;
    }

    private String generateMsgId(String sourceId) {
        if (sourceId == null || sourceId.trim().isEmpty()) {
            sourceId = "999998";
        }
        Random random = new Random();
        StringBuilder randomDigits = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            randomDigits.append(random.nextInt(10));
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return sourceId + timestamp + randomDigits;
    }

    private String generateEndToEndId(String sourceId) {
        if (sourceId == null || sourceId.trim().isEmpty()) {
            sourceId = "999998";
        }
        Random random = new Random();
        StringBuilder randomDigits = new StringBuilder();
        for (int i = 0; i < 29; i++) {
            randomDigits.append(random.nextInt(10));
        }
        return sourceId + randomDigits;
    }

    private String generateInstrId(String sourceId, String destinationId) {
        if (sourceId == null) sourceId = "999998";
        if (destinationId == null) destinationId = "999997";
        Random random = new Random();
        StringBuilder randomDigits = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            randomDigits.append(random.nextInt(10));
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return sourceId + destinationId + timestamp + randomDigits;
    }

    private String generateMandateId() {
        Random random = new Random();
        return "MNDT-RCUR-" + random.nextInt(1000000);
    }

    private String generateRptgReqId(String sourceId, String destinationId) {
        if (sourceId == null) sourceId = "999998";
        if (destinationId == null) destinationId = "999997";
        Random random = new Random();
        StringBuilder randomDigits = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            randomDigits.append(random.nextInt(10));
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return sourceId + destinationId + timestamp + randomDigits;
    }

    private String generateReqdExctnDt() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
