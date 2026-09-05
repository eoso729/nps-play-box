package org.example.signer.dto.quota;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DenyRequestRequest {

    @NotBlank(message = "Denial reason is required")
    private String reason;
}
