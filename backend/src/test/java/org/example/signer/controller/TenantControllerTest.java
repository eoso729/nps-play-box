package org.example.signer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.tenant.*;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.TenantRepository;
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
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Tenant platformTenant;
    private User platformAdminUser;
    private String platformAdminToken;

    private Tenant tenantA;
    private User tenantAdminUser;
    private String tenantAdminToken;

    private User developerUser;
    private String developerToken;

    private Tenant tenantB;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        // 1. Platform administration tenant and platform admin
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
                .email("admin@npsbox.io")
                .username("platformadmin")
                .passwordHash(passwordEncoder.encode("AdminPass123"))
                .firstName("Platform")
                .lastName("Admin")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        platformAdminToken = jwtService.generateToken(platformAdminUser, platformTenant.getSlug());

        // 2. Tenant A and tenant admin
        tenantA = tenantRepository.save(Tenant.builder()
                .name("Acme Bank")
                .slug("acme-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                .build());

        tenantAdminUser = userRepository.save(User.builder()
                .tenantId(tenantA.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@acmebank.com")
                .username("acmeadmin")
                .passwordHash(passwordEncoder.encode("AcmePass123"))
                .firstName("Acme")
                .lastName("Admin")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        tenantAdminToken = jwtService.generateToken(tenantAdminUser, tenantA.getSlug());

        // 3. Developer in Tenant A
        developerUser = userRepository.save(User.builder()
                .tenantId(tenantA.getId())
                .userUuid(UUID.randomUUID())
                .email("dev@acmebank.com")
                .username("acmedev")
                .passwordHash(passwordEncoder.encode("AcmePass123"))
                .firstName("Acme")
                .lastName("Dev")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        developerToken = jwtService.generateToken(developerUser, tenantA.getSlug());

        // 4. Tenant B
        tenantB = tenantRepository.save(Tenant.builder()
                .name("Beta Credit Union")
                .slug("beta-cu")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(5)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());
    }

    @Test
    @DisplayName("Platform admin should successfully create a new tenant")
    void platformAdminShouldCreateTenant() throws Exception {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("New Financial Institution")
                .slug("new-financial")
                .maxSeats(15)
                .subscriptionTier("PROFESSIONAL")
                .adminEmail("admin@newfinancial.com")
                .adminFirstName("John")
                .adminLastName("Doe")
                .adminPassword("SecurePassword123")
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.slug").value("new-financial"))
                .andExpect(jsonPath("$.maxSeats").value(15))
                .andExpect(jsonPath("$.usedSeats").value(1))
                .andExpect(jsonPath("$.subscriptionTier").value("PROFESSIONAL"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should return 409 Conflict when creating tenant with duplicate slug")
    void shouldRejectDuplicateSlugOnCreate() throws Exception {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Duplicate Slug Corp")
                .slug("acme-bank") // already exists
                .maxSeats(5)
                .subscriptionTier("STANDARD")
                .adminEmail("admin@dupslug.com")
                .adminFirstName("Dup")
                .adminLastName("User")
                .adminPassword("SecurePassword123")
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Slug already exists: acme-bank"));
    }

    @Test
    @DisplayName("Tenant admin should be forbidden from creating tenants")
    void tenantAdminShouldNotCreateTenant() throws Exception {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Unauthorized Org")
                .slug("unauthorized-org")
                .maxSeats(5)
                .subscriptionTier("STANDARD")
                .adminEmail("admin@unauth.com")
                .adminFirstName("Unauth")
                .adminLastName("User")
                .adminPassword("Password123")
                .build();

        mockMvc.perform(post("/api/v1/tenants")
                        .header("Authorization", "Bearer " + tenantAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated requests should be rejected with 401")
    void unauthenticatedShouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/v1/tenants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Platform admin should list all tenants paginated")
    void platformAdminShouldListAllTenants() throws Exception {
        mockMvc.perform(get("/api/v1/tenants")
                        .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3)); // platformTenant, tenantA, tenantB
    }

    @Test
    @DisplayName("Platform admin can view any tenant by ID")
    void platformAdminCanViewAnyTenant() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/" + tenantA.getId())
                        .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("acme-bank"))
                .andExpect(jsonPath("$.usedSeats").value(2)); // tenantAdminUser + developerUser
    }

    @Test
    @DisplayName("Tenant admin can view own tenant by ID")
    void tenantAdminCanViewOwnTenant() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/" + tenantA.getId())
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("acme-bank"))
                .andExpect(jsonPath("$.usedSeats").value(2));
    }

    @Test
    @DisplayName("Tenant admin cannot view another tenant by ID (403 Forbidden)")
    void tenantAdminCannotViewOtherTenant() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/" + tenantB.getId())
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Developer cannot view tenant by ID directly (403 Forbidden)")
    void developerCannotViewTenantById() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/" + tenantA.getId())
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Any authenticated tenant user can view own tenant via /current")
    void anyAuthenticatedUserCanViewCurrentTenant() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/current")
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("acme-bank"))
                .andExpect(jsonPath("$.name").value("Acme Bank"))
                .andExpect(jsonPath("$.usedSeats").value(2));
    }

    @Test
    @DisplayName("Platform admin can update tenant details")
    void platformAdminCanUpdateTenant() throws Exception {
        UpdateTenantRequest request = UpdateTenantRequest.builder()
                .name("Acme Bank International")
                .subscriptionTier("ENTERPRISE")
                .metadata("{\"region\":\"EU\"}")
                .build();

        mockMvc.perform(put("/api/v1/tenants/" + tenantA.getId())
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Bank International"))
                .andExpect(jsonPath("$.subscriptionTier").value("ENTERPRISE"))
                .andExpect(jsonPath("$.metadata").value("{\"region\":\"EU\"}"));
    }

    @Test
    @DisplayName("Platform admin can update tenant status")
    void platformAdminCanUpdateStatus() throws Exception {
        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .status("SUSPENDED")
                .reason("Account under compliance review")
                .build();

        mockMvc.perform(patch("/api/v1/tenants/" + tenantA.getId() + "/status")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    @DisplayName("Platform admin can update tenant seat quota")
    void platformAdminCanUpdateSeats() throws Exception {
        UpdateSeatsRequest request = UpdateSeatsRequest.builder()
                .maxSeats(25)
                .build();

        mockMvc.perform(patch("/api/v1/tenants/" + tenantA.getId() + "/seats")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxSeats").value(25));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when reducing seats below active usage")
    void shouldRejectReducingSeatsBelowActiveUsage() throws Exception {
        // Tenant A currently has 2 active users (tenantAdminUser + developerUser)
        UpdateSeatsRequest request = UpdateSeatsRequest.builder()
                .maxSeats(1)
                .build();

        mockMvc.perform(patch("/api/v1/tenants/" + tenantA.getId() + "/seats")
                        .header("Authorization", "Bearer " + platformAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot reduce seats below current usage")));
    }

    @Test
    @DisplayName("Platform admin can soft delete tenant")
    void platformAdminCanSoftDeleteTenant() throws Exception {
        mockMvc.perform(delete("/api/v1/tenants/" + tenantA.getId())
                        .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isNoContent());

        // Verify tenant is marked INACTIVE
        mockMvc.perform(get("/api/v1/tenants/" + tenantA.getId())
                        .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("Should return 404 Not Found for non-existent tenant")
    void shouldReturn404ForNonExistentTenant() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/999999")
                        .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tenant not found with ID: 999999"));
    }
}
