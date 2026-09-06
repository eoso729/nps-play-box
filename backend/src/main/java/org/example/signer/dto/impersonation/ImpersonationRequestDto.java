package org.example.signer.dto.impersonation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationRequestDto {

    @NotNull(message = "Target user ID is required")
    private Long targetUserId;

    @NotBlank(message = "Reason is required")
    @Size(min = 20, max = 1000, message = "Reason must be between 20 and 1000 characters")
    private String reason;

    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 240, message = "Duration cannot exceed 240 minutes (4 hours)")
    @Builder.Default
    private Integer durationMinutes = 240;
}
