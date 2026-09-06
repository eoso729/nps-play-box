package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.signer.dto.impersonation.*;
import org.example.signer.entity.User;
import org.example.signer.security.RequirePlatformAdmin;
import org.example.signer.security.TenantUserDetails;
import org.example.signer.service.ImpersonationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Support Impersonation", description = "Endpoints for managing supervised support impersonation sessions")
@RestController
@RequestMapping("/api/v1/impersonation")
@RequiredArgsConstructor
public class ImpersonationController {

    private final ImpersonationService impersonationService;

    @Operation(summary = "Request support impersonation", description = "Platform administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Impersonation session requested"),
            @ApiResponse(responseCode = "400", description = "Invalid request or active session exists"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequirePlatformAdmin
    @PostMapping("/request")
    public ResponseEntity<ImpersonationSessionResponse> requestImpersonation(
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            @Valid @RequestBody ImpersonationRequestDto request,
            Authentication authentication) {

        Long supportUserId = resolveUserId(requestUserId, authentication);
        ImpersonationSessionResponse response = impersonationService.requestImpersonation(
                supportUserId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Approve impersonation request", description = "Dual authorization: must be approved by a different platform admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Impersonation session approved"),
            @ApiResponse(responseCode = "400", description = "Invalid session state or self-approval attempt"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @RequirePlatformAdmin
    @PostMapping("/{sessionUuid}/approve")
    public ResponseEntity<ImpersonationSessionResponse> approveImpersonation(
            @PathVariable UUID sessionUuid,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            @Valid @RequestBody ApproveImpersonationDto request,
            Authentication authentication) {

        Long approverId = resolveUserId(requestUserId, authentication);
        ImpersonationSessionResponse response = impersonationService.approveImpersonation(
                sessionUuid, approverId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reject impersonation request", description = "Platform administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Impersonation session rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid session state"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @RequirePlatformAdmin
    @PostMapping("/{sessionUuid}/reject")
    public ResponseEntity<ImpersonationSessionResponse> rejectImpersonation(
            @PathVariable UUID sessionUuid,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            @Valid @RequestBody RejectImpersonationDto request,
            Authentication authentication) {

        Long rejecterId = resolveUserId(requestUserId, authentication);
        ImpersonationSessionResponse response = impersonationService.rejectImpersonation(
                sessionUuid, rejecterId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Start impersonation session and obtain temporary JWT", description = "Requesting platform support user only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Impersonation session started, token returned"),
            @ApiResponse(responseCode = "400", description = "Session not approved"),
            @ApiResponse(responseCode = "403", description = "Access denied or session belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @RequirePlatformAdmin
    @PostMapping("/{sessionUuid}/start")
    public ResponseEntity<ImpersonationTokenResponse> startImpersonation(
            @PathVariable UUID sessionUuid,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            Authentication authentication) {

        Long supportUserId = resolveUserId(requestUserId, authentication);
        ImpersonationTokenResponse response = impersonationService.startImpersonation(
                sessionUuid, supportUserId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Terminate active impersonation session", description = "Support user, tenant admin, or platform admin")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session terminated successfully"),
            @ApiResponse(responseCode = "400", description = "Session is not active"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping("/{sessionUuid}/terminate")
    public ResponseEntity<Void> terminateImpersonation(
            @PathVariable UUID sessionUuid,
            @RequestParam(required = false) String reason) {

        impersonationService.terminateImpersonation(
                sessionUuid, reason != null ? reason : "Manually terminated");

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List pending impersonation requests", description = "Platform administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequirePlatformAdmin
    @GetMapping("/pending")
    public ResponseEntity<Page<ImpersonationSessionResponse>> getPendingSessions(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ImpersonationSessionResponse> sessions =
                impersonationService.getPendingSessions(pageable);

        return ResponseEntity.ok(sessions);
    }

    @Operation(summary = "List sessions requested by the authenticated support user", description = "Platform administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "My sessions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequirePlatformAdmin
    @GetMapping("/my-sessions")
    public ResponseEntity<Page<ImpersonationSessionResponse>> getMySessions(
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        Long supportUserId = resolveUserId(requestUserId, authentication);
        Page<ImpersonationSessionResponse> sessions =
                impersonationService.getMySessions(supportUserId, pageable);

        return ResponseEntity.ok(sessions);
    }

    @Operation(summary = "List impersonation sessions for a tenant", description = "Platform administrators or tenant administrators for their own tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant sessions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<Page<ImpersonationSessionResponse>> getTenantSessions(
            @PathVariable Long tenantId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {

        boolean isPlatformAdmin = hasRole(authentication, User.UserRole.PLATFORM_ADMIN);
        Long callerTenantId = resolveCallerTenantId(authentication);

        if (!isPlatformAdmin && (callerTenantId == null || !callerTenantId.equals(tenantId))) {
            throw new AccessDeniedException("Cannot view impersonation sessions of another tenant");
        }

        Page<ImpersonationSessionResponse> sessions =
                impersonationService.getTenantSessions(tenantId, pageable);

        return ResponseEntity.ok(sessions);
    }

    @Operation(summary = "Get impersonation session details by UUID", description = "Platform administrator, session support user, or tenant administrator")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Session details retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @GetMapping("/{sessionUuid}")
    public ResponseEntity<ImpersonationSessionResponse> getSession(
            @PathVariable UUID sessionUuid,
            Authentication authentication) {

        ImpersonationSessionResponse session = impersonationService.getSession(sessionUuid);

        boolean isPlatformAdmin = hasRole(authentication, User.UserRole.PLATFORM_ADMIN);
        Long callerUserId = resolveUserId(null, authentication);
        Long callerTenantId = resolveCallerTenantId(authentication);

        if (!isPlatformAdmin && !callerUserId.equals(session.getSupportUserId())
                && (callerTenantId == null || !callerTenantId.equals(session.getTargetTenantId()))) {
            throw new AccessDeniedException("Cannot view this impersonation session");
        }

        return ResponseEntity.ok(session);
    }

    private Long resolveUserId(Long requestUserId, Authentication authentication) {
        if (requestUserId != null) return requestUserId;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof TenantUserDetails tud && tud.getUser() != null) {
                return tud.getUser().getId();
            } else if (authentication.getPrincipal() instanceof User u) {
                return u.getId();
            }
        }
        throw new AccessDeniedException("No user context available");
    }

    private Long resolveCallerTenantId(Authentication authentication) {
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof TenantUserDetails tud) {
                return tud.getTenantId();
            } else if (authentication.getPrincipal() instanceof User u) {
                return u.getTenantId();
            }
        }
        return null;
    }

    private boolean hasRole(Authentication authentication, User.UserRole role) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }
}
