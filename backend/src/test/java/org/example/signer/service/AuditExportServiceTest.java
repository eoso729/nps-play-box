package org.example.signer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.example.signer.entity.AuditEvent;
import org.example.signer.repository.AuditEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuditExportServiceTest {

    @Autowired
    private AuditExportService auditExportService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        auditEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Should export audit events as valid JSON")
    void shouldExportAsJson() throws IOException {
        AuditEvent event1 = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(10L)
                .userId(1L)
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.SUCCESS)
                .ipAddress("127.0.0.1")
                .build();

        AuditEvent event2 = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(10L)
                .userId(2L)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("INVITE_USER")
                .resourceType("USER")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build();

        auditEventRepository.saveAll(List.of(event1, event2));

        byte[] jsonBytes = auditExportService.exportAsJson(10L, new AuditEventSearchRequest(), 100);
        assertNotNull(jsonBytes);
        assertTrue(jsonBytes.length > 0);

        JsonNode jsonNode = objectMapper.readTree(jsonBytes);
        assertTrue(jsonNode.isArray());
        assertEquals(2, jsonNode.size());
    }

    @Test
    @DisplayName("Should export audit events as RFC-4180 compliant CSV")
    void shouldExportAsCsv() {
        AuditEvent event = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(10L)
                .userId(1L)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("UPDATE_USER")
                .resourceType("USER")
                .resourceId("42")
                .status(AuditEvent.EventStatus.SUCCESS)
                .ipAddress("192.168.1.50")
                .userAgent("Mozilla/5.0, Custom")
                .errorMessage("No error, \"all good\"")
                .build();

        auditEventRepository.save(event);

        byte[] csvBytes = auditExportService.exportAsCsv(10L, new AuditEventSearchRequest(), 100);
        assertNotNull(csvBytes);

        String csvString = new String(csvBytes, StandardCharsets.UTF_8);
        String[] lines = csvString.split("\n");
        assertTrue(lines.length >= 2);

        // Header check
        assertTrue(lines[0].contains("Event UUID"));
        assertTrue(lines[0].contains("Action"));
        assertTrue(lines[0].contains("Resource Type"));

        // Data row check
        assertTrue(lines[1].contains("UPDATE_USER"));
        assertTrue(lines[1].contains("USER"));
        assertTrue(lines[1].contains("42"));
        // Check escaping of commas and quotes
        assertTrue(lines[1].contains("\"Mozilla/5.0, Custom\""));
        assertTrue(lines[1].contains("\"No error, \"\"all good\"\"\""));
    }
}
