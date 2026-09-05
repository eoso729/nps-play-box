package org.example.signer.dto.quota;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatAvailabilityResponse {
    private Boolean available;
    private Integer maxSeats;
    private Integer usedSeats;
    private Integer availableSeats;
    private Integer pendingInvitations;
    private Integer requestedSeats;
    private String message;
}
