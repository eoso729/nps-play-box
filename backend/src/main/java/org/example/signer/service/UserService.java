package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.user.*;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.entity.UserInvitation;
import org.example.signer.exception.InvalidQuotaException;
import org.example.signer.exception.TenantNotFoundException;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    private static final int INVITATION_EXPIRY_HOURS = 72;

    @Transactional
    public InvitationResponse inviteUser(Long tenantId, Long invitedBy, InviteUserRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        if (tenant.getStatus() != Tenant.TenantStatus.ACTIVE) {
            throw new IllegalStateException("Cannot invite users to inactive tenant");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByTenantIdAndEmail(tenantId, email)) {
            throw new IllegalArgumentException("User with this email already exists in your organization");
        }

        List<UserInvitation> pendingInvitations = invitationRepository.findByTenantIdAndAcceptedAtIsNull(tenantId);
        boolean hasPending = pendingInvitations.stream()
                .anyMatch(inv -> inv.getEmail().equalsIgnoreCase(email) && !inv.isExpired());

        if (hasPending) {
            throw new IllegalArgumentException("Pending invitation already exists for this email");
        }

        User.UserRole role;
        try {
            role = User.UserRole.valueOf(request.getRole().trim().toUpperCase());
            if (role == User.UserRole.PLATFORM_ADMIN) {
                throw new IllegalArgumentException("Cannot invite platform admins");
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        long activeUsers = userRepository.countByTenantIdAndStatus(tenantId, User.UserStatus.ACTIVE);
        long activePendingCount = pendingInvitations.stream().filter(inv -> !inv.isExpired()).count();

        if (activeUsers + activePendingCount >= tenant.getMaxSeats()) {
            throw new InvalidQuotaException("Cannot invite user: seat quota full. Max seats: "
                    + tenant.getMaxSeats() + ", active users: " + activeUsers + ", pending invitations: " + activePendingCount);
        }

        String token = generateInvitationToken();
        UserInvitation invitation = UserInvitation.builder()
                .tenantId(tenantId)
                .email(email)
                .role(role)
                .invitationToken(token)
                .invitedBy(invitedBy != null ? invitedBy : 0L)
                .expiresAt(LocalDateTime.now().plusHours(INVITATION_EXPIRY_HOURS))
                .build();

        invitation = invitationRepository.save(invitation);

        User inviter = invitedBy != null ? userRepository.findById(invitedBy).orElse(null) : null;
        String inviterName = inviter != null ? (inviter.getFirstName() + " " + inviter.getLastName()).trim() : "An administrator";

        emailService.sendInvitationEmail(
                email,
                tenant.getName(),
                inviterName,
                token,
                request.getCustomMessage()
        );

        log.info("User invited: email={}, tenantId={}, role={}, invitedBy={}", email, tenantId, role, invitedBy);
        return mapInvitationToResponse(invitation, inviter, tenant);
    }

    @Transactional
    public UserResponse acceptInvitation(AcceptInvitationRequest request) {
        UserInvitation invitation = invitationRepository.findByInvitationToken(request.getToken().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (invitation.isAccepted()) {
            throw new IllegalStateException("Invitation has already been accepted");
        }

        if (invitation.isExpired()) {
            throw new IllegalStateException("Invitation has expired");
        }

        if (userRepository.existsByTenantIdAndEmail(invitation.getTenantId(), invitation.getEmail())) {
            throw new IllegalStateException("User already exists in this organization");
        }

        Tenant tenant = tenantRepository.findById(invitation.getTenantId())
                .orElseThrow(() -> new TenantNotFoundException(invitation.getTenantId()));

        if (tenant.getStatus() != Tenant.TenantStatus.ACTIVE) {
            throw new IllegalStateException("Tenant is not active");
        }

        long activeUsers = userRepository.countByTenantIdAndStatus(tenant.getId(), User.UserStatus.ACTIVE);
        if (activeUsers >= tenant.getMaxSeats()) {
            throw new InvalidQuotaException("Cannot accept invitation: seat limit reached. Max seats: " + tenant.getMaxSeats());
        }

        User user = User.builder()
                .tenantId(invitation.getTenantId())
                .userUuid(UUID.randomUUID())
                .email(invitation.getEmail())
                .username(invitation.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(invitation.getRole())
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build();

        user = userRepository.save(user);

        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        log.info("Invitation accepted: userId={}, tenantId={}, email={}", user.getId(), tenant.getId(), user.getEmail());
        return mapToResponse(user, tenant);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByTenant(Long tenantId, Pageable pageable) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        return userRepository.findByTenantId(tenantId, pageable)
                .map(user -> mapToResponse(user, tenant));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId, Long tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!user.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("User not found in your organization");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        return mapToResponse(user, tenant);
    }

    @Transactional
    public UserResponse updateUser(Long userId, Long tenantId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!user.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("User not found in your organization");
        }

        if (StringUtils.hasText(request.getEmail())) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByTenantIdAndEmail(tenantId, newEmail)) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(newEmail);
        }

        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName().trim());
        }

        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName().trim());
        }

        user = userRepository.save(user);
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        log.info("User updated: userId={}, tenantId={}", userId, tenantId);
        return mapToResponse(user, tenant);
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, Long tenantId, UpdateRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!user.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("User not found in your organization");
        }

        User.UserRole newRole;
        try {
            newRole = User.UserRole.valueOf(request.getRole().trim().toUpperCase());
            if (newRole == User.UserRole.PLATFORM_ADMIN) {
                throw new IllegalArgumentException("Cannot assign platform admin role");
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        if (user.getRole() == User.UserRole.TENANT_ADMIN && newRole != User.UserRole.TENANT_ADMIN) {
            long activeAdminCount = userRepository.countByTenantIdAndRoleAndStatus(
                    tenantId, User.UserRole.TENANT_ADMIN, User.UserStatus.ACTIVE);

            if (activeAdminCount <= 1) {
                throw new IllegalStateException("Cannot demote the last active tenant admin");
            }
        }

        user.setRole(newRole);
        user = userRepository.save(user);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        log.info("User role updated: userId={}, tenantId={}, newRole={}", userId, tenantId, newRole);
        return mapToResponse(user, tenant);
    }

    @Transactional
    public UserResponse deactivateUser(Long userId, Long tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!user.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("User not found in your organization");
        }

        if (user.getRole() == User.UserRole.TENANT_ADMIN && user.getStatus() == User.UserStatus.ACTIVE) {
            long activeAdminCount = userRepository.countByTenantIdAndRoleAndStatus(
                    tenantId, User.UserRole.TENANT_ADMIN, User.UserStatus.ACTIVE);

            if (activeAdminCount <= 1) {
                throw new IllegalStateException("Cannot deactivate the last active tenant admin");
            }
        }

        user.setStatus(User.UserStatus.INACTIVE);
        user = userRepository.save(user);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        log.info("User deactivated: userId={}, tenantId={}", userId, tenantId);
        return mapToResponse(user, tenant);
    }

    @Transactional
    public UserResponse reactivateUser(Long userId, Long tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!user.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("User not found in your organization");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        long activeUsers = userRepository.countByTenantIdAndStatus(tenantId, User.UserStatus.ACTIVE);
        if (activeUsers >= tenant.getMaxSeats()) {
            throw new InvalidQuotaException("Cannot reactivate user: seat quota full. Max seats: " + tenant.getMaxSeats());
        }

        user.setStatus(User.UserStatus.ACTIVE);
        user = userRepository.save(user);

        log.info("User reactivated: userId={}, tenantId={}", userId, tenantId);
        return mapToResponse(user, tenant);
    }

    @Transactional
    public void deleteUser(Long userId, Long tenantId) {
        // Soft delete per design interview
        deactivateUser(userId, tenantId);
        log.info("User deleted (deactivated): userId={}, tenantId={}", userId, tenantId);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getPendingInvitations(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        List<UserInvitation> invitations = invitationRepository.findByTenantIdAndAcceptedAtIsNull(tenantId);

        return invitations.stream()
                .map(inv -> {
                    User inviter = inv.getInvitedBy() != null ? userRepository.findById(inv.getInvitedBy()).orElse(null) : null;
                    return mapInvitationToResponse(inv, inviter, tenant);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelInvitation(Long invitationId, Long tenantId) {
        UserInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found: " + invitationId));

        if (!invitation.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Invitation not found in your organization");
        }

        if (invitation.isAccepted()) {
            throw new IllegalStateException("Cannot cancel accepted invitation");
        }

        invitationRepository.delete(invitation);
        log.info("Invitation cancelled: invitationId={}, tenantId={}", invitationId, tenantId);
    }

    @Transactional(readOnly = true)
    public SeatUtilizationResponse getSeatUtilization(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        long activeUsers = userRepository.countByTenantIdAndStatus(tenantId, User.UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByTenantIdAndStatus(tenantId, User.UserStatus.INACTIVE);
        long pendingInvitations = invitationRepository.findByTenantIdAndAcceptedAtIsNull(tenantId)
                .stream()
                .filter(inv -> !inv.isExpired())
                .count();

        int maxSeats = tenant.getMaxSeats() != null ? tenant.getMaxSeats() : 0;
        int usedSeats = (int) activeUsers;
        int availableSeats = Math.max(0, maxSeats - usedSeats);
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
                .userUuid(user.getUserUuid() != null ? user.getUserUuid().toString() : null)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
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

    private InvitationResponse mapInvitationToResponse(UserInvitation invitation, User inviter, Tenant tenant) {
        String inviterName = inviter != null
                ? (inviter.getFirstName() + " " + inviter.getLastName()).trim()
                : "Unknown";
        String inviterEmail = inviter != null ? inviter.getEmail() : "Unknown";

        return InvitationResponse.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .role(invitation.getRole() != null ? invitation.getRole().name() : null)
                .invitationToken(invitation.getInvitationToken())
                .invitedByName(inviterName)
                .invitedByEmail(inviterEmail)
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .expired(invitation.isExpired())
                .build();
    }
}
