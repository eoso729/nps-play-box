package org.example.signer.repository;

import org.example.signer.entity.ImpersonationSession;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ImpersonationSessionRepositoryTest {

    @Autowired
    private ImpersonationSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant testTenant;
    private User supportUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();

        testTenant = tenantRepository.save(Tenant.builder()
                .name("Repo Test Bank")
                .slug("repo-test-bank-" + UUID.randomUUID().toString().substring(0, 8))
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());

        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Tenant")
                .slug("platform-" + UUID.randomUUID().toString().substring(0, 8))
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .build());

        supportUser = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .email("support-" + UUID.randomUUID().toString().substring(0, 8) + "@platform.com")
                .passwordHash("hashed")
                .firstName("Support")
                .lastName("Eng")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        targetUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .email("target-" + UUID.randomUUID().toString().substring(0, 8) + "@bank.com")
                .passwordHash("hashed")
                .firstName("Target")
                .lastName("User")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Should persist and retrieve impersonation session by UUID")
    void shouldPersistAndRetrieveSession() {
        UUID sessionUuid = UUID.randomUUID();
        ImpersonationSession session = ImpersonationSession.builder()
                .sessionUuid(sessionUuid)
                .supportUserId(supportUser.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.PENDING)
                .reason("Need to troubleshoot payment submission issues")
                .maxDurationMinutes(120)
                .build();

        ImpersonationSession saved = sessionRepository.save(session);
        assertNotNull(saved.getId());

        Optional<ImpersonationSession> found = sessionRepository.findBySessionUuid(sessionUuid);
        assertTrue(found.isPresent());
        assertEquals("Need to troubleshoot payment submission issues", found.get().getReason());
        assertEquals(ImpersonationSession.SessionStatus.PENDING, found.get().getStatus());
    }

    @Test
    @DisplayName("Should find active session for specific support user and target user pair")
    void shouldFindActiveSessionForPair() {
        ImpersonationSession activeSession = ImpersonationSession.builder()
                .supportUserId(supportUser.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.ACTIVE)
                .reason("Troubleshooting active issue")
                .startedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(2))
                .maxDurationMinutes(120)
                .build();

        sessionRepository.save(activeSession);

        Optional<ImpersonationSession> found = sessionRepository.findActiveSession(
                supportUser.getId(), targetUser.getId());
        assertTrue(found.isPresent());
        assertEquals(ImpersonationSession.SessionStatus.ACTIVE, found.get().getStatus());
    }

    @Test
    @DisplayName("Should find expired active sessions")
    void shouldFindExpiredActiveSessions() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(10);
        ImpersonationSession expiredSession = ImpersonationSession.builder()
                .supportUserId(supportUser.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.ACTIVE)
                .reason("Past session")
                .startedAt(past.minusMinutes(60))
                .expiresAt(past)
                .maxDurationMinutes(60)
                .build();

        sessionRepository.save(expiredSession);

        List<ImpersonationSession> expired = sessionRepository.findExpiredActiveSessions(LocalDateTime.now());
        assertFalse(expired.isEmpty());
        assertTrue(expired.stream().anyMatch(s -> s.getId().equals(expiredSession.getId())));
    }

    @Test
    @DisplayName("Should find sessions by target tenant ID with pagination")
    void shouldFindByTargetTenantWithPagination() {
        ImpersonationSession s1 = ImpersonationSession.builder()
                .supportUserId(supportUser.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.PENDING)
                .reason("Issue 1 with at least twenty characters")
                .maxDurationMinutes(60)
                .build();

        ImpersonationSession s2 = ImpersonationSession.builder()
                .supportUserId(supportUser.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.APPROVED)
                .reason("Issue 2 with at least twenty characters")
                .maxDurationMinutes(120)
                .build();

        sessionRepository.save(s1);
        sessionRepository.save(s2);

        Page<ImpersonationSession> page = sessionRepository.findByTargetTenantIdOrderByCreatedAtDesc(
                testTenant.getId(), PageRequest.of(0, 10));
        assertEquals(2, page.getTotalElements());
    }
}
