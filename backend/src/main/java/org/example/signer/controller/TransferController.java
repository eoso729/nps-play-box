package org.example.signer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.*;
import org.example.signer.dto.response.MessageSendResponseDto;
import org.example.signer.dto.response.XmlGenerationResponseDto;
import org.example.signer.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransferController {

    private final MessagePipelineService messagePipelineService;

    // Existing services kept for legacy endpoints
    private final TransferService transferService;
    private final NameVerificationService nameVerificationService;
    private final NameVerificationReportService nameVerificationReportService;
    private final TransferResponseService transferResponseService;

    // =========================================================================
    // GROUP A: GENERATE ONLY (NO NETWORK SEND)
    // =========================================================================

    @PostMapping("/generate/payment-activation-pain013")
    public ResponseEntity<XmlGenerationResponseDto> generatePaymentActivationPain013(@RequestBody PaymentActivationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generatePaymentActivationPain013(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.013", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/payment-initiation-pain001")
    public ResponseEntity<XmlGenerationResponseDto> generatePaymentInitiationPain001(@RequestBody PaymentInitiationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generatePaymentInitiationPain001(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.001", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/mandate-creation-pain009")
    public ResponseEntity<XmlGenerationResponseDto> generateMandateCreationPain009(@RequestBody MandateCreationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateMandateCreationPain009(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.009", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/mandate-amendment-pain010")
    public ResponseEntity<XmlGenerationResponseDto> generateMandateAmendmentPain010(@RequestBody MandateAmendmentRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateMandateAmendmentPain010(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.010", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/mandate-cancellation-pain011")
    public ResponseEntity<XmlGenerationResponseDto> generateMandateCancellationPain011(@RequestBody MandateCancellationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateMandateCancellationPain011(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.011", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/direct-debit-pain008")
    public ResponseEntity<XmlGenerationResponseDto> generateDirectDebitPain008(@RequestBody DirectDebitRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateDirectDebitPain008(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.008", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/customer-direct-debit-pacs003")
    public ResponseEntity<XmlGenerationResponseDto> generateCustomerDirectDebitPacs003(@RequestBody CustomerDirectDebitRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateCustomerDirectDebitPacs003(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pacs.003", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/transfer-pacs008")
    public ResponseEntity<XmlGenerationResponseDto> generateTransferPacs008(@RequestBody TransferRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateTransferPacs008(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pacs.008", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/payment-return-pacs004")
    public ResponseEntity<XmlGenerationResponseDto> generatePaymentReturnPacs004(@RequestBody PaymentReturnRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generatePaymentReturnPacs004(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pacs.004", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/name-verification-acmt023")
    public ResponseEntity<XmlGenerationResponseDto> generateNameVerificationAcmt023(@RequestBody NameVerificationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateNameVerificationAcmt023(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating acmt.023", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/balance-enquiry-camt060")
    public ResponseEntity<XmlGenerationResponseDto> generateBalanceEnquiryCamt060(@RequestBody AccountReportingRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateBalanceEnquiryCamt060(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating camt.060", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================================
    // GROUP B: SEND FULL PIPELINE
    // =========================================================================

    @PostMapping("/payment-activation-pain013")
    public ResponseEntity<MessageSendResponseDto> sendPaymentActivationPain013(@RequestBody PaymentActivationRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendPaymentActivationPain013(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pain.013", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payment-initiation-pain001")
    public ResponseEntity<MessageSendResponseDto> sendPaymentInitiationPain001(@RequestBody PaymentInitiationRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendPaymentInitiationPain001(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pain.001", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/mandate-creation-pain009")
    public ResponseEntity<MessageSendResponseDto> sendMandateCreationPain009(@RequestBody MandateCreationRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendMandateCreationPain009(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pain.009", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/mandate-amendment-pain010")
    public ResponseEntity<MessageSendResponseDto> sendMandateAmendmentPain010(@RequestBody MandateAmendmentRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendMandateAmendmentPain010(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pain.010", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/mandate-cancellation-pain011")
    public ResponseEntity<MessageSendResponseDto> sendMandateCancellationPain011(@RequestBody MandateCancellationRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendMandateCancellationPain011(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pain.011", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/direct-debit-pain008")
    public ResponseEntity<MessageSendResponseDto> sendDirectDebitPain008(@RequestBody DirectDebitRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendDirectDebitPain008(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pain.008", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/customer-direct-debit-pacs003")
    public ResponseEntity<MessageSendResponseDto> sendCustomerDirectDebitPacs003(@RequestBody CustomerDirectDebitRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendCustomerDirectDebitPacs003(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pacs.003", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/transfer-pacs008")
    public ResponseEntity<MessageSendResponseDto> sendTransferPacs008(@RequestBody TransferRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendTransferPacs008(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pacs.008", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payment-return-pacs004")
    public ResponseEntity<MessageSendResponseDto> sendPaymentReturnPacs004(@RequestBody PaymentReturnRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendPaymentReturnPacs004(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pacs.004", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/name-verification-acmt023")
    public ResponseEntity<MessageSendResponseDto> sendNameVerificationAcmt023(@RequestBody NameVerificationRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendNameVerificationAcmt023(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending acmt.023", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/balance-enquiry-camt060")
    public ResponseEntity<MessageSendResponseDto> sendBalanceEnquiryCamt060(@RequestBody AccountReportingRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendBalanceEnquiryCamt060(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending camt.060", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================================
    // LEGACY ENDPOINTS (Preserved for compatibility)
    // =========================================================================

    @PostMapping("/name-verification")
    public ResponseEntity<Map<String, String>> nameVerification(@RequestBody NameVerificationRequestDto requestDto) {
        try {
            Map<String, String> response = nameVerificationService.executeNameVerification(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/gateway-name-verification-acmt023")
    public ResponseEntity<Map<String, String>> nameVerificationAcmt023Legacy(@RequestBody NameVerificationRequestDto requestDto) {
        try {
            Map<String, String> response = nameVerificationService.executeNameVerificationAcmt023(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/name-verification-report")
    public ResponseEntity<String> nameVerificationReport(@RequestBody NameVerificationReportDto requestDto) {
        try {
            String response = nameVerificationReportService.executeNameVerificationReport(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transferLegacy(@RequestBody TransferRequestDto requestDto) {
        try {
            Map<String, String> response = transferService.executeTransfer(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/transfer-response")
    public ResponseEntity<String> transferResponse(@RequestBody TransferResponseDto requestDto) {
        try {
            String response = transferResponseService.executeTransferResponse(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
