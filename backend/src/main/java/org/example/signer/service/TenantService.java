package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.audit.Auditable;
import org.example.signer.dto.tenant.*;
import org.example.signer.entity.AuditEvent;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.exception.DuplicateSlugException;
import org.example.signer.exception.InvalidQuotaException;
import org.example.signer.exception.TenantNotFoundException;
import org.example.signer.entity.UserInvitation;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserInvitationRepository invitationRepository;
    private final EmailService emailService;

    @Auditable(eventType = AuditEvent.EventType.TENANT_MANAGEMENT, action = "CREATE_TENANT", resourceType = "TENANT", resourceId = "#result.id")
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        String slug = request.getSlug().trim().toLowerCase();
        if (tenantRepository.existsBySlug(slug)) {
            throw new DuplicateSlugException(slug, null);
        }

        Tenant.SubscriptionTier tier;
        try {
            tier = Tenant.SubscriptionTier.valueOf(request.getSubscriptionTier().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid subscription tier: " + request.getSubscriptionTier());
        }

        Tenant tenant = Tenant.builder()
                .tenantUuid(UUID.randomUUID())
                .name(request.getName().trim())
                .slug(slug)
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(request.getMaxSeats())
                .subscriptionTier(tier)
                .build();

        tenant = tenantRepository.save(tenant);

        String invitationToken = null;
        String invitationUrl = null;
        int usedSeats = 0;

        if (StringUtils.hasText(request.getAdminPassword())) {
            User adminUser = User.builder()
                    .tenantId(tenant.getId())
                    .userUuid(UUID.randomUUID())
                    .email(request.getAdminEmail().trim())
                    .username(request.getAdminEmail().trim())
                    .passwordHash(passwordEncoder.encode(request.getAdminPassword().trim()))
                    .firstName(StringUtils.hasText(request.getAdminFirstName()) ? request.getAdminFirstName().trim() : "")
                    .lastName(StringUtils.hasText(request.getAdminLastName()) ? request.getAdminLastName().trim() : "")
                    .role(User.UserRole.TENANT_ADMIN)
                    .status(User.UserStatus.ACTIVE)
                    .authProvider("LOCAL")
                    .build();

            userRepository.save(adminUser);
            usedSeats = 1;
            log.info("Created tenant '{}' (id: {}) with active admin user '{}'", tenant.getName(), tenant.getId(), adminUser.getEmail());
        } else {
            String token = UUID.randomUUID().toString();
            UserInvitation invitation = UserInvitation.builder()
                    .tenantId(tenant.getId())
                    .email(request.getAdminEmail().trim().toLowerCase())
                    .role(User.UserRole.TENANT_ADMIN)
                    .invitationToken(token)
                    .invitedBy(0L)
                    .expiresAt(LocalDateTime.now().plusHours(72))
                    .build();

            invitationRepository.save(invitation);

            invitationToken = token;
            invitationUrl = emailService.buildInvitationUrl(token);

            emailService.sendInvitationEmail(
                    request.getAdminEmail().trim(),
                    tenant.getName(),
                    "Platform Administrator",
                    token,
                    "Welcome to NPS Play Box! You have been provisioned as the Primary Administrator for " + tenant.getName() + "."
            );

            log.info("Created tenant '{}' (id: {}) and dispatched invitation token to admin '{}'", tenant.getName(), tenant.getId(), request.getAdminEmail());
        }

        TenantResponse response = mapToResponse(tenant, usedSeats);
        response.setInvitationToken(invitationToken);
        response.setInvitationUrl(invitationUrl);
        return response;
    }

    @Transactional(readOnly = true)
    public Page<TenantResponse> getAllTenants(Pageable pageable) {
        return tenantRepository.findAll(pageable)
                .map(tenant -> {
                    long usedSeats = userRepository.countByTenantIdAndStatus(tenant.getId(), User.UserStatus.ACTIVE);
                    return mapToResponse(tenant, (int) usedSeats);
                });
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));

        long usedSeats = userRepository.countByTenantIdAndStatus(tenant.getId(), User.UserStatus.ACTIVE);
        return mapToResponse(tenant, (int) usedSeats);
    }

    @Transactional(readOnly = true)
    public TenantResponse getCurrentTenant(Long tenantId) {
        if (tenantId == null) {
            throw new TenantNotFoundException("No tenant context associated with current user");
        }
        return getTenantById(tenantId);
    }

    @Auditable(eventType = AuditEvent.EventType.TENANT_MANAGEMENT, action = "UPDATE_TENANT", resourceType = "TENANT", resourceId = "#id")
    @Transactional
    public TenantResponse updateTenant(Long id, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));

        if (StringUtils.hasText(request.getName())) {
            tenant.setName(request.getName().trim());
        }

        if (StringUtils.hasText(request.getSubscriptionTier())) {
            try {
                tenant.setSubscriptionTier(Tenant.SubscriptionTier.valueOf(request.getSubscriptionTier().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid subscription tier: " + request.getSubscriptionTier());
            }
        }

        if (request.getMetadata() != null) {
            tenant.setMetadata(request.getMetadata());
        }

        tenant = tenantRepository.save(tenant);
        long usedSeats = userRepository.countByTenantIdAndStatus(tenant.getId(), User.UserStatus.ACTIVE);
        return mapToResponse(tenant, (int) usedSeats);
    }

    @Auditable(eventType = AuditEvent.EventType.TENANT_MANAGEMENT, action = "CHANGE_TENANT_STATUS", resourceType = "TENANT", resourceId = "#id")
    @Transactional
    public TenantResponse updateStatus(Long id, UpdateStatusRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));

        try {
            tenant.setStatus(Tenant.TenantStatus.valueOf(request.getStatus().trim().toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid status: " + request.getStatus());
        }

        tenant = tenantRepository.save(tenant);
        long usedSeats = userRepository.countByTenantIdAndStatus(tenant.getId(), User.UserStatus.ACTIVE);
        return mapToResponse(tenant, (int) usedSeats);
    }

    @Auditable(eventType = AuditEvent.EventType.CONFIG_CHANGE, action = "UPDATE_SEATS", resourceType = "TENANT", resourceId = "#id")
    @Transactional
    public TenantResponse updateSeats(Long id, UpdateSeatsRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));

        long usedSeats = userRepository.countByTenantIdAndStatus(tenant.getId(), User.UserStatus.ACTIVE);

        if (request.getMaxSeats() < usedSeats) {
            throw new InvalidQuotaException(
                    "Cannot reduce seats below current usage. Used: " + usedSeats + ", requested: " + request.getMaxSeats());
        }

        tenant.setMaxSeats(request.getMaxSeats());
        tenant = tenantRepository.save(tenant);
        return mapToResponse(tenant, (int) usedSeats);
    }

    @Auditable(eventType = AuditEvent.EventType.TENANT_MANAGEMENT, action = "DELETE_TENANT", resourceType = "TENANT", resourceId = "#id")
    @Transactional
    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));

        tenant.setStatus(Tenant.TenantStatus.INACTIVE);
        tenantRepository.save(tenant);

        List<User> users = userRepository.findByTenantId(tenant.getId());
        for (User user : users) {
            user.setStatus(User.UserStatus.INACTIVE);
        }
        userRepository.saveAll(users);
        log.info("Soft-deleted tenant id: {} and deactivated {} tenant users", id, users.size());
    }

    private TenantResponse mapToResponse(Tenant tenant, int usedSeats) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .tenantUuid(tenant.getTenantUuid() != null ? tenant.getTenantUuid().toString() : null)
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .status(tenant.getStatus() != null ? tenant.getStatus().name() : null)
                .maxSeats(tenant.getMaxSeats())
                .usedSeats(usedSeats)
                .subscriptionTier(tenant.getSubscriptionTier() != null ? tenant.getSubscriptionTier().name() : null)
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .metadata(tenant.getMetadata())
                .build();
    }
}
