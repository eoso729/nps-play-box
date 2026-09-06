package org.example.signer.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {
    private String eventUuid;
    private Long tenantId;
    private Long userId;
    private String userEmail;
    private String eventType;
    private String action;
    private String resourceType;
    private String resourceId;
    private String status;
    private String ipAddress;
    private String userAgent;
    private String requestId;
    private Map<String, Object> metadata;
    private String errorMessage;
    private LocalDateTime createdAt;
}
