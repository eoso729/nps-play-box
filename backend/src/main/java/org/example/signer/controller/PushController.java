package org.example.signer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.*;
import org.example.signer.dto.response.MessageSendResponseDto;
import org.example.signer.service.MessagePipelineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PushController {

    private final MessagePipelineService messagePipelineService;

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

    @PostMapping("/payment-status-request-pacs028")
    public ResponseEntity<MessageSendResponseDto> sendPaymentStatusRequestPacs028(@RequestBody PaymentStatusRequestDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendPaymentStatusRequestPacs028(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pacs.028", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/mandate-acceptance-pain012")
    public ResponseEntity<MessageSendResponseDto> sendMandateAcceptancePain012(@RequestBody MandateAcceptanceReportDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendMandateAcceptancePain012(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pain.012", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/activation-status-report-pain014")
    public ResponseEntity<MessageSendResponseDto> sendActivationStatusReportPain014(@RequestBody PaymentActivationStatusReportDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendActivationStatusReportPain014(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pain.014", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payment-status-report-pacs002")
    public ResponseEntity<MessageSendResponseDto> sendTransferResponsePacs002(@RequestBody TransferResponseDto requestDto) {
        try {
            MessageSendResponseDto response = messagePipelineService.sendTransferResponsePacs002(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending pacs.002", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
