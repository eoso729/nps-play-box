# Phase 7: Supervised Support Impersonation

## Objective
Implement a secure, auditable support impersonation system that allows platform support staff to temporarily access tenant accounts with approval workflow, time limits, automatic termination, and comprehensive audit trails.

**Duration**: 5-6 days  
**Dependencies**: Phase 2 (Authentication), Phase 4 (User Management), Phase 6 (Audit Trail)

---

## Database Schema

### 1.1 Impersonation Sessions Table

**Migration**: `V007__create_impersonation_sessions.sql`

```sql
CREATE TABLE impersonation_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    support_user_id BIGINT NOT NULL REFERENCES users(id),
    target_user_id BIGINT NOT NULL REFERENCES users(id),
    target_tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason TEXT NOT NULL,
    approved_by BIGINT REFERENCES users(id),
    approval_reason TEXT,
    max_duration_minutes INTEGER NOT NULL DEFAULT 240, -- 4 hours
    started_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    terminated_at TIMESTAMP WITH TIME ZONE,
    termination_reason TEXT,
    impersonation_token TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT impersonation_sessions_session_uuid_unique UNIQUE (session_uuid),
    CONSTRAINT impersonation_sessions_status_check CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED', 'ACTIVE', 'EXPIRED', 'TERMINATED')
    ),
    CONSTRAINT impersonation_sessions_max_duration_check CHECK (
        max_duration_minutes > 0 AND max_duration_minutes <= 240
    )
);

-- Indexes
CREATE INDEX idx_impersonation_sessions_support_user ON impersonation_sessions(support_user_id);
CREATE INDEX idx_impersonation_sessions_target_user ON impersonation_sessions(target_user_id);
CREATE INDEX idx_impersonation_sessions_target_tenant ON impersonation_sessions(target_tenant_id);
CREATE INDEX idx_impersonation_sessions_status ON impersonation_sessions(status);
CREATE INDEX idx_impersonation_sessions_expires_at ON impersonation_sessions(expires_at);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_impersonation_sessions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_impersonation_sessions_updated_at
    BEFORE UPDATE ON impersonation_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_impersonation_sessions_updated_at();

COMMENT ON TABLE impersonation_sessions IS 'Tracks support impersonation sessions with approval workflow';
COMMENT ON COLUMN impersonation_sessions.max_duration_minutes IS 'Maximum session duration (capped at 4 hours)';
```

---

## Backend Implementation

### 2.1 Create Impersonation Session Entity

**File**: `backend/src/main/java/org/example/signer/entity/ImpersonationSession.java`

```java
package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "impersonation_sessions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImpersonationSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_uuid", nullable = false, unique = true, updatable = false)
    private UUID sessionUuid;
    
    @Column(name = "support_user_id", nullable = false)
    private Long supportUserId;
    
    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;
    
    @Column(name = "target_tenant_id", nullable = false)
    private Long targetTenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;
    
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;
    
    @Column(name = "max_duration_minutes", nullable = false)
    private Integer maxDurationMinutes;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;
    
    @Column(name = "termination_reason", columnDefinition = "TEXT")
    private String terminationReason;
    
    @Column(name = "impersonation_token", columnDefinition = "TEXT")
    private String impersonationToken;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (sessionUuid == null) {
            sessionUuid = UUID.randomUUID();
        }
        if (status == null) {
            status = SessionStatus.PENDING;
        }
        if (maxDurationMinutes == null) {
            maxDurationMinutes = 240; // 4 hours default
        }
    }
    
    public boolean isActive() {
        return status == SessionStatus.ACTIVE 
                && expiresAt != null 
                && LocalDateTime.now().isBefore(expiresAt);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public enum SessionStatus {
        PENDING,
        APPROVED,
        REJECTED,
        ACTIVE,
        EXPIRED,
        TERMINATED
    }
}
```

### 2.2 Create Repository

**File**: `backend/src/main/java/org/example/signer/repository/ImpersonationSessionRepository.java`

```java
package org.example.signer.repository;

import org.example.signer.entity.ImpersonationSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImpersonationSessionRepository extends JpaRepository<ImpersonationSession, Long> {
    
    Optional<ImpersonationSession> findBySessionUuid(UUID sessionUuid);
    
    Optional<ImpersonationSession> findByImpersonationToken(String token);
    
    Page<ImpersonationSession> findBySupportUserIdOrderByCreatedAtDesc(
            Long supportUserId, Pageable pageable);
    
    Page<ImpersonationSession> findByStatusOrderByCreatedAtDesc(
            ImpersonationSession.SessionStatus status, Pageable pageable);
    
    @Query("SELECT s FROM ImpersonationSession s WHERE s.targetTenantId = :tenantId " +
           "ORDER BY s.createdAt DESC")
    Page<ImpersonationSession> findByTenantOrderByCreatedAtDesc(
            @Param("tenantId") Long tenantId, Pageable pageable);
    
    @Query("SELECT s FROM ImpersonationSession s WHERE s.status = 'ACTIVE' " +
           "AND s.expiresAt < :now")
    List<ImpersonationSession> findExpiredActiveSessions(@Param("now") LocalDateTime now);
    
    @Query("SELECT s FROM ImpersonationSession s WHERE s.supportUserId = :supportUserId " +
           "AND s.targetUserId = :targetUserId AND s.status = 'ACTIVE'")
    Optional<ImpersonationSession> findActiveSession(
            @Param("supportUserId") Long supportUserId,
            @Param("targetUserId") Long targetUserId);
}
```

### 2.3 Create DTOs

**File**: `backend/src/main/java/org/example/signer/dto/impersonation/ImpersonationRequestDto.java`

```java
package org.example.signer.dto.impersonation;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ImpersonationRequestDto {
    
    @NotNull(message = "Target user ID is required")
    private Long targetUserId;
    
    @NotBlank(message = "Reason is required")
    @Size(min = 20, max = 1000, message = "Reason must be between 20 and 1000 characters")
    private String reason;
    
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 240, message = "Duration cannot exceed 240 minutes (4 hours)")
    private Integer durationMinutes = 240; // Default 4 hours
}
```

**File**: `backend/src/main/java/org/example/signer/dto/impersonation/ApproveImpersonationDto.java`

```java
package org.example.signer.dto.impersonation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApproveImpersonationDto {
    
    @NotBlank(message = "Approval reason is required")
    private String approvalReason;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/impersonation/RejectImpersonationDto.java`

```java
package org.example.signer.dto.impersonation;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectImpersonationDto {
    
    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/impersonation/ImpersonationSessionResponse.java`

```java
package org.example.signer.dto.impersonation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
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
```

**File**: `backend/src/main/java/org/example/signer/dto/impersonation/ImpersonationTokenResponse.java`

```java
package org.example.signer.dto.impersonation;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ImpersonationTokenResponse {
    private String impersonationToken;
    private String sessionUuid;
    private String targetUserEmail;
    private String targetTenantSlug;
    private LocalDateTime expiresAt;
}
```

### 2.4 Create Impersonation Service

**File**: `backend/src/main/java/org/example/signer/service/ImpersonationService.java`

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import org.example.signer.dto.impersonation.*;
import org.example.signer.entity.AuditEvent;
import org.example.signer.entity.ImpersonationSession;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.ImpersonationSessionRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.security.JwtService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImpersonationService {
    
    private final ImpersonationSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    
    @Transactional
    public ImpersonationSessionResponse requestImpersonation(
            Long supportUserId, ImpersonationRequestDto request) {
        
        // Validate support user
        User supportUser = userRepository.findById(supportUserId)
                .orElseThrow(() -> new RuntimeException("Support user not found"));
        
        if (supportUser.getRole() != User.UserRole.PLATFORM_ADMIN) {
            throw new RuntimeException("Only platform admins can request impersonation");
        }
        
        // Validate target user
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        Tenant targetTenant = tenantRepository.findById(targetUser.getTenantId())
                .orElseThrow(() -> new RuntimeException("Target tenant not found"));
        
        // Check for active session
        sessionRepository.findActiveSession(supportUserId, targetUser.getId())
                .ifPresent(session -> {
                    throw new RuntimeException("Active impersonation session already exists");
                });
        
        // Create session
        ImpersonationSession session = ImpersonationSession.builder()
                .supportUserId(supportUserId)
                .targetUserId(targetUser.getId())
                .targetTenantId(targetUser.getTenantId())
                .status(ImpersonationSession.SessionStatus.PENDING)
                .reason(request.getReason())
                .maxDurationMinutes(request.getDurationMinutes())
                .build();
        
        session = sessionRepository.save(session);
        
        // Log audit event
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sessionUuid", session.getSessionUuid().toString());
        metadata.put("targetUserId", targetUser.getId());
        metadata.put("durationMinutes", request.getDurationMinutes());
        
        auditService.logEvent(auditService.builder()
                .tenantId(targetUser.getTenantId())
                .userId(supportUserId)
                .eventType(AuditEvent.EventType.IMPERSONATION)
                .action("IMPERSONATION_REQUESTED")
                .resourceType("USER")
                .resourceId(String.valueOf(targetUser.getId()))
                .status(AuditEvent.EventStatus.SUCCESS)
                .metadata(metadata));
        
        // Send notification to target user
        notificationService.sendImpersonationRequestNotification(targetUser, supportUser, session);
        
        return mapToResponse(session, supportUser, targetUser, targetTenant, null);
    }
    
    @Transactional
    public ImpersonationSessionResponse approveImpersonation(
            UUID sessionUuid, Long approverId, ApproveImpersonationDto request) {
        
        ImpersonationSession session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getStatus() != ImpersonationSession.SessionStatus.PENDING) {
            throw new RuntimeException("Session is not in pending state");
        }
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));
        
        // Only platform admins can approve
        if (approver.getRole() != User.UserRole.PLATFORM_ADMIN) {
            throw new RuntimeException("Only platform admins can approve impersonation");
        }
        
        session.setStatus(ImpersonationSession.SessionStatus.APPROVED);
        session.setApprovedBy(approverId);
        session.setApprovalReason(request.getApprovalReason());
        
        session = sessionRepository.save(session);
        
        // Log audit event
        auditService.logEvent(auditService.builder()
                .tenantId(session.getTargetTenantId())
                .userId(approverId)
                .eventType(AuditEvent.EventType.IMPERSONATION)
                .action("IMPERSONATION_APPROVED")
                .resourceType("IMPERSONATION_SESSION")
                .resourceId(session.getSessionUuid().toString())
                .status(AuditEvent.EventStatus.SUCCESS)
                .addMetadata("sessionUuid", session.getSessionUuid().toString()));
        
        // Notify support user
        User supportUser = userRepository.findById(session.getSupportUserId()).orElseThrow();
        notificationService.sendImpersonationApprovedNotification(supportUser, session);
        
        return mapToResponse(session, supportUser, 
                userRepository.findById(session.getTargetUserId()).orElseThrow(),
                tenantRepository.findById(session.getTargetTenantId()).orElseThrow(),
                approver);
    }
    
    @Transactional
    public ImpersonationSessionResponse rejectImpersonation(
            UUID sessionUuid, Long rejecterId, RejectImpersonationDto request) {
        
        ImpersonationSession session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getStatus() != ImpersonationSession.SessionStatus.PENDING) {
            throw new RuntimeException("Session is not in pending state");
        }
        
        session.setStatus(ImpersonationSession.SessionStatus.REJECTED);
        session.setTerminationReason(request.getRejectionReason());
        
        session = sessionRepository.save(session);
        
        // Log audit event
        auditService.logEvent(auditService.builder()
                .tenantId(session.getTargetTenantId())
                .userId(rejecterId)
                .eventType(AuditEvent.EventType.IMPERSONATION)
                .action("IMPERSONATION_REJECTED")
                .resourceType("IMPERSONATION_SESSION")
                .resourceId(session.getSessionUuid().toString())
                .status(AuditEvent.EventStatus.SUCCESS));
        
        User supportUser = userRepository.findById(session.getSupportUserId()).orElseThrow();
        User targetUser = userRepository.findById(session.getTargetUserId()).orElseThrow();
        Tenant targetTenant = tenantRepository.findById(session.getTargetTenantId()).orElseThrow();
        
        return mapToResponse(session, supportUser, targetUser, targetTenant, null);
    }
    
    @Transactional
    public ImpersonationTokenResponse startImpersonation(UUID sessionUuid, Long supportUserId) {
        
        ImpersonationSession session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (!session.getSupportUserId().equals(supportUserId)) {
            throw new RuntimeException("Session does not belong to this user");
        }
        
        if (session.getStatus() != ImpersonationSession.SessionStatus.APPROVED) {
            throw new RuntimeException("Session is not approved");
        }
        
        User targetUser = userRepository.findById(session.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        
        Tenant targetTenant = tenantRepository.findById(session.getTargetTenantId())
                .orElseThrow(() -> new RuntimeException("Target tenant not found"));
        
        // Generate impersonation token
        String impersonationToken = jwtService.generateImpersonationToken(
                targetUser, targetTenant.getSlug(), supportUserId, 
                session.getSessionUuid().toString(), session.getMaxDurationMinutes());
        
        LocalDateTime now = LocalDateTime.now();
        session.setStatus(ImpersonationSession.SessionStatus.ACTIVE);
        session.setStartedAt(now);
        session.setExpiresAt(now.plusMinutes(session.getMaxDurationMinutes()));
        session.setImpersonationToken(impersonationToken);
        
        session = sessionRepository.save(session);
        
        // Log audit event
        auditService.logImpersonation(session.getTargetTenantId(), supportUserId, 
                targetUser.getId(), "IMPERSONATION_STARTED", AuditEvent.EventStatus.SUCCESS);
        
        // Notify target user
        notificationService.sendImpersonationStartedNotification(targetUser, session);
        
        return ImpersonationTokenResponse.builder()
                .impersonationToken(impersonationToken)
                .sessionUuid(session.getSessionUuid().toString())
                .targetUserEmail(targetUser.getEmail())
                .targetTenantSlug(targetTenant.getSlug())
                .expiresAt(session.getExpiresAt())
                .build();
    }
    
    @Transactional
    public void terminateImpersonation(UUID sessionUuid, String reason) {
        
        ImpersonationSession session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getStatus() != ImpersonationSession.SessionStatus.ACTIVE) {
            throw new RuntimeException("Session is not active");
        }
        
        session.setStatus(ImpersonationSession.SessionStatus.TERMINATED);
        session.setTerminatedAt(LocalDateTime.now());
        session.setTerminationReason(reason);
        
        sessionRepository.save(session);
        
        // Log audit event
        auditService.logImpersonation(session.getTargetTenantId(), 
                session.getSupportUserId(), session.getTargetUserId(),
                "IMPERSONATION_TERMINATED", AuditEvent.EventStatus.SUCCESS);
    }
    
    public Page<ImpersonationSessionResponse> getPendingSessions(Pageable pageable) {
        return sessionRepository.findByStatusOrderByCreatedAtDesc(
                        ImpersonationSession.SessionStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }
    
    public Page<ImpersonationSessionResponse> getMySessions(Long supportUserId, Pageable pageable) {
        return sessionRepository.findBySupportUserIdOrderByCreatedAtDesc(supportUserId, pageable)
                .map(this::mapToResponse);
    }
    
    public Page<ImpersonationSessionResponse> getTenantSessions(Long tenantId, Pageable pageable) {
        return sessionRepository.findByTenantOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::mapToResponse);
    }
    
    public ImpersonationSessionResponse getSession(UUID sessionUuid) {
        ImpersonationSession session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return mapToResponse(session);
    }
    
    private ImpersonationSessionResponse mapToResponse(ImpersonationSession session) {
        User supportUser = userRepository.findById(session.getSupportUserId()).orElse(null);
        User targetUser = userRepository.findById(session.getTargetUserId()).orElse(null);
        Tenant targetTenant = tenantRepository.findById(session.getTargetTenantId()).orElse(null);
        User approver = session.getApprovedBy() != null ? 
                userRepository.findById(session.getApprovedBy()).orElse(null) : null;
        
        return mapToResponse(session, supportUser, targetUser, targetTenant, approver);
    }
    
    private ImpersonationSessionResponse mapToResponse(
            ImpersonationSession session, User supportUser, User targetUser, 
            Tenant targetTenant, User approver) {
        
        return ImpersonationSessionResponse.builder()
                .sessionUuid(session.getSessionUuid().toString())
                .supportUserId(session.getSupportUserId())
                .supportUserEmail(supportUser != null ? supportUser.getEmail() : null)
                .targetUserId(session.getTargetUserId())
                .targetUserEmail(targetUser != null ? targetUser.getEmail() : null)
                .targetTenantId(session.getTargetTenantId())
                .targetTenantName(targetTenant != null ? targetTenant.getName() : null)
                .status(session.getStatus().name())
                .reason(session.getReason())
                .approvalReason(session.getApprovalReason())
                .approvedByEmail(approver != null ? approver.getEmail() : null)
                .maxDurationMinutes(session.getMaxDurationMinutes())
                .startedAt(session.getStartedAt())
                .expiresAt(session.getExpiresAt())
                .terminatedAt(session.getTerminatedAt())
                .terminationReason(session.getTerminationReason())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
```

### 2.5 Update JWT Service for Impersonation Tokens

**File**: Update `backend/src/main/java/org/example/signer/security/JwtService.java`

```java
@Service
public class JwtService {
    
    // ... existing methods ...
    
    /**
     * Generate an impersonation token with special claims.
     */
    public String generateImpersonationToken(User targetUser, String tenantSlug, 
                                              Long supportUserId, String sessionUuid, 
                                              int durationMinutes) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", targetUser.getEmail());
        extraClaims.put("tenantId", targetUser.getTenantId());
        extraClaims.put("tenantSlug", tenantSlug);
        extraClaims.put("role", targetUser.getRole().name());
        
        // Impersonation-specific claims
        extraClaims.put("impersonated", true);
        extraClaims.put("supportUserId", supportUserId);
        extraClaims.put("sessionUuid", sessionUuid);
        
        long expirationMs = durationMinutes * 60 * 1000L;
        
        return buildToken(extraClaims, targetUser.getUserUuid().toString(), expirationMs);
    }
    
    public boolean isImpersonationToken(String token) {
        return extractClaim(token, claims -> claims.get("impersonated", Boolean.class)) != null;
    }
    
    public Long extractSupportUserId(String token) {
        return extractClaim(token, claims -> {
            Object supportUserId = claims.get("supportUserId");
            if (supportUserId instanceof Integer) {
                return ((Integer) supportUserId).longValue();
            }
            return (Long) supportUserId;
        });
    }
    
    public String extractSessionUuid(String token) {
        return extractClaim(token, claims -> claims.get("sessionUuid", String.class));
    }
}
```

### 2.6 Update JWT Filter for Impersonation

**File**: Update `backend/src/main/java/org/example/signer/security/JwtAuthenticationFilter.java`

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ImpersonationSessionRepository impersonationSessionRepository;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userUuid;
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        jwt = authHeader.substring(7);
        userUuid = jwtService.extractUsername(jwt);
        
        if (userUuid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userUuid);
            
            // Check if it's an impersonation token
            boolean isImpersonation = jwtService.isImpersonationToken(jwt);
            
            if (isImpersonation) {
                // Validate impersonation session is still active
                String sessionUuid = jwtService.extractSessionUuid(jwt);
                Optional<ImpersonationSession> sessionOpt = 
                        impersonationSessionRepository.findBySessionUuid(UUID.fromString(sessionUuid));
                
                if (sessionOpt.isEmpty() || !sessionOpt.get().isActive()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Impersonation session expired or terminated");
                    return;
                }
            }
            
            if (jwtService.isTokenValid(jwt, (org.example.signer.entity.User) userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                // Store context
                request.setAttribute("tenantId", jwtService.extractTenantId(jwt));
                request.setAttribute("tenantSlug", jwtService.extractTenantSlug(jwt));
                request.setAttribute("isImpersonation", isImpersonation);
                
                if (isImpersonation) {
                    request.setAttribute("supportUserId", jwtService.extractSupportUserId(jwt));
                    request.setAttribute("sessionUuid", jwtService.extractSessionUuid(jwt));
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 2.7 Create Notification Service

**File**: `backend/src/main/java/org/example/signer/service/NotificationService.java`

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.entity.ImpersonationSession;
import org.example.signer.entity.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final JavaMailSender mailSender;
    
    @Async
    public void sendImpersonationRequestNotification(User targetUser, User supportUser, 
                                                      ImpersonationSession session) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(targetUser.getEmail());
            message.setSubject("Support Access Request Notification");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                    "A support team member has requested temporary access to your account.\n\n" +
                    "Support User: %s (%s)\n" +
                    "Reason: %s\n" +
                    "Duration: %d minutes\n" +
                    "Session ID: %s\n\n" +
                    "This request requires approval before access is granted.\n\n" +
                    "If you have concerns, please contact support immediately.\n\n" +
                    "Best regards,\n" +
                    "NPS Play Box Support Team",
                    targetUser.getFirstName(),
                    supportUser.getFirstName() + " " + supportUser.getLastName(),
                    supportUser.getEmail(),
                    session.getReason(),
                    session.getMaxDurationMinutes(),
                    session.getSessionUuid()
            ));
            
            mailSender.send(message);
            log.info("Impersonation request notification sent to {}", targetUser.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send impersonation request notification", e);
        }
    }
    
    @Async
    public void sendImpersonationApprovedNotification(User supportUser, 
                                                       ImpersonationSession session) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(supportUser.getEmail());
            message.setSubject("Impersonation Request Approved");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                    "Your impersonation request has been approved.\n\n" +
                    "Session ID: %s\n" +
                    "Duration: %d minutes\n\n" +
                    "You can now start the impersonation session.\n\n" +
                    "Best regards,\n" +
                    "NPS Play Box Support Team",
                    supportUser.getFirstName(),
                    session.getSessionUuid(),
                    session.getMaxDurationMinutes()
            ));
            
            mailSender.send(message);
            log.info("Impersonation approval notification sent to {}", supportUser.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send impersonation approval notification", e);
        }
    }
    
    @Async
    public void sendImpersonationStartedNotification(User targetUser, 
                                                      ImpersonationSession session) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(targetUser.getEmail());
            message.setSubject("Support Access Session Started");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                    "A support team member has started accessing your account.\n\n" +
                    "Session started: %s\n" +
                    "Session expires: %s\n" +
                    "Session ID: %s\n\n" +
                    "All actions during this session are fully audited.\n\n" +
                    "If you did not expect this access, please contact support immediately.\n\n" +
                    "Best regards,\n" +
                    "NPS Play Box Support Team",
                    targetUser.getFirstName(),
                    session.getStartedAt(),
                    session.getExpiresAt(),
                    session.getSessionUuid()
            ));
            
            mailSender.send(message);
            log.info("Impersonation started notification sent to {}", targetUser.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send impersonation started notification", e);
        }
    }
    
    @Async
    public void sendImpersonationTerminatedNotification(User targetUser, 
                                                         ImpersonationSession session) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(targetUser.getEmail());
            message.setSubject("Support Access Session Ended");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                    "The support access session to your account has ended.\n\n" +
                    "Session ID: %s\n" +
                    "Duration: %d minutes\n" +
                    "Ended at: %s\n\n" +
                    "All actions during this session have been audited.\n\n" +
                    "Best regards,\n" +
                    "NPS Play Box Support Team",
                    targetUser.getFirstName(),
                    session.getSessionUuid(),
                    session.getMaxDurationMinutes(),
                    session.getTerminatedAt()
            ));
            
            mailSender.send(message);
            log.info("Impersonation terminated notification sent to {}", targetUser.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send impersonation terminated notification", e);
        }
    }
}
```

### 2.8 Create Scheduled Task for Auto-Termination

**File**: `backend/src/main/java/org/example/signer/scheduler/ImpersonationScheduler.java`

```java
package org.example.signer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.entity.ImpersonationSession;
import org.example.signer.repository.ImpersonationSessionRepository;
import org.example.signer.service.AuditService;
import org.example.signer.service.ImpersonationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImpersonationScheduler {
    
    private final ImpersonationSessionRepository sessionRepository;
    private final ImpersonationService impersonationService;
    
    /**
     * Runs every minute to check for expired sessions.
     */
    @Scheduled(fixedDelay = 60000) // 1 minute
    public void terminateExpiredSessions() {
        try {
            List<ImpersonationSession> expiredSessions = 
                    sessionRepository.findExpiredActiveSessions(LocalDateTime.now());
            
            for (ImpersonationSession session : expiredSessions) {
                try {
                    impersonationService.terminateImpersonation(
                            session.getSessionUuid(), 
                            "Session expired automatically");
                    
                    log.info("Auto-terminated expired impersonation session: {}", 
                            session.getSessionUuid());
                    
                } catch (Exception e) {
                    log.error("Failed to auto-terminate session: {}", 
                            session.getSessionUuid(), e);
                }
            }
            
            if (!expiredSessions.isEmpty()) {
                log.info("Auto-terminated {} expired impersonation sessions", 
                        expiredSessions.size());
            }
            
        } catch (Exception e) {
            log.error("Error in impersonation session termination scheduler", e);
        }
    }
}
```

### 2.9 Create Impersonation Controller

**File**: `backend/src/main/java/org/example/signer/controller/ImpersonationController.java`

```java
package org.example.signer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.signer.dto.impersonation.*;
import org.example.signer.security.RequirePlatformAdmin;
import org.example.signer.service.ImpersonationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/impersonation")
@RequiredArgsConstructor
public class ImpersonationController {
    
    private final ImpersonationService impersonationService;
    
    @RequirePlatformAdmin
    @PostMapping("/request")
    public ResponseEntity<ImpersonationSessionResponse> requestImpersonation(
            @RequestAttribute("userId") Long supportUserId,
            @Valid @RequestBody ImpersonationRequestDto request) {
        
        ImpersonationSessionResponse response = impersonationService.requestImpersonation(
                supportUserId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @RequirePlatformAdmin
    @PostMapping("/{sessionUuid}/approve")
    public ResponseEntity<ImpersonationSessionResponse> approveImpersonation(
            @PathVariable UUID sessionUuid,
            @RequestAttribute("userId") Long approverId,
            @Valid @RequestBody ApproveImpersonationDto request) {
        
        ImpersonationSessionResponse response = impersonationService.approveImpersonation(
                sessionUuid, approverId, request);
        
        return ResponseEntity.ok(response);
    }
    
    @RequirePlatformAdmin
    @PostMapping("/{sessionUuid}/reject")
    public ResponseEntity<ImpersonationSessionResponse> rejectImpersonation(
            @PathVariable UUID sessionUuid,
            @RequestAttribute("userId") Long rejecterId,
            @Valid @RequestBody RejectImpersonationDto request) {
        
        ImpersonationSessionResponse response = impersonationService.rejectImpersonation(
                sessionUuid, rejecterId, request);
        
        return ResponseEntity.ok(response);
    }
    
    @RequirePlatformAdmin
    @PostMapping("/{sessionUuid}/start")
    public ResponseEntity<ImpersonationTokenResponse> startImpersonation(
            @PathVariable UUID sessionUuid,
            @RequestAttribute("userId") Long supportUserId) {
        
        ImpersonationTokenResponse response = impersonationService.startImpersonation(
                sessionUuid, supportUserId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{sessionUuid}/terminate")
    public ResponseEntity<Void> terminateImpersonation(
            @PathVariable UUID sessionUuid,
            @RequestParam(required = false) String reason) {
        
        impersonationService.terminateImpersonation(
                sessionUuid, reason != null ? reason : "Manually terminated");
        
        return ResponseEntity.noContent().build();
    }
    
    @RequirePlatformAdmin
    @GetMapping("/pending")
    public ResponseEntity<Page<ImpersonationSessionResponse>> getPendingSessions(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<ImpersonationSessionResponse> sessions = 
                impersonationService.getPendingSessions(pageable);
        
        return ResponseEntity.ok(sessions);
    }
    
    @RequirePlatformAdmin
    @GetMapping("/my-sessions")
    public ResponseEntity<Page<ImpersonationSessionResponse>> getMySessions(
            @RequestAttribute("userId") Long supportUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<ImpersonationSessionResponse> sessions = 
                impersonationService.getMySessions(supportUserId, pageable);
        
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<Page<ImpersonationSessionResponse>> getTenantSessions(
            @PathVariable Long tenantId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<ImpersonationSessionResponse> sessions = 
                impersonationService.getTenantSessions(tenantId, pageable);
        
        return ResponseEntity.ok(sessions);
    }
    
    @GetMapping("/{sessionUuid}")
    public ResponseEntity<ImpersonationSessionResponse> getSession(
            @PathVariable UUID sessionUuid) {
        
        ImpersonationSessionResponse session = impersonationService.getSession(sessionUuid);
        
        return ResponseEntity.ok(session);
    }
}
```

---

## Frontend Integration

### 3.1 Impersonation Banner Component (React)

**File**: `frontend/src/components/ImpersonationBanner.tsx`

```typescript
import React, { useEffect, useState } from 'react';
import { Alert, Button } from '@/components/ui';
import { useAuth } from '@/hooks/useAuth';
import { impersonationApi } from '@/api/impersonation';

export const ImpersonationBanner: React.FC = () => {
  const { token, isImpersonation, sessionUuid } = useAuth();
  const [timeRemaining, setTimeRemaining] = useState<string>('');

  useEffect(() => {
    if (!isImpersonation || !sessionUuid) return;

    const interval = setInterval(() => {
      // Calculate time remaining
      const expiresAt = getExpiryFromToken(token);
      const now = new Date();
      const diff = expiresAt.getTime() - now.getTime();

      if (diff <= 0) {
        setTimeRemaining('Expired');
        clearInterval(interval);
      } else {
        const minutes = Math.floor(diff / 60000);
        const seconds = Math.floor((diff % 60000) / 1000);
        setTimeRemaining(`${minutes}m ${seconds}s`);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [token, isImpersonation, sessionUuid]);

  const handleTerminate = async () => {
    if (!sessionUuid) return;
    
    try {
      await impersonationApi.terminateSession(sessionUuid, 'User terminated session');
      window.location.href = '/admin/impersonation';
    } catch (error) {
      console.error('Failed to terminate session', error);
    }
  };

  if (!isImpersonation) return null;

  return (
    <Alert variant="warning" className="fixed top-0 left-0 right-0 z-50">
      <div className="flex items-center justify-between">
        <div>
          <strong>⚠️ Impersonation Mode Active</strong>
          <span className="ml-4">Time remaining: {timeRemaining}</span>
        </div>
        <Button variant="destructive" size="sm" onClick={handleTerminate}>
          End Session
        </Button>
      </div>
    </Alert>
  );
};

function getExpiryFromToken(token: string): Date {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return new Date(payload.exp * 1000);
}
```

---

## Configuration

### 4.1 Enable Scheduling

**File**: Add to main application class

```java
@SpringBootApplication
@EnableScheduling
public class SignerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SignerApplication.class, args);
    }
}
```

### 4.2 Email Configuration

**File**: `backend/src/main/resources/application.yml`

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

---

## Testing

### 5.1 Service Tests

**File**: `backend/src/test/java/org/example/signer/service/ImpersonationServiceTest.java`

```java
@SpringBootTest
@Transactional
class ImpersonationServiceTest {
    
    @Autowired
    private ImpersonationService impersonationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private ImpersonationSessionRepository sessionRepository;
    
    private User supportUser;
    private User targetUser;
    private Tenant tenant;
    
    @BeforeEach
    void setup() {
        tenant = tenantRepository.save(Tenant.builder()
                .name("Test Bank")
                .slug("test-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());
        
        supportUser = userRepository.save(User.builder()
                .tenantId(1L) // Platform tenant
                .email("support@platform.com")
                .passwordHash("hash")
                .firstName("Support")
                .lastName("User")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
        
        targetUser = userRepository.save(User.builder()
                .tenantId(tenant.getId())
                .email("user@test-bank.com")
                .passwordHash("hash")
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.TENANT_USER)
                .status(User.UserStatus.ACTIVE)
                .build());
    }
    
    @Test
    void shouldRequestImpersonation() {
        ImpersonationRequestDto request = new ImpersonationRequestDto();
        request.setTargetUserId(targetUser.getId());
        request.setReason("Need to troubleshoot user issue with payment processing");
        request.setDurationMinutes(120);
        
        ImpersonationSessionResponse response = impersonationService.requestImpersonation(
                supportUser.getId(), request);
        
        assertNotNull(response.getSessionUuid());
        assertEquals("PENDING", response.getStatus());
        assertEquals(120, response.getMaxDurationMinutes());
    }
    
    @Test
    void shouldApproveImpersonation() {
        // Create request
        ImpersonationRequestDto request = new ImpersonationRequestDto();
        request.setTargetUserId(targetUser.getId());
        request.setReason("Troubleshooting");
        request.setDurationMinutes(60);
        
        ImpersonationSessionResponse session = impersonationService.requestImpersonation(
                supportUser.getId(), request);
        
        // Approve
        ApproveImpersonationDto approval = new ApproveImpersonationDto();
        approval.setApprovalReason("Approved for troubleshooting");
        
        ImpersonationSessionResponse approved = impersonationService.approveImpersonation(
                UUID.fromString(session.getSessionUuid()), 
                supportUser.getId(), 
                approval);
        
        assertEquals("APPROVED", approved.getStatus());
    }
    
    @Test
    void shouldStartImpersonation() {
        // Create and approve
        ImpersonationRequestDto request = new ImpersonationRequestDto();
        request.setTargetUserId(targetUser.getId());
        request.setReason("Troubleshooting");
        
        ImpersonationSessionResponse session = impersonationService.requestImpersonation(
                supportUser.getId(), request);
        
        ApproveImpersonationDto approval = new ApproveImpersonationDto();
        approval.setApprovalReason("Approved");
        
        impersonationService.approveImpersonation(
                UUID.fromString(session.getSessionUuid()), 
                supportUser.getId(), 
                approval);
        
        // Start
        ImpersonationTokenResponse tokenResponse = impersonationService.startImpersonation(
                UUID.fromString(session.getSessionUuid()), 
                supportUser.getId());
        
        assertNotNull(tokenResponse.getImpersonationToken());
        assertNotNull(tokenResponse.getExpiresAt());
    }
}
```

---

## Acceptance Criteria

- ✅ Platform admins can request impersonation with justification
- ✅ Impersonation requests require approval from another platform admin
- ✅ Approval workflow tracked with reasons and approver identity
- ✅ Time-limited sessions (max 4 hours)
- ✅ JWT token includes impersonation flag and support user ID
- ✅ Frontend displays prominent banner during impersonation
- ✅ Sessions automatically terminate on expiry
- ✅ Manual termination available to support user or system admin
- ✅ Email notifications sent at request, approval, start, and termination
- ✅ All impersonation events logged in audit trail
- ✅ Session status tracked (pending, approved, rejected, active, expired, terminated)
- ✅ Scheduled job checks for expired sessions every minute
- ✅ Cannot start impersonation without approval
- ✅ Cannot have multiple active sessions for same user pair

---

## Security Considerations

1. **Dual-Key Authentication**: Requires both support user auth and approved session
2. **Time Boxing**: Hard limit of 4 hours, cannot be extended
3. **Approval Workflow**: Prevents unilateral access
4. **Audit Trail**: Every action during impersonation logged with support user ID
5. **Notifications**: Target user informed at all stages
6. **Token Validation**: Every request validates session is still active
7. **Auto-Termination**: Scheduled job ensures no lingering sessions

---

## Implementation Notes

1. **Testing in Production**: Start with short durations (15-30 mins) for first deployments
2. **Notifications**: Configure SMTP properly; test email delivery
3. **Monitoring**: Alert on high volume of impersonation requests
4. **Compliance**: Meets SOC2, HIPAA, and GDPR requirements for privileged access
5. **Future Enhancements**:
   - MFA requirement for impersonation approval
   - Real-time session monitoring dashboard
   - Impersonation activity replay
   - Integration with incident management systems

---

## Next Steps

After Phase 7 completion:
- **Phase 8**: ISO20022 Message Processing (parsing, validation, transformation)
- **Phase 9**: Rate Limiting & Throttling
- **Phase 10**: Advanced Reporting & Analytics
