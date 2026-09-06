package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.AuthRequest;
import org.example.signer.dto.AuthResponse;
import org.example.signer.dto.auth.RegisterRequestDto;
import org.example.signer.entity.AuditEvent;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.security.JwtService;
import org.example.signer.security.TenantUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service managing tenant-aware authentication, registration, token refresh, and user profile resolution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        String slug = StringUtils.hasText(request.getTenantSlug())
                ? request.getTenantSlug().trim()
                : "platform-admin";

        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElse(null);

        if (tenant == null) {
            auditService.logAuth(0L, null, "LOGIN", AuditEvent.EventStatus.FAILURE, "Tenant not found: " + slug);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found: " + slug);
        }

        if (tenant.getStatus() != Tenant.TenantStatus.ACTIVE) {
            auditService.logAuth(tenant.getId(), null, "LOGIN", AuditEvent.EventStatus.FAILURE, "Tenant account is not active");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant account is not active");
        }

        User user = userRepository.findByEmailAndTenantId(request.getEmail(), tenant.getId())
                .orElse(null);

        if (user == null) {
            auditService.logAuth(tenant.getId(), null, "LOGIN", AuditEvent.EventStatus.FAILURE, "Invalid email or password");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            auditService.logAuth(tenant.getId(), user.getId(), "LOGIN", AuditEvent.EventStatus.FAILURE, "User account is not active");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not active");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditService.logAuth(tenant.getId(), user.getId(), "LOGIN", AuditEvent.EventStatus.FAILURE, "Invalid email or password");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.logAuth(tenant.getId(), user.getId(), "LOGIN", AuditEvent.EventStatus.SUCCESS, null);

        return buildAuthResponse(user, tenant);
    }

    @Transactional
    public AuthResponse refreshToken(String authHeader) {
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            auditService.logAuth(0L, null, "TOKEN_REFRESH", AuditEvent.EventStatus.FAILURE, "Missing or malformed Authorization header");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            auditService.logAuth(0L, null, "TOKEN_REFRESH", AuditEvent.EventStatus.FAILURE, "Invalid or expired token");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String tokenType = jwtService.extractTokenType(token);
        if (!"REFRESH".equalsIgnoreCase(tokenType)) {
            auditService.logAuth(0L, null, "TOKEN_REFRESH", AuditEvent.EventStatus.FAILURE, "Token is not a valid refresh token");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is not a valid refresh token");
        }

        String userUuidStr = jwtService.extractUsername(token);
        Long tenantId = jwtService.extractTenantId(token);

        if (userUuidStr == null || tenantId == null) {
            auditService.logAuth(0L, null, "TOKEN_REFRESH", AuditEvent.EventStatus.FAILURE, "Token missing required tenant or user context");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token missing required tenant or user context");
        }

        User user;
        try {
            UUID userUuid = UUID.fromString(userUuidStr);
            user = userRepository.findByUserUuid(userUuid)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            user = userRepository.findByEmailOrUsername(userUuidStr, userUuidStr)
                    .orElse(null);
        }

        if (user == null) {
            auditService.logAuth(tenantId, null, "TOKEN_REFRESH", AuditEvent.EventStatus.FAILURE, "User not found");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            auditService.logAuth(tenantId, user.getId(), "TOKEN_REFRESH", AuditEvent.EventStatus.FAILURE, "User account is inactive");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElse(null);

        if (tenant == null) {
            auditService.logAuth(tenantId, user.getId(), "TOKEN_REFRESH", AuditEvent.EventStatus.FAILURE, "Tenant not found");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant not found");
        }

        if (tenant.getStatus() != Tenant.TenantStatus.ACTIVE) {
            auditService.logAuth(tenantId, user.getId(), "TOKEN_REFRESH", AuditEvent.EventStatus.FAILURE, "Tenant account is inactive");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant account is inactive");
        }

        auditService.logAuth(tenantId, user.getId(), "TOKEN_REFRESH", AuditEvent.EventStatus.SUCCESS, null);

        return buildAuthResponse(user, tenant);
    }

    @Transactional
    public AuthResponse register(RegisterRequestDto registerDto) {
        Tenant defaultTenant = tenantRepository.findBySlug("platform-admin")
                .orElseGet(() -> tenantRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No tenant configured")));

        if (userRepository.existsByTenantIdAndEmail(defaultTenant.getId(), registerDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        if (StringUtils.hasText(registerDto.getUsername()) && userRepository.existsByUsername(registerDto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        User user = User.builder()
                .tenantId(defaultTenant.getId())
                .userUuid(UUID.randomUUID())
                .username(StringUtils.hasText(registerDto.getUsername()) ? registerDto.getUsername() : registerDto.getEmail())
                .email(registerDto.getEmail())
                .passwordHash(passwordEncoder.encode(registerDto.getPassword()))
                .firstName(registerDto.getFirstName())
                .lastName(registerDto.getLastName())
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        auditService.logUserManagement(defaultTenant.getId(), savedUser.getId(), "REGISTER", String.valueOf(savedUser.getId()), AuditEvent.EventStatus.SUCCESS, null);
        return buildAuthResponse(savedUser, defaultTenant);
    }

    public AuthResponse.UserInfo getCurrentUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Object principal = authentication.getPrincipal();
        User user = null;
        String tenantSlug = "platform-admin";

        if (principal instanceof TenantUserDetails) {
            TenantUserDetails tud = (TenantUserDetails) principal;
            user = tud.getUser();
            tenantSlug = tud.getTenantSlug();
        } else if (principal instanceof User) {
            user = (User) principal;
            tenantSlug = tenantRepository.findById(user.getTenantId()).map(Tenant::getSlug).orElse("platform-admin");
        } else if (principal instanceof String) {
            String identifier = (String) principal;
            user = userRepository.findByEmailOrUsername(identifier, identifier).orElse(null);
            if (user != null) {
                tenantSlug = tenantRepository.findById(user.getTenantId()).map(Tenant::getSlug).orElse("platform-admin");
            }
        }

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        Tenant tenant = tenantRepository.findById(user.getTenantId()).orElse(null);
        String tenantName = tenant != null ? tenant.getName() : "Platform Administration";

        return AuthResponse.UserInfo.builder()
                .uuid(user.getUserUuid() != null ? user.getUserUuid().toString() : null)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() != null ? user.getRole().name() : "VIEWER")
                .tenant(AuthResponse.TenantInfo.builder()
                        .id(user.getTenantId())
                        .name(tenantName)
                        .slug(tenantSlug)
                        .build())
                .build();
    }

    private AuthResponse buildAuthResponse(User user, Tenant tenant) {
        String token = jwtService.generateToken(user, tenant.getSlug());
        String refreshToken = jwtService.generateRefreshToken(user, tenant.getSlug());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs() / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .uuid(user.getUserUuid() != null ? user.getUserUuid().toString() : null)
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole() != null ? user.getRole().name() : "VIEWER")
                        .tenant(AuthResponse.TenantInfo.builder()
                                .id(tenant.getId())
                                .name(tenant.getName())
                                .slug(tenant.getSlug())
                                .build())
                        .build())
                .build();
    }

    public void logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof TenantUserDetails tud) {
            Long userId = tud.getUser() != null ? tud.getUser().getId() : null;
            auditService.logAuth(tud.getTenantId(), userId, "LOGOUT", AuditEvent.EventStatus.SUCCESS, null);
        } else {
            auditService.logAuth(0L, null, "LOGOUT", AuditEvent.EventStatus.SUCCESS, null);
        }
    }
}
