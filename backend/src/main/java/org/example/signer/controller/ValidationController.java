package org.example.signer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.validation.*;
import org.example.signer.validation.IsoMessageDefinition;
import org.example.signer.validation.IsoMessageRegistry;
import org.example.signer.validation.NibssValidationRules;
import org.example.signer.validation.XmlAutoFixEngine;
import org.example.signer.validation.XmlValidationEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final XmlValidationEngine validationEngine;
    private final XmlAutoFixEngine autoFixEngine;

    @PostMapping("/inspect")
    public ResponseEntity<ValidationReportDto> inspectXml(@RequestBody XmlInspectRequestDto request) {
        try {
            ValidationReportDto report = validationEngine.validate(request.getXmlContent(), request.getMessageType());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error during XML health check inspection", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/auto-fix")
    public ResponseEntity<XmlAutoFixResponseDto> autoFixXml(@RequestBody XmlAutoFixRequestDto request) {
        try {
            XmlAutoFixResponseDto response = autoFixEngine.autoFix(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during XML auto-fix", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/samples")
    public ResponseEntity<List<MessageSampleDto>> getAllSamples() {
        List<MessageSampleDto> samples = IsoMessageRegistry.getAllDefinitions().stream()
                .map(def -> MessageSampleDto.builder()
                        .key(def.getKey())
                        .name(def.getName())
                        .isoCode(def.getIsoCode())
                        .category(def.getCategory())
                        .rootElement(def.getRootElement())
                        .sampleXml(def.getSampleXml())
                        .fields(def.getFields())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(samples);
    }

    @GetMapping("/samples/{messageType}")
    public ResponseEntity<MessageSampleDto> getSampleByMessageType(@PathVariable String messageType) {
        IsoMessageDefinition def = IsoMessageRegistry.getDefinition(messageType);
        if (def == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(MessageSampleDto.builder()
                .key(def.getKey())
                .name(def.getName())
                .isoCode(def.getIsoCode())
                .category(def.getCategory())
                .rootElement(def.getRootElement())
                .sampleXml(def.getSampleXml())
                .fields(def.getFields())
                .build());
    }

    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> getValidationRules() {
        Map<String, Object> rules = new LinkedHashMap<>();
        rules.put("accountDesignations", NibssValidationRules.ACCOUNT_DESIGNATIONS);
        rules.put("accountTiers", NibssValidationRules.ACCOUNT_TIERS);
        rules.put("idTypes", NibssValidationRules.ID_TYPES);
        rules.put("channelCodes", NibssValidationRules.CHANNEL_CODES);
        rules.put("sequenceTypes", NibssValidationRules.SEQUENCE_TYPES);
        rules.put("frequencyTypes", NibssValidationRules.FREQUENCY_TYPES);
        rules.put("settlementMethods", NibssValidationRules.SETTLEMENT_METHODS);
        rules.put("clearingChannels", NibssValidationRules.CLEARING_CHANNELS);
        rules.put("localInstruments", NibssValidationRules.LOCAL_INSTRUMENTS);
        rules.put("chargeBearers", NibssValidationRules.CHARGE_BEARERS);
        rules.put("cancellationIds", NibssValidationRules.CANCELLATION_IDS);
        rules.put("statusCodes", NibssValidationRules.STATUS_CODES);
        rules.put("creditDebitIndicators", NibssValidationRules.CREDIT_DEBIT_INDICATORS);
        rules.put("requestedMessageNameIds", NibssValidationRules.REQD_MSG_NM_IDS);
        rules.put("entryStatuses", NibssValidationRules.ENTRY_STATUSES);
        rules.put("domainCodes", NibssValidationRules.DOMAIN_CODES);
        rules.put("familyCodes", NibssValidationRules.FAMILY_CODES);
        rules.put("subFamilyCodes", NibssValidationRules.SUB_FAMILY_CODES);
        rules.put("reportingPeriodTypes", NibssValidationRules.REPORTING_PERIOD_TYPES);
        rules.put("rejectReasonCodes", NibssValidationRules.REJECT_REASON_CODES);
        return ResponseEntity.ok(rules);
    }
}
