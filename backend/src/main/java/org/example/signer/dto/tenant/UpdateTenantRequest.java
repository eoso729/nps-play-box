package org.example.signer.dto.tenant;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTenantRequest {

    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    private String subscriptionTier;

    private String metadata; // JSON string
}
