package org.example.signer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.quota.ApproveRequestRequest;
import org.example.signer.dto.quota.DenyRequestRequest;
import org.example.signer.dto.quota.SeatRequestRequest;
import org.example.signer.entity.SeatRequest;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.SeatRequestRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QuotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInvitationRepository invitationRepository;

    @Autowired
    private SeatRequestRepository seatRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Tenant testTenant;
    private User tenantAdminUser;
    private String tenantAdminToken;

    private User developerUser;
    private String developerToken;

    private Tenant platformTenant;
    private User platformAdminUser;
    private String platformAdminToken;

    @BeforeEach
    void setup() {
        seatRequestRepository.deleteAll();
        seatRequestRepository.flush();
        invitationRepository.deleteAll();
        invitationRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
        tenantRepository.deleteAll();
        tenantRepository.flush();

        testTenant = tenantRepository.save(Tenant.builder()
                .name("Acme Bank")
                .slug("acme-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                .build());

        tenantAdminUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@acmebank.com")
                .username("acmeadmin")
                .passwordHash(passwordEncoder.encode("AdminPass123!"))
                .firstName("Acme")
                .lastName("Admin")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());
        tenantAdminToken = jwtService.generateToken(tenantAdminUser, testTenant.getSlug());

        developerUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("dev@acmebank.com")
                .username("acmedev")
                .passwordHash(passwordEncoder.encode("DevPass123!"))
                .firstName("Dev")
                .lastName("User")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());
        developerToken = jwtService.generateToken(developerUser, testTenant.getSlug());

        platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Admin Tenant")
                .slug("platform-admin")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .build());

        platformAdminUser = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("platform@npsbox.io")
                .username("platformadmin")
                .passwordHash(passwordEncoder.encode("PlatformPass123!"))
                .firstName("Platform")
                .lastName("Admin")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());
        platformAdminToken = jwtService.generateToken(platformAdminUser, platformTenant.getSlug());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        seatRequestRepository.deleteAll();
        seatRequestRepository.flush();
        invitationRepository.deleteAll();
        invitationRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
        tenantRepository.deleteAll();
        tenantRepository.flush();
    }

    @Test
    @DisplayName("Should check seat availability for tenant admin")
    void shouldCheckAvailability() throws Exception {
        mockMvc.perform(get("/api/v1/quota/availability")
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.maxSeats").value(10))
                .andExpect(jsonPath("$.usedSeats").value(2))
                .andExpect(jsonPath("$.availableSeats").value(8))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should block non-tenant-admin from checking availability")
    void shouldBlockNonAdminFromCheckingAvailability() throws Exception {
        mockMvc.perform(get("/api/v1/quota/availability")
                .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get quota status for tenant admin")
    void shouldGetQuotaStatus() throws Exception {
        mockMvc.perform(get("/api/v1/quota/status")
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(testTenant.getId()))
                .andExpect(jsonPath("$.maxSeats").value(10))
                .andExpect(jsonPath("$.usedSeats").value(2))
                .andExpect(jsonPath("$.availableSeats").value(8))
                .andExpect(jsonPath("$.quotaExceeded").value(false))
                .andExpect(jsonPath("$.nearingLimit").value(false));
    }

    @Test
    @DisplayName("Should request additional seats successfully")
    void shouldRequestAdditionalSeats() throws Exception {
        SeatRequestRequest request = SeatRequestRequest.builder()
                .additionalSeats(5)
                .justification("Team scaling in Q3")
                .expectedGrowth("5 new engineers")
                .build();

        mockMvc.perform(post("/api/v1/quota/request")
                .header("Authorization", "Bearer " + tenantAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tenantId").value(testTenant.getId()))
                .andExpect(jsonPath("$.currentSeats").value(10))
                .andExpect(jsonPath("$.requestedAdditionalSeats").value(5))
                .andExpect(jsonPath("$.newTotalSeats").value(15))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Should list tenant seat requests")
    void shouldListTenantRequests() throws Exception {
        SeatRequest seatRequest = seatRequestRepository.save(SeatRequest.builder()
                .tenantId(testTenant.getId())
                .currentSeats(10)
                .requestedAdditionalSeats(5)
                .justification("Growth")
                .status(SeatRequest.RequestStatus.PENDING)
                .requestedBy(tenantAdminUser.getId())
                .build());

        mockMvc.perform(get("/api/v1/quota/requests")
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(seatRequest.getId()))
                .andExpect(jsonPath("$[0].requestedAdditionalSeats").value(5));
    }

    @Test
    @DisplayName("Platform admin should list all pending requests")
    void platformAdminShouldListAllPendingRequests() throws Exception {
        seatRequestRepository.save(SeatRequest.builder()
                .tenantId(testTenant.getId())
                .currentSeats(10)
                .requestedAdditionalSeats(8)
                .justification("Scaling")
                .status(SeatRequest.RequestStatus.PENDING)
                .requestedBy(tenantAdminUser.getId())
                .build());

        mockMvc.perform(get("/api/v1/quota/requests/all")
                .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].requestedAdditionalSeats").value(8));
    }

    @Test
    @DisplayName("Tenant admin should be forbidden from listing all pending requests")
    void tenantAdminShouldBeForbiddenFromAllRequests() throws Exception {
        mockMvc.perform(get("/api/v1/quota/requests/all")
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Platform admin should approve seat request")
    void platformAdminShouldApproveRequest() throws Exception {
        SeatRequest seatRequest = seatRequestRepository.save(SeatRequest.builder()
                .tenantId(testTenant.getId())
                .currentSeats(10)
                .requestedAdditionalSeats(5)
                .justification("Expansion")
                .status(SeatRequest.RequestStatus.PENDING)
                .requestedBy(tenantAdminUser.getId())
                .build());

        ApproveRequestRequest approveReq = ApproveRequestRequest.builder()
                .approvedSeats(5)
                .notes("Approved by platform team")
                .build();

        mockMvc.perform(patch("/api/v1/quota/requests/" + seatRequest.getId() + "/approve")
                .header("Authorization", "Bearer " + platformAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedSeats").value(5));
    }

    @Test
    @DisplayName("Platform admin should deny seat request")
    void platformAdminShouldDenyRequest() throws Exception {
        SeatRequest seatRequest = seatRequestRepository.save(SeatRequest.builder()
                .tenantId(testTenant.getId())
                .currentSeats(10)
                .requestedAdditionalSeats(50)
                .justification("Large ask")
                .status(SeatRequest.RequestStatus.PENDING)
                .requestedBy(tenantAdminUser.getId())
                .build());

        DenyRequestRequest denyReq = DenyRequestRequest.builder()
                .reason("Need contract upgrade before increasing seats")
                .build();

        mockMvc.perform(patch("/api/v1/quota/requests/" + seatRequest.getId() + "/deny")
                .header("Authorization", "Bearer " + platformAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(denyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DENIED"))
                .andExpect(jsonPath("$.denialReason").value("Need contract upgrade before increasing seats"));
    }
}
