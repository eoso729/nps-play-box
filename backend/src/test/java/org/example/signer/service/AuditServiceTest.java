package org.example.signer.service;

import org.example.signer.entity.AuditEvent;
import org.example.signer.repository.AuditEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuditServiceTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void setUp() {
        auditEventRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        auditEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Should enrich audit event with HTTP context headers")
    void shouldEnrichAuditEventWithHttpContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("User-Agent", "Mozilla/5.0 Chrome/120");
        request.addHeader("X-Request-ID", "req-xyz-123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        auditService.logEvent(auditService.builder()
                .tenantId(10L)
                .userId(5L)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("INVITE_USER")
                .resourceType("USER")
                .resourceId("50")
                .status(AuditEvent.EventStatus.SUCCESS));

        List<AuditEvent> events = auditEventRepository.findAll();
        assertEquals(1, events.size());
        AuditEvent event = events.get(0);
        assertEquals("10.0.0.1", event.getIpAddress());
        assertEquals("Mozilla/5.0 Chrome/120", event.getUserAgent());
        assertEquals("req-xyz-123", event.getRequestId());
        assertEquals(10L, event.getTenantId());
        assertEquals(5L, event.getUserId());
        assertEquals("INVITE_USER", event.getAction());
    }

    @Test
    @DisplayName("Should log authentication events correctly")
    void shouldLogAuthEvent() {
        auditService.logAuth(10L, 1L, "LOGIN", AuditEvent.EventStatus.SUCCESS, null);
        auditService.logAuth(10L, null, "LOGIN", AuditEvent.EventStatus.FAILURE, "Invalid password");

        List<AuditEvent> events = auditEventRepository.findAll();
        assertEquals(2, events.size());

        AuditEvent success = events.stream().filter(e -> e.getStatus() == AuditEvent.EventStatus.SUCCESS).findFirst().orElseThrow();
        assertEquals(AuditEvent.EventType.AUTH, success.getEventType());
        assertEquals("LOGIN", success.getAction());
        assertEquals(1L, success.getUserId());

        AuditEvent failure = events.stream().filter(e -> e.getStatus() == AuditEvent.EventStatus.FAILURE).findFirst().orElseThrow();
        assertEquals("Invalid password", failure.getErrorMessage());
        assertNull(failure.getUserId());
    }

    @Test
    @DisplayName("Should log user management, tenant management, and quota events")
    void shouldLogDomainEvents() {
        auditService.logUserManagement(10L, 1L, "DEACTIVATE_USER", "25",
                AuditEvent.EventStatus.SUCCESS, Map.of("reason", "Left company"));

        auditService.logTenantManagement(10L, 1L, "UPDATE_TENANT", "10",
                AuditEvent.EventStatus.SUCCESS, Map.of("changedField", "name"));

        auditService.logQuotaEvent(10L, 1L, "REQUEST_SEATS", "1",
                AuditEvent.EventStatus.SUCCESS, Map.of("additionalSeats", 5));

        auditService.logImpersonation(10L, 99L, 1L, "START_IMPERSONATION",
                AuditEvent.EventStatus.SUCCESS);

        List<AuditEvent> events = auditEventRepository.findAll();
        assertEquals(4, events.size());

        assertTrue(events.stream().anyMatch(e -> e.getEventType() == AuditEvent.EventType.USER_MANAGEMENT));
        assertTrue(events.stream().anyMatch(e -> e.getEventType() == AuditEvent.EventType.TENANT_MANAGEMENT));
        assertTrue(events.stream().anyMatch(e -> e.getEventType() == AuditEvent.EventType.CONFIG_CHANGE));
        assertTrue(events.stream().anyMatch(e -> e.getEventType() == AuditEvent.EventType.IMPERSONATION));
    }
}
