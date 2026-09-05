package org.example.signer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.impersonation.ApproveImpersonationDto;
import org.example.signer.dto.impersonation.ImpersonationRequestDto;
import org.example.signer.dto.impersonation.RejectImpersonationDto;
import org.example.signer.entity.ImpersonationSession;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.ImpersonationSessionRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImpersonationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImpersonationSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant testTenant;
    private Tenant otherTenant;
    private User platformAdmin1;
    private String platformAdminToken1;
    private User platformAdmin2;
    private String platformAdminToken2;
    private User tenantAdmin;
    private String tenantAdminToken;
    private User targetUser;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();

        testTenant = tenantRepository.save(Tenant.builder()
                .name("Controller Bank A")
                .slug("bank-a-" + UUID.randomUUID().toString().substring(0, 8))
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());

        otherTenant = tenantRepository.save(Tenant.builder()
                .name("Controller Bank B")
                .slug("bank-b-" + UUID.randomUUID().toString().substring(0, 8))
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(5)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());

        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Controller Tenant")
                .slug("platform-ctrl-" + UUID.randomUUID().toString().substring(0, 8))
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .build());

        platformAdmin1 = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .email("admin1-" + UUID.randomUUID().toString().substring(0, 8) + "@platform.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .firstName("Platform")
                .lastName("Admin1")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
        platformAdminToken1 = jwtService.generateToken(platformAdmin1, "platform");

        platformAdmin2 = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .email("admin2-" + UUID.randomUUID().toString().substring(0, 8) + "@platform.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .firstName("Platform")
                .lastName("Admin2")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
        platformAdminToken2 = jwtService.generateToken(platformAdmin2, "platform");

        tenantAdmin = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .email("admin-" + UUID.randomUUID().toString().substring(0, 8) + "@banka.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .firstName("Tenant")
                .lastName("Admin")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
        tenantAdminToken = jwtService.generateToken(tenantAdmin, testTenant.getSlug());

        targetUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .email("dev-" + UUID.randomUUID().toString().substring(0, 8) + "@banka.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .firstName("Dev")
                .lastName("User")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Platform admin can request impersonation session")
    void platformAdminShouldRequestImpersonation() throws Exception {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Investigating pacs.008 message transmission issue")
                .durationMinutes(120)
                .build();

        mockMvc.perform(post("/api/v1/impersonation/request")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + platformAdminToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionUuid", notNullValue()))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.targetUserEmail", is(targetUser.getEmail())))
                .andExpect(jsonPath("$.maxDurationMinutes", is(120)));
    }

    @Test
    @DisplayName("Tenant admin cannot request impersonation session")
    void tenantAdminCannotRequestImpersonation() throws Exception {
        ImpersonationRequestDto request = ImpersonationRequestDto.builder()
                .targetUserId(targetUser.getId())
                .reason("Unauthorized impersonation attempt")
                .durationMinutes(60)
                .build();

        mockMvc.perform(post("/api/v1/impersonation/request")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Second platform admin can approve impersonation request")
    void secondPlatformAdminCanApprove() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        ImpersonationSession session = ImpersonationSession.builder()
                .sessionUuid(sessionUuid)
                .supportUserId(platformAdmin1.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.PENDING)
                .reason("Investigating transmission timeout")
                .maxDurationMinutes(60)
                .build();
        sessionRepository.save(session);

        ApproveImpersonationDto approveDto = ApproveImpersonationDto.builder()
                .approvalReason("Approved after verifying incident ticket #7890")
                .build();

        mockMvc.perform(post("/api/v1/impersonation/" + sessionUuid + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + platformAdminToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.approvedByEmail", is(platformAdmin2.getEmail())));
    }

    @Test
    @DisplayName("Dual authorization: Platform admin cannot approve own impersonation request")
    void platformAdminCannotApproveOwnRequest() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        ImpersonationSession session = ImpersonationSession.builder()
                .sessionUuid(sessionUuid)
                .supportUserId(platformAdmin1.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.PENDING)
                .reason("Investigating transmission timeout")
                .maxDurationMinutes(60)
                .build();
        sessionRepository.save(session);

        ApproveImpersonationDto approveDto = ApproveImpersonationDto.builder()
                .approvalReason("Self approval attempt")
                .build();

        mockMvc.perform(post("/api/v1/impersonation/" + sessionUuid + "/approve")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + platformAdminToken1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Platform admin can reject impersonation request")
    void platformAdminCanReject() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        ImpersonationSession session = ImpersonationSession.builder()
                .sessionUuid(sessionUuid)
                .supportUserId(platformAdmin1.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.PENDING)
                .reason("Need access for troubleshooting")
                .maxDurationMinutes(60)
                .build();
        sessionRepository.save(session);

        RejectImpersonationDto rejectDto = RejectImpersonationDto.builder()
                .rejectionReason("Insufficient logs attached to ticket")
                .build();

        mockMvc.perform(post("/api/v1/impersonation/" + sessionUuid + "/reject")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + platformAdminToken2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.terminationReason", is("Insufficient logs attached to ticket")));
    }

    @Test
    @DisplayName("Requesting platform admin can start approved session")
    void platformAdminCanStartApprovedSession() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        ImpersonationSession session = ImpersonationSession.builder()
                .sessionUuid(sessionUuid)
                .supportUserId(platformAdmin1.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.APPROVED)
                .reason("Approved support session")
                .approvedBy(platformAdmin2.getId())
                .approvalReason("Approved by lead")
                .maxDurationMinutes(60)
                .build();
        sessionRepository.save(session);

        mockMvc.perform(post("/api/v1/impersonation/" + sessionUuid + "/start")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + platformAdminToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.impersonationToken", notNullValue()))
                .andExpect(jsonPath("$.sessionUuid", is(sessionUuid.toString())))
                .andExpect(jsonPath("$.targetUserEmail", is(targetUser.getEmail())));
    }

    @Test
    @DisplayName("Can terminate active impersonation session")
    void shouldTerminateActiveSession() throws Exception {
        UUID sessionUuid = UUID.randomUUID();
        ImpersonationSession session = ImpersonationSession.builder()
                .sessionUuid(sessionUuid)
                .supportUserId(platformAdmin1.getId())
                .targetUserId(targetUser.getId())
                .targetTenantId(testTenant.getId())
                .status(ImpersonationSession.SessionStatus.ACTIVE)
                .reason("Active session")
                .startedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .maxDurationMinutes(60)
                .build();
        sessionRepository.save(session);

        mockMvc.perform(post("/api/v1/impersonation/" + sessionUuid + "/terminate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + platformAdminToken1)
                        .param("reason", "Finished investigation early"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Platform admin can list pending sessions")
    void platformAdminCanListPendingSessions() throws Exception {
        mockMvc.perform(get("/api/v1/impersonation/pending")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + platformAdminToken1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @DisplayName("Tenant admin cannot list all pending sessions")
    void tenantAdminCannotListPendingSessions() throws Exception {
        mockMvc.perform(get("/api/v1/impersonation/pending")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tenant admin can view impersonation sessions for own tenant")
    void tenantAdminCanViewOwnTenantSessions() throws Exception {
        mockMvc.perform(get("/api/v1/impersonation/tenant/" + testTenant.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Tenant admin cannot view impersonation sessions for another tenant")
    void tenantAdminCannotViewOtherTenantSessions() throws Exception {
        mockMvc.perform(get("/api/v1/impersonation/tenant/" + otherTenant.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminToken))
                .andExpect(status().isForbidden());
    }
}
