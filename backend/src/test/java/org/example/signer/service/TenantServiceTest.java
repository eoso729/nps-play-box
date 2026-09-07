package org.example.signer.service;

import org.example.signer.dto.tenant.*;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.exception.DuplicateSlugException;
import org.example.signer.exception.InvalidQuotaException;
import org.example.signer.exception.ResourceNotFoundException;
import org.example.signer.exception.TenantNotFoundException;
import org.example.signer.entity.UserInvitation;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.security.TenantUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TenantServiceTest {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInvitationRepository userInvitationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userInvitationRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create tenant and generate invitation when admin password is omitted with authenticated platform admin")
    void shouldCreateTenantWithInvitationWhenPasswordOmitted() {
        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Admin")
                .slug("platform-admin-ctx")
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .build());

        User platformAdmin = userRepository.save(User.builder()
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

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new TenantUserDetails(platformAdmin, "platform-admin-ctx"),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"))
                )
        );

        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Zenith Bank")
                .slug("zenith-bank")
                .maxSeats(10)
                .subscriptionTier("PROFESSIONAL")
                .adminEmail("admin@zenithbank.com")
                .adminFirstName("Emmanuel")
                .adminLastName("Oso")
                .build();

        TenantResponse response = tenantService.createTenant(request);

        assertNotNull(response.getId());
        assertEquals("zenith-bank", response.getSlug());
        assertEquals(10, response.getMaxSeats());
        assertEquals(0, response.getUsedSeats());
        assertEquals("ACTIVE", response.getStatus());
        assertNotNull(response.getInvitationToken());
        assertNotNull(response.getInvitationUrl());
        assertTrue(response.getInvitationUrl().contains(response.getInvitationToken()));

        // Active admin user should NOT exist yet
        assertTrue(userRepository.findByEmailAndTenantId("admin@zenithbank.com", response.getId()).isEmpty());

        // Pending invitation should exist
        UserInvitation inv = userInvitationRepository.findByInvitationToken(response.getInvitationToken())
                .orElseThrow();
        assertEquals(response.getId(), inv.getTenantId());
        assertEquals("admin@zenithbank.com", inv.getEmail());
        assertEquals(User.UserRole.TENANT_ADMIN, inv.getRole());
        assertNull(inv.getAcceptedAt());
        assertFalse(inv.isExpired());
        assertEquals(platformAdmin.getId(), inv.getInvitedBy());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when no inviter is present in security context or request")
    void shouldThrowExceptionWhenNoInviterPresentInSecurityContext() {
        SecurityContextHolder.clearContext();

        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("No Auth Bank")
                .slug("no-auth-bank")
                .maxSeats(10)
                .subscriptionTier("PROFESSIONAL")
                .adminEmail("admin@noauthbank.com")
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> tenantService.createTenant(request));
        assertTrue(ex.getMessage().contains("Authenticated platform administrator context is required"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when explicit inviter user ID does not exist in database")
    void shouldThrowExceptionWhenInviterNotFoundInDatabase() {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Ghost User Bank")
                .slug("ghost-user-bank")
                .maxSeats(10)
                .subscriptionTier("PROFESSIONAL")
                .adminEmail("admin@ghostbank.com")
                .build();

        assertThrows(ResourceNotFoundException.class, () -> tenantService.createTenant(request, 999999L));
    }

    @Test
    @DisplayName("Should record platform admin as inviter when currentUserId is provided")
    void shouldCreateTenantWithInvitationWithInviterWhenCurrentUserIdProvided() {
        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Admin")
                .slug("platform-admin-test")
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .build());

        User platformAdmin = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("superadmin@npsbox.io")
                .username("superadmin")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Access Bank")
                .slug("access-bank")
                .maxSeats(15)
                .subscriptionTier("ENTERPRISE")
                .adminEmail("admin@accessbank.com")
                .build();

        TenantResponse response = tenantService.createTenant(request, platformAdmin.getId());
        assertNotNull(response.getInvitationToken());

        UserInvitation inv = userInvitationRepository.findByInvitationToken(response.getInvitationToken()).orElseThrow();
        assertEquals(platformAdmin.getId(), inv.getInvitedBy());
    }

    @Test
    @DisplayName("Should create tenant and provision initial tenant admin user")
    void shouldCreateTenantWithAdminUser() {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Test Financial Corp")
                .slug("test-financial")
                .maxSeats(10)
                .subscriptionTier("STANDARD")
                .adminEmail("admin@testfinancial.com")
                .adminFirstName("Admin")
                .adminLastName("User")
                .adminPassword("SecurePass123")
                .build();

        TenantResponse response = tenantService.createTenant(request);

        assertNotNull(response.getId());
        assertEquals("test-financial", response.getSlug());
        assertEquals(10, response.getMaxSeats());
        assertEquals(1, response.getUsedSeats());
        assertEquals("STANDARD", response.getSubscriptionTier());
        assertEquals("ACTIVE", response.getStatus());

        // Verify admin user created
        User admin = userRepository.findByEmailAndTenantId("admin@testfinancial.com", response.getId())
                .orElseThrow();
        assertEquals(User.UserRole.TENANT_ADMIN, admin.getRole());
        assertEquals(User.UserStatus.ACTIVE, admin.getStatus());
        assertTrue(passwordEncoder.matches("SecurePass123", admin.getPasswordHash()));
    }

    @Test
    @DisplayName("Should reject duplicate tenant slug with DuplicateSlugException")
    void shouldRejectDuplicateSlug() {
        CreateTenantRequest request1 = createTestTenantRequest("duplicate-slug");
        tenantService.createTenant(request1);

        CreateTenantRequest request2 = createTestTenantRequest("duplicate-slug");
        assertThrows(DuplicateSlugException.class, () -> tenantService.createTenant(request2));
    }

    @Test
    @DisplayName("Should update tenant seat quota successfully")
    void shouldUpdateTenantSeats() {
        CreateTenantRequest createReq = createTestTenantRequest("update-test");
        TenantResponse tenant = tenantService.createTenant(createReq);

        UpdateSeatsRequest updateReq = UpdateSeatsRequest.builder()
                .maxSeats(20)
                .build();

        TenantResponse updated = tenantService.updateSeats(tenant.getId(), updateReq);
        assertEquals(20, updated.getMaxSeats());
    }

    @Test
    @DisplayName("Should reject seat quota reduction below current active user usage")
    void shouldRejectSeatReductionBelowUsage() {
        CreateTenantRequest createReq = createTestTenantRequest("seat-test");
        createReq.setMaxSeats(10);
        TenantResponse tenant = tenantService.createTenant(createReq);

        // Add a second user to this tenant so usedSeats becomes 2
        userRepository.save(User.builder()
                .tenantId(tenant.getId())
                .userUuid(UUID.randomUUID())
                .email("dev@seat-test.com")
                .username("dev@seat-test.com")
                .passwordHash(passwordEncoder.encode("devpass123"))
                .firstName("Dev")
                .lastName("User")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        // Try to reduce to 1 seat (below current usage of 2)
        UpdateSeatsRequest updateReq = UpdateSeatsRequest.builder()
                .maxSeats(1)
                .build();

        assertThrows(InvalidQuotaException.class, () -> tenantService.updateSeats(tenant.getId(), updateReq));
    }

    @Test
    @DisplayName("Should update tenant details, status, and metadata")
    void shouldUpdateTenantDetailsAndStatus() {
        CreateTenantRequest createReq = createTestTenantRequest("details-test");
        TenantResponse tenant = tenantService.createTenant(createReq);

        // Update details
        UpdateTenantRequest updateReq = UpdateTenantRequest.builder()
                .name("Updated Corp")
                .subscriptionTier("ENTERPRISE")
                .metadata("{\"plan\":\"custom\"}")
                .build();
        TenantResponse updated = tenantService.updateTenant(tenant.getId(), updateReq);
        assertEquals("Updated Corp", updated.getName());
        assertEquals("ENTERPRISE", updated.getSubscriptionTier());
        assertEquals("{\"plan\":\"custom\"}", updated.getMetadata());

        // Update status
        UpdateStatusRequest statusReq = UpdateStatusRequest.builder()
                .status("SUSPENDED")
                .reason("Billing review")
                .build();
        TenantResponse suspended = tenantService.updateStatus(tenant.getId(), statusReq);
        assertEquals("SUSPENDED", suspended.getStatus());
    }

    @Test
    @DisplayName("Should soft-delete tenant and deactivate all tenant users")
    void shouldSoftDeleteTenantAndDeactivateUsers() {
        CreateTenantRequest createReq = createTestTenantRequest("soft-delete-test");
        TenantResponse tenant = tenantService.createTenant(createReq);

        // Add developer user
        User devUser = userRepository.save(User.builder()
                .tenantId(tenant.getId())
                .userUuid(UUID.randomUUID())
                .email("dev@softdelete.com")
                .username("dev@softdelete.com")
                .passwordHash(passwordEncoder.encode("devpass123"))
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        tenantService.deleteTenant(tenant.getId());

        Tenant deletedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertEquals(Tenant.TenantStatus.INACTIVE, deletedTenant.getStatus());

        List<User> users = userRepository.findByTenantId(tenant.getId());
        assertEquals(2, users.size());
        assertTrue(users.stream().allMatch(u -> u.getStatus() == User.UserStatus.INACTIVE));
    }

    @Test
    @DisplayName("Should throw TenantNotFoundException when querying non-existent tenant")
    void shouldThrowWhenTenantNotFound() {
        assertThrows(TenantNotFoundException.class, () -> tenantService.getTenantById(999999L));
    }

    @Test
    @DisplayName("Should fetch all tenants paginated with correct seat usage")
    void shouldFetchAllTenantsPaginated() {
        tenantService.createTenant(createTestTenantRequest("paginated-1"));
        tenantService.createTenant(createTestTenantRequest("paginated-2"));

        Page<TenantResponse> page = tenantService.getAllTenants(PageRequest.of(0, 10));
        assertTrue(page.getTotalElements() >= 2);
        assertTrue(page.getContent().stream().allMatch(t -> t.getUsedSeats() >= 1));
    }

    private CreateTenantRequest createTestTenantRequest(String slug) {
        return CreateTenantRequest.builder()
                .name("Test Tenant " + slug)
                .slug(slug)
                .maxSeats(5)
                .subscriptionTier("STANDARD")
                .adminEmail("admin@" + slug + ".com")
                .adminFirstName("Admin")
                .adminLastName("User")
                .adminPassword("password123")
                .build();
    }
}
