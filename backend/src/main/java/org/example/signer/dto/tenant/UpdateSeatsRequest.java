package org.example.signer.dto.tenant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSeatsRequest {

    @NotNull(message = "Max seats is required")
    @Min(value = 1, message = "At least 1 seat is required")
    private Integer maxSeats;
}
