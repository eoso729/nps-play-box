package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.quota.*;
import org.example.signer.entity.User;
import org.example.signer.security.RequirePlatformAdmin;
import org.example.signer.security.RequireTenantAdmin;
import org.example.signer.security.TenantContext;
import org.example.signer.security.TenantUserDetails;
import org.example.signer.service.QuotaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Seat Quota Enforcement", description = "APIs for seat quota availability, status, requests, and administration")
@RestController
@RequestMapping("/api/v1/quota")
@RequiredArgsConstructor
public class QuotaController {

    private final QuotaService quotaService;

    @Operation(summary = "Check seat availability", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Availability checked successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping("/availability")
    public ResponseEntity<SeatAvailabilityResponse> checkAvailability(
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        return ResponseEntity.ok(quotaService.checkAvailability(tenantId));
    }

    @Operation(summary = "Get current quota status", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Quota status retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping("/status")
    public ResponseEntity<QuotaStatusResponse> getQuotaStatus(
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        return ResponseEntity.ok(quotaService.getQuotaStatus(tenantId));
    }

    @Operation(summary = "Request additional seats", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Seat request submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or request already pending"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @PostMapping("/request")
    public ResponseEntity<SeatRequestResponse> requestAdditionalSeats(
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            @Valid @RequestBody SeatRequestRequest request,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        Long currentUserId = resolveUserId(requestUserId, authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quotaService.requestAdditionalSeats(tenantId, currentUserId, request));
    }

    @Operation(summary = "List seat requests for tenant organization", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Seat requests retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping("/requests")
    public ResponseEntity<List<SeatRequestResponse>> getTenantRequests(
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        return ResponseEntity.ok(quotaService.getTenantRequests(tenantId));
    }

    @Operation(summary = "List all pending seat requests", description = "Platform administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequirePlatformAdmin
    @GetMapping("/requests/all")
    public ResponseEntity<List<SeatRequestResponse>> getAllPendingRequests() {
        return ResponseEntity.ok(quotaService.getAllPendingRequests());
    }

    @Operation(summary = "Approve a seat request", description = "Platform administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Seat request approved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or request not pending"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Seat request not found")
    })
    @RequirePlatformAdmin
    @PatchMapping("/requests/{id}/approve")
    public ResponseEntity<SeatRequestResponse> approveRequest(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            @Valid @RequestBody(required = false) ApproveRequestRequest request,
            Authentication authentication) {

        Long reviewerId = resolveUserId(requestUserId, authentication);
        return ResponseEntity.ok(quotaService.approveRequest(id, reviewerId, request != null ? request : new ApproveRequestRequest()));
    }

    @Operation(summary = "Deny a seat request", description = "Platform administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Seat request denied successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or request not pending"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Seat request not found")
    })
    @RequirePlatformAdmin
    @PatchMapping("/requests/{id}/deny")
    public ResponseEntity<SeatRequestResponse> denyRequest(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            @Valid @RequestBody DenyRequestRequest request,
            Authentication authentication) {

        Long reviewerId = resolveUserId(requestUserId, authentication);
        return ResponseEntity.ok(quotaService.denyRequest(id, reviewerId, request));
    }

    private Long resolveTenantId(Long requestTenantId, Authentication authentication) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) return tenantId;
        if (requestTenantId != null) return requestTenantId;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof TenantUserDetails tud) {
                return tud.getTenantId();
            } else if (authentication.getPrincipal() instanceof User u) {
                return u.getTenantId();
            }
        }
        throw new AccessDeniedException("No tenant context available");
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
        return 0L;
    }
}
