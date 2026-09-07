package org.example.signer.dto.tenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {
    private Long id;
    private String tenantUuid;
    private String name;
    private String slug;
    private String status;
    private Integer maxSeats;
    private Integer usedSeats;
    private String subscriptionTier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String metadata;
    private String invitationToken;
    private String invitationUrl;
}
