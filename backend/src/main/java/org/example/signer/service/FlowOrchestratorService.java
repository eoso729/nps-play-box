package org.example.signer.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.*;
import org.example.signer.dto.orchestrator.*;
import org.example.signer.dto.response.MessageSendResponseDto;
import org.example.signer.dto.response.XmlGenerationResponseDto;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowOrchestratorService {

    private final MessagePipelineService messagePipelineService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Pattern MANDATE_ID_PATTERN = Pattern.compile("<MndtId>([^<]+)</MndtId>");
    private static final Pattern END_TO_END_ID_PATTERN = Pattern.compile("<EndToEndId>([^<]+)</EndToEndId>");
    private static final Pattern INSTR_ID_PATTERN = Pattern.compile("<InstrId>([^<]+)</InstrId>");
    private static final Pattern TX_ID_PATTERN = Pattern.compile("<TxId>([^<]+)</TxId>");
    private static final Pattern MSG_ID_PATTERN = Pattern.compile("<MsgId>([^<]+)</MsgId>");

    /**
     * Returns all registered workflow journeys.
     */
    public List<FlowDefinitionDto> getAllFlows() {
        return List.of(
                getDirectDebitFlowDefinition(),
                getInstantCreditTransferFlowDefinition(),
                getRequestToPayFlowDefinition()
        );
    }

    /**
     * Find a flow by its unique ID.
     */
    public FlowDefinitionDto getFlowById(String flowId) {
        return getAllFlows().stream()
                .filter(f -> f.getId().equalsIgnoreCase(flowId))
                .findFirst()
                .orElse(getDirectDebitFlowDefinition());
    }

    /**
     * Executes a single step in a guided workflow journey.
     */
    public FlowStepExecutionResponseDto executeStep(FlowStepExecutionRequestDto request) {
        long startTime = System.currentTimeMillis();
        String flowId = request.getFlowId() != null ? request.getFlowId() : "direct-debit";
        FlowDefinitionDto flow = getFlowById(flowId);
        int stepIndex = request.getStepIndex();
        String messageType = request.getMessageType();

        if (messageType == null && stepIndex >= 0 && stepIndex < flow.getSteps().size()) {
            messageType = flow.getSteps().get(stepIndex).getMessageType();
        }

        Map<String, String> currentContext = request.getCurrentContext() != null
                ? new HashMap<>(request.getCurrentContext())
                : new HashMap<>();

        Map<String, Object> payload = request.getPayload() != null
                ? new HashMap<>(request.getPayload())
                : new HashMap<>();

        boolean isSend = "SEND".equalsIgnoreCase(request.getAction());

        try {
            String plainXml;
            String signedXml;
            String messageId;
            org.example.signer.dto.response.ServicePushResult servicePushResult = null;

            // Execute based on messageType
            switch (messageType != null ? messageType.trim() : "") {
                case "pain.009": {
                    MandateCreationRequestDto dto = OBJECT_MAPPER.convertValue(payload, MandateCreationRequestDto.class);
                    if (isSend) {
                        MessageSendResponseDto res = messagePipelineService.sendMandateCreationPain009(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                        servicePushResult = res.getServiceResponse();
                    } else {
                        XmlGenerationResponseDto res = messagePipelineService.generateMandateCreationPain009(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                    }
                    break;
                }
                case "pain.012": {
                    MandateAcceptanceReportDto dto = OBJECT_MAPPER.convertValue(payload, MandateAcceptanceReportDto.class);
                    if (isSend) {
                        MessageSendResponseDto res = messagePipelineService.sendMandateAcceptancePain012(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                        servicePushResult = res.getServiceResponse();
                    } else {
                        XmlGenerationResponseDto res = messagePipelineService.generateMandateAcceptancePain012(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                    }
                    break;
                }
                case "pain.008": {
                    DirectDebitRequestDto dto = OBJECT_MAPPER.convertValue(payload, DirectDebitRequestDto.class);
                    if (isSend) {
                        MessageSendResponseDto res = messagePipelineService.sendDirectDebitPain008(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                        servicePushResult = res.getServiceResponse();
                    } else {
                        XmlGenerationResponseDto res = messagePipelineService.generateDirectDebitPain008(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                    }
                    break;
                }
                case "pacs.003": {
                    CustomerDirectDebitRequestDto dto = OBJECT_MAPPER.convertValue(payload, CustomerDirectDebitRequestDto.class);
                    if (isSend) {
                        MessageSendResponseDto res = messagePipelineService.sendCustomerDirectDebitPacs003(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                        servicePushResult = res.getServiceResponse();
                    } else {
                        XmlGenerationResponseDto res = messagePipelineService.generateCustomerDirectDebitPacs003(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                    }
                    break;
                }
                case "acmt.023": {
                    NameVerificationRequestDto dto = OBJECT_MAPPER.convertValue(payload, NameVerificationRequestDto.class);
                    if (isSend) {
                        MessageSendResponseDto res = messagePipelineService.sendNameVerificationAcmt023(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                        servicePushResult = res.getServiceResponse();
                    } else {
                        XmlGenerationResponseDto res = messagePipelineService.generateNameVerificationAcmt023(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                    }
                    break;
                }
                case "pacs.008": {
                    TransferRequestDto dto = OBJECT_MAPPER.convertValue(payload, TransferRequestDto.class);
                    if (isSend) {
                        MessageSendResponseDto res = messagePipelineService.sendTransferPacs008(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                        servicePushResult = res.getServiceResponse();
                    } else {
                        XmlGenerationResponseDto res = messagePipelineService.generateTransferPacs008(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                    }
                    break;
                }
                case "pacs.004": {
                    PaymentReturnRequestDto dto = OBJECT_MAPPER.convertValue(payload, PaymentReturnRequestDto.class);
                    if (isSend) {
                        MessageSendResponseDto res = messagePipelineService.sendPaymentReturnPacs004(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                        servicePushResult = res.getServiceResponse();
                    } else {
                        XmlGenerationResponseDto res = messagePipelineService.generatePaymentReturnPacs004(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                    }
                    break;
                }
                case "pain.013": {
                    PaymentActivationRequestDto dto = OBJECT_MAPPER.convertValue(payload, PaymentActivationRequestDto.class);
                    if (isSend) {
                        MessageSendResponseDto res = messagePipelineService.sendPaymentActivationPain013(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                        servicePushResult = res.getServiceResponse();
                    } else {
                        XmlGenerationResponseDto res = messagePipelineService.generatePaymentActivationPain013(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                    }
                    break;
                }
                case "pacs.002": {
                    TransferResponseDto dto = OBJECT_MAPPER.convertValue(payload, TransferResponseDto.class);
                    if (isSend) {
                        MessageSendResponseDto res = messagePipelineService.sendTransferResponsePacs002(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                        servicePushResult = res.getServiceResponse();
                    } else {
                        XmlGenerationResponseDto res = messagePipelineService.generatePaymentStatusReportPacs002(dto);
                        plainXml = res.getPlainXml();
                        signedXml = res.getSignedXml();
                        messageId = res.getMessageId();
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unsupported workflow message type: " + messageType);
            }

            // Extract context variables produced by this step
            Map<String, String> extractedContext = extractContextVariables(messageType, payload, plainXml, messageId);

            // Merge into updated cumulative context
            Map<String, String> updatedContext = new HashMap<>(currentContext);
            updatedContext.putAll(extractedContext);

            // Determine next step prefill
            Integer nextStepIndex = null;
            String nextMessageType = null;
            Map<String, Object> nextStepPrefill = null;

            if (stepIndex + 1 < flow.getSteps().size()) {
                nextStepIndex = stepIndex + 1;
                nextMessageType = flow.getSteps().get(nextStepIndex).getMessageType();
                FlowMapNextStepResponseDto mapRes = mapNextStep(FlowMapNextStepRequestDto.builder()
                        .flowId(flowId)
                        .targetStepIndex(nextStepIndex)
                        .targetMessageType(nextMessageType)
                        .context(updatedContext)
                        .previousStepPayload(payload)
                        .build());
                nextStepPrefill = mapRes.getPrefilledPayload();
            }

            long elapsed = System.currentTimeMillis() - startTime;

            return FlowStepExecutionResponseDto.builder()
                    .success(true)
                    .flowId(flowId)
                    .stepIndex(stepIndex)
                    .messageType(messageType)
                    .messageId(messageId)
                    .plainXml(plainXml)
                    .signedXml(signedXml)
                    .serviceResponse(servicePushResult)
                    .executionTime(elapsed + "ms")
                    .extractedContext(extractedContext)
                    .updatedContext(updatedContext)
                    .nextStepPrefill(nextStepPrefill)
                    .nextMessageType(nextMessageType)
                    .nextStepIndex(nextStepIndex)
                    .build();

        } catch (Exception e) {
            log.error("Error executing flow step {} in flow {}", stepIndex, flowId, e);
            long elapsed = System.currentTimeMillis() - startTime;
            return FlowStepExecutionResponseDto.builder()
                    .success(false)
                    .flowId(flowId)
                    .stepIndex(stepIndex)
                    .messageType(messageType)
                    .executionTime(elapsed + "ms")
                    .errorMessage(e.getMessage() != null ? e.getMessage() : e.toString())
                    .updatedContext(currentContext)
                    .build();
        }
    }

    /**
     * Maps accumulated context into the target step's prefilled form payload.
     */
    public FlowMapNextStepResponseDto mapNextStep(FlowMapNextStepRequestDto request) {
        String flowId = request.getFlowId() != null ? request.getFlowId() : "direct-debit";
        FlowDefinitionDto flow = getFlowById(flowId);
        int targetIndex = request.getTargetStepIndex();
        String targetMsgType = request.getTargetMessageType();

        if (targetMsgType == null && targetIndex >= 0 && targetIndex < flow.getSteps().size()) {
            targetMsgType = flow.getSteps().get(targetIndex).getMessageType();
        }

        Map<String, String> ctx = request.getContext() != null ? request.getContext() : Collections.emptyMap();
        Map<String, Object> prev = request.getPreviousStepPayload() != null ? request.getPreviousStepPayload() : Collections.emptyMap();

        Map<String, Object> prefill = new HashMap<>();
        List<FlowMapNextStepResponseDto.MappedFieldInfo> mappedFields = new ArrayList<>();

        if (targetIndex >= 0 && targetIndex < flow.getSteps().size()) {
            Map<String, Object> defaults = flow.getSteps().get(targetIndex).getDefaultPayload();
            if (defaults != null) {
                prefill.putAll(defaults);
            }
        }

        if (targetMsgType != null) {
            switch (targetMsgType) {
                case "pain.012":
                    applyField(prefill, mappedFields, "originalMandateId", ctx.get("mandateId"), "mandateId", "Mandate ID from Mandate Creation (pain.009)");
                    applyField(prefill, mappedFields, "originalMsgId", ctx.get("pain009MsgId") != null ? ctx.get("pain009MsgId") : ctx.get("originalMsgId"), "pain009MsgId", "Message ID from pain.009");
                    applyField(prefill, mappedFields, "originalMsgNmId", "pain.009.001.07", "constant", "Original Message Definition");
                    applyField(prefill, mappedFields, "accepted", "true", "constant", "Authorization Confirmation");
                    applyField(prefill, mappedFields, "creditorName", ctx.get("creditorName"), "creditorName", "Creditor Name from pain.009");
                    applyField(prefill, mappedFields, "creditorAccountNumber", ctx.get("creditorAccountNumber"), "creditorAccountNumber", "Creditor Account");
                    applyField(prefill, mappedFields, "creditorAgentMemberId", ctx.get("creditorAgentMemberId"), "creditorAgentMemberId", "Creditor Agent Member ID");
                    applyField(prefill, mappedFields, "debtorName", ctx.get("debtorName"), "debtorName", "Debtor Name from pain.009");
                    applyField(prefill, mappedFields, "debtorAccountNumber", ctx.get("debtorAccountNumber"), "debtorAccountNumber", "Debtor Account");
                    applyField(prefill, mappedFields, "debtorAgentMemberId", ctx.get("debtorAgentMemberId"), "debtorAgentMemberId", "Debtor Agent Member ID");
                    applyField(prefill, mappedFields, "sequenceType", ctx.getOrDefault("sequenceType", "RCUR"), "sequenceType", "Sequence Type");
                    applyField(prefill, mappedFields, "frequencyType", ctx.getOrDefault("frequencyType", "MNTH"), "frequencyType", "Frequency Type");
                    applyField(prefill, mappedFields, "firstCollectionDate", ctx.get("firstCollectionDate"), "firstCollectionDate", "First Collection Date");
                    applyField(prefill, mappedFields, "finalCollectionDate", ctx.get("finalCollectionDate"), "finalCollectionDate", "Final Collection Date");
                    break;

                case "pain.008":
                    applyField(prefill, mappedFields, "mandateId", ctx.get("mandateId"), "mandateId", "Authorized Mandate ID from pain.009/pain.012");
                    applyField(prefill, mappedFields, "nameEnquiryMsgId", ctx.get("nameEnquiryMsgId") != null ? ctx.get("nameEnquiryMsgId") : ctx.get("sessionId"), "nameEnquiryMsgId", "Session ID / Name Enquiry Reference");
                    applyField(prefill, mappedFields, "creditorId", ctx.get("creditorAgentMemberId"), "creditorAgentMemberId", "Creditor Clearing Member ID");
                    applyField(prefill, mappedFields, "creditorName", ctx.get("creditorName"), "creditorName", "Creditor Name");
                    applyField(prefill, mappedFields, "creditorIban", ctx.get("creditorAccountNumber"), "creditorAccountNumber", "Creditor Account");
                    applyField(prefill, mappedFields, "debtorId", ctx.get("debtorAgentMemberId"), "debtorAgentMemberId", "Debtor Clearing Member ID");
                    applyField(prefill, mappedFields, "debtorName", ctx.get("debtorName"), "debtorName", "Debtor Name");
                    applyField(prefill, mappedFields, "debtorIban", ctx.get("debtorAccountNumber"), "debtorAccountNumber", "Debtor Account");
                    applyField(prefill, mappedFields, "amount", ctx.get("amount") != null ? ctx.get("amount") : "25000.00", "amount", "Collection Amount");
                    applyField(prefill, mappedFields, "currency", ctx.getOrDefault("currency", "NGN"), "currency", "Currency");
                    applyField(prefill, mappedFields, "sequenceType", ctx.getOrDefault("sequenceType", "RCUR"), "sequenceType", "Mandate Sequence Type");
                    applyField(prefill, mappedFields, "freqTp", ctx.getOrDefault("frequencyType", "MNTH"), "frequencyType", "Mandate Frequency");
                    applyField(prefill, mappedFields, "frstColltnDt", ctx.getOrDefault("firstCollectionDate", "2026-09-10"), "firstCollectionDate", "First Collection Date");
                    applyField(prefill, mappedFields, "fnlColltnDt", ctx.getOrDefault("finalCollectionDate", "2027-09-10"), "finalCollectionDate", "Final Collection Date");
                    applyField(prefill, mappedFields, "dtOfSgntr", ctx.getOrDefault("firstCollectionDate", "2026-09-01"), "dateOfSignature", "Signature Date");
                    break;

                case "pacs.003":
                    applyField(prefill, mappedFields, "mandateId", ctx.get("mandateId"), "mandateId", "Mandate ID from pain.009/pain.012");
                    applyField(prefill, mappedFields, "nameEnquiryMsgId", ctx.get("nameEnquiryMsgId") != null ? ctx.get("nameEnquiryMsgId") : ctx.get("sessionId"), "nameEnquiryMsgId", "Session ID / Name Enquiry Reference");
                    applyField(prefill, mappedFields, "sourceId", ctx.get("creditorAgentMemberId") != null ? ctx.get("creditorAgentMemberId") : "999057", "creditorAgentMemberId", "Originating Creditor Member ID");
                    applyField(prefill, mappedFields, "destinationId", ctx.get("debtorAgentMemberId") != null ? ctx.get("debtorAgentMemberId") : "999058", "debtorAgentMemberId", "Instructed Debtor Member ID");
                    applyField(prefill, mappedFields, "creditorName", ctx.get("creditorName"), "creditorName", "Creditor Name");
                    applyField(prefill, mappedFields, "creditorAccountNumber", ctx.get("creditorAccountNumber"), "creditorAccountNumber", "Creditor Account");
                    applyField(prefill, mappedFields, "debtorName", ctx.get("debtorName"), "debtorName", "Debtor Name");
                    applyField(prefill, mappedFields, "debtorAccountNumber", ctx.get("debtorAccountNumber"), "debtorAccountNumber", "Debtor Account");
                    applyField(prefill, mappedFields, "amount", ctx.get("amount") != null ? ctx.get("amount") : "25000.00", "amount", "Debit Amount");
                    applyField(prefill, mappedFields, "currency", ctx.getOrDefault("currency", "NGN"), "currency", "Currency");
                    applyField(prefill, mappedFields, "frequencyType", ctx.getOrDefault("frequencyType", "MNTH"), "frequencyType", "Mandate Frequency");
                    applyField(prefill, mappedFields, "firstCollectionDate", ctx.getOrDefault("firstCollectionDate", "2026-09-10"), "firstCollectionDate", "Collection Date");
                    applyField(prefill, mappedFields, "finalCollectionDate", ctx.getOrDefault("finalCollectionDate", "2027-09-10"), "finalCollectionDate", "Final Date");
                    applyField(prefill, mappedFields, "dateOfSignature", ctx.getOrDefault("firstCollectionDate", "2026-09-01"), "dateOfSignature", "Signature Date");
                    break;

                case "pacs.008":
                    applyField(prefill, mappedFields, "nameEnquiryMsgId", ctx.get("nameEnquiryMsgId") != null ? ctx.get("nameEnquiryMsgId") : ctx.get("sessionId"), "nameEnquiryMsgId", "Session ID / Name Enquiry MsgId from acmt.023");
                    applyField(prefill, mappedFields, "sourceId", ctx.get("sourceId") != null ? ctx.get("sourceId") : "999999", "sourceId", "Sending Institution ID");
                    applyField(prefill, mappedFields, "destinationId", ctx.get("beneficiaryId") != null ? ctx.get("beneficiaryId") : (ctx.get("destinationId") != null ? ctx.get("destinationId") : "999015"), "destinationId", "Beneficiary Bank ID");
                    applyField(prefill, mappedFields, "beneficiaryName", ctx.get("partyToBeVerifiedName") != null ? ctx.get("partyToBeVerifiedName") : ctx.get("beneficiaryName"), "partyToBeVerifiedName", "Verified Beneficiary Name");
                    applyField(prefill, mappedFields, "beneficiaryAccountNumber", ctx.get("partyToBeVerifiedAccountNumber") != null ? ctx.get("partyToBeVerifiedAccountNumber") : ctx.get("beneficiaryAccountNumber"), "partyToBeVerifiedAccountNumber", "Verified Beneficiary Account");
                    applyField(prefill, mappedFields, "beneficiaryAccountName", ctx.get("partyToBeVerifiedName") != null ? ctx.get("partyToBeVerifiedName") : ctx.get("beneficiaryAccountName"), "partyToBeVerifiedName", "Beneficiary Account Name");
                    applyField(prefill, mappedFields, "senderName", ctx.get("sendingPartyName") != null ? ctx.get("sendingPartyName") : (ctx.get("debtorName") != null ? ctx.get("debtorName") : "Zenith Originator"), "sendingPartyName", "Sender Name");
                    if (ctx.get("amount") != null) {
                        applyField(prefill, mappedFields, "amount", ctx.get("amount"), "amount", "Transfer Amount");
                    }
                    if (ctx.get("endToEndId") != null) {
                        applyField(prefill, mappedFields, "endToEndId", ctx.get("endToEndId"), "endToEndId", "Pre-negotiated End-to-End Reference");
                    }
                    break;

                case "pacs.004":
                    // Reversed Routing: Receiving Bank sends return to Originating Bank
                    applyField(prefill, mappedFields, "sourceId", ctx.get("destinationId") != null ? ctx.get("destinationId") : "999998", "destinationId", "Reversed Source (Returning Agent Member ID)");
                    applyField(prefill, mappedFields, "destinationId", ctx.get("sourceId") != null ? ctx.get("sourceId") : "999057", "sourceId", "Reversed Destination (Original Instructing Agent)");
                    applyField(prefill, mappedFields, "originalMsgId", ctx.get("pacs008MsgId") != null ? ctx.get("pacs008MsgId") : ctx.get("originalMsgId"), "pacs008MsgId", "Original pacs.008 Message ID");
                    applyField(prefill, mappedFields, "originalMsgNameId", "pacs.008.001.10", "constant", "Original Message Definition");
                    applyField(prefill, mappedFields, "originalInstrId", ctx.get("instructionId"), "instructionId", "Original Instruction ID from pacs.008");
                    applyField(prefill, mappedFields, "originalEndToEndId", ctx.get("endToEndId"), "endToEndId", "Original EndToEnd ID from pacs.008");
                    applyField(prefill, mappedFields, "originalTxId", ctx.get("endToEndId"), "endToEndId", "Original Transaction ID");
                    applyField(prefill, mappedFields, "returnedAmount", ctx.get("amount") != null ? ctx.get("amount") : "50000.00", "amount", "Returned Amount");
                    applyField(prefill, mappedFields, "originalIntrBkSttlmAmt", ctx.get("amount") != null ? ctx.get("amount") : "50000.00", "amount", "Original Settlement Amount");
                    applyField(prefill, mappedFields, "debtorName", ctx.get("senderName") != null ? ctx.get("senderName") : "Original Debtor", "senderName", "Original Debtor Name");
                    applyField(prefill, mappedFields, "debtorAccountNumber", ctx.get("senderAccountNumber") != null ? ctx.get("senderAccountNumber") : "0000002110", "senderAccountNumber", "Original Debtor Account");
                    applyField(prefill, mappedFields, "debtorAccountName", ctx.get("senderAccountName") != null ? ctx.get("senderAccountName") : ctx.get("senderName"), "senderName", "Original Debtor Account Name");
                    applyField(prefill, mappedFields, "debtorAgentMmbId", ctx.get("sourceId") != null ? ctx.get("sourceId") : "999057", "sourceId", "Original Debtor Agent Member ID");
                    applyField(prefill, mappedFields, "creditorName", ctx.get("beneficiaryName") != null ? ctx.get("beneficiaryName") : "Beneficiary", "beneficiaryName", "Original Creditor Name");
                    applyField(prefill, mappedFields, "creditorAccountNumber", ctx.get("beneficiaryAccountNumber") != null ? ctx.get("beneficiaryAccountNumber") : "3157417712", "beneficiaryAccountNumber", "Original Creditor Account");
                    applyField(prefill, mappedFields, "creditorAgentMmbId", ctx.get("destinationId") != null ? ctx.get("destinationId") : "999998", "destinationId", "Original Creditor Agent Member ID");
                    applyField(prefill, mappedFields, "returnReasonCode", "AC04", "constant", "Return Reason Code (Closed Account)");
                    applyField(prefill, mappedFields, "returnReasonInfo", "Account closed / regulatory return", "constant", "Return Narrative");
                    break;

                case "pain.013":
                    applyField(prefill, mappedFields, "nameEnquiryMsgId", ctx.get("nameEnquiryMsgId") != null ? ctx.get("nameEnquiryMsgId") : ctx.get("sessionId"), "nameEnquiryMsgId", "Session ID / Name Enquiry Reference");
                    applyField(prefill, mappedFields, "creditorName", ctx.get("partyToBeVerifiedName") != null ? ctx.get("partyToBeVerifiedName") : "Creditor Corp", "partyToBeVerifiedName", "Beneficiary / Creditor Name");
                    applyField(prefill, mappedFields, "creditorAccountNumber", ctx.get("partyToBeVerifiedAccountNumber") != null ? ctx.get("partyToBeVerifiedAccountNumber") : "3157417712", "partyToBeVerifiedAccountNumber", "Creditor Account Number");
                    applyField(prefill, mappedFields, "creditorAgentMemberId", ctx.get("beneficiaryId") != null ? ctx.get("beneficiaryId") : "999997", "beneficiaryId", "Creditor Agent Member ID");
                    applyField(prefill, mappedFields, "sourceId", ctx.get("beneficiaryId") != null ? ctx.get("beneficiaryId") : "999997", "beneficiaryId", "Originator Clearing Member ID");
                    applyField(prefill, mappedFields, "destinationId", ctx.get("sourceId") != null ? ctx.get("sourceId") : "991015", "sourceId", "Payer Agent Member ID");
                    if (ctx.get("amount") != null) {
                        applyField(prefill, mappedFields, "amount", ctx.get("amount"), "amount", "Requested Amount");
                    }
                    break;

                case "pacs.002":
                    String origMsgId = ctx.get("directDebitMsgId") != null ? ctx.get("directDebitMsgId") :
                            (ctx.get("pacs008MsgId") != null ? ctx.get("pacs008MsgId") :
                                    (ctx.get("pain008MsgId") != null ? ctx.get("pain008MsgId") :
                                            (ctx.get("pacs003MsgId") != null ? ctx.get("pacs003MsgId") : ctx.get("originalMsgId"))));
                    applyField(prefill, mappedFields, "originalMsgId", origMsgId, "previousMsgId", "Message ID from preceding transaction step");
                    applyField(prefill, mappedFields, "originalMsgNmId", ctx.getOrDefault("lastTriggerMsgNmId", "pacs.008.001.10"), "lastTriggerMsgNmId", "Trigger Message ISO Definition");
                    applyField(prefill, mappedFields, "originalInstrId", ctx.get("instructionId"), "instructionId", "Instruction ID from preceding step");
                    applyField(prefill, mappedFields, "originalEndToEndId", ctx.get("endToEndId"), "endToEndId", "End-to-End Reference from preceding step");
                    applyField(prefill, mappedFields, "originalTxId", ctx.get("endToEndId"), "endToEndId", "Transaction Reference from preceding step");
                    applyField(prefill, mappedFields, "groupStatus", "ACSC", "constant", "Settlement Confirmation Status (Accepted Settlement Completed)");
                    applyField(prefill, mappedFields, "statusId", ctx.get("endToEndId"), "endToEndId", "Status Tracking Identifier");
                    applyField(prefill, mappedFields, "sourceId", ctx.get("destinationId") != null ? ctx.get("destinationId") : "999998", "destinationId", "Confirming Institution Member ID");
                    applyField(prefill, mappedFields, "destinationId", ctx.get("sourceId") != null ? ctx.get("sourceId") : "999057", "sourceId", "Receiving Institution Member ID");
                    break;
            }
        }

        return FlowMapNextStepResponseDto.builder()
                .flowId(flowId)
                .targetStepIndex(targetIndex)
                .targetMessageType(targetMsgType)
                .prefilledPayload(prefill)
                .mappedFields(mappedFields)
                .build();
    }

    /**
     * Executes an entire flow sequentially from start to finish.
     */
    public FlowAutoRunResponseDto autoRunFlow(FlowAutoRunRequestDto request) {
        long overallStart = System.currentTimeMillis();
        String flowId = request.getFlowId() != null ? request.getFlowId() : "direct-debit";
        FlowDefinitionDto flow = getFlowById(flowId);
        String action = request.getAction() != null ? request.getAction() : "GENERATE";

        Map<String, String> context = request.getInitialContext() != null
                ? new HashMap<>(request.getInitialContext())
                : new HashMap<>();

        List<FlowStepExecutionResponseDto> transcript = new ArrayList<>();
        Map<String, Object> currentPayload = request.getInitialStepPayload() != null
                ? new HashMap<>(request.getInitialStepPayload())
                : null;

        for (int i = 0; i < flow.getSteps().size(); i++) {
            FlowStepDefinitionDto stepDef = flow.getSteps().get(i);

            // If not step 0, map next payload using current context
            if (i > 0 || currentPayload == null) {
                FlowMapNextStepResponseDto mapRes = mapNextStep(FlowMapNextStepRequestDto.builder()
                        .flowId(flowId)
                        .targetStepIndex(i)
                        .targetMessageType(stepDef.getMessageType())
                        .context(context)
                        .build());
                currentPayload = mapRes.getPrefilledPayload();
            }

            FlowStepExecutionResponseDto stepResult = executeStep(FlowStepExecutionRequestDto.builder()
                    .flowId(flowId)
                    .stepIndex(i)
                    .messageType(stepDef.getMessageType())
                    .action(action)
                    .payload(currentPayload)
                    .currentContext(context)
                    .build());

            transcript.add(stepResult);

            if (!stepResult.isSuccess()) {
                long duration = System.currentTimeMillis() - overallStart;
                return FlowAutoRunResponseDto.builder()
                        .success(false)
                        .flowId(flowId)
                        .flowName(flow.getName())
                        .totalSteps(flow.getSteps().size())
                        .executedSteps(i + 1)
                        .stepsTranscript(transcript)
                        .finalContext(context)
                        .executionDuration(duration + "ms")
                        .errorMessage("Step " + (i + 1) + " (" + stepDef.getMessageType() + ") failed: " + stepResult.getErrorMessage())
                        .build();
            }

            context = stepResult.getUpdatedContext();
            currentPayload = stepResult.getNextStepPrefill();
        }

        long duration = System.currentTimeMillis() - overallStart;
        return FlowAutoRunResponseDto.builder()
                .success(true)
                .flowId(flowId)
                .flowName(flow.getName())
                .totalSteps(flow.getSteps().size())
                .executedSteps(flow.getSteps().size())
                .stepsTranscript(transcript)
                .finalContext(context)
                .executionDuration(duration + "ms")
                .build();
    }

    // ==========================================
    // INTERNAL HELPERS & CONTEXT EXTRACTION
    // ==========================================

    private void applyField(Map<String, Object> target, List<FlowMapNextStepResponseDto.MappedFieldInfo> mappedList,
                            String fieldKey, Object value, String sourceKey, String description) {
        if (value != null && !String.valueOf(value).trim().isEmpty()) {
            target.put(fieldKey, value);
            mappedList.add(FlowMapNextStepResponseDto.MappedFieldInfo.builder()
                    .fieldKey(fieldKey)
                    .value(value)
                    .sourceKey(sourceKey)
                    .description(description)
                    .build());
        }
    }

    private Map<String, String> extractContextVariables(String messageType, Map<String, Object> payload, String plainXml, String messageId) {
        Map<String, String> extracted = new HashMap<>();

        if (messageId != null && !messageId.trim().isEmpty()) {
            extracted.put("originalMsgId", messageId);
            extracted.put("msgId_" + messageType, messageId);
        }

        // Parse regex elements directly from the generated plain XML
        if (plainXml != null && !plainXml.isEmpty()) {
            Matcher mndtMatcher = MANDATE_ID_PATTERN.matcher(plainXml);
            if (mndtMatcher.find()) {
                extracted.put("mandateId", mndtMatcher.group(1).trim());
            }

            Matcher e2eMatcher = END_TO_END_ID_PATTERN.matcher(plainXml);
            if (e2eMatcher.find()) {
                extracted.put("endToEndId", e2eMatcher.group(1).trim());
            }

            Matcher instrMatcher = INSTR_ID_PATTERN.matcher(plainXml);
            if (instrMatcher.find()) {
                extracted.put("instructionId", instrMatcher.group(1).trim());
            }

            Matcher txMatcher = TX_ID_PATTERN.matcher(plainXml);
            if (txMatcher.find()) {
                extracted.put("transactionId", txMatcher.group(1).trim());
            }
        }

        // Copy explicit input payload values
        copyIfPresent(payload, extracted, "mandateId", "mandateId");
        copyIfPresent(payload, extracted, "endToEndId", "endToEndId");
        copyIfPresent(payload, extracted, "instructionId", "instructionId");
        copyIfPresent(payload, extracted, "amount", "amount");
        copyIfPresent(payload, extracted, "currency", "currency");
        copyIfPresent(payload, extracted, "sourceId", "sourceId");
        copyIfPresent(payload, extracted, "destinationId", "destinationId");

        // Message specific mappings
        switch (messageType != null ? messageType.trim() : "") {
            case "acmt.023":
                extracted.put("nameEnquiryMsgId", messageId);
                extracted.put("sessionId", messageId);
                copyIfPresent(payload, extracted, "partyToBeVerifiedName", "partyToBeVerifiedName");
                copyIfPresent(payload, extracted, "partyToBeVerifiedAccountNumber", "partyToBeVerifiedAccountNumber");
                copyIfPresent(payload, extracted, "sendingPartyName", "sendingPartyName");
                copyIfPresent(payload, extracted, "beneficiaryId", "beneficiaryId");
                break;

            case "pain.009":
                extracted.put("pain009MsgId", messageId);
                copyIfPresent(payload, extracted, "creditorName", "creditorName");
                copyIfPresent(payload, extracted, "creditorAccountNumber", "creditorAccountNumber");
                copyIfPresent(payload, extracted, "creditorAgentMemberId", "creditorAgentMemberId");
                copyIfPresent(payload, extracted, "debtorName", "debtorName");
                copyIfPresent(payload, extracted, "debtorAccountNumber", "debtorAccountNumber");
                copyIfPresent(payload, extracted, "debtorAgentMemberId", "debtorAgentMemberId");
                copyIfPresent(payload, extracted, "sequenceType", "sequenceType");
                copyIfPresent(payload, extracted, "frequencyType", "frequencyType");
                copyIfPresent(payload, extracted, "firstCollectionDate", "firstCollectionDate");
                copyIfPresent(payload, extracted, "finalCollectionDate", "finalCollectionDate");
                copyIfPresent(payload, extracted, "collectionAmount", "amount");
                break;

            case "pain.012":
                extracted.put("pain012MsgId", messageId);
                extracted.put("mandateAccepted", "true");
                break;

            case "pain.008":
                extracted.put("pain008MsgId", messageId);
                extracted.put("lastTriggerMsgNmId", "pain.008.001.11");
                extracted.put("directDebitMsgId", messageId);
                copyIfPresent(payload, extracted, "creditorName", "creditorName");
                copyIfPresent(payload, extracted, "creditorIban", "creditorAccountNumber");
                copyIfPresent(payload, extracted, "debtorName", "debtorName");
                copyIfPresent(payload, extracted, "debtorIban", "debtorAccountNumber");
                break;

            case "pacs.003":
                extracted.put("pacs003MsgId", messageId);
                extracted.put("lastTriggerMsgNmId", "pacs.003.001.09");
                extracted.put("directDebitMsgId", messageId);
                copyIfPresent(payload, extracted, "creditorName", "creditorName");
                copyIfPresent(payload, extracted, "creditorAccountNumber", "creditorAccountNumber");
                copyIfPresent(payload, extracted, "debtorName", "debtorName");
                copyIfPresent(payload, extracted, "debtorAccountNumber", "debtorAccountNumber");
                break;

            case "pacs.008":
                extracted.put("pacs008MsgId", messageId);
                extracted.put("lastTriggerMsgNmId", "pacs.008.001.10");
                copyIfPresent(payload, extracted, "senderName", "senderName");
                copyIfPresent(payload, extracted, "senderAccountNumber", "senderAccountNumber");
                copyIfPresent(payload, extracted, "beneficiaryName", "beneficiaryName");
                copyIfPresent(payload, extracted, "beneficiaryAccountNumber", "beneficiaryAccountNumber");
                break;

            case "pacs.004":
                extracted.put("pacs004MsgId", messageId);
                break;

            case "pain.013":
                extracted.put("pain013MsgId", messageId);
                copyIfPresent(payload, extracted, "creditorName", "creditorName");
                copyIfPresent(payload, extracted, "creditorAccountNumber", "creditorAccountNumber");
                copyIfPresent(payload, extracted, "debtorName", "debtorName");
                copyIfPresent(payload, extracted, "debtorAccountNumber", "debtorAccountNumber");
                break;

            case "pacs.002":
                extracted.put("pacs002MsgId", messageId);
                extracted.put("settlementStatus", "ACSC");
                break;
        }

        return extracted;
    }

    private void copyIfPresent(Map<String, Object> source, Map<String, String> target, String sourceKey, String targetKey) {
        if (source != null && source.containsKey(sourceKey) && source.get(sourceKey) != null) {
            String val = String.valueOf(source.get(sourceKey)).trim();
            if (!val.isEmpty()) {
                target.put(targetKey, val);
            }
        }
    }

    // ==========================================
    // FLOW DEFINITIONS
    // ==========================================

    private FlowDefinitionDto getDirectDebitFlowDefinition() {
        return FlowDefinitionDto.builder()
                .id("direct-debit")
                .name("Direct Debit Lifecycle Flow")
                .category("Direct Debit Operations")
                .badge("Mandate + Debit + Settlement")
                .description("End-to-end journey from Mandate Initiation (pain.009), Debtor Authorization (pain.012), Collection Trigger (pain.008 / pacs.003) to Final Settlement Check (pacs.002). Automatically passes MandateId, accounts, and references across all 4 steps.")
                .steps(List.of(
                        FlowStepDefinitionDto.builder()
                                .stepIndex(0)
                                .stepId("dd-step-1")
                                .messageType("pain.009")
                                .isoCode("pain.009.001.07")
                                .title("1. Create Mandate")
                                .description("Creditor initiates a recurring or one-off direct debit mandate request.")
                                .role("Creditor Institution")
                                .producedContextKeys(List.of("mandateId", "pain009MsgId", "creditorName", "creditorAccountNumber", "debtorName", "debtorAccountNumber", "amount"))
                                .defaultPayload(Map.ofEntries(
                                        Map.entry("sourceId", "999057"),
                                        Map.entry("destinationId", "999058"),
                                        Map.entry("mandateId", "MNDT-" + System.currentTimeMillis() % 1000000),
                                        Map.entry("creditorName", "Swift Telecom Ltd"),
                                        Map.entry("creditorAccountNumber", "3157417712"),
                                        Map.entry("creditorAgentMemberId", "999057"),
                                        Map.entry("debtorName", "Tunde Fabiyi"),
                                        Map.entry("debtorAccountNumber", "0000002110"),
                                        Map.entry("debtorAgentMemberId", "999058"),
                                        Map.entry("sequenceType", "RCUR"),
                                        Map.entry("frequencyType", "MNTH"),
                                        Map.entry("firstCollectionDate", "2026-09-10"),
                                        Map.entry("finalCollectionDate", "2027-09-10"),
                                        Map.entry("currency", "NGN"),
                                        Map.entry("collectionAmount", 25000.00)
                                ))
                                .build(),
                        FlowStepDefinitionDto.builder()
                                .stepIndex(1)
                                .stepId("dd-step-2")
                                .messageType("pain.012")
                                .isoCode("pain.012.001.07")
                                .title("2. Authorize Mandate")
                                .description("Debtor bank acknowledges and confirms acceptance of the mandate terms.")
                                .role("Debtor Bank")
                                .requiredContextKeys(List.of("mandateId", "pain009MsgId"))
                                .producedContextKeys(List.of("pain012MsgId", "mandateAccepted"))
                                .defaultPayload(Map.of(
                                        "accepted", "true",
                                        "sequenceType", "RCUR",
                                        "frequencyType", "MNTH"
                                ))
                                .build(),
                        FlowStepDefinitionDto.builder()
                                .stepIndex(2)
                                .stepId("dd-step-3")
                                .messageType("pain.008")
                                .isoCode("pain.008.001.11")
                                .title("3. Trigger Direct Debit")
                                .description("Creditor initiates the debit collection against the authorized mandate.")
                                .role("Creditor Bank / Switch")
                                .requiredContextKeys(List.of("mandateId"))
                                .producedContextKeys(List.of("directDebitMsgId", "endToEndId", "instructionId"))
                                .defaultPayload(Map.of(
                                        "initiatorId", "999057",
                                        "serviceLevelCode", "NURG",
                                        "localInstrumentCode", "NPSDD",
                                        "currency", "NGN"
                                ))
                                .build(),
                        FlowStepDefinitionDto.builder()
                                .stepIndex(3)
                                .stepId("dd-step-4")
                                .messageType("pacs.002")
                                .isoCode("pacs.002.001.10")
                                .title("4. Check Settlement")
                                .description("NPS clearing engine or counterparty returns payment status report confirming ACSC settlement.")
                                .role("Clearing Switch / Debtor Bank")
                                .requiredContextKeys(List.of("directDebitMsgId", "endToEndId", "instructionId"))
                                .producedContextKeys(List.of("pacs002MsgId", "settlementStatus"))
                                .defaultPayload(Map.of(
                                        "groupStatus", "ACSC"
                                ))
                                .build()
                ))
                .build();
    }

    private FlowDefinitionDto getInstantCreditTransferFlowDefinition() {
        return FlowDefinitionDto.builder()
                .id("instant-credit-transfer")
                .name("Instant Credit Transfer Flow")
                .category("Credit Transfer & Returns")
                .badge("Enquiry + Transfer + Return")
                .description("Simulate real-world instant payment execution: Verify beneficiary name (acmt.023) ➔ Execute credit transfer (pacs.008) ➔ Trigger automated return (pacs.004) with inverted routing and original tracking IDs.")
                .steps(List.of(
                        FlowStepDefinitionDto.builder()
                                .stepIndex(0)
                                .stepId("ict-step-1")
                                .messageType("acmt.023")
                                .isoCode("acmt.023.001.04")
                                .title("1. Name Enquiry")
                                .description("Originating bank checks beneficiary account number and retrieves account name.")
                                .role("Originating Bank")
                                .producedContextKeys(List.of("nameEnquiryMsgId", "sessionId", "partyToBeVerifiedName", "partyToBeVerifiedAccountNumber", "beneficiaryId", "sourceId"))
                                .defaultPayload(Map.of(
                                        "sourceId", "999999",
                                        "beneficiaryId", "999015",
                                        "partyToBeVerifiedName", "Oso Emmanuel",
                                        "partyToBeVerifiedAccountNumber", "1111111111",
                                        "sendingPartyName", "Fidelity Direct"
                                ))
                                .build(),
                        FlowStepDefinitionDto.builder()
                                .stepIndex(1)
                                .stepId("ict-step-2")
                                .messageType("pacs.008")
                                .isoCode("pacs.008.001.10")
                                .title("2. Credit Transfer")
                                .description("Execute the instant credit transfer referencing the verified name enquiry session ID.")
                                .role("Originating Bank")
                                .requiredContextKeys(List.of("nameEnquiryMsgId", "partyToBeVerifiedName", "partyToBeVerifiedAccountNumber"))
                                .producedContextKeys(List.of("pacs008MsgId", "endToEndId", "instructionId", "amount", "senderAccountNumber", "beneficiaryAccountNumber"))
                                .defaultPayload(Map.of(
                                        "amount", 50000.00,
                                        "currency", "NGN",
                                        "narration", "Instant Transfer Settlement",
                                        "settlementMethod", "CLRG",
                                        "channelCode", "1"
                                ))
                                .build(),
                        FlowStepDefinitionDto.builder()
                                .stepIndex(2)
                                .stepId("ict-step-3")
                                .messageType("pacs.004")
                                .isoCode("pacs.004.001.10")
                                .title("3. Payment Return")
                                .description("Beneficiary bank returns payment due to regulatory or account status issues with swapped routing.")
                                .role("Beneficiary Bank (Returning Agent)")
                                .requiredContextKeys(List.of("pacs008MsgId", "endToEndId", "instructionId", "amount"))
                                .producedContextKeys(List.of("pacs004MsgId", "returnReasonCode"))
                                .defaultPayload(Map.of(
                                        "returnReasonCode", "AC04",
                                        "returnReasonInfo", "Account Closed or Restricted",
                                        "currency", "NGN",
                                        "clearingChannel", "RTNS",
                                        "localInstrument", "CTAA"
                                ))
                                .build()
                ))
                .build();
    }

    private FlowDefinitionDto getRequestToPayFlowDefinition() {
        return FlowDefinitionDto.builder()
                .id("request-to-pay")
                .name("Request to Pay (RTP) Flow")
                .category("Payment Initiation & Activation")
                .badge("Enquiry + RTP + Transfer + ACSC")
                .description("Conversational payment activation journey: Verify counterparty (acmt.023) ➔ Send Payment Activation Request (pain.013) ➔ Debtor executes Credit Transfer (pacs.008) ➔ Confirm Settlement Status (pacs.002).")
                .steps(List.of(
                        FlowStepDefinitionDto.builder()
                                .stepIndex(0)
                                .stepId("rtp-step-1")
                                .messageType("acmt.023")
                                .isoCode("acmt.023.001.04")
                                .title("1. Name Enquiry")
                                .description("Look up payer account details before dispatching payment activation request.")
                                .role("Merchant / Payee Bank")
                                .producedContextKeys(List.of("nameEnquiryMsgId", "sessionId", "partyToBeVerifiedName", "partyToBeVerifiedAccountNumber"))
                                .defaultPayload(Map.of(
                                        "sourceId", "999997",
                                        "beneficiaryId", "991015",
                                        "partyToBeVerifiedName", "Tunde Fabiyi",
                                        "partyToBeVerifiedAccountNumber", "0000002110",
                                        "sendingPartyName", "Ponmile Joy"
                                ))
                                .build(),
                        FlowStepDefinitionDto.builder()
                                .stepIndex(1)
                                .stepId("rtp-step-2")
                                .messageType("pain.013")
                                .isoCode("pain.013.001.11")
                                .title("2. Payment Activation (RTP)")
                                .description("Creditor submits payment activation request asking the debtor to approve payment.")
                                .role("Creditor Institution")
                                .requiredContextKeys(List.of("nameEnquiryMsgId"))
                                .producedContextKeys(List.of("pain013MsgId", "endToEndId", "amount"))
                                .defaultPayload(Map.of(
                                        "amount", 1000.00,
                                        "currency", "NGN",
                                        "sourceName", "Ponmile Joy",
                                        "clientId", "ClientID-123456",
                                        "paymentInformationId", "GSFPMTINF035985837",
                                        "requestedExecutionDate", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                        "purpose", "Invoice Funding"
                                ))
                                .build(),
                        FlowStepDefinitionDto.builder()
                                .stepIndex(2)
                                .stepId("rtp-step-3")
                                .messageType("pacs.008")
                                .isoCode("pacs.008.001.10")
                                .title("3. Credit Transfer")
                                .description("Debtor authorizes payment, triggering instant credit transfer linked to the RTP.")
                                .role("Debtor Bank")
                                .requiredContextKeys(List.of("endToEndId", "nameEnquiryMsgId"))
                                .producedContextKeys(List.of("pacs008MsgId", "instructionId"))
                                .defaultPayload(Map.of(
                                        "narration", "Settlement for RTP Invoice",
                                        "channelCode", "1"
                                ))
                                .build(),
                        FlowStepDefinitionDto.builder()
                                .stepIndex(3)
                                .stepId("rtp-step-4")
                                .messageType("pacs.002")
                                .isoCode("pacs.002.001.10")
                                .title("4. Status Confirmation")
                                .description("Clearing network delivers settlement status report confirming final ACSC state.")
                                .role("Clearing Switch")
                                .requiredContextKeys(List.of("pacs008MsgId", "endToEndId"))
                                .producedContextKeys(List.of("pacs002MsgId", "settlementStatus"))
                                .defaultPayload(Map.of(
                                        "groupStatus", "ACSC"
                                ))
                                .build()
                ))
                .build();
    }
}
