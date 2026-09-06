package org.example.signer.controller;

import org.example.signer.entity.AuditEvent;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.AuditEventRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.security.JwtService;
import org.junit.jupiter.api.AfterEach;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Tenant platformTenant;
    private User platformAdminUser;
    private String platformAdminToken;

    private Tenant tenantA;
    private User tenantAdminA;
    private String tenantAdminTokenA;
    private User viewerA;
    private String viewerTokenA;

    private Tenant tenantB;
    private User tenantAdminB;
    private String tenantAdminTokenB;

    @BeforeEach
    void setup() {
        cleanDb();

        platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Admin")
                .slug("platform-admin")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(100)
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .build());

        platformAdminUser = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@platform.io")
                .username("platformadmin")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .firstName("Platform")
                .lastName("Admin")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        platformAdminToken = jwtService.generateToken(platformAdminUser, platformTenant.getSlug());

        tenantA = tenantRepository.save(Tenant.builder()
                .name("Acme Bank")
                .slug("acme-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                .build());

        tenantAdminA = userRepository.save(User.builder()
                .tenantId(tenantA.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@acme.com")
                .username("acmeadmin")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .firstName("Acme")
                .lastName("Admin")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        tenantAdminTokenA = jwtService.generateToken(tenantAdminA, tenantA.getSlug());

        viewerA = userRepository.save(User.builder()
                .tenantId(tenantA.getId())
                .userUuid(UUID.randomUUID())
                .email("viewer@acme.com")
                .username("acmeviewer")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .firstName("Acme")
                .lastName("Viewer")
                .role(User.UserRole.VIEWER)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        viewerTokenA = jwtService.generateToken(viewerA, tenantA.getSlug());

        tenantB = tenantRepository.save(Tenant.builder()
                .name("Beta Corp")
                .slug("beta-corp")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());

        tenantAdminB = userRepository.save(User.builder()
                .tenantId(tenantB.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@beta.com")
                .username("betaadmin")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .firstName("Beta")
                .lastName("Admin")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        tenantAdminTokenB = jwtService.generateToken(tenantAdminB, tenantB.getSlug());

        // Seed events
        seedEvents();
    }

    @AfterEach
    void tearDown() {
        cleanDb();
    }

    private void cleanDb() {
        auditEventRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    private void seedEvents() {
        AuditEvent eventA1 = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(tenantA.getId())
                .userId(tenantAdminA.getId())
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("INVITE_USER")
                .resourceType("USER")
                .resourceId("100")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build();

        AuditEvent eventA2 = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(tenantA.getId())
                .userId(tenantAdminA.getId())
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN")
                .resourceType("AUTH")
                .resourceId("100")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build();

        AuditEvent eventB1 = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(tenantB.getId())
                .userId(tenantAdminB.getId())
                .eventType(AuditEvent.EventType.CONFIG_CHANGE)
                .action("UPDATE_SEATS")
                .resourceType("SEAT_QUOTA")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build();

        auditEventRepository.saveAll(List.of(eventA1, eventA2, eventB1));
    }

    @Test
    @DisplayName("Should fetch tenant audit events with strict isolation")
    void shouldFetchTenantEventsWithIsolation() throws Exception {
        mockMvc.perform(get("/api/v1/audit/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].tenantId", everyItem(is(tenantA.getId().intValue()))));
    }

    @Test
    @DisplayName("Should fetch resource history for specific resource within tenant")
    void shouldFetchResourceHistory() throws Exception {
        mockMvc.perform(get("/api/v1/audit/resource-history")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminTokenA)
                        .param("resourceType", "USER")
                        .param("resourceId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].action").value("INVITE_USER"))
                .andExpect(jsonPath("$[0].resourceId").value("100"));
    }

    @Test
    @DisplayName("Should export tenant audit events as JSON file")
    void shouldExportTenantJson() throws Exception {
        mockMvc.perform(get("/api/v1/audit/export/json")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminTokenA))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"audit-events-")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should export tenant audit events as CSV file")
    void shouldExportTenantCsv() throws Exception {
        mockMvc.perform(get("/api/v1/audit/export/csv")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminTokenA))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"audit-events-")))
                .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
                .andExpect(content().string(containsString("Event UUID,Timestamp,Event Type,Action")))
                .andExpect(content().string(containsString("INVITE_USER")));
    }

    @Test
    @DisplayName("Should allow platform admin to search events across all tenants")
    void shouldAllowPlatformAdminSearch() throws Exception {
        mockMvc.perform(get("/api/v1/audit/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));

        // Filter by tenant B specifically
        mockMvc.perform(get("/api/v1/audit/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + platformAdminToken)
                        .param("tenantId", tenantB.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].action").value("UPDATE_SEATS"));
    }

    @Test
    @DisplayName("Should deny access to unauthorized users or missing tokens")
    void shouldDenyUnauthorizedAccess() throws Exception {
        // Unauthenticated
        mockMvc.perform(get("/api/v1/audit/events"))
                .andExpect(status().isUnauthorized());

        // Non-admin (VIEWER)
        mockMvc.perform(get("/api/v1/audit/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + viewerTokenA))
                .andExpect(status().isForbidden());

        // Platform admin endpoint accessed by tenant admin
        mockMvc.perform(get("/api/v1/audit/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tenantAdminTokenA))
                .andExpect(status().isForbidden());
    }
}
