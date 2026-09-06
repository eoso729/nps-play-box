package org.example.signer.repository;

import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.example.signer.entity.AuditEvent;
import org.example.signer.specification.AuditEventSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuditEventRepositoryTest {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void setUp() {
        auditEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save and retrieve audit events by tenant ID")
    void shouldSaveAndFindEventsByTenantId() {
        AuditEvent event1 = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(100L)
                .userId(1L)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("CREATE_USER")
                .resourceType("USER")
                .resourceId("5")
                .status(AuditEvent.EventStatus.SUCCESS)
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .requestId(UUID.randomUUID().toString())
                .build();

        AuditEvent event2 = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(200L)
                .userId(2L)
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build();

        auditEventRepository.saveAll(List.of(event1, event2));

        Page<AuditEvent> tenant100Events = auditEventRepository.findByTenantIdOrderByCreatedAtDesc(
                100L, PageRequest.of(0, 10));

        assertEquals(1, tenant100Events.getTotalElements());
        AuditEvent found = tenant100Events.getContent().get(0);
        assertEquals("CREATE_USER", found.getAction());
        assertEquals("USER", found.getResourceType());
        assertEquals("5", found.getResourceId());
        assertEquals(AuditEvent.EventType.USER_MANAGEMENT, found.getEventType());
    }

    @Test
    @DisplayName("Should save and retrieve JSON metadata correctly")
    void shouldSaveAndRetrieveJsonMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("previousRole", "DEVELOPER");
        metadata.put("newRole", "TENANT_ADMIN");
        metadata.put("reason", "Promotion");

        AuditEvent event = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(100L)
                .userId(1L)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("CHANGE_ROLE")
                .resourceType("USER")
                .resourceId("5")
                .status(AuditEvent.EventStatus.SUCCESS)
                .metadata(metadata)
                .build();

        AuditEvent saved = auditEventRepository.save(event);
        assertNotNull(saved.getId());

        AuditEvent fetched = auditEventRepository.findById(saved.getId()).orElseThrow();
        assertNotNull(fetched.getMetadata());
        assertEquals("DEVELOPER", fetched.getMetadata().get("previousRole"));
        assertEquals("TENANT_ADMIN", fetched.getMetadata().get("newRole"));
        assertEquals("Promotion", fetched.getMetadata().get("reason"));
    }

    @Test
    @DisplayName("Should query resource history in chronological order")
    void shouldFindResourceHistory() {
        LocalDateTime now = LocalDateTime.now();

        AuditEvent create = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(100L)
                .userId(1L)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("CREATE_USER")
                .resourceType("USER")
                .resourceId("42")
                .status(AuditEvent.EventStatus.SUCCESS)
                .createdAt(now.minusHours(2))
                .build();

        AuditEvent update = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(100L)
                .userId(1L)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("UPDATE_USER")
                .resourceType("USER")
                .resourceId("42")
                .status(AuditEvent.EventStatus.SUCCESS)
                .createdAt(now.minusHours(1))
                .build();

        AuditEvent otherResource = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(100L)
                .userId(1L)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("CREATE_USER")
                .resourceType("USER")
                .resourceId("99")
                .status(AuditEvent.EventStatus.SUCCESS)
                .createdAt(now)
                .build();

        auditEventRepository.saveAll(List.of(create, update, otherResource));

        List<AuditEvent> history = auditEventRepository.findResourceHistory(100L, "USER", "42");
        assertEquals(2, history.size());
        assertEquals("UPDATE_USER", history.get(0).getAction());
        assertEquals("CREATE_USER", history.get(1).getAction());
    }

    @Test
    @DisplayName("Should filter audit events using dynamic specifications")
    void shouldFilterUsingSpecification() {
        AuditEvent authEvent = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(100L)
                .userId(1L)
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build();

        AuditEvent failedAuth = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(100L)
                .userId(2L)
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN_FAILED")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.FAILURE)
                .errorMessage("Invalid password")
                .build();

        AuditEvent configChange = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(100L)
                .userId(1L)
                .eventType(AuditEvent.EventType.CONFIG_CHANGE)
                .action("UPDATE_SEATS")
                .resourceType("SEAT_QUOTA")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build();

        auditEventRepository.saveAll(List.of(authEvent, failedAuth, configChange));

        // Filter by event type
        AuditEventSearchRequest request1 = AuditEventSearchRequest.builder()
                .eventType(AuditEvent.EventType.AUTH)
                .build();
        Specification<AuditEvent> spec1 = AuditEventSpecification.buildSpecification(100L, request1);
        assertEquals(2, auditEventRepository.findAll(spec1).size());

        // Filter by status FAILURE
        AuditEventSearchRequest request2 = AuditEventSearchRequest.builder()
                .status(AuditEvent.EventStatus.FAILURE)
                .build();
        Specification<AuditEvent> spec2 = AuditEventSpecification.buildSpecification(100L, request2);
        List<AuditEvent> failed = auditEventRepository.findAll(spec2);
        assertEquals(1, failed.size());
        assertEquals("LOGIN_FAILED", failed.get(0).getAction());
    }
}
