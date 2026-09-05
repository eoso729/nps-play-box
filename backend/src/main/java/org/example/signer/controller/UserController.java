package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.user.*;
import org.example.signer.entity.User;
import org.example.signer.security.RequireTenantAdmin;
import org.example.signer.security.TenantContext;
import org.example.signer.security.TenantUserDetails;
import org.example.signer.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Team & User Management", description = "APIs for managing users, invitations, and seat utilization within a tenant")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Invite a user via email", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User invited successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or seat quota reached"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @PostMapping("/invite")
    public ResponseEntity<InvitationResponse> inviteUser(
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            @Valid @RequestBody InviteUserRequest request,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        Long currentUserId = resolveUserId(requestUserId, authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.inviteUser(tenantId, currentUserId, request));
    }

    @Operation(summary = "Accept an invitation and create user account", description = "Public endpoint accessible with a valid token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Invitation accepted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token, expired, or seat quota full")
    })
    @PostMapping("/accept-invitation")
    public ResponseEntity<UserResponse> acceptInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {
        return ResponseEntity.ok(userService.acceptInvitation(request));
    }

    @Operation(summary = "List users in the tenant organization", description = "Tenant administrators only, paginated")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        return ResponseEntity.ok(userService.getUsersByTenant(tenantId, pageable));
    }

    @Operation(summary = "Get user details", description = "Tenant administrators can view anyone in organization; users can view themselves")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Long id,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        Long currentUserId = resolveUserId(requestUserId, authentication);

        boolean isTenantAdmin = isTenantOrPlatformAdmin(authentication);
        if (!isTenantAdmin && !id.equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to view this user profile");
        }

        return ResponseEntity.ok(userService.getUserById(id, tenantId));
    }

    @Operation(summary = "Update user details", description = "Tenant administrators can update anyone in organization; users can update themselves")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or duplicate email"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            @RequestAttribute(value = "userId", required = false) Long requestUserId,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        Long currentUserId = resolveUserId(requestUserId, authentication);

        boolean isTenantAdmin = isTenantOrPlatformAdmin(authentication);
        if (!isTenantAdmin && !id.equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this user profile");
        }

        return ResponseEntity.ok(userService.updateUser(id, tenantId, request));
    }

    @Operation(summary = "Update user role", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role or last admin demotion prevented"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        return ResponseEntity.ok(userService.updateUserRole(id, tenantId, request));
    }

    @Operation(summary = "Deactivate user", description = "Tenant administrators only; soft delete")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User deactivated successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot deactivate last active admin"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable Long id,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        return ResponseEntity.ok(userService.deactivateUser(id, tenantId));
    }

    @Operation(summary = "Reactivate user", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User reactivated successfully"),
        @ApiResponse(responseCode = "400", description = "Seat quota full"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<UserResponse> reactivateUser(
            @PathVariable Long id,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        return ResponseEntity.ok(userService.reactivateUser(id, tenantId));
    }

    @Operation(summary = "Delete user", description = "Tenant administrators only; soft-deactivates user")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        userService.deleteUser(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get pending invitations", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pending invitations retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationResponse>> getPendingInvitations(
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        return ResponseEntity.ok(userService.getPendingInvitations(tenantId));
    }

    @Operation(summary = "Cancel pending invitation", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Invitation cancelled successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Invitation not found")
    })
    @RequireTenantAdmin
    @DeleteMapping("/invitations/{id}")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long id,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        userService.cancelInvitation(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get seat utilization statistics", description = "Tenant administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Seat utilization stats retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping("/seats/utilization")
    public ResponseEntity<SeatUtilizationResponse> getSeatUtilization(
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        return ResponseEntity.ok(userService.getSeatUtilization(tenantId));
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
        return null;
    }

    private boolean isTenantOrPlatformAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_TENANT_ADMIN".equals(auth.getAuthority())
                        || "ROLE_PLATFORM_ADMIN".equals(auth.getAuthority()));
    }
}
