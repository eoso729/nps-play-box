package org.example.signer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.*;
import org.example.signer.dto.response.XmlGenerationResponseDto;
import org.example.signer.service.MessagePipelineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
public class XmlGenerationController {

    private final MessagePipelineService messagePipelineService;

    @PostMapping("/payment-activation-pain013")
    public ResponseEntity<XmlGenerationResponseDto> generatePaymentActivationPain013(@RequestBody PaymentActivationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generatePaymentActivationPain013(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.013", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payment-initiation-pain001")
    public ResponseEntity<XmlGenerationResponseDto> generatePaymentInitiationPain001(@RequestBody PaymentInitiationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generatePaymentInitiationPain001(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.001", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/mandate-creation-pain009")
    public ResponseEntity<XmlGenerationResponseDto> generateMandateCreationPain009(@RequestBody MandateCreationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateMandateCreationPain009(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.009", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/mandate-amendment-pain010")
    public ResponseEntity<XmlGenerationResponseDto> generateMandateAmendmentPain010(@RequestBody MandateAmendmentRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateMandateAmendmentPain010(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.010", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/mandate-cancellation-pain011")
    public ResponseEntity<XmlGenerationResponseDto> generateMandateCancellationPain011(@RequestBody MandateCancellationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateMandateCancellationPain011(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.011", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/direct-debit-pain008")
    public ResponseEntity<XmlGenerationResponseDto> generateDirectDebitPain008(@RequestBody DirectDebitRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateDirectDebitPain008(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pain.008", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/customer-direct-debit-pacs003")
    public ResponseEntity<XmlGenerationResponseDto> generateCustomerDirectDebitPacs003(@RequestBody CustomerDirectDebitRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateCustomerDirectDebitPacs003(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pacs.003", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/transfer-pacs008")
    public ResponseEntity<XmlGenerationResponseDto> generateTransferPacs008(@RequestBody TransferRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateTransferPacs008(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pacs.008", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payment-return-pacs004")
    public ResponseEntity<XmlGenerationResponseDto> generatePaymentReturnPacs004(@RequestBody PaymentReturnRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generatePaymentReturnPacs004(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating pacs.004", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/name-verification-acmt023")
    public ResponseEntity<XmlGenerationResponseDto> generateNameVerificationAcmt023(@RequestBody NameVerificationRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateNameVerificationAcmt023(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating acmt.023", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/name-verification-report-acmt024")
    public ResponseEntity<XmlGenerationResponseDto> generateNameVerificationReportAcmt024(@RequestBody NameVerificationReportDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateNameVerificationReportAcmt024(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating acmt.024", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/balance-enquiry-camt060")
    public ResponseEntity<XmlGenerationResponseDto> generateBalanceEnquiryCamt060(@RequestBody AccountReportingRequestDto requestDto) {
        try {
            XmlGenerationResponseDto response = messagePipelineService.generateBalanceEnquiryCamt060(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating camt.060", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
