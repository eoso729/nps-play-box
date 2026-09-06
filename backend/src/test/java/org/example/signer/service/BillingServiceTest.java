package org.example.signer.service;

import org.example.signer.dto.billing.SeatUsageResponse;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.exception.TenantNotFoundException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BillingServiceTest {

    @Autowired
    private BillingService billingService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInvitationRepository invitationRepository;

    @Autowired
    private SeatRequestRepository seatRequestRepository;

    private Tenant tenant1;
    private Tenant tenant2;

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

        tenant1 = tenantRepository.save(Tenant.builder()
                .name("Alpha Bank")
                .slug("alpha-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());

        tenant2 = tenantRepository.save(Tenant.builder()
                .name("Beta Capital")
                .slug("beta-capital")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(25)
                .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                .build());

        // Alpha Bank: 2 active users
        userRepository.save(User.builder()
                .tenantId(tenant1.getId())
                .userUuid(UUID.randomUUID())
                .email("user1@alpha.com")
                .passwordHash("hashed")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        userRepository.save(User.builder()
                .tenantId(tenant1.getId())
                .userUuid(UUID.randomUUID())
                .email("user2@alpha.com")
                .passwordHash("hashed")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .build());

        // Beta Capital: 1 active user, 1 inactive user
        userRepository.save(User.builder()
                .tenantId(tenant2.getId())
                .userUuid(UUID.randomUUID())
                .email("user1@beta.com")
                .passwordHash("hashed")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());

        userRepository.save(User.builder()
                .tenantId(tenant2.getId())
                .userUuid(UUID.randomUUID())
                .email("inactive@beta.com")
                .passwordHash("hashed")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.INACTIVE)
                .build());
    }

    @Test
    @DisplayName("Should return seat usage for all tenants")
    void shouldGetAllTenantsSeatUsage() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        List<SeatUsageResponse> usageList = billingService.getAllTenantsSeatUsage(start, end);

        assertEquals(2, usageList.size());

        SeatUsageResponse alphaUsage = usageList.stream()
                .filter(u -> u.getTenantSlug().equals("alpha-bank"))
                .findFirst()
                .orElseThrow();
        assertEquals(10, alphaUsage.getMaxSeats());
        assertEquals(2, alphaUsage.getUsedSeats());
        assertEquals(10, alphaUsage.getBillableSeats()); // Committed seats model

        SeatUsageResponse betaUsage = usageList.stream()
                .filter(u -> u.getTenantSlug().equals("beta-capital"))
                .findFirst()
                .orElseThrow();
        assertEquals(25, betaUsage.getMaxSeats());
        assertEquals(1, betaUsage.getUsedSeats()); // Only active count as used
        assertEquals(25, betaUsage.getBillableSeats());
    }

    @Test
    @DisplayName("Should return seat usage for a single tenant")
    void shouldGetTenantSeatUsage() {
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        LocalDateTime end = LocalDateTime.now();

        SeatUsageResponse response = billingService.getTenantSeatUsage(tenant1.getId(), start, end);

        assertEquals(tenant1.getId(), response.getTenantId());
        assertEquals("Alpha Bank", response.getTenantName());
        assertEquals("alpha-bank", response.getTenantSlug());
        assertEquals("STANDARD", response.getSubscriptionTier());
        assertEquals(10, response.getMaxSeats());
        assertEquals(2, response.getUsedSeats());
        assertEquals(10, response.getBillableSeats());
    }

    @Test
    @DisplayName("Should throw TenantNotFoundException when tenant does not exist")
    void shouldThrowWhenTenantNotFound() {
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        LocalDateTime end = LocalDateTime.now();

        assertThrows(TenantNotFoundException.class, () ->
                billingService.getTenantSeatUsage(999999L, start, end));
    }
}
