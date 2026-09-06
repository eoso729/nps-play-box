package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support user not found"));

        if (supportUser.getRole() != User.UserRole.PLATFORM_ADMIN) {
            throw new AccessDeniedException("Only platform admins can request impersonation");
        }

        // Validate target user
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        Tenant targetTenant = tenantRepository.findById(targetUser.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target tenant not found"));

        // Check for active session
        sessionRepository.findActiveSession(supportUserId, targetUser.getId())
                .ifPresent(session -> {
                    throw new IllegalStateException("Active impersonation session already exists for this user");
                });

        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : 240;

        // Create session
        ImpersonationSession session = ImpersonationSession.builder()
                .supportUserId(supportUserId)
                .targetUserId(targetUser.getId())
                .targetTenantId(targetUser.getTenantId())
                .status(ImpersonationSession.SessionStatus.PENDING)
                .reason(request.getReason())
                .maxDurationMinutes(duration)
                .build();

        session = sessionRepository.save(session);

        // Log audit event
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sessionUuid", session.getSessionUuid().toString());
        metadata.put("targetUserId", targetUser.getId());
        metadata.put("durationMinutes", duration);
        metadata.put("reason", request.getReason());

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Impersonation session not found: " + sessionUuid));

        if (session.getStatus() != ImpersonationSession.SessionStatus.PENDING) {
            throw new IllegalStateException("Session is not in pending state: " + session.getStatus());
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approver not found: " + approverId));

        // Only platform admins can approve
        if (approver.getRole() != User.UserRole.PLATFORM_ADMIN) {
            throw new AccessDeniedException("Only platform admins can approve impersonation");
        }

        // Dual authorization check: cannot approve own request
        if (session.getSupportUserId().equals(approverId)) {
            throw new IllegalArgumentException("Dual authorization required: cannot approve own impersonation request");
        }

        session.setStatus(ImpersonationSession.SessionStatus.APPROVED);
        session.setApprovedBy(approverId);
        session.setApprovalReason(request.getApprovalReason());

        session = sessionRepository.save(session);

        // Log audit event
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sessionUuid", session.getSessionUuid().toString());
        metadata.put("approvalReason", request.getApprovalReason());

        auditService.logEvent(auditService.builder()
                .tenantId(session.getTargetTenantId())
                .userId(approverId)
                .eventType(AuditEvent.EventType.IMPERSONATION)
                .action("IMPERSONATION_APPROVED")
                .resourceType("IMPERSONATION_SESSION")
                .resourceId(session.getSessionUuid().toString())
                .status(AuditEvent.EventStatus.SUCCESS)
                .metadata(metadata));

        // Notify support user
        User supportUser = userRepository.findById(session.getSupportUserId()).orElse(null);
        if (supportUser != null) {
            notificationService.sendImpersonationApprovedNotification(supportUser, session);
        }

        User targetUser = userRepository.findById(session.getTargetUserId()).orElse(null);
        Tenant targetTenant = tenantRepository.findById(session.getTargetTenantId()).orElse(null);

        return mapToResponse(session, supportUser, targetUser, targetTenant, approver);
    }

    @Transactional
    public ImpersonationSessionResponse rejectImpersonation(
            UUID sessionUuid, Long rejecterId, RejectImpersonationDto request) {

        ImpersonationSession session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Impersonation session not found: " + sessionUuid));

        if (session.getStatus() != ImpersonationSession.SessionStatus.PENDING) {
            throw new IllegalStateException("Session is not in pending state: " + session.getStatus());
        }

        User rejecter = userRepository.findById(rejecterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rejecter not found: " + rejecterId));

        if (rejecter.getRole() != User.UserRole.PLATFORM_ADMIN) {
            throw new AccessDeniedException("Only platform admins can reject impersonation");
        }

        session.setStatus(ImpersonationSession.SessionStatus.REJECTED);
        session.setTerminationReason(request.getRejectionReason());

        session = sessionRepository.save(session);

        // Log audit event
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sessionUuid", session.getSessionUuid().toString());
        metadata.put("rejectionReason", request.getRejectionReason());

        auditService.logEvent(auditService.builder()
                .tenantId(session.getTargetTenantId())
                .userId(rejecterId)
                .eventType(AuditEvent.EventType.IMPERSONATION)
                .action("IMPERSONATION_REJECTED")
                .resourceType("IMPERSONATION_SESSION")
                .resourceId(session.getSessionUuid().toString())
                .status(AuditEvent.EventStatus.SUCCESS)
                .metadata(metadata));

        User supportUser = userRepository.findById(session.getSupportUserId()).orElse(null);
        User targetUser = userRepository.findById(session.getTargetUserId()).orElse(null);
        Tenant targetTenant = tenantRepository.findById(session.getTargetTenantId()).orElse(null);

        return mapToResponse(session, supportUser, targetUser, targetTenant, null);
    }

    @Transactional
    public ImpersonationTokenResponse startImpersonation(UUID sessionUuid, Long supportUserId) {

        ImpersonationSession session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Impersonation session not found: " + sessionUuid));

        if (!session.getSupportUserId().equals(supportUserId)) {
            throw new AccessDeniedException("Session does not belong to this user");
        }

        if (session.getStatus() != ImpersonationSession.SessionStatus.APPROVED) {
            throw new IllegalStateException("Session is not approved. Current status: " + session.getStatus());
        }

        User targetUser = userRepository.findById(session.getTargetUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        Tenant targetTenant = tenantRepository.findById(session.getTargetTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target tenant not found"));

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Impersonation session not found: " + sessionUuid));

        if (session.getStatus() != ImpersonationSession.SessionStatus.ACTIVE) {
            throw new IllegalStateException("Session is not active. Current status: " + session.getStatus());
        }

        session.setStatus(ImpersonationSession.SessionStatus.TERMINATED);
        session.setTerminatedAt(LocalDateTime.now());
        session.setTerminationReason(reason);

        session = sessionRepository.save(session);

        // Log audit event
        auditService.logImpersonation(session.getTargetTenantId(),
                session.getSupportUserId(), session.getTargetUserId(),
                "IMPERSONATION_TERMINATED", AuditEvent.EventStatus.SUCCESS);

        // Notify target user
        User targetUser = userRepository.findById(session.getTargetUserId()).orElse(null);
        if (targetUser != null) {
            notificationService.sendImpersonationTerminatedNotification(targetUser, session);
        }
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
        return sessionRepository.findByTargetTenantIdOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::mapToResponse);
    }

    public ImpersonationSessionResponse getSession(UUID sessionUuid) {
        ImpersonationSession session = sessionRepository.findBySessionUuid(sessionUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Impersonation session not found: " + sessionUuid));
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
