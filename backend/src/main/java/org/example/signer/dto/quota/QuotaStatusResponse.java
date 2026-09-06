package org.example.signer.dto.quota;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotaStatusResponse {
    private Long tenantId;
    private String tenantName;
    private Integer maxSeats;
    private Integer usedSeats;
    private Integer availableSeats;
    private Integer activeUsers;
    private Integer inactiveUsers;
    private Integer pendingInvitations;
    private Double utilizationPercentage;
    private String subscriptionTier;
    private boolean quotaExceeded;
    private boolean nearingLimit;
    private LocalDateTime lastUpdated;
}
