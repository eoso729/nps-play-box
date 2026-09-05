package org.example.signer.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatUtilizationResponse {
    private Integer maxSeats;
    private Integer usedSeats;
    private Integer availableSeats;
    private Integer activeUsers;
    private Integer inactiveUsers;
    private Integer pendingInvitations;
    private Double utilizationPercentage;
}
