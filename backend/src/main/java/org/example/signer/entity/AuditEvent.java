package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
@Immutable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_uuid", nullable = false, unique = true, updatable = false)
    private UUID eventUuid;

    @Column(name = "tenant_id", updatable = false)
    private Long tenantId;

    @Column(name = "user_id", updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50, updatable = false)
    private EventType eventType;

    @Column(name = "action", nullable = false, length = 100, updatable = false)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 50, updatable = false)
    private String resourceType;

    @Column(name = "resource_id", length = 255, updatable = false)
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, updatable = false)
    private EventStatus status;

    @Column(name = "ip_address", length = 45, updatable = false)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT", updatable = false)
    private String userAgent;

    @Column(name = "request_id", length = 100, updatable = false)
    private String requestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb", updatable = false)
    private Map<String, Object> metadata;

    @Column(name = "error_message", columnDefinition = "TEXT", updatable = false)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (eventUuid == null) {
            eventUuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = EventStatus.SUCCESS;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        throw new UnsupportedOperationException("Audit events are immutable and cannot be updated");
    }

    public enum EventType {
        AUTH,
        USER_MANAGEMENT,
        TENANT_MANAGEMENT,
        ISO20022_OPERATION,
        CONFIG_CHANGE,
        IMPERSONATION,
        DATA_ACCESS,
        DATA_MODIFICATION
    }

    public enum EventStatus {
        SUCCESS,
        FAILURE,
        PARTIAL
    }
}
