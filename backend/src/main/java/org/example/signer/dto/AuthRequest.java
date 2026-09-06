package org.example.signer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for authentication supporting both v1 tenant-scoped logins
 * and legacy username/email credentials.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {

    private String email;

    private String emailOrUsername;

    @NotBlank(message = "Password is required")
    private String password;

    private String tenantSlug;

    public String getEmail() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return emailOrUsername;
    }
}
