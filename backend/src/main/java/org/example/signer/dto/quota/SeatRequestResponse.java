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
public class SeatRequestResponse {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Integer currentSeats;
    private Integer requestedAdditionalSeats;
    private Integer newTotalSeats;
    private String justification;
    private String expectedGrowth;
    private String contactEmail;
    private String status; // PENDING, APPROVED, DENIED
    private String requestedByName;
    private String requestedByEmail;
    private LocalDateTime requestedAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String denialReason;
    private String notes;
}
