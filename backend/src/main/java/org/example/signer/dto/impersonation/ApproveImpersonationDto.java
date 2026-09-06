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
public class ApproveImpersonationDto {

    @NotBlank(message = "Approval reason is required")
    private String approvalReason;
}
