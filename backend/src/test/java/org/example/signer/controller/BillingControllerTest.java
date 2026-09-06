package org.example.signer.controller;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("Platform admin should retrieve seat usage for all tenants")
    void platformAdminShouldGetAllSeatUsage() throws Exception {
        mockMvc.perform(get("/api/v1/billing/seat-usage")
                .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Tenant admin should be forbidden from accessing all tenants seat usage")
    void tenantAdminShouldBeForbiddenFromAllSeatUsage() throws Exception {
        mockMvc.perform(get("/api/v1/billing/seat-usage")
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Platform admin should retrieve seat usage for a specific tenant")
    void platformAdminShouldGetTenantSeatUsage() throws Exception {
        mockMvc.perform(get("/api/v1/billing/seat-usage/" + testTenant.getId())
                .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(testTenant.getId()))
                .andExpect(jsonPath("$.tenantSlug").value("acme-bank"))
                .andExpect(jsonPath("$.maxSeats").value(10))
                .andExpect(jsonPath("$.usedSeats").value(1))
                .andExpect(jsonPath("$.billableSeats").value(10));
    }

    @Test
    @DisplayName("Tenant admin should be forbidden from accessing specific tenant billing endpoint")
    void tenantAdminShouldBeForbiddenFromTenantSeatUsage() throws Exception {
        mockMvc.perform(get("/api/v1/billing/seat-usage/" + testTenant.getId())
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isForbidden());
    }
}
