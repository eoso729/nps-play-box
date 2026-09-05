package org.example.signer.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatUsageResponse {
    private Long tenantId;
    private String tenantName;
    private String tenantSlug;
    private String subscriptionTier;
    private Integer maxSeats;
    private Integer usedSeats;
    private Integer billableSeats;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private List<DailyUsage> dailyUsage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyUsage {
        private LocalDateTime date;
        private Integer activeSeats;
        private Integer peakSeats;
    }
}
