package org.example.signer.dto.impersonation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectImpersonationDto {

    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}
