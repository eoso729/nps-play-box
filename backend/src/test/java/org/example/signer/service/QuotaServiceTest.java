package org.example.signer.service;

import org.example.signer.dto.quota.*;
import org.example.signer.entity.SeatRequest;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.exception.QuotaExceededException;
import org.example.signer.repository.SeatRequestRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QuotaServiceTest {

    @Autowired
    private QuotaService quotaService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInvitationRepository invitationRepository;

    @Autowired
    private SeatRequestRepository seatRequestRepository;

    private Tenant testTenant;
    private User adminUser;

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
                .name("Test Corp")
                .slug("test-corp")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(5)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());

        adminUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@test.com")
                .passwordHash("hashed")
                .firstName("Admin")
                .lastName("User")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Should check seat availability correctly")
    void shouldCheckAvailability() {
        SeatAvailabilityResponse response = quotaService.checkAvailability(testTenant.getId());

        assertTrue(response.getAvailable());
        assertEquals(5, response.getMaxSeats());
        assertEquals(1, response.getUsedSeats());
        assertEquals(4, response.getAvailableSeats());
        assertNotNull(response.getMessage());
    }

    @Test
    @DisplayName("Should detect when quota is exceeded")
    void shouldDetectQuotaExceeded() {
        // Fill up remaining 4 seats
        for (int i = 0; i < 4; i++) {
            userRepository.save(User.builder()
                    .tenantId(testTenant.getId())
                    .userUuid(UUID.randomUUID())
                    .email("user" + i + "@test.com")
                    .passwordHash("hashed")
                    .firstName("User")
                    .lastName(String.valueOf(i))
                    .role(User.UserRole.DEVELOPER)
                    .status(User.UserStatus.ACTIVE)
                    .build());
        }

        SeatAvailabilityResponse response = quotaService.checkAvailability(testTenant.getId());

        assertFalse(response.getAvailable());
        assertEquals(5, response.getUsedSeats());
        assertEquals(0, response.getAvailableSeats());
    }

    @Test
    @DisplayName("Should enforce quota and throw QuotaExceededException when seats are full")
    void shouldEnforceQuota() {
        for (int i = 0; i < 4; i++) {
            userRepository.save(User.builder()
                    .tenantId(testTenant.getId())
                    .userUuid(UUID.randomUUID())
                    .email("user" + i + "@test.com")
                    .passwordHash("hashed")
                    .firstName("User")
                    .lastName(String.valueOf(i))
                    .role(User.UserRole.DEVELOPER)
                    .status(User.UserStatus.ACTIVE)
                    .build());
        }

        assertThrows(QuotaExceededException.class, () ->
                quotaService.enforceQuotaForUserCreation(testTenant.getId()));
    }

    @Test
    @DisplayName("Should create seat request successfully")
    void shouldCreateSeatRequest() {
        SeatRequestRequest request = SeatRequestRequest.builder()
                .additionalSeats(10)
                .justification("Team expansion for Q4")
                .expectedGrowth("20% growth expected")
                .build();

        SeatRequestResponse response = quotaService.requestAdditionalSeats(
                testTenant.getId(), adminUser.getId(), request);

        assertNotNull(response.getId());
        assertEquals(5, response.getCurrentSeats());
        assertEquals(10, response.getRequestedAdditionalSeats());
        assertEquals(15, response.getNewTotalSeats());
        assertEquals("PENDING", response.getStatus());
        assertEquals("Team expansion for Q4", response.getJustification());
        assertEquals(adminUser.getEmail(), response.getContactEmail());
    }

    @Test
    @DisplayName("Should reject duplicate pending request for same tenant")
    void shouldRejectDuplicatePendingRequest() {
        SeatRequestRequest request = SeatRequestRequest.builder()
                .additionalSeats(5)
                .justification("Need more seats")
                .build();

        quotaService.requestAdditionalSeats(testTenant.getId(), adminUser.getId(), request);

        assertThrows(IllegalStateException.class, () ->
                quotaService.requestAdditionalSeats(testTenant.getId(), adminUser.getId(), request));
    }

    @Test
    @DisplayName("Should approve seat request and update tenant max seats immediately")
    void shouldApproveRequest() {
        SeatRequestRequest createReq = SeatRequestRequest.builder()
                .additionalSeats(10)
                .justification("Growth")
                .build();
        SeatRequestResponse created = quotaService.requestAdditionalSeats(
                testTenant.getId(), adminUser.getId(), createReq);

        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Admin Tenant")
                .slug("platform-admin-corp")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .build());

        User platformAdmin = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("platform@admin.com")
                .passwordHash("hashed")
                .firstName("Platform")
                .lastName("Admin")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        ApproveRequestRequest approveReq = ApproveRequestRequest.builder()
                .approvedSeats(10)
                .notes("Approved for Q4 growth")
                .build();

        SeatRequestResponse approved = quotaService.approveRequest(
                created.getId(), platformAdmin.getId(), approveReq);

        assertEquals("APPROVED", approved.getStatus());
        assertEquals(10, approved.getRequestedAdditionalSeats());

        Tenant updatedTenant = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertEquals(15, updatedTenant.getMaxSeats());
    }

    @Test
    @DisplayName("Should deny seat request with reason without updating tenant max seats")
    void shouldDenyRequest() {
        SeatRequestRequest createReq = SeatRequestRequest.builder()
                .additionalSeats(100)
                .justification("Big expansion")
                .build();
        SeatRequestResponse created = quotaService.requestAdditionalSeats(
                testTenant.getId(), adminUser.getId(), createReq);

        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform Admin Tenant")
                .slug("platform-admin-corp2")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .subscriptionTier(Tenant.SubscriptionTier.ENTERPRISE)
                .build());

        User platformAdmin = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("platform2@admin.com")
                .passwordHash("hashed")
                .firstName("Platform")
                .lastName("Admin")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        DenyRequestRequest denyReq = DenyRequestRequest.builder()
                .reason("Request exceeds tier limits")
                .build();

        SeatRequestResponse denied = quotaService.denyRequest(
                created.getId(), platformAdmin.getId(), denyReq);

        assertEquals("DENIED", denied.getStatus());
        assertEquals("Request exceeds tier limits", denied.getDenialReason());

        Tenant notUpdated = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertEquals(5, notUpdated.getMaxSeats());
    }

    @Test
    @DisplayName("Should get quota status with utilization details")
    void shouldGetQuotaStatus() {
        QuotaStatusResponse status = quotaService.getQuotaStatus(testTenant.getId());

        assertEquals(testTenant.getId(), status.getTenantId());
        assertEquals(5, status.getMaxSeats());
        assertEquals(1, status.getUsedSeats());
        assertEquals(4, status.getAvailableSeats());
        assertEquals(20.0, status.getUtilizationPercentage());
        assertFalse(status.isQuotaExceeded());
        assertFalse(status.isNearingLimit());
    }

    @Test
    @DisplayName("Should detect nearing limit when utilization reaches 80%")
    void shouldDetectNearingLimit() {
        // Add 3 users so total active is 4 of 5 (80%)
        for (int i = 0; i < 3; i++) {
            userRepository.save(User.builder()
                    .tenantId(testTenant.getId())
                    .userUuid(UUID.randomUUID())
                    .email("user" + i + "@test.com")
                    .passwordHash("hashed")
                    .firstName("User")
                    .lastName(String.valueOf(i))
                    .role(User.UserRole.DEVELOPER)
                    .status(User.UserStatus.ACTIVE)
                    .build());
        }

        QuotaStatusResponse status = quotaService.getQuotaStatus(testTenant.getId());

        assertTrue(status.isNearingLimit());
        assertEquals(80.0, status.getUtilizationPercentage());
    }

    @Test
    @DisplayName("Should list tenant requests and pending requests for platform admin")
    void shouldListTenantRequestsAndPending() {
        SeatRequestRequest request = SeatRequestRequest.builder()
                .additionalSeats(5)
                .justification("Growth")
                .build();

        quotaService.requestAdditionalSeats(testTenant.getId(), adminUser.getId(), request);

        List<SeatRequestResponse> tenantRequests = quotaService.getTenantRequests(testTenant.getId());
        assertEquals(1, tenantRequests.size());

        List<SeatRequestResponse> allPending = quotaService.getAllPendingRequests();
        assertEquals(1, allPending.size());
    }
}
