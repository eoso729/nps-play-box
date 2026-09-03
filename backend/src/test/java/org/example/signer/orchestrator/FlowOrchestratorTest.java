package org.example.signer.orchestrator;

import org.example.signer.dto.orchestrator.*;
import org.example.signer.service.FlowOrchestratorService;
import org.example.signer.service.MessagePipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class FlowOrchestratorTest {

    @Autowired
    private FlowOrchestratorService flowOrchestratorService;

    @Autowired
    private MessagePipelineService messagePipelineService;

    @Test
    @DisplayName("Should retrieve all 3 predefined guided workflow journeys")
    void testGetAllFlows() {
        List<FlowDefinitionDto> flows = flowOrchestratorService.getAllFlows();
        assertNotNull(flows);
        assertEquals(3, flows.size(), "Should have Direct Debit, Instant Credit Transfer, and Request to Pay flows");

        FlowDefinitionDto directDebit = flowOrchestratorService.getFlowById("direct-debit");
        assertNotNull(directDebit);
        assertEquals("direct-debit", directDebit.getId());
        assertEquals(4, directDebit.getSteps().size(), "Direct debit flow should have 4 steps");
        assertEquals("pain.009", directDebit.getSteps().get(0).getMessageType());
        assertEquals("pain.012", directDebit.getSteps().get(1).getMessageType());
        assertEquals("pain.008", directDebit.getSteps().get(2).getMessageType());
        assertEquals("pacs.002", directDebit.getSteps().get(3).getMessageType());

        FlowDefinitionDto instantCredit = flowOrchestratorService.getFlowById("instant-credit-transfer");
        assertNotNull(instantCredit);
        assertEquals(3, instantCredit.getSteps().size(), "Instant credit transfer flow should have 3 steps");
        assertEquals("acmt.023", instantCredit.getSteps().get(0).getMessageType());
        assertEquals("pacs.008", instantCredit.getSteps().get(1).getMessageType());
        assertEquals("pacs.004", instantCredit.getSteps().get(2).getMessageType());

        FlowDefinitionDto rtp = flowOrchestratorService.getFlowById("request-to-pay");
        assertNotNull(rtp);
        assertEquals(4, rtp.getSteps().size(), "RTP flow should have 4 steps");
        assertEquals("acmt.023", rtp.getSteps().get(0).getMessageType());
        assertEquals("pain.013", rtp.getSteps().get(1).getMessageType());
        assertEquals("pacs.008", rtp.getSteps().get(2).getMessageType());
        assertEquals("pacs.002", rtp.getSteps().get(3).getMessageType());
    }

    @Test
    @DisplayName("Direct Debit Flow: End-to-end execution with automated MandateId & status context passing")
    void testDirectDebitFlowAutoRun() {
        String testMandateId = "MNDT-AUTO-" + System.currentTimeMillis() % 1000000;

        FlowAutoRunResponseDto response = flowOrchestratorService.autoRunFlow(FlowAutoRunRequestDto.builder()
                .flowId("direct-debit")
                .action("GENERATE")
                .initialStepPayload(Map.ofEntries(
                        Map.entry("sourceId", "999057"),
                        Map.entry("destinationId", "999058"),
                        Map.entry("mandateId", testMandateId),
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
                        Map.entry("collectionAmount", 35000.00)
                ))
                .build());

        assertTrue(response.isSuccess(), "Direct Debit Auto-run flow should succeed: " + response.getErrorMessage());
        assertEquals(4, response.getTotalSteps());
        assertEquals(4, response.getExecutedSteps());

        List<FlowStepExecutionResponseDto> transcript = response.getStepsTranscript();
        assertEquals(4, transcript.size());

        // Step 0: pain.009 Mandate Creation
        FlowStepExecutionResponseDto step0 = transcript.get(0);
        assertTrue(step0.isSuccess());
        assertEquals("pain.009", step0.getMessageType());
        assertNotNull(step0.getPlainXml());
        assertNotNull(step0.getMessageId());
        assertEquals(testMandateId, step0.getExtractedContext().get("mandateId"));
        assertTrue(step0.getPlainXml().contains(testMandateId));

        // Step 1: pain.012 Mandate Authorize / Acceptance
        FlowStepExecutionResponseDto step1 = transcript.get(1);
        assertTrue(step1.isSuccess());
        assertEquals("pain.012", step1.getMessageType());
        assertNotNull(step1.getPlainXml());
        assertTrue(step1.getPlainXml().contains(testMandateId), "pain.012 should contain original mandate ID");
        assertTrue(step1.getPlainXml().contains(step0.getMessageId()), "pain.012 should contain original message ID from step 0");

        // Step 2: pain.008 Customer Direct Debit Initiation
        FlowStepExecutionResponseDto step2 = transcript.get(2);
        assertTrue(step2.isSuccess());
        assertEquals("pain.008", step2.getMessageType());
        assertNotNull(step2.getPlainXml());
        assertTrue(step2.getPlainXml().contains(testMandateId), "pain.008 should contain authorized mandate ID");
        String step2EndToEndId = step2.getExtractedContext().get("endToEndId");
        assertNotNull(step2EndToEndId, "pain.008 should produce an endToEndId");

        // Step 3: pacs.002 Check Settlement Confirmation
        FlowStepExecutionResponseDto step3 = transcript.get(3);
        assertTrue(step3.isSuccess());
        assertEquals("pacs.002", step3.getMessageType());
        assertNotNull(step3.getPlainXml());
        assertTrue(step3.getPlainXml().contains(step2.getMessageId()), "pacs.002 should reference direct debit msgId");
        assertTrue(step3.getPlainXml().contains("ACSC"), "pacs.002 should contain ACSC settlement status");
    }

    @Test
    @DisplayName("Instant Credit Transfer Flow: End-to-end execution with NameEnquiryMsgId and payment return")
    void testInstantCreditTransferFlowAutoRun() {
        FlowAutoRunResponseDto response = flowOrchestratorService.autoRunFlow(FlowAutoRunRequestDto.builder()
                .flowId("instant-credit-transfer")
                .action("GENERATE")
                .initialStepPayload(Map.of(
                        "sourceId", "999999",
                        "beneficiaryId", "999015",
                        "partyToBeVerifiedName", "Oso Emmanuel",
                        "partyToBeVerifiedAccountNumber", "1111111111",
                        "sendingPartyName", "Fidelity Direct"
                ))
                .build());

        assertTrue(response.isSuccess(), "Instant Credit Transfer Auto-run flow should succeed: " + response.getErrorMessage());
        assertEquals(3, response.getTotalSteps());
        assertEquals(3, response.getExecutedSteps());

        List<FlowStepExecutionResponseDto> transcript = response.getStepsTranscript();

        // Step 0: acmt.023 Name Verification Enquiry
        FlowStepExecutionResponseDto step0 = transcript.get(0);
        assertTrue(step0.isSuccess());
        assertEquals("acmt.023", step0.getMessageType());
        String nameEnquiryMsgId = step0.getMessageId();
        assertNotNull(nameEnquiryMsgId);
        assertEquals(nameEnquiryMsgId, step0.getExtractedContext().get("nameEnquiryMsgId"));

        // Step 1: pacs.008 Credit Transfer
        FlowStepExecutionResponseDto step1 = transcript.get(1);
        assertTrue(step1.isSuccess());
        assertEquals("pacs.008", step1.getMessageType());
        assertNotNull(step1.getPlainXml());
        assertTrue(step1.getPlainXml().contains(nameEnquiryMsgId), "pacs.008 must reference nameEnquiryMsgId");
        assertTrue(step1.getPlainXml().contains("Oso Emmanuel"), "pacs.008 must carry verified party name");
        String pacs008MsgId = step1.getMessageId();
        String endToEndId = step1.getExtractedContext().get("endToEndId");
        assertNotNull(endToEndId);

        // Step 2: pacs.004 Payment Return
        FlowStepExecutionResponseDto step2 = transcript.get(2);
        assertTrue(step2.isSuccess());
        assertEquals("pacs.004", step2.getMessageType());
        assertNotNull(step2.getPlainXml());
        assertTrue(step2.getPlainXml().contains(pacs008MsgId), "pacs.004 must contain original pacs.008 message ID");
        assertTrue(step2.getPlainXml().contains(endToEndId), "pacs.004 must contain original endToEndId");
        assertTrue(step2.getPlainXml().contains("AC04"), "pacs.004 must contain return reason code");
    }

    @Test
    @DisplayName("Request to Pay (RTP) Flow: End-to-end execution across 4 steps")
    void testRequestToPayFlowAutoRun() {
        FlowAutoRunResponseDto response = flowOrchestratorService.autoRunFlow(FlowAutoRunRequestDto.builder()
                .flowId("request-to-pay")
                .action("GENERATE")
                .build());

        assertTrue(response.isSuccess(), "RTP Auto-run flow should succeed: " + response.getErrorMessage());
        assertEquals(4, response.getTotalSteps());
        assertEquals(4, response.getExecutedSteps());

        List<FlowStepExecutionResponseDto> transcript = response.getStepsTranscript();
        assertEquals("acmt.023", transcript.get(0).getMessageType());
        assertEquals("pain.013", transcript.get(1).getMessageType());
        assertEquals("pacs.008", transcript.get(2).getMessageType());
        assertEquals("pacs.002", transcript.get(3).getMessageType());

        String nameEnquiryMsgId = transcript.get(0).getMessageId();
        assertTrue(transcript.get(1).getPlainXml().contains("Invoice Funding") || transcript.get(1).getPlainXml().contains("GSFPMTINF035985837"));
        assertTrue(transcript.get(2).getPlainXml().contains(nameEnquiryMsgId), "pacs.008 in RTP flow must reference name enquiry message ID");
    }

    @Test
    @DisplayName("Should correctly map accumulated context to next step prefill")
    void testContextMapping() {
        Map<String, String> context = Map.of(
                "mandateId", "MNDT-TEST-123",
                "pain009MsgId", "99905720260904000100",
                "creditorName", "Acme Corporation",
                "debtorName", "John Doe",
                "amount", "75000.00"
        );

        FlowMapNextStepResponseDto mapped = flowOrchestratorService.mapNextStep(FlowMapNextStepRequestDto.builder()
                .flowId("direct-debit")
                .targetStepIndex(1)
                .targetMessageType("pain.012")
                .context(context)
                .build());

        assertNotNull(mapped);
        assertEquals("pain.012", mapped.getTargetMessageType());
        Map<String, Object> payload = mapped.getPrefilledPayload();
        assertEquals("MNDT-TEST-123", payload.get("originalMandateId"));
        assertEquals("99905720260904000100", payload.get("originalMsgId"));
        assertEquals("Acme Corporation", payload.get("creditorName"));
        assertEquals("John Doe", payload.get("debtorName"));
        assertEquals("true", payload.get("accepted"));
    }
}
