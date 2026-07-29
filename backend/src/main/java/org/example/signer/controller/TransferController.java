package org.example.signer.controller;

import org.example.signer.dto.AccountReportingRequestDto;
import org.example.signer.dto.CustomerDirectDebitRequestDto;
import org.example.signer.dto.DirectDebitRequestDto;
import org.example.signer.dto.MandateAmendmentRequestDto;
import org.example.signer.dto.MandateCancellationRequestDto;
import org.example.signer.dto.MandateCreationRequestDto;
import org.example.signer.dto.NameVerificationReportDto;
import org.example.signer.dto.PaymentActivationRequestDto;
import org.example.signer.dto.PaymentInitiationRequestDto;
import org.example.signer.dto.PaymentReturnRequestDto;
import org.example.signer.dto.NameVerificationRequestDto;
import org.example.signer.dto.TransferRequestDto;
import org.example.signer.dto.TransferResponseDto;
import org.example.signer.service.BalanceEnquiryService;
import org.example.signer.service.CustomerDirectDebitService;
import org.example.signer.service.DirectDebitService;
import org.example.signer.service.MandateAmendmentService;
import org.example.signer.service.MandateCancellationService;
import org.example.signer.service.MandateCreationService;
import org.example.signer.service.NameVerificationReportService;
import org.example.signer.service.PaymentActivationService;
import org.example.signer.service.PaymentInitiationService;
import org.example.signer.service.PaymentReturnService;
import org.example.signer.service.NameVerificationService;
import org.example.signer.service.TransferResponseService;
import org.example.signer.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransferController {

    private final TransferService transferService;
    private final NameVerificationService nameVerificationService;
    private final NameVerificationReportService nameVerificationReportService;
    private final TransferResponseService transferResponseService;
    private final MandateCreationService mandateCreationService;
    private final MandateAmendmentService mandateAmendmentService;
    private final DirectDebitService directDebitService;
    private final PaymentInitiationService paymentInitiationService;
    private final BalanceEnquiryService balanceEnquiryService;
    private final PaymentActivationService paymentActivationService;
    private final PaymentReturnService paymentReturnService;
    private final MandateCancellationService mandateCancellationService;
    private final CustomerDirectDebitService customerDirectDebitService;

    public TransferController(TransferService transferService, NameVerificationService nameVerificationService, NameVerificationReportService nameVerificationReportService, TransferResponseService transferResponseService, MandateCreationService mandateCreationService, MandateAmendmentService mandateAmendmentService, DirectDebitService directDebitService, PaymentInitiationService paymentInitiationService, BalanceEnquiryService balanceEnquiryService, PaymentActivationService paymentActivationService, PaymentReturnService paymentReturnService, MandateCancellationService mandateCancellationService, CustomerDirectDebitService customerDirectDebitService) {
        this.transferService = transferService;
        this.nameVerificationService = nameVerificationService;
        this.nameVerificationReportService = nameVerificationReportService;
        this.transferResponseService = transferResponseService;
        this.mandateCreationService = mandateCreationService;
        this.mandateAmendmentService = mandateAmendmentService;
        this.directDebitService = directDebitService;
        this.paymentInitiationService = paymentInitiationService;
        this.balanceEnquiryService = balanceEnquiryService;
        this.paymentActivationService = paymentActivationService;
        this.paymentReturnService = paymentReturnService;
        this.mandateCancellationService = mandateCancellationService;
        this.customerDirectDebitService = customerDirectDebitService;
    }

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
    public ResponseEntity<Map<String, String>> nameVerificationAcmt023(@RequestBody NameVerificationRequestDto requestDto) {
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
    public ResponseEntity<Map<String, String>> transfer(@RequestBody TransferRequestDto requestDto) {
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

    @PostMapping("/mandate-creation-pain009")
    public ResponseEntity<Map<String, String>> mandateCreation(@RequestBody MandateCreationRequestDto requestDto) {
        try {
            Map<String, String> response = mandateCreationService.executeMandateCreation(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/mandate-amendment-pain010")
    public ResponseEntity<Map<String, String>> mandateAmendment(@RequestBody MandateAmendmentRequestDto requestDto) {
        try {
            Map<String, String> response = mandateAmendmentService.executeMandateAmendment(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/mandate-cancellation-pain011")
    public ResponseEntity<Map<String, String>> mandateCancellation(@RequestBody MandateCancellationRequestDto requestDto) {
        try {
            Map<String, String> response = mandateCancellationService.executeMandateCancellation(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/customer-direct-debit-pacs003")
    public ResponseEntity<Map<String, String>> customerDirectDebit(@RequestBody CustomerDirectDebitRequestDto requestDto) {
        try {
            Map<String, String> response = customerDirectDebitService.executeCustomerDirectDebit(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/direct-debit-pain008")
    public ResponseEntity<Map<String, String>> directDebit(@RequestBody DirectDebitRequestDto requestDto) {
        try {
            Map<String, String> response = directDebitService.executeDirectDebit(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/payment-initiation-pain001")
    public ResponseEntity<Map<String, String>> paymentInitiation(@RequestBody PaymentInitiationRequestDto requestDto) {
        try {
            Map<String, String> response = paymentInitiationService.executePaymentInitiation(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/balance-enquiry-camt060")
    public ResponseEntity<Map<String, String>> balanceEnquiry(@RequestBody AccountReportingRequestDto requestDto) {
        try {
            Map<String, String> response = balanceEnquiryService.executeBalanceEnquiry(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/payment-activation-pain013")
    public ResponseEntity<Map<String, String>> paymentActivation(@RequestBody PaymentActivationRequestDto requestDto) {
        try {
            Map<String, String> response = paymentActivationService.executePaymentActivation(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/payment-return-pacs004")
    public ResponseEntity<Map<String, String>> paymentReturn(@RequestBody PaymentReturnRequestDto requestDto) {
        try {
            Map<String, String> response = paymentReturnService.executePaymentReturn(requestDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
