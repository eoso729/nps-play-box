package org.example.signer.dto.impersonation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationTokenResponse {
    private String impersonationToken;
    private String sessionUuid;
    private String targetUserEmail;
    private String targetTenantSlug;
    private LocalDateTime expiresAt;
}
