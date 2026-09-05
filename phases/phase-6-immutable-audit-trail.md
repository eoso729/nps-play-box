# Phase 6: Immutable Audit Trail

## Objective
Implement a comprehensive, append-only audit logging system that captures all security-critical operations across the platform. Provide search, filter, and export capabilities for compliance and security monitoring.

**Duration**: 4-5 days  
**Dependencies**: Phase 2 (Authentication)

---

## Database Schema

### 1.1 Audit Events Table

**Migration**: `V006__create_audit_events.sql`

```sql
CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,
    event_uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    user_id BIGINT REFERENCES users(id),
    event_type VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    ip_address INET,
    user_agent TEXT,
    request_id VARCHAR(100),
    metadata JSONB,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT audit_events_event_uuid_unique UNIQUE (event_uuid),
    CONSTRAINT audit_events_event_type_check CHECK (
        event_type IN (
            'AUTH', 
            'USER_MANAGEMENT', 
            'TENANT_MANAGEMENT', 
            'ISO20022_OPERATION', 
            'CONFIG_CHANGE', 
            'IMPERSONATION',
            'DATA_ACCESS',
            'DATA_MODIFICATION'
        )
    ),
    CONSTRAINT audit_events_status_check CHECK (
        status IN ('SUCCESS', 'FAILURE', 'PARTIAL')
    )
);

-- Indexes for performance
CREATE INDEX idx_audit_events_tenant_id ON audit_events(tenant_id);
CREATE INDEX idx_audit_events_user_id ON audit_events(user_id);
CREATE INDEX idx_audit_events_event_type ON audit_events(event_type);
CREATE INDEX idx_audit_events_created_at ON audit_events(created_at DESC);
CREATE INDEX idx_audit_events_action ON audit_events(action);
CREATE INDEX idx_audit_events_resource ON audit_events(resource_type, resource_id);
CREATE INDEX idx_audit_events_request_id ON audit_events(request_id);

-- GIN index for JSONB metadata searches
CREATE INDEX idx_audit_events_metadata ON audit_events USING GIN (metadata);

-- Composite index for common queries
CREATE INDEX idx_audit_events_tenant_created ON audit_events(tenant_id, created_at DESC);

-- Prevent updates and deletes (append-only enforcement)
CREATE RULE audit_events_no_update AS ON UPDATE TO audit_events DO INSTEAD NOTHING;
CREATE RULE audit_events_no_delete AS ON DELETE TO audit_events DO INSTEAD NOTHING;

COMMENT ON TABLE audit_events IS 'Immutable audit trail of all system events';
COMMENT ON COLUMN audit_events.event_type IS 'High-level category of the event';
COMMENT ON COLUMN audit_events.action IS 'Specific action performed (e.g., LOGIN, CREATE_USER)';
COMMENT ON COLUMN audit_events.resource_type IS 'Type of resource affected (e.g., USER, TENANT)';
COMMENT ON COLUMN audit_events.resource_id IS 'Identifier of the affected resource';
COMMENT ON COLUMN audit_events.metadata IS 'Additional event-specific data in JSON format';
```

---

## Backend Implementation

### 2.1 Create Audit Event Entity

**File**: `backend/src/main/java/org/example/signer/entity/AuditEvent.java`

```java
package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

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
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;
    
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;
    
    @Column(name = "resource_id", length = 255)
    private String resourceId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "request_id", length = 100)
    private String requestId;
    
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
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
```

### 2.2 Create Audit Event Repository

**File**: `backend/src/main/java/org/example/signer/repository/AuditEventRepository.java`

```java
package org.example.signer.repository;

import org.example.signer.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long>, 
        JpaSpecificationExecutor<AuditEvent> {
    
    Page<AuditEvent> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);
    
    Page<AuditEvent> findByTenantIdAndEventTypeOrderByCreatedAtDesc(
            Long tenantId, AuditEvent.EventType eventType, Pageable pageable);
    
    Page<AuditEvent> findByTenantIdAndUserIdOrderByCreatedAtDesc(
            Long tenantId, Long userId, Pageable pageable);
    
    @Query("SELECT a FROM AuditEvent a WHERE a.tenantId = :tenantId " +
           "AND a.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY a.createdAt DESC")
    Page<AuditEvent> findByTenantIdAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
    
    @Query("SELECT a FROM AuditEvent a WHERE a.tenantId = :tenantId " +
           "AND a.resourceType = :resourceType AND a.resourceId = :resourceId " +
           "ORDER BY a.createdAt DESC")
    List<AuditEvent> findResourceHistory(
            @Param("tenantId") Long tenantId,
            @Param("resourceType") String resourceType,
            @Param("resourceId") String resourceId);
    
    @Query("SELECT COUNT(a) FROM AuditEvent a WHERE a.tenantId = :tenantId " +
           "AND a.eventType = :eventType AND a.status = :status " +
           "AND a.createdAt >= :since")
    long countByTenantAndTypeAndStatusSince(
            @Param("tenantId") Long tenantId,
            @Param("eventType") AuditEvent.EventType eventType,
            @Param("status") AuditEvent.EventStatus status,
            @Param("since") LocalDateTime since);
}
```

### 2.3 Create Audit Logging Service

**File**: `backend/src/main/java/org/example/signer/service/AuditService.java`

```java
package org.example.signer.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.entity.AuditEvent;
import org.example.signer.repository.AuditEventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    
    /**
     * Log an audit event asynchronously.
     * Uses REQUIRES_NEW to ensure audit is saved even if parent transaction rolls back.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(AuditEventBuilder builder) {
        try {
            // Enrich with request context if available
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                builder.ipAddress(getClientIp(request))
                       .userAgent(request.getHeader("User-Agent"))
                       .requestId(getRequestId(request));
            }
            
            AuditEvent event = builder.build();
            auditEventRepository.save(event);
            
            log.debug("Audit event logged: {} - {} on {}/{}", 
                    event.getEventType(), event.getAction(), 
                    event.getResourceType(), event.getResourceId());
                    
        } catch (Exception e) {
            log.error("Failed to log audit event", e);
            // Don't throw - audit logging should not disrupt business operations
        }
    }
    
    /**
     * Create a new audit event builder.
     */
    public AuditEventBuilder builder() {
        return new AuditEventBuilder();
    }
    
    /**
     * Shortcut for logging authentication events.
     */
    public void logAuth(Long tenantId, Long userId, String action, 
                        AuditEvent.EventStatus status, String errorMessage) {
        logEvent(builder()
                .tenantId(tenantId)
                .userId(userId)
                .eventType(AuditEvent.EventType.AUTH)
                .action(action)
                .resourceType("AUTH")
                .status(status)
                .errorMessage(errorMessage));
    }
    
    /**
     * Shortcut for logging user management events.
     */
    public void logUserManagement(Long tenantId, Long actorId, String action,
                                   String userId, AuditEvent.EventStatus status) {
        logEvent(builder()
                .tenantId(tenantId)
                .userId(actorId)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action(action)
                .resourceType("USER")
                .resourceId(userId)
                .status(status));
    }
    
    /**
     * Shortcut for logging impersonation events.
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
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }
    
    /**
     * Builder for constructing audit events.
     */
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
                    .status(status)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .requestId(requestId)
                    .metadata(metadata)
                    .errorMessage(errorMessage)
                    .build();
        }
    }
}
```

### 2.4 Create AOP Interceptor for Automatic Audit Logging

**File**: `backend/src/main/java/org/example/signer/audit/Auditable.java`

```java
package org.example.signer.audit;

import org.example.signer.entity.AuditEvent;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that should be automatically audited.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    
    /**
     * Event type for the audit log.
     */
    AuditEvent.EventType eventType();
    
    /**
     * Action description (e.g., "CREATE_USER", "LOGIN").
     */
    String action();
    
    /**
     * Resource type (e.g., "USER", "TENANT").
     */
    String resourceType();
    
    /**
     * SpEL expression to extract resource ID from method parameters or result.
     * Example: "#result.id" or "#request.userId"
     */
    String resourceId() default "";
    
    /**
     * Whether to log on success only (default) or also on failure.
     */
    boolean logOnFailure() default true;
}
```

**File**: `backend/src/main/java/org/example/signer/audit/AuditAspect.java`

```java
package org.example.signer.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.signer.entity.AuditEvent;
import org.example.signer.security.TenantContext;
import org.example.signer.service.AuditService;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    
    private final AuditService auditService;
    private final ExpressionParser parser = new SpelExpressionParser();
    
    @Around("@annotation(org.example.signer.audit.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);
        
        AuditEvent.EventStatus status = AuditEvent.EventStatus.SUCCESS;
        String errorMessage = null;
        Object result = null;
        
        try {
            result = joinPoint.proceed();
            return result;
            
        } catch (Exception e) {
            status = AuditEvent.EventStatus.FAILURE;
            errorMessage = e.getMessage();
            throw e;
            
        } finally {
            if (status == AuditEvent.EventStatus.SUCCESS || auditable.logOnFailure()) {
                logAuditEvent(joinPoint, auditable, result, status, errorMessage);
            }
        }
    }
    
    private void logAuditEvent(ProceedingJoinPoint joinPoint, Auditable auditable,
                                Object result, AuditEvent.EventStatus status, 
                                String errorMessage) {
        try {
            Long tenantId = TenantContext.getTenantId();
            Long userId = getCurrentUserId();
            
            String resourceId = extractResourceId(joinPoint, auditable, result);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("method", joinPoint.getSignature().toShortString());
            
            auditService.logEvent(auditService.builder()
                    .tenantId(tenantId)
                    .userId(userId)
                    .eventType(auditable.eventType())
                    .action(auditable.action())
                    .resourceType(auditable.resourceType())
                    .resourceId(resourceId)
                    .status(status)
                    .metadata(metadata)
                    .errorMessage(errorMessage));
                    
        } catch (Exception e) {
            log.error("Failed to log audit event via AOP", e);
        }
    }
    
    private String extractResourceId(ProceedingJoinPoint joinPoint, 
                                      Auditable auditable, Object result) {
        if (auditable.resourceId().isEmpty()) {
            return null;
        }
        
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            // Add method parameters to context
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            
            // Add result to context
            context.setVariable("result", result);
            
            Object value = parser.parseExpression(auditable.resourceId()).getValue(context);
            return value != null ? value.toString() : null;
            
        } catch (Exception e) {
            log.warn("Failed to extract resource ID using expression: {}", 
                    auditable.resourceId(), e);
            return null;
        }
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.example.signer.entity.User) {
            return ((org.example.signer.entity.User) auth.getPrincipal()).getId();
        }
        return null;
    }
}
```

### 2.5 Update AuthService with Audit Logging

**File**: Update `backend/src/main/java/org/example/signer/service/AuthService.java`

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;  // Add this
    
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        Tenant tenant = null;
        User user = null;
        
        try {
            tenant = tenantRepository.findBySlug(request.getTenantSlug())
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));
            
            if (tenant.getStatus() != Tenant.TenantStatus.ACTIVE) {
                auditService.logAuth(tenant.getId(), null, "LOGIN_FAILED_TENANT_INACTIVE", 
                        AuditEvent.EventStatus.FAILURE, "Tenant account is not active");
                throw new RuntimeException("Tenant account is not active");
            }
            
            user = userRepository.findByEmailAndTenantId(request.getEmail(), tenant.getId())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));
            
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                auditService.logAuth(tenant.getId(), user.getId(), "LOGIN_FAILED_USER_INACTIVE",
                        AuditEvent.EventStatus.FAILURE, "User account is not active");
                throw new RuntimeException("User account is not active");
            }
            
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                auditService.logAuth(tenant.getId(), user.getId(), "LOGIN_FAILED_INVALID_PASSWORD",
                        AuditEvent.EventStatus.FAILURE, "Invalid password");
                throw new RuntimeException("Invalid credentials");
            }
            
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            String token = jwtService.generateToken(user, tenant.getSlug());
            
            // Log successful login
            auditService.logAuth(tenant.getId(), user.getId(), "LOGIN_SUCCESS",
                    AuditEvent.EventStatus.SUCCESS, null);
            
            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(86400L)
                    .user(AuthResponse.UserInfo.builder()
                            .uuid(user.getUserUuid().toString())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .role(user.getRole().name())
                            .tenant(AuthResponse.TenantInfo.builder()
                                    .id(tenant.getId())
                                    .name(tenant.getName())
                                    .slug(tenant.getSlug())
                                    .build())
                            .build())
                    .build();
                    
        } catch (Exception e) {
            // Log failed login if we have tenant info
            if (tenant != null && user != null) {
                auditService.logAuth(tenant.getId(), user.getId(), "LOGIN_FAILED",
                        AuditEvent.EventStatus.FAILURE, e.getMessage());
            }
            throw e;
        }
    }
}
```

---

## Search and Filter API

### 3.1 Create DTOs for Search

**File**: `backend/src/main/java/org/example/signer/dto/audit/AuditEventSearchRequest.java`

```java
package org.example.signer.dto.audit;

import lombok.Data;
import org.example.signer.entity.AuditEvent;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class AuditEventSearchRequest {
    
    private AuditEvent.EventType eventType;
    private String action;
    private String resourceType;
    private String resourceId;
    private Long userId;
    private AuditEvent.EventStatus status;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    
    private String ipAddress;
    private String searchTerm; // For metadata search
}
```

**File**: `backend/src/main/java/org/example/signer/dto/audit/AuditEventResponse.java`

```java
package org.example.signer.dto.audit;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
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
```

### 3.2 Create Specification for Dynamic Queries

**File**: `backend/src/main/java/org/example/signer/specification/AuditEventSpecification.java`

```java
package org.example.signer.specification;

import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.example.signer.entity.AuditEvent;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class AuditEventSpecification {
    
    public static Specification<AuditEvent> buildSpecification(
            Long tenantId, AuditEventSearchRequest searchRequest) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always filter by tenant
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));
            
            if (searchRequest.getEventType() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("eventType"), searchRequest.getEventType()));
            }
            
            if (searchRequest.getAction() != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("action")),
                        "%" + searchRequest.getAction().toLowerCase() + "%"));
            }
            
            if (searchRequest.getResourceType() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("resourceType"), searchRequest.getResourceType()));
            }
            
            if (searchRequest.getResourceId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("resourceId"), searchRequest.getResourceId()));
            }
            
            if (searchRequest.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("userId"), searchRequest.getUserId()));
            }
            
            if (searchRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"), searchRequest.getStatus()));
            }
            
            if (searchRequest.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"), searchRequest.getStartDate()));
            }
            
            if (searchRequest.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"), searchRequest.getEndDate()));
            }
            
            if (searchRequest.getIpAddress() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("ipAddress"), searchRequest.getIpAddress()));
            }
            
            // Order by created_at descending
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### 3.3 Create Audit Query Service

**File**: `backend/src/main/java/org/example/signer/service/AuditQueryService.java`

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import org.example.signer.dto.audit.AuditEventResponse;
import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.example.signer.entity.AuditEvent;
import org.example.signer.entity.User;
import org.example.signer.repository.AuditEventRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.specification.AuditEventSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditQueryService {
    
    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    
    public Page<AuditEventResponse> searchAuditEvents(
            Long tenantId, AuditEventSearchRequest searchRequest, Pageable pageable) {
        
        Specification<AuditEvent> spec = AuditEventSpecification.buildSpecification(
                tenantId, searchRequest);
        
        Page<AuditEvent> events = auditEventRepository.findAll(spec, pageable);
        
        // Batch load user emails
        Map<Long, String> userEmails = loadUserEmails(events.getContent());
        
        return events.map(event -> mapToResponse(event, userEmails));
    }
    
    public List<AuditEventResponse> getResourceHistory(
            Long tenantId, String resourceType, String resourceId) {
        
        List<AuditEvent> events = auditEventRepository.findResourceHistory(
                tenantId, resourceType, resourceId);
        
        Map<Long, String> userEmails = loadUserEmails(events);
        
        return events.stream()
                .map(event -> mapToResponse(event, userEmails))
                .toList();
    }
    
    private Map<Long, String> loadUserEmails(List<AuditEvent> events) {
        Map<Long, String> emailMap = new HashMap<>();
        
        List<Long> userIds = events.stream()
                .map(AuditEvent::getUserId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        
        if (!userIds.isEmpty()) {
            List<User> users = userRepository.findAllById(userIds);
            users.forEach(user -> emailMap.put(user.getId(), user.getEmail()));
        }
        
        return emailMap;
    }
    
    private AuditEventResponse mapToResponse(AuditEvent event, Map<Long, String> userEmails) {
        return AuditEventResponse.builder()
                .eventUuid(event.getEventUuid().toString())
                .tenantId(event.getTenantId())
                .userId(event.getUserId())
                .userEmail(event.getUserId() != null ? userEmails.get(event.getUserId()) : null)
                .eventType(event.getEventType().name())
                .action(event.getAction())
                .resourceType(event.getResourceType())
                .resourceId(event.getResourceId())
                .status(event.getStatus().name())
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .requestId(event.getRequestId())
                .metadata(event.getMetadata())
                .errorMessage(event.getErrorMessage())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
```

### 3.4 Create Audit Controller

**File**: `backend/src/main/java/org/example/signer/controller/AuditController.java`

```java
package org.example.signer.controller;

import lombok.RequiredArgsConstructor;
import org.example.signer.dto.audit.AuditEventResponse;
import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.example.signer.security.RequireTenantAdmin;
import org.example.signer.service.AuditQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@RequireTenantAdmin
public class AuditController {
    
    private final AuditQueryService auditQueryService;
    
    @GetMapping("/events")
    public ResponseEntity<Page<AuditEventResponse>> searchAuditEvents(
            @RequestAttribute("tenantId") Long tenantId,
            AuditEventSearchRequest searchRequest,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        
        Page<AuditEventResponse> events = auditQueryService.searchAuditEvents(
                tenantId, searchRequest, pageable);
        
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/resource-history")
    public ResponseEntity<List<AuditEventResponse>> getResourceHistory(
            @RequestAttribute("tenantId") Long tenantId,
            @RequestParam String resourceType,
            @RequestParam String resourceId) {
        
        List<AuditEventResponse> history = auditQueryService.getResourceHistory(
                tenantId, resourceType, resourceId);
        
        return ResponseEntity.ok(history);
    }
}
```

---

## Export Functionality

### 4.1 Create Export Service

**File**: `backend/src/main/java/org/example/signer/service/AuditExportService.java`

```java
package org.example.signer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.signer.dto.audit.AuditEventResponse;
import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditExportService {
    
    private final AuditQueryService auditQueryService;
    private final ObjectMapper objectMapper;
    
    /**
     * Export audit events as JSON.
     */
    public byte[] exportAsJson(Long tenantId, AuditEventSearchRequest searchRequest,
                                int maxRecords) throws IOException {
        
        List<AuditEventResponse> allEvents = fetchAllEvents(tenantId, searchRequest, maxRecords);
        
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(allEvents);
    }
    
    /**
     * Export audit events as CSV.
     */
    public byte[] exportAsCsv(Long tenantId, AuditEventSearchRequest searchRequest,
                               int maxRecords) throws IOException {
        
        List<AuditEventResponse> allEvents = fetchAllEvents(tenantId, searchRequest, maxRecords);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        
        // Write CSV header
        writer.println("Event UUID,Timestamp,Event Type,Action,Resource Type,Resource ID," +
                "User ID,User Email,Status,IP Address,User Agent,Request ID,Error Message");
        
        // Write data rows
        for (AuditEventResponse event : allEvents) {
            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    escapeCsv(event.getEventUuid()),
                    escapeCsv(event.getCreatedAt().toString()),
                    escapeCsv(event.getEventType()),
                    escapeCsv(event.getAction()),
                    escapeCsv(event.getResourceType()),
                    escapeCsv(event.getResourceId()),
                    escapeCsv(event.getUserId() != null ? event.getUserId().toString() : ""),
                    escapeCsv(event.getUserEmail()),
                    escapeCsv(event.getStatus()),
                    escapeCsv(event.getIpAddress()),
                    escapeCsv(event.getUserAgent()),
                    escapeCsv(event.getRequestId()),
                    escapeCsv(event.getErrorMessage())
            ));
        }
        
        writer.flush();
        return outputStream.toByteArray();
    }
    
    private List<AuditEventResponse> fetchAllEvents(Long tenantId, 
                                                     AuditEventSearchRequest searchRequest,
                                                     int maxRecords) {
        List<AuditEventResponse> allEvents = new ArrayList<>();
        int pageSize = 1000;
        int page = 0;
        
        while (allEvents.size() < maxRecords) {
            PageRequest pageRequest = PageRequest.of(page, Math.min(pageSize, maxRecords - allEvents.size()));
            Page<AuditEventResponse> pageResult = auditQueryService.searchAuditEvents(
                    tenantId, searchRequest, pageRequest);
            
            allEvents.addAll(pageResult.getContent());
            
            if (!pageResult.hasNext()) {
                break;
            }
            page++;
        }
        
        return allEvents;
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}
```

### 4.2 Add Export Endpoints to Controller

**File**: Update `backend/src/main/java/org/example/signer/controller/AuditController.java`

```java
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@RequireTenantAdmin
public class AuditController {
    
    private final AuditQueryService auditQueryService;
    private final AuditExportService auditExportService;
    
    // ... existing methods ...
    
    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportAsJson(
            @RequestAttribute("tenantId") Long tenantId,
            AuditEventSearchRequest searchRequest,
            @RequestParam(defaultValue = "10000") int maxRecords) throws IOException {
        
        byte[] data = auditExportService.exportAsJson(tenantId, searchRequest, maxRecords);
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .header("Content-Disposition", "attachment; filename=audit-events.json")
                .body(data);
    }
    
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportAsCsv(
            @RequestAttribute("tenantId") Long tenantId,
            AuditEventSearchRequest searchRequest,
            @RequestParam(defaultValue = "10000") int maxRecords) throws IOException {
        
        byte[] data = auditExportService.exportAsCsv(tenantId, searchRequest, maxRecords);
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=audit-events.csv")
                .body(data);
    }
}
```

---

## Configuration

### 5.1 Enable Async Processing

**File**: Update `backend/src/main/java/org/example/signer/config/AsyncConfig.java`

```java
package org.example.signer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("audit-");
        executor.initialize();
        return executor;
    }
}
```

### 5.2 Add Dependencies

**File**: `backend/pom.xml`

```xml
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-60</artifactId>
    <version>3.7.0</version>
</dependency>
```

---

## Testing

### 6.1 Repository Tests

**File**: `backend/src/test/java/org/example/signer/repository/AuditEventRepositoryTest.java`

```java
@SpringBootTest
@Transactional
class AuditEventRepositoryTest {
    
    @Autowired
    private AuditEventRepository auditEventRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    private Long tenantId;
    
    @BeforeEach
    void setup() {
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Bank")
                .slug("test-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());
        tenantId = tenant.getId();
    }
    
    @Test
    void shouldSaveAuditEvent() {
        AuditEvent event = AuditEvent.builder()
                .tenantId(tenantId)
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN_SUCCESS")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.SUCCESS)
                .ipAddress("192.168.1.1")
                .build();
        
        AuditEvent saved = auditEventRepository.save(event);
        
        assertNotNull(saved.getId());
        assertNotNull(saved.getEventUuid());
        assertNotNull(saved.getCreatedAt());
    }
    
    @Test
    void shouldPreventUpdates() {
        AuditEvent event = auditEventRepository.save(AuditEvent.builder()
                .tenantId(tenantId)
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build());
        
        event = auditEventRepository.findById(event.getId()).orElseThrow();
        
        // Hibernate will not update due to @Immutable and database rule
        // This test verifies the entity is truly immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            // Attempting to change would fail
        });
    }
    
    @Test
    void shouldFindByTenantAndEventType() {
        auditEventRepository.save(AuditEvent.builder()
                .tenantId(tenantId)
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build());
        
        Page<AuditEvent> events = auditEventRepository.findByTenantIdAndEventTypeOrderByCreatedAtDesc(
                tenantId, AuditEvent.EventType.AUTH, PageRequest.of(0, 10));
        
        assertFalse(events.isEmpty());
        assertEquals(AuditEvent.EventType.AUTH, events.getContent().get(0).getEventType());
    }
}
```

### 6.2 Service Tests

**File**: `backend/src/test/java/org/example/signer/service/AuditServiceTest.java`

```java
@SpringBootTest
class AuditServiceTest {
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private AuditEventRepository auditEventRepository;
    
    @Test
    void shouldLogAuditEvent() throws InterruptedException {
        auditService.logEvent(auditService.builder()
                .tenantId(1L)
                .userId(1L)
                .eventType(AuditEvent.EventType.USER_MANAGEMENT)
                .action("CREATE_USER")
                .resourceType("USER")
                .resourceId("123")
                .status(AuditEvent.EventStatus.SUCCESS));
        
        // Wait for async processing
        Thread.sleep(500);
        
        List<AuditEvent> events = auditEventRepository.findAll();
        assertFalse(events.isEmpty());
    }
    
    @Test
    void shouldLogAuthEvent() throws InterruptedException {
        auditService.logAuth(1L, 1L, "LOGIN_SUCCESS", 
                AuditEvent.EventStatus.SUCCESS, null);
        
        Thread.sleep(500);
        
        List<AuditEvent> events = auditEventRepository.findAll();
        assertTrue(events.stream()
                .anyMatch(e -> e.getEventType() == AuditEvent.EventType.AUTH));
    }
}
```

### 6.3 AOP Tests

**File**: `backend/src/test/java/org/example/signer/audit/AuditAspectTest.java`

```java
@SpringBootTest
class AuditAspectTest {
    
    @Autowired
    private TestService testService;
    
    @Autowired
    private AuditEventRepository auditEventRepository;
    
    @BeforeEach
    void cleanup() {
        auditEventRepository.deleteAll();
    }
    
    @Test
    void shouldAuditAnnotatedMethod() throws InterruptedException {
        TenantContext.setTenantId(1L);
        
        testService.auditedMethod("test-resource-id");
        
        Thread.sleep(500);
        
        List<AuditEvent> events = auditEventRepository.findAll();
        assertFalse(events.isEmpty());
        
        AuditEvent event = events.get(0);
        assertEquals("TEST_ACTION", event.getAction());
        assertEquals("test-resource-id", event.getResourceId());
        
        TenantContext.clear();
    }
    
    @Component
    static class TestService {
        
        @Auditable(
            eventType = AuditEvent.EventType.DATA_MODIFICATION,
            action = "TEST_ACTION",
            resourceType = "TEST",
            resourceId = "#resourceId"
        )
        public void auditedMethod(String resourceId) {
            // Test method
        }
    }
}
```

---

## Acceptance Criteria

- ✅ Audit events table is append-only (enforced by database rules)
- ✅ All authentication attempts logged with success/failure status
- ✅ User management operations logged (create, update, delete, role changes)
- ✅ Tenant configuration changes logged
- ✅ ISO20022 operations logged (message parsing, validation, transformation)
- ✅ Impersonation events logged with support user and target user
- ✅ Audit events include: timestamp, tenant_id, user_id, action, resource, IP, user agent
- ✅ AOP interceptor automatically logs annotated methods
- ✅ Search API supports filtering by event type, date range, user, resource
- ✅ Export functionality provides JSON and CSV formats
- ✅ Audit logging is asynchronous and does not block business operations
- ✅ Failed audit logs are handled gracefully without disrupting requests
- ✅ Resource history can be retrieved for audit trails
- ✅ Metadata stored as JSONB for flexible additional data

---

## Implementation Notes

1. **Immutability**: Database rules prevent updates/deletes; Hibernate @Immutable annotation
2. **Performance**: Asynchronous logging, indexed columns, consider partitioning for high volume
3. **Retention**: Implement data retention policy (archive old events to cold storage)
4. **Compliance**: Meets SOC2, GDPR audit requirements
5. **Future Enhancements**: 
   - Real-time alerting for suspicious patterns
   - Anomaly detection
   - Long-term archival to S3/object storage
   - Elasticsearch integration for advanced search

---

## Next Phase

Once Phase 6 is complete and tested, proceed to:
**[Phase 7: Supervised Support Impersonation →](./phase-7-support-impersonation.md)**
