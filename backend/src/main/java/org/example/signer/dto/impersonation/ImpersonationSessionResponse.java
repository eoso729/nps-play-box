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
public class ImpersonationSessionResponse {
    private String sessionUuid;
    private Long supportUserId;
    private String supportUserEmail;
    private Long targetUserId;
    private String targetUserEmail;
    private Long targetTenantId;
    private String targetTenantName;
    private String status;
    private String reason;
    private String approvalReason;
    private String approvedByEmail;
    private Integer maxDurationMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime terminatedAt;
    private String terminationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
