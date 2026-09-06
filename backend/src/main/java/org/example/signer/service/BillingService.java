package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.billing.SeatUsageResponse;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.exception.TenantNotFoundException;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    /**
     * Get seat usage for all tenants (for billing purposes)
     */
    @Transactional(readOnly = true)
    public List<SeatUsageResponse> getAllTenantsSeatUsage(
            LocalDateTime periodStart, LocalDateTime periodEnd) {

        List<Tenant> tenants = tenantRepository.findAll();
        List<SeatUsageResponse> usageList = new ArrayList<>();

        for (Tenant tenant : tenants) {
            SeatUsageResponse usage = getTenantSeatUsage(
                    tenant.getId(), periodStart, periodEnd);
            usageList.add(usage);
        }

        return usageList;
    }

    /**
     * Get seat usage for specific tenant
     */
    @Transactional(readOnly = true)
    public SeatUsageResponse getTenantSeatUsage(
            Long tenantId, LocalDateTime periodStart, LocalDateTime periodEnd) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        long activeSeats = userRepository.countByTenantIdAndStatus(
                tenantId, User.UserStatus.ACTIVE);

        // Committed seats model: billableSeats = tenant.getMaxSeats()
        int billableSeats = tenant.getMaxSeats();

        List<SeatUsageResponse.DailyUsage> dailyUsage = new ArrayList<>();

        return SeatUsageResponse.builder()
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .tenantSlug(tenant.getSlug())
                .subscriptionTier(tenant.getSubscriptionTier() != null ? tenant.getSubscriptionTier().name() : "STANDARD")
                .maxSeats(tenant.getMaxSeats())
                .usedSeats((int) activeSeats)
                .billableSeats(billableSeats)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .dailyUsage(dailyUsage)
                .build();
    }
}
