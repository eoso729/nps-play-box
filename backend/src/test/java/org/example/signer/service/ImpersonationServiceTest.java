package org.example.signer.service;

import org.example.signer.dto.impersonation.*;
import org.example.signer.entity.ImpersonationSession;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.ImpersonationSessionRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.scheduler.ImpersonationScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ImpersonationServiceTest {

    @Autowired
    private ImpersonationService impersonationService;

    @Autowired
    private ImpersonationScheduler impersonationScheduler;

    @Autowired
    private ImpersonationSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;
    private User supportUser1;
    private User supportUser2;
    private User tenantAdmin;
    private User targetUser;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();

        tenant = tenantRepository.save(Tenant.builder()
                .name("Service Test Bank")
                .slug("service-test-bank-" + UUID.randomUUID().toString().substring(0, 8))
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());

        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Service Tenant")
                .slug("platform-service-" + UUID.randomUUID().toString().substring(0, 8))
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .build());

        supportUser1 = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .email("support1-" + UUID.randomUUID().toString().substring(0, 8) + "@platform.com")
                .passwordHash("hashed")
                .firstName("Support1")
                .lastName("Eng")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        supportUser2 = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .email("support2-" + UUID.randomUUID().toString().substring(0, 8) + "@platform.com")
                .passwordHash("hashed")
                .firstName("Support2")
                .lastName("Lead")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        tenantAdmin = userRepository.save(User.builder()
                .tenantId(tenant.getId())
                .email("admin-" + UUID.randomUUID().toString().substring(0, 8) + "@bank.com")
                .passwordHash("hashed")
                .firstName("Tenant")
                .lastName("Admin")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        targetUser = userRepository.save(User.builder()
                .tenantId(tenant.getId())
                .email("target-" + UUID.randomUUID().toString().substring(0, 8) + "@bank.com")
                .passwordHash("hashed")
                .firstName("Target")
                .lastName("Developer")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Platform admin can request impersonation with justification")
    void shouldRequestImpersonation() {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Troubleshooting urgent XML payload validation failure")
                .durationMinutes(120)
                .build();

        ImpersonationSessionResponse response = impersonationService.requestImpersonation(
                supportUser1.getId(), request);

        assertNotNull(response.getSessionUuid());
        assertEquals("PENDING", response.getStatus());
        assertEquals(120, response.getMaxDurationMinutes());
        assertEquals(supportUser1.getEmail(), response.getSupportUserEmail());
        assertEquals(targetUser.getEmail(), response.getTargetUserEmail());
    }

    @Test
    @DisplayName("Non-platform admin cannot request impersonation")
    void shouldRejectNonPlatformAdminRequester() {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Unauthorized attempt to access another user's session")
                .durationMinutes(60)
                .build();

        assertThrows(AccessDeniedException.class, () ->
                impersonationService.requestImpersonation(tenantAdmin.getId(), request));
    }

    @Test
    @DisplayName("Dual authorization: Platform admin cannot approve their own impersonation request")
    void shouldPreventSelfApprovalDueToDualAuthorization() {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Troubleshooting urgent production payment flow")
                .durationMinutes(60)
                .build();

        ImpersonationSessionResponse session = impersonationService.requestImpersonation(
                supportUser1.getId(), request);

        ApproveImpersonationDto approveDto = ApproveImpersonationDto.builder()
                .approvalReason("Self approval attempt")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                impersonationService.approveImpersonation(
                        UUID.fromString(session.getSessionUuid()), supportUser1.getId(), approveDto));

        assertTrue(ex.getMessage().contains("Dual authorization required"));
    }

    @Test
    @DisplayName("Another platform admin can approve impersonation request")
    void shouldApproveImpersonation() {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Assisting with customer integration failure")
                .durationMinutes(90)
                .build();

        ImpersonationSessionResponse session = impersonationService.requestImpersonation(
                supportUser1.getId(), request);

        ApproveImpersonationDto approveDto = ApproveImpersonationDto.builder()
                .approvalReason("Verified incident ticket #12345")
                .build();

        ImpersonationSessionResponse approved = impersonationService.approveImpersonation(
                UUID.fromString(session.getSessionUuid()), supportUser2.getId(), approveDto);

        assertEquals("APPROVED", approved.getStatus());
        assertEquals(supportUser2.getEmail(), approved.getApprovedByEmail());
        assertEquals("Verified incident ticket #12345", approved.getApprovalReason());
    }

    @Test
    @DisplayName("Platform admin can reject an impersonation request")
    void shouldRejectImpersonation() {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Assisting with minor UI layout glitch")
                .durationMinutes(30)
                .build();

        ImpersonationSessionResponse session = impersonationService.requestImpersonation(
                supportUser1.getId(), request);

        RejectImpersonationDto rejectDto = RejectImpersonationDto.builder()
                .rejectionReason("Insufficient business justification for account access")
                .build();

        ImpersonationSessionResponse rejected = impersonationService.rejectImpersonation(
                UUID.fromString(session.getSessionUuid()), supportUser2.getId(), rejectDto);

        assertEquals("REJECTED", rejected.getStatus());
        assertEquals("Insufficient business justification for account access", rejected.getTerminationReason());
    }

    @Test
    @DisplayName("Support user can start approved impersonation and obtain temporary JWT")
    void shouldStartImpersonationAndGenerateToken() {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Live troubleshooting on banking portal")
                .durationMinutes(60)
                .build();

        ImpersonationSessionResponse session = impersonationService.requestImpersonation(
                supportUser1.getId(), request);

        impersonationService.approveImpersonation(
                UUID.fromString(session.getSessionUuid()),
                supportUser2.getId(),
                ApproveImpersonationDto.builder().approvalReason("Approved by lead").build());

        ImpersonationTokenResponse tokenResponse = impersonationService.startImpersonation(
                UUID.fromString(session.getSessionUuid()), supportUser1.getId());

        assertNotNull(tokenResponse.getImpersonationToken());
        assertEquals(targetUser.getEmail(), tokenResponse.getTargetUserEmail());
        assertEquals(session.getSessionUuid(), tokenResponse.getSessionUuid());
        assertNotNull(tokenResponse.getExpiresAt());

        ImpersonationSession activeSession = sessionRepository.findBySessionUuid(
                UUID.fromString(session.getSessionUuid())).orElseThrow();
        assertEquals(ImpersonationSession.SessionStatus.ACTIVE, activeSession.getStatus());
        assertNotNull(activeSession.getStartedAt());
        assertNotNull(activeSession.getExpiresAt());
    }

    @Test
    @DisplayName("Cannot start impersonation without approval")
    void shouldRejectStartingNonApprovedSession() {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Need immediate access without approval")
                .durationMinutes(60)
                .build();

        ImpersonationSessionResponse session = impersonationService.requestImpersonation(
                supportUser1.getId(), request);

        assertThrows(IllegalStateException.class, () ->
                impersonationService.startImpersonation(
                        UUID.fromString(session.getSessionUuid()), supportUser1.getId()));
    }

    @Test
    @DisplayName("Can terminate active impersonation session")
    void shouldTerminateImpersonation() {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Temporary access for hotfix test")
                .durationMinutes(60)
                .build();

        ImpersonationSessionResponse session = impersonationService.requestImpersonation(
                supportUser1.getId(), request);

        impersonationService.approveImpersonation(
                UUID.fromString(session.getSessionUuid()),
                supportUser2.getId(),
                ApproveImpersonationDto.builder().approvalReason("Approved").build());

        impersonationService.startImpersonation(
                UUID.fromString(session.getSessionUuid()), supportUser1.getId());

        impersonationService.terminateImpersonation(
                UUID.fromString(session.getSessionUuid()), "Investigation complete");

        ImpersonationSession terminated = sessionRepository.findBySessionUuid(
                UUID.fromString(session.getSessionUuid())).orElseThrow();

        assertEquals(ImpersonationSession.SessionStatus.TERMINATED, terminated.getStatus());
        assertEquals("Investigation complete", terminated.getTerminationReason());
        assertNotNull(terminated.getTerminatedAt());
    }

    @Test
    @DisplayName("Scheduler auto-terminates expired active sessions")
    void shouldAutoTerminateExpiredSessionsViaScheduler() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        ImpersonationSession expiredSession = ImpersonationSession.builder()
                .supportUserId(supportUser1.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(tenant.getId())
                .status(ImpersonationSession.SessionStatus.ACTIVE)
                .reason("Past troubleshooting session")
                .startedAt(past.minusMinutes(60))
                .expiresAt(past)
                .maxDurationMinutes(60)
                .build();

        sessionRepository.save(expiredSession);

        impersonationScheduler.terminateExpiredSessions();

        ImpersonationSession result = sessionRepository.findById(expiredSession.getId()).orElseThrow();
        assertEquals(ImpersonationSession.SessionStatus.TERMINATED, result.getStatus());
        assertEquals("Session expired automatically", result.getTerminationReason());
    }
}
