# Phase 4: Team & User Management

## Objective
Implement comprehensive team and user management capabilities within tenant boundaries. Enable tenant admins to invite users via email tokens, assign roles, manage user lifecycle, and track seat utilization.

**Duration**: 5-7 days  
**Dependencies**: Phase 2 (Authentication), Phase 3 (Tenant Management)

---

## API Endpoints

### 4.1 User Management Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/users/invite` | Invite user via email | Tenant Admin |
| POST | `/api/v1/users/accept-invitation` | Accept invitation with token | Public |
| GET | `/api/v1/users` | List all users in tenant | Tenant Admin |
| GET | `/api/v1/users/{id}` | Get user details | Tenant Admin, Self |
| PUT | `/api/v1/users/{id}` | Update user details | Tenant Admin, Self (limited) |
| PATCH | `/api/v1/users/{id}/role` | Change user role | Tenant Admin |
| PATCH | `/api/v1/users/{id}/deactivate` | Deactivate user | Tenant Admin |
| PATCH | `/api/v1/users/{id}/reactivate` | Reactivate user | Tenant Admin |
| DELETE | `/api/v1/users/{id}` | Delete user | Tenant Admin |
| GET | `/api/v1/users/invitations` | List pending invitations | Tenant Admin |
| DELETE | `/api/v1/users/invitations/{id}` | Cancel invitation | Tenant Admin |
| GET | `/api/v1/users/seats/utilization` | Get seat utilization stats | Tenant Admin |

---

## Backend Implementation

### 1.1 Create DTOs

**File**: `backend/src/main/java/org/example/signer/dto/user/InviteUserRequest.java`

```java
package org.example.signer.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotNull(message = "Role is required")
    private String role; // TENANT_ADMIN, DEVELOPER, VIEWER
    
    private String firstName;
    
    private String lastName;
    
    private String customMessage;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/user/AcceptInvitationRequest.java`

```java
package org.example.signer.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcceptInvitationRequest {
    
    @NotBlank(message = "Invitation token is required")
    private String token;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/user/UpdateUserRequest.java`

```java
package org.example.signer.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(min = 1, max = 100)
    private String firstName;
    
    @Size(min = 1, max = 100)
    private String lastName;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/user/UpdateRoleRequest.java`

```java
package org.example.signer.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    
    @NotBlank(message = "Role is required")
    private String role; // TENANT_ADMIN, DEVELOPER, VIEWER
}
```

**File**: `backend/src/main/java/org/example/signer/dto/user/UserResponse.java`

```java
package org.example.signer.dto.user;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String userUuid;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private TenantInfo tenant;
    
    @Data
    @Builder
    public static class TenantInfo {
        private Long id;
        private String name;
        private String slug;
    }
}
```

**File**: `backend/src/main/java/org/example/signer/dto/user/InvitationResponse.java`

```java
package org.example.signer.dto.user;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class InvitationResponse {
    private Long id;
    private String email;
    private String role;
    private String invitationToken;
    private String invitedByName;
    private String invitedByEmail;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean expired;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/user/SeatUtilizationResponse.java`

```java
package org.example.signer.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatUtilizationResponse {
    private Integer maxSeats;
    private Integer usedSeats;
    private Integer availableSeats;
    private Integer activeUsers;
    private Integer inactiveUsers;
    private Integer pendingInvitations;
    private Double utilizationPercentage;
}
```

### 1.2 Create User Service

**File**: `backend/src/main/java/org/example/signer/service/UserService.java`

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.user.*;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.entity.UserInvitation;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserInvitationRepository invitationRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    private static final int INVITATION_EXPIRY_HOURS = 72; // 3 days
    
    @Transactional
    public InvitationResponse inviteUser(Long tenantId, Long invitedBy, InviteUserRequest request) {
        // Validate tenant exists and is active
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        if (tenant.getStatus() != Tenant.TenantStatus.ACTIVE) {
            throw new IllegalStateException("Cannot invite users to inactive tenant");
        }
        
        // Check if user already exists in tenant
        if (userRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists in your organization");
        }
        
        // Check if there's already a pending invitation
        List<UserInvitation> pendingInvitations = invitationRepository
                .findByTenantIdAndAcceptedAtIsNull(tenantId);
        boolean hasPendingInvitation = pendingInvitations.stream()
                .anyMatch(inv -> inv.getEmail().equalsIgnoreCase(request.getEmail()) 
                        && !inv.isExpired());
        
        if (hasPendingInvitation) {
            throw new IllegalArgumentException("Pending invitation already exists for this email");
        }
        
        // Validate role
        User.UserRole role;
        try {
            role = User.UserRole.valueOf(request.getRole());
            if (role == User.UserRole.PLATFORM_ADMIN) {
                throw new IllegalArgumentException("Cannot invite platform admins");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }
        
        // Create invitation
        String token = generateInvitationToken();
        UserInvitation invitation = UserInvitation.builder()
                .tenantId(tenantId)
                .email(request.getEmail())
                .role(role)
                .invitationToken(token)
                .invitedBy(invitedBy)
                .expiresAt(LocalDateTime.now().plusHours(INVITATION_EXPIRY_HOURS))
                .build();
        
        invitation = invitationRepository.save(invitation);
        
        // Get inviter details
        User inviter = userRepository.findById(invitedBy)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));
        
        // Send invitation email
        emailService.sendInvitationEmail(
                request.getEmail(),
                tenant.getName(),
                inviter.getFirstName() + " " + inviter.getLastName(),
                token,
                request.getCustomMessage()
        );
        
        log.info("User invited: email={}, tenantId={}, role={}, invitedBy={}", 
                request.getEmail(), tenantId, role, invitedBy);
        
        return mapInvitationToResponse(invitation, inviter, tenant);
    }
    
    @Transactional
    public UserResponse acceptInvitation(AcceptInvitationRequest request) {
        // Find invitation
        UserInvitation invitation = invitationRepository
                .findByInvitationToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
        
        // Validate invitation
        if (invitation.isAccepted()) {
            throw new IllegalStateException("Invitation has already been accepted");
        }
        
        if (invitation.isExpired()) {
            throw new IllegalStateException("Invitation has expired");
        }
        
        // Check if user already exists
        if (userRepository.existsByTenantIdAndEmail(
                invitation.getTenantId(), invitation.getEmail())) {
            throw new IllegalStateException("User already exists in this organization");
        }
        
        // Get tenant
        Tenant tenant = tenantRepository.findById(invitation.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        // Create user
        User user = User.builder()
                .tenantId(invitation.getTenantId())
                .email(invitation.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(invitation.getRole())
                .status(User.UserStatus.ACTIVE)
                .build();
        
        user = userRepository.save(user);
        
        // Mark invitation as accepted
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        
        log.info("Invitation accepted: userId={}, tenantId={}, email={}", 
                user.getId(), tenant.getId(), user.getEmail());
        
        return mapToResponse(user, tenant);
    }
    
    public Page<UserResponse> getUsersByTenant(Long tenantId, Pageable pageable) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        return userRepository.findAll(pageable)
                .map(user -> mapToResponse(user, tenant));
    }
    
    public UserResponse getUserById(Long userId, Long tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify user belongs to tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("User not found in your organization");
        }
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        return mapToResponse(user, tenant);
    }
    
    @Transactional
    public UserResponse updateUser(Long userId, Long tenantId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify user belongs to tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("User not found in your organization");
        }
        
        if (request.getEmail() != null) {
            // Check email uniqueness within tenant
            if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        
        user = userRepository.save(user);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        log.info("User updated: userId={}, tenantId={}", userId, tenantId);
        
        return mapToResponse(user, tenant);
    }
    
    @Transactional
    public UserResponse updateUserRole(Long userId, Long tenantId, UpdateRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify user belongs to tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("User not found in your organization");
        }
        
        // Validate role
        User.UserRole newRole;
        try {
            newRole = User.UserRole.valueOf(request.getRole());
            if (newRole == User.UserRole.PLATFORM_ADMIN) {
                throw new IllegalArgumentException("Cannot assign platform admin role");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }
        
        // Prevent last tenant admin from being demoted
        if (user.getRole() == User.UserRole.TENANT_ADMIN && newRole != User.UserRole.TENANT_ADMIN) {
            long adminCount = userRepository.findByTenantId(tenantId).stream()
                    .filter(u -> u.getRole() == User.UserRole.TENANT_ADMIN 
                            && u.getStatus() == User.UserStatus.ACTIVE)
                    .count();
            
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot demote the last active tenant admin");
            }
        }
        
        user.setRole(newRole);
        user = userRepository.save(user);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        log.info("User role updated: userId={}, tenantId={}, newRole={}", 
                userId, tenantId, newRole);
        
        return mapToResponse(user, tenant);
    }
    
    @Transactional
    public UserResponse deactivateUser(Long userId, Long tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify user belongs to tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("User not found in your organization");
        }
        
        // Prevent last tenant admin from being deactivated
        if (user.getRole() == User.UserRole.TENANT_ADMIN) {
            long activeAdminCount = userRepository.findByTenantId(tenantId).stream()
                    .filter(u -> u.getRole() == User.UserRole.TENANT_ADMIN 
                            && u.getStatus() == User.UserStatus.ACTIVE)
                    .count();
            
            if (activeAdminCount <= 1) {
                throw new IllegalStateException("Cannot deactivate the last active tenant admin");
            }
        }
        
        user.setStatus(User.UserStatus.INACTIVE);
        user = userRepository.save(user);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        log.info("User deactivated: userId={}, tenantId={}", userId, tenantId);
        
        return mapToResponse(user, tenant);
    }
    
    @Transactional
    public UserResponse reactivateUser(Long userId, Long tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify user belongs to tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("User not found in your organization");
        }
        
        user.setStatus(User.UserStatus.ACTIVE);
        user = userRepository.save(user);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        log.info("User reactivated: userId={}, tenantId={}", userId, tenantId);
        
        return mapToResponse(user, tenant);
    }
    
    @Transactional
    public void deleteUser(Long userId, Long tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify user belongs to tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("User not found in your organization");
        }
        
        // Prevent last tenant admin from being deleted
        if (user.getRole() == User.UserRole.TENANT_ADMIN) {
            long adminCount = userRepository.findByTenantId(tenantId).stream()
                    .filter(u -> u.getRole() == User.UserRole.TENANT_ADMIN)
                    .count();
            
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last tenant admin");
            }
        }
        
        userRepository.delete(user);
        
        log.info("User deleted: userId={}, tenantId={}", userId, tenantId);
    }
    
    public List<InvitationResponse> getPendingInvitations(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        List<UserInvitation> invitations = invitationRepository
                .findByTenantIdAndAcceptedAtIsNull(tenantId);
        
        return invitations.stream()
                .map(inv -> {
                    User inviter = userRepository.findById(inv.getInvitedBy())
                            .orElse(null);
                    return mapInvitationToResponse(inv, inviter, tenant);
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void cancelInvitation(Long invitationId, Long tenantId) {
        UserInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        
        // Verify invitation belongs to tenant
        if (!invitation.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Invitation not found in your organization");
        }
        
        if (invitation.isAccepted()) {
            throw new IllegalStateException("Cannot cancel accepted invitation");
        }
        
        invitationRepository.delete(invitation);
        
        log.info("Invitation cancelled: invitationId={}, tenantId={}", invitationId, tenantId);
    }
    
    public SeatUtilizationResponse getSeatUtilization(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        long activeUsers = userRepository.countByTenantIdAndStatus(
                tenantId, User.UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByTenantIdAndStatus(
                tenantId, User.UserStatus.INACTIVE);
        long pendingInvitations = invitationRepository
                .findByTenantIdAndAcceptedAtIsNull(tenantId)
                .stream()
                .filter(inv -> !inv.isExpired())
                .count();
        
        int maxSeats = tenant.getMaxSeats();
        int usedSeats = (int) activeUsers;
        int availableSeats = maxSeats - usedSeats;
        double utilization = maxSeats > 0 ? (usedSeats * 100.0 / maxSeats) : 0.0;
        
        return SeatUtilizationResponse.builder()
                .maxSeats(maxSeats)
                .usedSeats(usedSeats)
                .availableSeats(availableSeats)
                .activeUsers((int) activeUsers)
                .inactiveUsers((int) inactiveUsers)
                .pendingInvitations((int) pendingInvitations)
                .utilizationPercentage(Math.round(utilization * 100.0) / 100.0)
                .build();
    }
    
    @Transactional
    public void cleanupExpiredInvitations() {
        invitationRepository.deleteByExpiresAtBeforeAndAcceptedAtIsNull(LocalDateTime.now());
        log.info("Cleaned up expired invitations");
    }
    
    private String generateInvitationToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }
    
    private UserResponse mapToResponse(User user, Tenant tenant) {
        return UserResponse.builder()
                .id(user.getId())
                .userUuid(user.getUserUuid().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .tenant(UserResponse.TenantInfo.builder()
                        .id(tenant.getId())
                        .name(tenant.getName())
                        .slug(tenant.getSlug())
                        .build())
                .build();
    }
    
    private InvitationResponse mapInvitationToResponse(
            UserInvitation invitation, User inviter, Tenant tenant) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .role(invitation.getRole().name())
                .invitationToken(invitation.getInvitationToken())
                .invitedByName(inviter != null ? 
                        inviter.getFirstName() + " " + inviter.getLastName() : "Unknown")
                .invitedByEmail(inviter != null ? inviter.getEmail() : "Unknown")
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .expired(invitation.isExpired())
                .build();
    }
}
```

### 1.3 Create Email Service

**File**: `backend/src/main/java/org/example/signer/service/EmailService.java`

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.email.from:noreply@npsplaybox.com}")
    private String fromEmail;
    
    public void sendInvitationEmail(
            String toEmail,
            String tenantName,
            String inviterName,
            String token,
            String customMessage) {
        
        String invitationUrl = frontendUrl + "/accept-invitation?token=" + token;
        
        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n");
        body.append(inviterName).append(" has invited you to join ")
            .append(tenantName).append(" on NPS Play Box.\n\n");
        
        if (customMessage != null && !customMessage.isEmpty()) {
            body.append("Message from ").append(inviterName).append(":\n");
            body.append(customMessage).append("\n\n");
        }
        
        body.append("Click the link below to accept your invitation:\n");
        body.append(invitationUrl).append("\n\n");
        body.append("This invitation will expire in 72 hours.\n\n");
        body.append("If you didn't expect this invitation, you can safely ignore this email.\n\n");
        body.append("Best regards,\n");
        body.append("The NPS Play Box Team");
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("You're invited to join " + tenantName);
        message.setText(body.toString());
        
        try {
            mailSender.send(message);
            log.info("Invitation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send invitation email to: {}", toEmail, e);
            // In production, consider implementing retry logic or dead letter queue
        }
    }
}
```

### 1.4 Create User Controller

**File**: `backend/src/main/java/org/example/signer/controller/UserController.java`

```java
package org.example.signer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.signer.dto.user.*;
import org.example.signer.security.RequireTenantAdmin;
import org.example.signer.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @RequireTenantAdmin
    @PostMapping("/invite")
    public ResponseEntity<InvitationResponse> inviteUser(
            @RequestAttribute("tenantId") Long tenantId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody InviteUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.inviteUser(tenantId, currentUserId, request));
    }
    
    @PostMapping("/accept-invitation")
    public ResponseEntity<UserResponse> acceptInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {
        return ResponseEntity.ok(userService.acceptInvitation(request));
    }
    
    @RequireTenantAdmin
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestAttribute("tenantId") Long tenantId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByTenant(tenantId, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable Long id,
            @RequestAttribute("tenantId") Long tenantId,
            @RequestAttribute("userId") Long currentUserId,
            Authentication authentication) {
        
        // Users can view their own profile, admins can view anyone in tenant
        boolean isTenantAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TENANT_ADMIN"));
        
        if (!isTenantAdmin && !id.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(userService.getUserById(id, tenantId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestAttribute("tenantId") Long tenantId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        
        // Users can update their own profile, admins can update anyone in tenant
        boolean isTenantAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TENANT_ADMIN"));
        
        if (!isTenantAdmin && !id.equals(currentUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(userService.updateUser(id, tenantId, request));
    }
    
    @RequireTenantAdmin
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestAttribute("tenantId") Long tenantId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(id, tenantId, request));
    }
    
    @RequireTenantAdmin
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable Long id,
            @RequestAttribute("tenantId") Long tenantId) {
        return ResponseEntity.ok(userService.deactivateUser(id, tenantId));
    }
    
    @RequireTenantAdmin
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<UserResponse> reactivateUser(
            @PathVariable Long id,
            @RequestAttribute("tenantId") Long tenantId) {
        return ResponseEntity.ok(userService.reactivateUser(id, tenantId));
    }
    
    @RequireTenantAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestAttribute("tenantId") Long tenantId) {
        userService.deleteUser(id, tenantId);
        return ResponseEntity.noContent().build();
    }
    
    @RequireTenantAdmin
    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationResponse>> getPendingInvitations(
            @RequestAttribute("tenantId") Long tenantId) {
        return ResponseEntity.ok(userService.getPendingInvitations(tenantId));
    }
    
    @RequireTenantAdmin
    @DeleteMapping("/invitations/{id}")
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long id,
            @RequestAttribute("tenantId") Long tenantId) {
        userService.cancelInvitation(id, tenantId);
        return ResponseEntity.noContent().build();
    }
    
    @RequireTenantAdmin
    @GetMapping("/seats/utilization")
    public ResponseEntity<SeatUtilizationResponse> getSeatUtilization(
            @RequestAttribute("tenantId") Long tenantId) {
        return ResponseEntity.ok(userService.getSeatUtilization(tenantId));
    }
}
```

### 1.5 Create Scheduled Task for Cleanup

**File**: `backend/src/main/java/org/example/signer/scheduled/InvitationCleanupTask.java`

```java
package org.example.signer.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvitationCleanupTask {
    
    private final UserService userService;
    
    // Run daily at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredInvitations() {
        log.info("Starting expired invitation cleanup task");
        userService.cleanupExpiredInvitations();
        log.info("Expired invitation cleanup task completed");
    }
}
```

### 1.6 Update Security Filter to Include User ID

**File**: `backend/src/main/java/org/example/signer/security/JwtAuthenticationFilter.java`

Update the filter to store user ID in request attributes:

```java
// Inside doFilterInternal method, after authentication:
if (jwtService.isTokenValid(jwt, (org.example.signer.entity.User) userDetails)) {
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
    );
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
    
    // Store tenant context in request attributes
    request.setAttribute("tenantId", jwtService.extractTenantId(jwt));
    request.setAttribute("tenantSlug", jwtService.extractTenantSlug(jwt));
    
    // Store user ID for authorization checks
    User user = (User) userDetails;
    request.setAttribute("userId", user.getId());
}
```

---

## Configuration

### 2.1 Email Configuration

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
            required: true
          connectiontimeout: 5000
          timeout: 3000
          writetimeout: 5000

app:
  frontend:
    url: ${FRONTEND_URL:http://localhost:3000}
  email:
    from: ${EMAIL_FROM:noreply@npsplaybox.com}
```

### 2.2 Enable Scheduling

**File**: `backend/src/main/java/org/example/signer/NpsPlayBoxApplication.java`

```java
@SpringBootApplication
@EnableScheduling
public class NpsPlayBoxApplication {
    public static void main(String[] args) {
        SpringApplication.run(NpsPlayBoxApplication.class, args);
    }
}
```

---

## Testing Requirements

### 3.1 Service Layer Tests

**File**: `backend/src/test/java/org/example/signer/service/UserServiceTest.java`

```java
@SpringBootTest
@Transactional
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserInvitationRepository invitationRepository;
    
    private Tenant testTenant;
    private User adminUser;
    
    @BeforeEach
    void setup() {
        testTenant = tenantRepository.save(Tenant.builder()
                .name("Test Corp")
                .slug("test-corp")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());
        
        adminUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .email("admin@test.com")
                .passwordHash("hashed")
                .firstName("Admin")
                .lastName("User")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
    }
    
    @Test
    void shouldInviteUser() {
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail("newuser@test.com");
        request.setRole("DEVELOPER");
        request.setFirstName("New");
        request.setLastName("User");
        
        InvitationResponse response = userService.inviteUser(
                testTenant.getId(), adminUser.getId(), request);
        
        assertNotNull(response.getId());
        assertEquals("newuser@test.com", response.getEmail());
        assertEquals("DEVELOPER", response.getRole());
        assertFalse(response.isExpired());
    }
    
    @Test
    void shouldRejectDuplicateInvitation() {
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail("duplicate@test.com");
        request.setRole("DEVELOPER");
        
        userService.inviteUser(testTenant.getId(), adminUser.getId(), request);
        
        assertThrows(IllegalArgumentException.class, () -> 
                userService.inviteUser(testTenant.getId(), adminUser.getId(), request));
    }
    
    @Test
    void shouldAcceptInvitation() {
        // Create invitation
        InviteUserRequest inviteReq = new InviteUserRequest();
        inviteReq.setEmail("invited@test.com");
        inviteReq.setRole("VIEWER");
        InvitationResponse invitation = userService.inviteUser(
                testTenant.getId(), adminUser.getId(), inviteReq);
        
        // Accept invitation
        AcceptInvitationRequest acceptReq = new AcceptInvitationRequest();
        acceptReq.setToken(invitation.getInvitationToken());
        acceptReq.setFirstName("John");
        acceptReq.setLastName("Doe");
        acceptReq.setPassword("SecurePass123");
        
        UserResponse user = userService.acceptInvitation(acceptReq);
        
        assertEquals("invited@test.com", user.getEmail());
        assertEquals("VIEWER", user.getRole());
        assertEquals("ACTIVE", user.getStatus());
    }
    
    @Test
    void shouldRejectExpiredInvitation() {
        // Create expired invitation manually
        UserInvitation invitation = UserInvitation.builder()
                .tenantId(testTenant.getId())
                .email("expired@test.com")
                .role(User.UserRole.DEVELOPER)
                .invitationToken(UUID.randomUUID().toString())
                .invitedBy(adminUser.getId())
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        invitation = invitationRepository.save(invitation);
        
        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setToken(invitation.getInvitationToken());
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPassword("password");
        
        assertThrows(IllegalStateException.class, 
                () -> userService.acceptInvitation(request));
    }
    
    @Test
    void shouldUpdateUserRole() {
        User user = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .email("user@test.com")
                .passwordHash("hashed")
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.VIEWER)
                .status(User.UserStatus.ACTIVE)
                .build());
        
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("DEVELOPER");
        
        UserResponse updated = userService.updateUserRole(
                user.getId(), testTenant.getId(), request);
        
        assertEquals("DEVELOPER", updated.getRole());
    }
    
    @Test
    void shouldPreventLastAdminDemotion() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("VIEWER");
        
        assertThrows(IllegalStateException.class, () -> 
                userService.updateUserRole(adminUser.getId(), testTenant.getId(), request));
    }
    
    @Test
    void shouldDeactivateUser() {
        User user = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .email("deactivate@test.com")
                .passwordHash("hashed")
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .build());
        
        UserResponse deactivated = userService.deactivateUser(
                user.getId(), testTenant.getId());
        
        assertEquals("INACTIVE", deactivated.getStatus());
    }
    
    @Test
    void shouldTrackSeatUtilization() {
        // Create additional users
        for (int i = 0; i < 3; i++) {
            userRepository.save(User.builder()
                    .tenantId(testTenant.getId())
                    .email("user" + i + "@test.com")
                    .passwordHash("hashed")
                    .firstName("User")
                    .lastName(String.valueOf(i))
                    .role(User.UserRole.DEVELOPER)
                    .status(User.UserStatus.ACTIVE)
                    .build());
        }
        
        SeatUtilizationResponse utilization = userService.getSeatUtilization(
                testTenant.getId());
        
        assertEquals(10, utilization.getMaxSeats());
        assertEquals(4, utilization.getUsedSeats()); // 1 admin + 3 developers
        assertEquals(6, utilization.getAvailableSeats());
        assertEquals(40.0, utilization.getUtilizationPercentage());
    }
    
    @Test
    void shouldCleanupExpiredInvitations() {
        // Create expired invitation
        UserInvitation expired = invitationRepository.save(UserInvitation.builder()
                .tenantId(testTenant.getId())
                .email("expired@test.com")
                .role(User.UserRole.DEVELOPER)
                .invitationToken(UUID.randomUUID().toString())
                .invitedBy(adminUser.getId())
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build());
        
        userService.cleanupExpiredInvitations();
        
        assertFalse(invitationRepository.findById(expired.getId()).isPresent());
    }
}
```

### 3.2 API Integration Tests

**File**: `backend/src/test/java/org/example/signer/controller/UserControllerTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String tenantAdminToken;
    private Tenant testTenant;
    
    @BeforeEach
    void setup() {
        // Setup test tenant and admin, get token
        testTenant = createTestTenant();
        tenantAdminToken = createTenantAdminAndGetToken(testTenant);
    }
    
    @Test
    void shouldInviteUser() throws Exception {
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail("newuser@test.com");
        request.setRole("DEVELOPER");
        request.setCustomMessage("Welcome to the team!");
        
        mockMvc.perform(post("/api/v1/users/invite")
                .header("Authorization", "Bearer " + tenantAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"))
                .andExpect(jsonPath("$.invitationToken").exists())
                .andExpect(jsonPath("$.expired").value(false));
    }
    
    @Test
    void shouldListUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
    
    @Test
    void shouldGetSeatUtilization() throws Exception {
        mockMvc.perform(get("/api/v1/users/seats/utilization")
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxSeats").exists())
                .andExpect(jsonPath("$.usedSeats").exists())
                .andExpect(jsonPath("$.availableSeats").exists())
                .andExpect(jsonPath("$.utilizationPercentage").exists());
    }
    
    @Test
    void developerShouldNotInviteUsers() throws Exception {
        String developerToken = createDeveloperAndGetToken(testTenant);
        
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail("test@test.com");
        request.setRole("VIEWER");
        
        mockMvc.perform(post("/api/v1/users/invite")
                .header("Authorization", "Bearer " + developerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
```

---

## Acceptance Criteria

- ✅ Tenant admins can invite users via email with roles (TENANT_ADMIN, DEVELOPER, VIEWER)
- ✅ Invitation emails sent with unique tokens valid for 72 hours
- ✅ Users can accept invitations and create accounts
- ✅ Expired invitations rejected and cleaned up automatically
- ✅ Duplicate invitations prevented (same email pending)
- ✅ Users cannot be invited if email already exists in tenant
- ✅ Tenant admins can view all users in their organization
- ✅ Users can view and update their own profiles
- ✅ Tenant admins can update any user's role within tenant
- ✅ Last active tenant admin cannot be demoted or deactivated
- ✅ Users can be deactivated (soft delete) and reactivated
- ✅ Seat utilization tracked in real-time (active users count)
- ✅ Pending invitations visible to tenant admins
- ✅ Invitations can be cancelled before acceptance
- ✅ All operations scoped to tenant (no cross-tenant access)

---

## Implementation Notes

1. **Email Service**: Configure SMTP settings or use AWS SES in production
2. **Invitation Tokens**: Use cryptographically secure random tokens
3. **Password Validation**: Implement strong password requirements
4. **Rate Limiting**: Consider rate limiting invitation endpoints to prevent abuse
5. **Audit Trail**: Log all user management operations (Phase 6)
6. **Custom Invitation Messages**: Allow personalizing invitation emails
7. **Resend Invitations**: Consider adding ability to resend expired invitations
8. **Bulk Operations**: Future enhancement for bulk user import/invitation
9. **Self-Service Removal**: Consider allowing users to remove themselves from tenant
10. **Invitation Expiry**: Make expiry duration configurable per tenant tier

---

## Next Phase

Once Phase 4 is complete and tested, proceed to:
**[Phase 5: Seat Quota Enforcement →](./phase-5-seat-quota-enforcement.md)**
