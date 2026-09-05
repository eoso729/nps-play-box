# Phase 2: Tenant-Aware Authentication & Authorization

## Objective
Implement JWT-based authentication that includes tenant context in every token. Establish role-based access control (RBAC) with tenant isolation enforced at the authorization layer.

**Duration**: 4-6 days  
**Dependencies**: Phase 1 (Database Foundation)

---

## Backend Implementation

### 1.1 JWT Token Structure

JWT tokens must include tenant context:

```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "tenantId": 123,
  "tenantSlug": "acme-bank",
  "role": "TENANT_ADMIN",
  "iat": 1726272000,
  "exp": 1726358400
}
```

### 1.2 Create Security Configuration

**File**: `backend/src/main/java/org/example/signer/config/SecurityConfig.java`

```java
package org.example.signer.config;

import lombok.RequiredArgsConstructor;
import org.example.signer.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configure(http))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/api/v1/public/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/health"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 1.3 Create JWT Utility Service

**File**: `backend/src/main/java/org/example/signer/security/JwtService.java`

```java
package org.example.signer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.signer.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}") // 24 hours default
    private long jwtExpiration;
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Long extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get("tenantId", Long.class));
    }
    
    public String extractTenantSlug(String token) {
        return extractClaim(token, claims -> claims.get("tenantSlug", String.class));
    }
    
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public String generateToken(User user, String tenantSlug) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", user.getEmail());
        extraClaims.put("tenantId", user.getTenantId());
        extraClaims.put("tenantSlug", tenantSlug);
        extraClaims.put("role", user.getRole().name());
        
        return buildToken(extraClaims, user.getUserUuid().toString(), jwtExpiration);
    }
    
    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            long expiration
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        final Long tenantId = extractTenantId(token);
        return (username.equals(user.getUserUuid().toString())) 
                && (tenantId.equals(user.getTenantId()))
                && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Key getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### 1.4 Create JWT Authentication Filter

**File**: `backend/src/main/java/org/example/signer/security/JwtAuthenticationFilter.java`

```java
package org.example.signer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
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
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 1.5 Create UserDetailsService Implementation

**File**: `backend/src/main/java/org/example/signer/security/CustomUserDetailsService.java`

```java
package org.example.signer.security;

import lombok.RequiredArgsConstructor;
import org.example.signer.entity.User;
import org.example.signer.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Username is actually the userUuid in JWT
        UUID userUuid = UUID.fromString(username);
        User user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new org.springframework.security.core.userdetails.User(
                user.getUserUuid().toString(),
                user.getPasswordHash(),
                user.getStatus() == User.UserStatus.ACTIVE,
                true,
                true,
                true,
                getAuthorities(user)
        );
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}
```

### 1.6 Create Tenant Context Holder

**File**: `backend/src/main/java/org/example/signer/security/TenantContext.java`

```java
package org.example.signer.security;

public class TenantContext {
    
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();
    
    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }
    
    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }
    
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
```

### 1.7 Create Authentication Controller

**File**: `backend/src/main/java/org/example/signer/controller/AuthController.java`

```java
package org.example.signer.controller;

import lombok.RequiredArgsConstructor;
import org.example.signer.dto.AuthRequest;
import org.example.signer.dto.AuthResponse;
import org.example.signer.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Token invalidation logic (if needed)
        return ResponseEntity.noContent().build();
    }
}
```

### 1.8 Create DTOs

**File**: `backend/src/main/java/org/example/signer/dto/AuthRequest.java`

```java
package org.example.signer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "Tenant slug is required")
    private String tenantSlug;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/AuthResponse.java`

```java
package org.example.signer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;
    
    @Data
    @Builder
    public static class UserInfo {
        private String uuid;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private TenantInfo tenant;
    }
    
    @Data
    @Builder
    public static class TenantInfo {
        private Long id;
        private String name;
        private String slug;
    }
}
```

### 1.9 Create Authentication Service

**File**: `backend/src/main/java/org/example/signer/service/AuthService.java`

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import org.example.signer.dto.AuthRequest;
import org.example.signer.dto.AuthResponse;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        // Find tenant
        Tenant tenant = tenantRepository.findBySlug(request.getTenantSlug())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        if (tenant.getStatus() != Tenant.TenantStatus.ACTIVE) {
            throw new RuntimeException("Tenant account is not active");
        }
        
        // Find user
        User user = userRepository.findByEmailAndTenantId(request.getEmail(), tenant.getId())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("User account is not active");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate token
        String token = jwtService.generateToken(user, tenant.getSlug());
        
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours
                .user(AuthResponse.UserInfo.builder()
                        .uuid(user.getUserUuid().toString())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole().name())
                        .tenant(AuthResponse.TenantInfo.builder()
                                .id(tenant.getId())
                                .name(tenant.getName())
                                .slug(tenant.getSlug())
                                .build())
                        .build())
                .build();
    }
    
    public AuthResponse refreshToken(String token) {
        // Implementation for token refresh
        throw new UnsupportedOperationException("Refresh token not yet implemented");
    }
}
```

---

## Configuration

### 2.1 Application Properties

**File**: `backend/src/main/resources/application.yml`

```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-this-in-production}
  expiration: 86400000  # 24 hours in milliseconds

spring:
  security:
    user:
      name: admin
      password: admin  # Remove in production
```

---

## Testing Requirements

### 3.1 Integration Tests

**File**: `backend/src/test/java/org/example/signer/controller/AuthControllerTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setup() {
        // Create test tenant and user
        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Test Bank")
                .slug("test-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());
        
        userRepository.save(User.builder()
                .tenantId(tenant.getId())
                .email("admin@test-bank.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
    }
    
    @Test
    void shouldAuthenticateValidUser() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("admin@test-bank.com");
        request.setPassword("password123");
        request.setTenantSlug("test-bank");
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("admin@test-bank.com"))
                .andExpect(jsonPath("$.user.role").value("TENANT_ADMIN"))
                .andExpect(jsonPath("$.user.tenant.slug").value("test-bank"));
    }
    
    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("admin@test-bank.com");
        request.setPassword("wrongpassword");
        request.setTenantSlug("test-bank");
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void shouldRejectUserFromDifferentTenant() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("admin@test-bank.com");
        request.setPassword("password123");
        request.setTenantSlug("different-tenant");
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
```

---

## Security Annotations

### 4.1 Role-Based Access Control Annotations

Create custom annotations for method-level security:

**File**: `backend/src/main/java/org/example/signer/security/RequireTenantAdmin.java`

```java
package org.example.signer.security;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'PLATFORM_ADMIN')")
public @interface RequireTenantAdmin {
}
```

**File**: `backend/src/main/java/org/example/signer/security/RequirePlatformAdmin.java`

```java
package org.example.signer.security;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public @interface RequirePlatformAdmin {
}
```

Usage example:
```java
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {
    
    @RequirePlatformAdmin
    @PostMapping
    public ResponseEntity<TenantDto> createTenant(@RequestBody CreateTenantRequest request) {
        // Only platform admins can create tenants
    }
    
    @RequireTenantAdmin
    @GetMapping("/current/users")
    public ResponseEntity<List<UserDto>> getTenantUsers() {
        // Tenant admins can view their tenant's users
    }
}
```

---

## Acceptance Criteria

- ✅ JWT tokens include tenant_id, tenant_slug, and role claims
- ✅ Authentication endpoint validates credentials against tenant-scoped users
- ✅ Users from different tenants with same email can authenticate independently
- ✅ Inactive users and suspended tenants are rejected at login
- ✅ JWT filter extracts and validates tenant context on every request
- ✅ Role-based annotations enforce access control at method level
- ✅ Security configuration allows public endpoints (login, health check)
- ✅ Password hashing uses BCrypt with appropriate strength
- ✅ Integration tests verify tenant isolation in authentication
- ✅ Token expiration and refresh logic implemented

---

## Implementation Notes

1. **JWT Secret**: Generate a strong 256-bit secret for production use
2. **Token Storage**: Consider implementing token blacklist for logout functionality
3. **Password Policy**: Enforce minimum password requirements in registration flow
4. **Rate Limiting**: Add rate limiting to prevent brute force attacks (future phase)
5. **Audit Logging**: Log all authentication attempts (success and failure) for Phase 6

---

## Next Phase

Once Phase 2 is complete and tested, proceed to:
**[Phase 3: Organization & Tenant Management API →](./phase-3-tenant-management-api.md)**