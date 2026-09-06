package org.example.signer.security;

import org.example.signer.entity.ImpersonationSession;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.ImpersonationSessionRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtImpersonationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ImpersonationSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;
    private User supportUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();

        tenant = tenantRepository.save(Tenant.builder()
                .name("Filter Test Bank")
                .slug("filter-bank-" + UUID.randomUUID().toString().substring(0, 8))
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(5)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());

        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Filter Tenant")
                .slug("platform-filter-" + UUID.randomUUID().toString().substring(0, 8))
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .build());

        supportUser = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .email("filter-support-" + UUID.randomUUID().toString().substring(0, 8) + "@platform.com")
                .passwordHash("hashed")
                .firstName("Platform")
                .lastName("Support")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        targetUser = userRepository.save(User.builder()
                .tenantId(tenant.getId())
                .email("filter-target-" + UUID.randomUUID().toString().substring(0, 8) + "@bank.com")
                .passwordHash("hashed")
                .firstName("Target")
                .lastName("Developer")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Active impersonation token allows access to protected endpoints")
    void shouldAllowAccessWithActiveImpersonationToken() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        ImpersonationSession session = ImpersonationSession.builder()
                .sessionUuid(sessionUuid)
                .supportUserId(supportUser.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(tenant.getId())
                .status(ImpersonationSession.SessionStatus.ACTIVE)
                .reason("Testing filter integration")
                .startedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .maxDurationMinutes(60)
                .build();

        sessionRepository.save(session);

        String token = jwtService.generateImpersonationToken(
                targetUser, tenant.getSlug(), supportUser.getId(), sessionUuid.toString(), 60);

        // Access target user's own profile endpoint
        mockMvc.perform(get("/api/v1/users/" + targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Terminated impersonation session is rejected with 401 Unauthorized")
    void shouldRejectTerminatedImpersonationToken() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        ImpersonationSession session = ImpersonationSession.builder()
                .sessionUuid(sessionUuid)
                .supportUserId(supportUser.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(tenant.getId())
                .status(ImpersonationSession.SessionStatus.TERMINATED)
                .reason("Terminated session test")
                .startedAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .terminatedAt(LocalDateTime.now())
                .terminationReason("Manual termination")
                .maxDurationMinutes(60)
                .build();

        sessionRepository.save(session);

        String token = jwtService.generateImpersonationToken(
                targetUser, tenant.getSlug(), supportUser.getId(), sessionUuid.toString(), 60);

        mockMvc.perform(get("/api/v1/users/" + targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Impersonation session expired or terminated")));
    }

    @Test
    @DisplayName("Expired impersonation session is rejected with 401 Unauthorized")
    void shouldRejectExpiredImpersonationToken() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        ImpersonationSession session = ImpersonationSession.builder()
                .sessionUuid(sessionUuid)
                .supportUserId(supportUser.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(tenant.getId())
                .status(ImpersonationSession.SessionStatus.ACTIVE)
                .reason("Expired session test")
                .startedAt(LocalDateTime.now().minusHours(2))
                .expiresAt(LocalDateTime.now().minusMinutes(5))
                .maxDurationMinutes(60)
                .build();

        sessionRepository.save(session);

        String token = jwtService.generateImpersonationToken(
                targetUser, tenant.getSlug(), supportUser.getId(), sessionUuid.toString(), 60);

        mockMvc.perform(get("/api/v1/users/" + targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Impersonation session expired or terminated")));
    }
}
