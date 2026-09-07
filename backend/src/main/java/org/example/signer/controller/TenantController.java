package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.tenant.*;
import org.example.signer.entity.User;
import org.example.signer.security.RequirePlatformAdmin;
import org.example.signer.security.TenantContext;
import org.example.signer.security.TenantUserDetails;
import org.example.signer.service.TenantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Tenant Management", description = "APIs for managing tenant organizations")
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "Create new tenant", description = "Platform administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tenant created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "409", description = "Slug already exists")
    })
    @RequirePlatformAdmin
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody CreateTenantRequest request,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            Authentication authentication) {
        Long currentUserId = resolveUserId(requestUserId, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.createTenant(request, currentUserId));
    }

    @Operation(summary = "List all tenants", description = "Platform administrators only, paginated")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tenants"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequirePlatformAdmin
    @GetMapping
    public ResponseEntity<Page<TenantResponse>> getAllTenants(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(tenantService.getAllTenants(pageable));
    }

    @Operation(summary = "Get tenant details by ID", description = "Platform admins can view any tenant; Tenant admins can view their own tenant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tenant retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenant(
            @PathVariable Long id,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        boolean isPlatformAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_PLATFORM_ADMIN".equals(auth.getAuthority()));
        boolean isTenantAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_TENANT_ADMIN".equals(auth.getAuthority()));

        Long callerTenantId = TenantContext.getTenantId();
        if (callerTenantId == null) {
            callerTenantId = requestTenantId;
        }
        if (callerTenantId == null && authentication.getPrincipal() instanceof TenantUserDetails tud) {
            callerTenantId = tud.getTenantId();
        } else if (callerTenantId == null && authentication.getPrincipal() instanceof User u) {
            callerTenantId = u.getTenantId();
        }

        if (isPlatformAdmin || (isTenantAdmin && id.equals(callerTenantId))) {
            return ResponseEntity.ok(tenantService.getTenantById(id));
        }

        throw new AccessDeniedException("You are not authorized to view tenant with ID: " + id);
    }

    @Operation(summary = "Get current tenant info", description = "Accessible by any authenticated user for their active tenant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Current tenant retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    @GetMapping("/current")
    public ResponseEntity<TenantResponse> getCurrentTenant(
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long callerTenantId = TenantContext.getTenantId();
        if (callerTenantId == null) {
            callerTenantId = requestTenantId;
        }
        if (callerTenantId == null && authentication != null) {
            if (authentication.getPrincipal() instanceof TenantUserDetails tud) {
                callerTenantId = tud.getTenantId();
            } else if (authentication.getPrincipal() instanceof User u) {
                callerTenantId = u.getTenantId();
            }
        }

        return ResponseEntity.ok(tenantService.getCurrentTenant(callerTenantId));
    }

    @Operation(summary = "Update tenant details", description = "Platform administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tenant updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    @RequirePlatformAdmin
    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(tenantService.updateTenant(id, request));
    }

    @Operation(summary = "Update tenant status", description = "Platform administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tenant status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    @RequirePlatformAdmin
    @PatchMapping("/{id}/status")
    public ResponseEntity<TenantResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(tenantService.updateStatus(id, request));
    }

    @Operation(summary = "Update tenant seat quota", description = "Platform administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Seat quota updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid quota or reduction below current active users"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    @RequirePlatformAdmin
    @PatchMapping("/{id}/seats")
    public ResponseEntity<TenantResponse> updateSeats(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSeatsRequest request) {
        return ResponseEntity.ok(tenantService.updateSeats(id, request));
    }

    @Operation(summary = "Soft delete tenant", description = "Platform administrators only; deactivates tenant and users")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tenant deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    @RequirePlatformAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
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
        return null;
    }
}
