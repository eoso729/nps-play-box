package org.example.signer.dto.quota;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRequestRequest {

    @Min(value = 1, message = "Must approve at least 1 seat")
    private Integer approvedSeats; // Can be different from requested

    private String notes;
}
