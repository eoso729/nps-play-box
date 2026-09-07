package org.example.signer.service;

import org.example.signer.dto.user.*;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.entity.UserInvitation;
import org.example.signer.exception.InvalidQuotaException;
import org.example.signer.exception.QuotaExceededException;
import org.example.signer.exception.ResourceNotFoundException;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserInvitationRepository invitationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Tenant testTenant;
    private User adminUser;

    @BeforeEach
    void setup() {
        invitationRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
        invitationRepository.flush();
        userRepository.flush();
        tenantRepository.flush();

        testTenant = tenantRepository.save(Tenant.builder()
                .name("Acme Bank")
                .slug("acme-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());

        adminUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@acmebank.com")
                .username("acmeadmin")
                .passwordHash(passwordEncoder.encode("SecretPass123"))
                .firstName("Admin")
                .lastName("User")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());
    }

    @Test
    @DisplayName("Should successfully invite user with token and email notification")
    void shouldInviteUser() {
        InviteUserRequest request = InviteUserRequest.builder()
                .email("newuser@acmebank.com")
                .role("DEVELOPER")
                .firstName("Alice")
                .lastName("Smith")
                .customMessage("Welcome to the team!")
                .build();

        InvitationResponse response = userService.inviteUser(testTenant.getId(), adminUser.getId(), request);

        assertNotNull(response.getId());
        assertEquals("newuser@acmebank.com", response.getEmail());
        assertEquals("DEVELOPER", response.getRole());
        assertNotNull(response.getInvitationToken());
        assertFalse(response.isExpired());
        assertEquals("Admin User", response.getInvitedByName());
    }

    @Test
    @DisplayName("Should reject duplicate pending invitation for same email in tenant")
    void shouldRejectDuplicatePendingInvitation() {
        InviteUserRequest request = InviteUserRequest.builder()
                .email("duplicate@acmebank.com")
                .role("DEVELOPER")
                .build();

        userService.inviteUser(testTenant.getId(), adminUser.getId(), request);

        assertThrows(IllegalArgumentException.class, () ->
                userService.inviteUser(testTenant.getId(), adminUser.getId(), request));
    }

    @Test
    @DisplayName("Should reject invitation if user email already exists in tenant")
    void shouldRejectInvitationIfUserAlreadyExists() {
        InviteUserRequest request = InviteUserRequest.builder()
                .email("admin@acmebank.com") // Already exists
                .role("DEVELOPER")
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                userService.inviteUser(testTenant.getId(), adminUser.getId(), request));
    }

    @Test
    @DisplayName("Should reject invitation if seat quota is full")
    void shouldRejectInvitationIfQuotaFull() {
        Tenant tinyTenant = tenantRepository.save(Tenant.builder()
                .name("Tiny Tenant")
                .slug("tiny-tenant")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(1)
                .subscriptionTier(Tenant.SubscriptionTier.TRIAL)
                .build());

        User tinyAdmin = userRepository.save(User.builder()
                .tenantId(tinyTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@tiny.com")
                .passwordHash("hashed")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        InviteUserRequest request = InviteUserRequest.builder()
                .email("overflow@tiny.com")
                .role("DEVELOPER")
                .build();

        assertThrows(QuotaExceededException.class, () ->
                userService.inviteUser(tinyTenant.getId(), tinyAdmin.getId(), request));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when invitedBy is null")
    void shouldRejectInvitationIfInviterIdIsNull() {
        InviteUserRequest request = InviteUserRequest.builder()
                .email("noinviter@acmebank.com")
                .role("DEVELOPER")
                .build();

        assertThrows(IllegalStateException.class, () ->
                userService.inviteUser(testTenant.getId(), null, request));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when inviter user does not exist in database")
    void shouldRejectInvitationIfInviterDoesNotExist() {
        InviteUserRequest request = InviteUserRequest.builder()
                .email("ghostinviter@acmebank.com")
                .role("DEVELOPER")
                .build();

        assertThrows(ResourceNotFoundException.class, () ->
                userService.inviteUser(testTenant.getId(), 999999L, request));
    }

    @Test
    @DisplayName("Should accept valid invitation and create active user account")
    void shouldAcceptInvitation() {
        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email("invited@acmebank.com")
                .role("VIEWER")
                .build();
        InvitationResponse invitation = userService.inviteUser(testTenant.getId(), adminUser.getId(), inviteReq);

        AcceptInvitationRequest acceptReq = AcceptInvitationRequest.builder()
                .token(invitation.getInvitationToken())
                .firstName("Bob")
                .lastName("Jones")
                .password("SecurePassword123")
                .build();

        UserResponse user = userService.acceptInvitation(acceptReq);

        assertEquals("invited@acmebank.com", user.getEmail());
        assertEquals("VIEWER", user.getRole());
        assertEquals("ACTIVE", user.getStatus());
        assertEquals(testTenant.getSlug(), user.getTenant().getSlug());

        // Verify invitation is marked accepted
        UserInvitation savedInv = invitationRepository.findById(invitation.getId()).orElseThrow();
        assertTrue(savedInv.isAccepted());
    }

    @Test
    @DisplayName("Should reject expired invitation")
    void shouldRejectExpiredInvitation() {
        UserInvitation expired = invitationRepository.save(UserInvitation.builder()
                .tenantId(testTenant.getId())
                .email("expired@acmebank.com")
                .role(User.UserRole.DEVELOPER)
                .invitationToken(UUID.randomUUID().toString())
                .invitedBy(adminUser.getId())
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build());

        AcceptInvitationRequest acceptReq = AcceptInvitationRequest.builder()
                .token(expired.getInvitationToken())
                .firstName("Expired")
                .lastName("User")
                .password("SecurePassword123")
                .build();

        assertThrows(IllegalStateException.class, () -> userService.acceptInvitation(acceptReq));
    }

    @Test
    @DisplayName("Should update user role")
    void shouldUpdateUserRole() {
        User dev = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("dev@acmebank.com")
                .passwordHash("hashed")
                .firstName("Dev")
                .lastName("User")
                .role(User.UserRole.VIEWER)
                .status(User.UserStatus.ACTIVE)
                .build());

        UpdateRoleRequest updateReq = UpdateRoleRequest.builder()
                .role("DEVELOPER")
                .build();

        UserResponse updated = userService.updateUserRole(dev.getId(), testTenant.getId(), updateReq);
        assertEquals("DEVELOPER", updated.getRole());
    }

    @Test
    @DisplayName("Should prevent last active tenant admin from being demoted")
    void shouldPreventLastAdminDemotion() {
        UpdateRoleRequest request = UpdateRoleRequest.builder()
                .role("VIEWER")
                .build();

        assertThrows(IllegalStateException.class, () ->
                userService.updateUserRole(adminUser.getId(), testTenant.getId(), request));
    }

    @Test
    @DisplayName("Should prevent last active tenant admin from being deactivated")
    void shouldPreventLastAdminDeactivation() {
        assertThrows(IllegalStateException.class, () ->
                userService.deactivateUser(adminUser.getId(), testTenant.getId()));
    }

    @Test
    @DisplayName("Should deactivate and reactivate non-admin user")
    void shouldDeactivateAndReactivateUser() {
        User dev = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("lifecycle@acmebank.com")
                .passwordHash("hashed")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .build());

        UserResponse deactivated = userService.deactivateUser(dev.getId(), testTenant.getId());
        assertEquals("INACTIVE", deactivated.getStatus());

        UserResponse reactivated = userService.reactivateUser(dev.getId(), testTenant.getId());
        assertEquals("ACTIVE", reactivated.getStatus());
    }

    @Test
    @DisplayName("Should track seat utilization accurately")
    void shouldTrackSeatUtilization() {
        // testTenant has maxSeats = 10, adminUser = 1 active
        // Add 2 active developers
        for (int i = 1; i <= 2; i++) {
            userRepository.save(User.builder()
                    .tenantId(testTenant.getId())
                    .userUuid(UUID.randomUUID())
                    .email("dev" + i + "@acmebank.com")
                    .passwordHash("hashed")
                    .role(User.UserRole.DEVELOPER)
                    .status(User.UserStatus.ACTIVE)
                    .build());
        }

        // Add 1 pending invitation
        userService.inviteUser(testTenant.getId(), adminUser.getId(), InviteUserRequest.builder()
                .email("pending@acmebank.com")
                .role("VIEWER")
                .build());

        SeatUtilizationResponse stats = userService.getSeatUtilization(testTenant.getId());

        assertEquals(10, stats.getMaxSeats());
        assertEquals(3, stats.getUsedSeats()); // 1 admin + 2 dev
        assertEquals(7, stats.getAvailableSeats());
        assertEquals(3, stats.getActiveUsers());
        assertEquals(1, stats.getPendingInvitations());
        assertEquals(30.0, stats.getUtilizationPercentage());
    }

    @Test
    @DisplayName("Should cancel pending invitation")
    void shouldCancelInvitation() {
        InvitationResponse inv = userService.inviteUser(testTenant.getId(), adminUser.getId(), InviteUserRequest.builder()
                .email("cancel@acmebank.com")
                .role("DEVELOPER")
                .build());

        userService.cancelInvitation(inv.getId(), testTenant.getId());
        assertTrue(invitationRepository.findById(inv.getId()).isEmpty());
    }

    @Test
    @DisplayName("Should cleanup expired invitations")
    void shouldCleanupExpiredInvitations() {
        UserInvitation expired = invitationRepository.save(UserInvitation.builder()
                .tenantId(testTenant.getId())
                .email("old@acmebank.com")
                .role(User.UserRole.DEVELOPER)
                .invitationToken(UUID.randomUUID().toString())
                .invitedBy(adminUser.getId())
                .expiresAt(LocalDateTime.now().minusDays(2))
                .build());

        userService.cleanupExpiredInvitations();
        assertTrue(invitationRepository.findById(expired.getId()).isEmpty());
    }

    @Test
    @DisplayName("Should get paginated users scoped to tenant")
    void shouldGetUsersByTenant() {
        Page<UserResponse> users = userService.getUsersByTenant(testTenant.getId(), PageRequest.of(0, 10));
        assertEquals(1, users.getTotalElements());
        assertEquals("admin@acmebank.com", users.getContent().get(0).getEmail());
    }
}
