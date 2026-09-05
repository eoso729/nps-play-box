# Phase 3: Organization & Tenant Management API

## Objective
Build REST APIs for platform administrators to create, read, update, and manage tenant organizations. Provide endpoints for tenant configuration, status management, and subscription tier updates.

**Duration**: 3-4 days  
**Dependencies**: Phase 1 (Database), Phase 2 (Authentication)

---

## API Endpoints

### 3.1 Tenant Management Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/v1/tenants` | Create new tenant | Platform Admin |
| GET | `/api/v1/tenants` | List all tenants (paginated) | Platform Admin |
| GET | `/api/v1/tenants/{id}` | Get tenant details | Platform Admin, Tenant Admin (own) |
| PUT | `/api/v1/tenants/{id}` | Update tenant | Platform Admin |
| PATCH | `/api/v1/tenants/{id}/status` | Update tenant status | Platform Admin |
| PATCH | `/api/v1/tenants/{id}/seats` | Update seat quota | Platform Admin |
| DELETE | `/api/v1/tenants/{id}` | Soft delete tenant | Platform Admin |
| GET | `/api/v1/tenants/current` | Get current tenant info | Any authenticated user |

---

## Backend Implementation

### 1.1 Create DTOs

**File**: `backend/src/main/java/org/example/signer/dto/tenant/CreateTenantRequest.java`

```java
package org.example.signer.dto.tenant;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateTenantRequest {
    
    @NotBlank(message = "Tenant name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;
    
    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Size(min = 2, max = 100, message = "Slug must be between 2 and 100 characters")
    private String slug;
    
    @NotNull(message = "Max seats is required")
    @Min(value = 1, message = "At least 1 seat is required")
    @Max(value = 10000, message = "Maximum 10000 seats allowed")
    private Integer maxSeats;
    
    @NotNull(message = "Subscription tier is required")
    private String subscriptionTier; // TRIAL, STANDARD, PROFESSIONAL, ENTERPRISE
    
    // Admin user details for initial setup
    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;
    
    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;
    
    @NotBlank(message = "Admin last name is required")
    private String adminLastName;
    
    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String adminPassword;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/tenant/UpdateTenantRequest.java`

```java
package org.example.signer.dto.tenant;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTenantRequest {
    
    @Size(min = 2, max = 255)
    private String name;
    
    private String subscriptionTier;
    
    private String metadata; // JSON string
}
```

**File**: `backend/src/main/java/org/example/signer/dto/tenant/TenantResponse.java`

```java
package org.example.signer.dto.tenant;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TenantResponse {
    private Long id;
    private String tenantUuid;
    private String name;
    private String slug;
    private String status;
    private Integer maxSeats;
    private Integer usedSeats;
    private String subscriptionTier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String metadata;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/tenant/UpdateStatusRequest.java`

```java
package org.example.signer.dto.tenant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    
    @NotBlank(message = "Status is required")
    private String status; // ACTIVE, SUSPENDED, INACTIVE
    
    private String reason;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/tenant/UpdateSeatsRequest.java`

```java
package org.example.signer.dto.tenant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSeatsRequest {
    
    @NotNull(message = "Max seats is required")
    @Min(value = 1, message = "At least 1 seat is required")
    private Integer maxSeats;
}
```

### 1.2 Create Tenant Service

**File**: `backend/src/main/java/org/example/signer/service/TenantService.java`

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import org.example.signer.dto.tenant.*;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantService {
    
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        // Validate slug uniqueness
        if (tenantRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Slug already exists: " + request.getSlug());
        }
        
        // Create tenant
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(request.getMaxSeats())
                .subscriptionTier(Tenant.SubscriptionTier.valueOf(request.getSubscriptionTier()))
                .build();
        
        tenant = tenantRepository.save(tenant);
        
        // Create admin user for tenant
        User adminUser = User.builder()
                .tenantId(tenant.getId())
                .email(request.getAdminEmail())
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build();
        
        userRepository.save(adminUser);
        
        return mapToResponse(tenant, 1);
    }
    
    public Page<TenantResponse> getAllTenants(Pageable pageable) {
        return tenantRepository.findAll(pageable)
                .map(tenant -> {
                    long usedSeats = userRepository.countByTenantIdAndStatus(
                            tenant.getId(), User.UserStatus.ACTIVE);
                    return mapToResponse(tenant, (int) usedSeats);
                });
    }
    
    public TenantResponse getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + id));
        
        long usedSeats = userRepository.countByTenantIdAndStatus(
                tenant.getId(), User.UserStatus.ACTIVE);
        
        return mapToResponse(tenant, (int) usedSeats);
    }
    
    @Transactional
    public TenantResponse updateTenant(Long id, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + id));
        
        if (request.getName() != null) {
            tenant.setName(request.getName());
        }
        
        if (request.getSubscriptionTier() != null) {
            tenant.setSubscriptionTier(Tenant.SubscriptionTier.valueOf(request.getSubscriptionTier()));
        }
        
        if (request.getMetadata() != null) {
            tenant.setMetadata(request.getMetadata());
        }
        
        tenant = tenantRepository.save(tenant);
        
        long usedSeats = userRepository.countByTenantIdAndStatus(
                tenant.getId(), User.UserStatus.ACTIVE);
        
        return mapToResponse(tenant, (int) usedSeats);
    }
    
    @Transactional
    public TenantResponse updateStatus(Long id, UpdateStatusRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + id));
        
        tenant.setStatus(Tenant.TenantStatus.valueOf(request.getStatus()));
        tenant = tenantRepository.save(tenant);
        
        long usedSeats = userRepository.countByTenantIdAndStatus(
                tenant.getId(), User.UserStatus.ACTIVE);
        
        return mapToResponse(tenant, (int) usedSeats);
    }
    
    @Transactional
    public TenantResponse updateSeats(Long id, UpdateSeatsRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + id));
        
        long usedSeats = userRepository.countByTenantIdAndStatus(
                tenant.getId(), User.UserStatus.ACTIVE);
        
        if (request.getMaxSeats() < usedSeats) {
            throw new IllegalArgumentException(
                    "Cannot reduce seats below current usage. Used: " + usedSeats);
        }
        
        tenant.setMaxSeats(request.getMaxSeats());
        tenant = tenantRepository.save(tenant);
        
        return mapToResponse(tenant, (int) usedSeats);
    }
    
    public TenantResponse getCurrentTenant(Long tenantId) {
        return getTenantById(tenantId);
    }
    
    private TenantResponse mapToResponse(Tenant tenant, int usedSeats) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .tenantUuid(tenant.getTenantUuid().toString())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .status(tenant.getStatus().name())
                .maxSeats(tenant.getMaxSeats())
                .usedSeats(usedSeats)
                .subscriptionTier(tenant.getSubscriptionTier().name())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .metadata(tenant.getMetadata())
                .build();
    }
}
```

### 1.3 Create Tenant Controller

**File**: `backend/src/main/java/org/example/signer/controller/TenantController.java`

```java
package org.example.signer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.signer.dto.tenant.*;
import org.example.signer.security.RequirePlatformAdmin;
import org.example.signer.service.TenantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {
    
    private final TenantService tenantService;
    
    @RequirePlatformAdmin
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.createTenant(request));
    }
    
    @RequirePlatformAdmin
    @GetMapping
    public ResponseEntity<Page<TenantResponse>> getAllTenants(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(tenantService.getAllTenants(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenant(
            @PathVariable Long id,
            @RequestAttribute("tenantId") Long currentTenantId,
            Authentication authentication) {
        
        // Platform admins can view any tenant, others only their own
        boolean isPlatformAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_PLATFORM_ADMIN"));
        
        if (!isPlatformAdmin && !id.equals(currentTenantId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }
    
    @GetMapping("/current")
    public ResponseEntity<TenantResponse> getCurrentTenant(
            @RequestAttribute("tenantId") Long tenantId) {
        return ResponseEntity.ok(tenantService.getCurrentTenant(tenantId));
    }
    
    @RequirePlatformAdmin
    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(tenantService.updateTenant(id, request));
    }
    
    @RequirePlatformAdmin
    @PatchMapping("/{id}/status")
    public ResponseEntity<TenantResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(tenantService.updateStatus(id, request));
    }
    
    @RequirePlatformAdmin
    @PatchMapping("/{id}/seats")
    public ResponseEntity<TenantResponse> updateSeats(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSeatsRequest request) {
        return ResponseEntity.ok(tenantService.updateSeats(id, request));
    }
}
```

---

## Testing Requirements

### 2.1 Service Layer Tests

**File**: `backend/src/test/java/org/example/signer/service/TenantServiceTest.java`

```java
@SpringBootTest
@Transactional
class TenantServiceTest {
    
    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldCreateTenantWithAdminUser() {
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Test Financial Corp");
        request.setSlug("test-financial");
        request.setMaxSeats(10);
        request.setSubscriptionTier("STANDARD");
        request.setAdminEmail("admin@testfinancial.com");
        request.setAdminFirstName("Admin");
        request.setAdminLastName("User");
        request.setAdminPassword("SecurePass123");
        
        TenantResponse response = tenantService.createTenant(request);
        
        assertNotNull(response.getId());
        assertEquals("test-financial", response.getSlug());
        assertEquals(10, response.getMaxSeats());
        assertEquals(1, response.getUsedSeats());
        
        // Verify admin user created
        User admin = userRepository.findByEmailAndTenantId(
                "admin@testfinancial.com", response.getId()).orElseThrow();
        assertEquals(User.UserRole.TENANT_ADMIN, admin.getRole());
    }
    
    @Test
    void shouldRejectDuplicateSlug() {
        CreateTenantRequest request1 = createTestTenantRequest("duplicate-slug");
        tenantService.createTenant(request1);
        
        CreateTenantRequest request2 = createTestTenantRequest("duplicate-slug");
        
        assertThrows(IllegalArgumentException.class, 
                () -> tenantService.createTenant(request2));
    }
    
    @Test
    void shouldUpdateTenantSeats() {
        CreateTenantRequest createReq = createTestTenantRequest("update-test");
        TenantResponse tenant = tenantService.createTenant(createReq);
        
        UpdateSeatsRequest updateReq = new UpdateSeatsRequest();
        updateReq.setMaxSeats(20);
        
        TenantResponse updated = tenantService.updateSeats(tenant.getId(), updateReq);
        
        assertEquals(20, updated.getMaxSeats());
    }
    
    @Test
    void shouldRejectSeatReductionBelowUsage() {
        // Create tenant with 10 seats and 1 user
        CreateTenantRequest createReq = createTestTenantRequest("seat-test");
        createReq.setMaxSeats(10);
        TenantResponse tenant = tenantService.createTenant(createReq);
        
        // Try to reduce to 0 seats (below usage of 1)
        UpdateSeatsRequest updateReq = new UpdateSeatsRequest();
        updateReq.setMaxSeats(0);
        
        assertThrows(IllegalArgumentException.class,
                () -> tenantService.updateSeats(tenant.getId(), updateReq));
    }
    
    private CreateTenantRequest createTestTenantRequest(String slug) {
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Test Tenant " + slug);
        request.setSlug(slug);
        request.setMaxSeats(5);
        request.setSubscriptionTier("STANDARD");
        request.setAdminEmail("admin@" + slug + ".com");
        request.setAdminFirstName("Admin");
        request.setAdminLastName("User");
        request.setAdminPassword("password123");
        return request;
    }
}
```

### 2.2 API Integration Tests

**File**: `backend/src/test/java/org/example/signer/controller/TenantControllerTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class TenantControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String platformAdminToken;
    
    @BeforeEach
    void setup() {
        // Create platform admin and get token
        platformAdminToken = createPlatformAdminAndGetToken();
    }
    
    @Test
    void platformAdminShouldCreateTenant() throws Exception {
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("New Financial Institution");
        request.setSlug("new-financial");
        request.setMaxSeats(15);
        request.setSubscriptionTier("PROFESSIONAL");
        request.setAdminEmail("admin@newfinancial.com");
        request.setAdminFirstName("John");
        request.setAdminLastName("Doe");
        request.setAdminPassword("SecurePassword123");
        
        mockMvc.perform(post("/api/v1/tenants")
                .header("Authorization", "Bearer " + platformAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("new-financial"))
                .andExpect(jsonPath("$.maxSeats").value(15))
                .andExpect(jsonPath("$.usedSeats").value(1));
    }
    
    @Test
    void tenantAdminShouldNotCreateTenant() throws Exception {
        String tenantAdminToken = createTenantAdminAndGetToken();
        
        CreateTenantRequest request = new CreateTenantRequest();
        // ... populate request
        
        mockMvc.perform(post("/api/v1/tenants")
                .header("Authorization", "Bearer " + tenantAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void shouldListAllTenantsForPlatformAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/tenants")
                .header("Authorization", "Bearer " + platformAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
```

---

## API Documentation

Add Swagger annotations to controller:

```java
@Tag(name = "Tenant Management", description = "APIs for managing tenant organizations")
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {
    
    @Operation(summary = "Create new tenant", description = "Platform admins only")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tenant created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "409", description = "Slug already exists")
    })
    @RequirePlatformAdmin
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(...) {
        // ...
    }
}
```

---

## Acceptance Criteria

- ✅ Platform admins can create new tenants with admin users
- ✅ Slug uniqueness enforced across all tenants
- ✅ Tenant creation automatically creates first admin user
- ✅ Seat quota can be updated (but not below current usage)
- ✅ Tenant status can be changed (ACTIVE, SUSPENDED, INACTIVE)
- ✅ Tenant admins can only view their own tenant details
- ✅ Platform admins can view all tenants with pagination
- ✅ Used seats calculated correctly based on active users
- ✅ Validation prevents invalid subscription tiers
- ✅ API documentation generated in Swagger UI

---

## Implementation Notes

1. **Slug Validation**: Use lowercase, numbers, and hyphens only
2. **Initial Password**: Consider forcing password change on first login
3. **Metadata Field**: Use for storing arbitrary configuration (billing info, etc.)
4. **Soft Deletes**: Consider adding `deleted_at` timestamp instead of hard deletes
5. **Audit Trail**: Log all tenant management operations (Phase 6)

---

## Next Phase

Once Phase 3 is complete and tested, proceed to:
**[Phase 4: Team & User Management →](./phase-4-team-user-management.md)**