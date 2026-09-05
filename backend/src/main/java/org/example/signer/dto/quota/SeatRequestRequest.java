package org.example.signer.dto.quota;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequestRequest {

    @NotNull(message = "Additional seats count is required")
    @Min(value = 1, message = "Must request at least 1 additional seat")
    private Integer additionalSeats;

    @NotBlank(message = "Business justification is required")
    private String justification;

    private String expectedGrowth;

    private String contactEmail;
}
