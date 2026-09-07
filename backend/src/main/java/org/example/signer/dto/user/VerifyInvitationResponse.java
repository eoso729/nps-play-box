package org.example.signer.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyInvitationResponse {
    private String token;
    private String email;
    private String role;
    private Long tenantId;
    private String tenantName;
    private String tenantSlug;
    private boolean valid;
    private String message;
    private LocalDateTime expiresAt;
}
