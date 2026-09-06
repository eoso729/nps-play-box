package org.example.signer.audit;

import org.example.signer.entity.AuditEvent;
import org.example.signer.repository.AuditEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(AuditAspectTest.TestAuditedService.class)
class AuditAspectTest {

    @Autowired
    private TestAuditedService testAuditedService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        auditEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Should intercept @Auditable method on success and extract SpEL result resource ID")
    void shouldInterceptAndLogSuccess() {
        String result = testAuditedService.doSuccess(10L, 5L);
        assertEquals("res-456", result);

        List<AuditEvent> events = auditEventRepository.findAll();
        assertEquals(1, events.size());
        AuditEvent event = events.get(0);
        assertEquals(10L, event.getTenantId());
        assertEquals(5L, event.getUserId());
        assertEquals("TEST_SUCCESS", event.getAction());
        assertEquals("res-456", event.getResourceId());
        assertEquals(AuditEvent.EventStatus.SUCCESS, event.getStatus());
    }

    @Test
    @DisplayName("Should intercept @Auditable method on failure and record error details")
    void shouldInterceptAndLogFailure() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                testAuditedService.doFailure(20L, 99L));
        assertEquals("Simulated error", ex.getMessage());

        List<AuditEvent> events = auditEventRepository.findAll();
        assertEquals(1, events.size());
        AuditEvent event = events.get(0);
        assertEquals(20L, event.getTenantId());
        assertEquals("TEST_FAILURE", event.getAction());
        assertEquals("99", event.getResourceId());
        assertEquals(AuditEvent.EventStatus.FAILURE, event.getStatus());
        assertEquals("Simulated error", event.getErrorMessage());
    }

    @Component
    public static class TestAuditedService {

        @Auditable(eventType = AuditEvent.EventType.USER_MANAGEMENT, action = "TEST_SUCCESS", resourceType = "TEST_RESOURCE", resourceId = "#result")
        public String doSuccess(Long tenantId, Long userId) {
            return "res-456";
        }

        @Auditable(eventType = AuditEvent.EventType.USER_MANAGEMENT, action = "TEST_FAILURE", resourceType = "TEST_RESOURCE", resourceId = "#id", logOnFailure = true)
        public void doFailure(Long tenantId, Long id) {
            throw new IllegalStateException("Simulated error");
        }
    }
}
