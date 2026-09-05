package org.example.signer.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.entity.AuditEvent;
import org.example.signer.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * Log an audit event.
     * Uses REQUIRES_NEW via TransactionTemplate to ensure audit record is saved independently of parent transaction.
     */
    public void logEvent(AuditEventBuilder builder) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (!StringUtils.hasText(builder.ipAddress)) {
                    builder.ipAddress(getClientIp(request));
                }
                if (!StringUtils.hasText(builder.userAgent)) {
                    builder.userAgent(request.getHeader("User-Agent"));
                }
                if (!StringUtils.hasText(builder.requestId)) {
                    builder.requestId(getRequestId(request));
                }
            }

            if (!StringUtils.hasText(builder.requestId)) {
                builder.requestId(UUID.randomUUID().toString());
            }

            AuditEvent event = builder.build();

            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            template.execute(status -> {
                auditEventRepository.save(event);
                return null;
            });

            log.debug("Audit event saved: type={}, action={}, tenantId={}, resource={}/{}",
                    event.getEventType(), event.getAction(), event.getTenantId(),
                    event.getResourceType(), event.getResourceId());

        } catch (Exception e) {
            log.error("Failed to persist audit event: {}", e.getMessage(), e);
            // Fail-safe: never bubble up audit failures to interrupt business operations
        }
    }

    public AuditEventBuilder builder() {
        return new AuditEventBuilder();
    }

    /**
     * Helper for logging authentication events.
     */
    public void logAuth(Long tenantId, Long userId, String action,
                        AuditEvent.EventStatus status, String errorMessage) {
        logEvent(builder()
                .tenantId(tenantId)
                .userId(userId)
                .eventType(AuditEvent.EventType.AUTH)
                .action(action)
                .resourceType("AUTH")
                .resourceId(userId != null ? String.valueOf(userId) : null)
                .status(status)
                .errorMessage(errorMessage));
    }

    /**
     * Helper for logging user management events.
     */
    public void logUserManagement(Long tenantId, Long actorId, String action,
                                  String resourceId, AuditEvent.EventStatus status,
                                  Map<String, Object> metadata) {
        logEvent(builder()
                .tenantId(tenantId)
                .userId(actorId)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action(action)
                .resourceType("USER")
                .resourceId(resourceId)
                .status(status)
                .metadata(metadata));
    }

    /**
     * Helper for logging tenant management events.
     */
    public void logTenantManagement(Long tenantId, Long actorId, String action,
                                    String resourceId, AuditEvent.EventStatus status,
                                    Map<String, Object> metadata) {
        logEvent(builder()
                .tenantId(tenantId)
                .userId(actorId)
                .eventType(AuditEvent.EventType.TENANT_MANAGEMENT)
                .action(action)
                .resourceType("TENANT")
                .resourceId(resourceId)
                .status(status)
                .metadata(metadata));
    }

    /**
     * Helper for logging quota & seat events.
     */
    public void logQuotaEvent(Long tenantId, Long actorId, String action,
                              String resourceId, AuditEvent.EventStatus status,
                              Map<String, Object> metadata) {
        logEvent(builder()
                .tenantId(tenantId)
                .userId(actorId)
                .eventType(AuditEvent.EventType.CONFIG_CHANGE)
                .action(action)
                .resourceType("SEAT_QUOTA")
                .resourceId(resourceId)
                .status(status)
                .metadata(metadata));
    }

    /**
     * Helper for logging impersonation events.
     */
    public void logImpersonation(Long tenantId, Long supportUserId, Long targetUserId,
                                 String action, AuditEvent.EventStatus status) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("targetUserId", targetUserId);

        logEvent(builder()
                .tenantId(tenantId)
                .userId(supportUserId)
                .eventType(AuditEvent.EventType.IMPERSONATION)
                .action(action)
                .resourceType("USER")
                .resourceId(String.valueOf(targetUserId))
                .status(status)
                .metadata(metadata));
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip.trim())) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (StringUtils.hasText(requestId)) {
            return requestId.trim();
        }
        return UUID.randomUUID().toString();
    }

    public static class AuditEventBuilder {
        private Long tenantId;
        private Long userId;
        private AuditEvent.EventType eventType;
        private String action;
        private String resourceType;
        private String resourceId;
        private AuditEvent.EventStatus status = AuditEvent.EventStatus.SUCCESS;
        private String ipAddress;
        private String userAgent;
        private String requestId;
        private Map<String, Object> metadata;
        private String errorMessage;

        public AuditEventBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public AuditEventBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AuditEventBuilder eventType(AuditEvent.EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public AuditEventBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditEventBuilder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public AuditEventBuilder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public AuditEventBuilder status(AuditEvent.EventStatus status) {
            this.status = status;
            return this;
        }

        public AuditEventBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public AuditEventBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AuditEventBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public AuditEventBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public AuditEventBuilder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }

        public AuditEventBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public AuditEvent build() {
            return AuditEvent.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .eventType(eventType)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .status(status != null ? status : AuditEvent.EventStatus.SUCCESS)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .requestId(requestId)
                    .metadata(metadata)
                    .errorMessage(errorMessage)
                    .build();
        }
    }
}
