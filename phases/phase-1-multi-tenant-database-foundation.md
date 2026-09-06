# Phase 1: Multi-Tenant Database Foundation

## Objective
Establish the database schema foundation with tenant isolation built into the data model. Every table that stores tenant-specific data must include a `tenant_id` column with appropriate constraints and indexes.

**Duration**: 3-5 days  
**Dependencies**: None

---

## Database Changes

### 1.1 Create Tenants Table

```sql
CREATE TABLE tenants (
    id BIGSERIAL PRIMARY KEY,
    tenant_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    max_seats INTEGER NOT NULL DEFAULT 5,
    subscription_tier VARCHAR(50) DEFAULT 'STANDARD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE')),
    CONSTRAINT chk_tier CHECK (subscription_tier IN ('TRIAL', 'STANDARD', 'PROFESSIONAL', 'ENTERPRISE'))
);

CREATE INDEX idx_tenants_slug ON tenants(slug);
CREATE INDEX idx_tenants_status ON tenants(status);
```

### 1.2 Create Users Table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT chk_role CHECK (role IN ('PLATFORM_ADMIN', 'TENANT_ADMIN', 'DEVELOPER', 'VIEWER')),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING')),
    CONSTRAINT uq_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(tenant_id, status);
```

### 1.3 Create User Invitations Table

```sql
CREATE TABLE user_invitations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    invitation_token VARCHAR(255) UNIQUE NOT NULL,
    invited_by BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_invitations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_inviter FOREIGN KEY (invited_by) REFERENCES users(id),
    CONSTRAINT chk_invitation_role CHECK (role IN ('TENANT_ADMIN', 'DEVELOPER', 'VIEWER'))
);

CREATE INDEX idx_invitations_tenant_id ON user_invitations(tenant_id);
CREATE INDEX idx_invitations_token ON user_invitations(invitation_token);
CREATE INDEX idx_invitations_expires_at ON user_invitations(expires_at);
```

### 1.4 Migrate Existing ISO 20022 Tables (if they exist)

Add `tenant_id` to existing tables:

```sql
-- Example: Add tenant_id to messages table (adjust based on your schema)
ALTER TABLE iso20022_messages 
ADD COLUMN tenant_id BIGINT,
ADD CONSTRAINT fk_messages_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

CREATE INDEX idx_messages_tenant_id ON iso20022_messages(tenant_id);

-- Repeat for other domain tables:
-- - certificates
-- - signing_keys
-- - test_scenarios
-- - validation_results
```

---

## Backend Implementation

### 2.1 Create Entity Classes

**File**: `backend/src/main/java/org/example/signer/entity/Tenant.java`

```java
package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_uuid", unique = true, nullable = false, updatable = false)
    private UUID tenantUuid;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false, length = 100)
    private String slug;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status;
    
    @Column(name = "max_seats", nullable = false)
    private Integer maxSeats = 5;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier")
    private SubscriptionTier subscriptionTier;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(columnDefinition = "jsonb")
    private String metadata;
    
    @PrePersist
    public void prePersist() {
        if (tenantUuid == null) {
            tenantUuid = UUID.randomUUID();
        }
        if (status == null) {
            status = TenantStatus.ACTIVE;
        }
        if (subscriptionTier == null) {
            subscriptionTier = SubscriptionTier.STANDARD;
        }
    }
    
    public enum TenantStatus {
        ACTIVE, SUSPENDED, INACTIVE
    }
    
    public enum SubscriptionTier {
        TRIAL, STANDARD, PROFESSIONAL, ENTERPRISE
    }
}
```

**File**: `backend/src/main/java/org/example/signer/entity/User.java`

```java
package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "email"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "user_uuid", unique = true, nullable = false, updatable = false)
    private UUID userUuid;
    
    @Column(nullable = false)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @PrePersist
    public void prePersist() {
        if (userUuid == null) {
            userUuid = UUID.randomUUID();
        }
        if (role == null) {
            role = UserRole.VIEWER;
        }
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }
    
    public enum UserRole {
        PLATFORM_ADMIN, TENANT_ADMIN, DEVELOPER, VIEWER
    }
    
    public enum UserStatus {
        ACTIVE, INACTIVE, PENDING
    }
}
```

**File**: `backend/src/main/java/org/example/signer/entity/UserInvitation.java`

```java
package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_invitations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInvitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private User.UserRole role;
    
    @Column(name = "invitation_token", unique = true, nullable = false)
    private String invitationToken;
    
    @Column(name = "invited_by", nullable = false)
    private Long invitedBy;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isAccepted() {
        return acceptedAt != null;
    }
}
```

### 2.2 Create Repository Interfaces

**File**: `backend/src/main/java/org/example/signer/repository/TenantRepository.java`

```java
package org.example.signer.repository;

import org.example.signer.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findByTenantUuid(UUID tenantUuid);
    boolean existsBySlug(String slug);
}
```

**File**: `backend/src/main/java/org/example/signer/repository/UserRepository.java`

```java
package org.example.signer.repository;

import org.example.signer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);
    Optional<User> findByUserUuid(UUID userUuid);
    List<User> findByTenantId(Long tenantId);
    long countByTenantIdAndStatus(Long tenantId, User.UserStatus status);
    boolean existsByTenantIdAndEmail(Long tenantId, String email);
}
```

**File**: `backend/src/main/java/org/example/signer/repository/UserInvitationRepository.java`

```java
package org.example.signer.repository;

import org.example.signer.entity.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByInvitationToken(String token);
    List<UserInvitation> findByTenantIdAndAcceptedAtIsNull(Long tenantId);
    void deleteByExpiresAtBeforeAndAcceptedAtIsNull(LocalDateTime cutoffDate);
}
```

---

## Database Migration

### 3.1 Setup Flyway (if not already configured)

**File**: `backend/src/main/resources/application.yml` (or `.properties`)

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

### 3.2 Create Migration Script

**File**: `backend/src/main/resources/db/migration/V1__create_multi_tenant_schema.sql`

```sql
-- V1: Create multi-tenant foundation schema

-- Create tenants table
CREATE TABLE tenants (
    id BIGSERIAL PRIMARY KEY,
    tenant_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    max_seats INTEGER NOT NULL DEFAULT 5,
    subscription_tier VARCHAR(50) DEFAULT 'STANDARD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE')),
    CONSTRAINT chk_tier CHECK (subscription_tier IN ('TRIAL', 'STANDARD', 'PROFESSIONAL', 'ENTERPRISE'))
);

CREATE INDEX idx_tenants_slug ON tenants(slug);
CREATE INDEX idx_tenants_status ON tenants(status);

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    
    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT chk_role CHECK (role IN ('PLATFORM_ADMIN', 'TENANT_ADMIN', 'DEVELOPER', 'VIEWER')),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING')),
    CONSTRAINT uq_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(tenant_id, status);

-- Create user invitations table
CREATE TABLE user_invitations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    invitation_token VARCHAR(255) UNIQUE NOT NULL,
    invited_by BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_invitations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_inviter FOREIGN KEY (invited_by) REFERENCES users(id),
    CONSTRAINT chk_invitation_role CHECK (role IN ('TENANT_ADMIN', 'DEVELOPER', 'VIEWER'))
);

CREATE INDEX idx_invitations_tenant_id ON user_invitations(tenant_id);
CREATE INDEX idx_invitations_token ON user_invitations(invitation_token);
CREATE INDEX idx_invitations_expires_at ON user_invitations(expires_at);

-- Create default platform admin tenant
INSERT INTO tenants (name, slug, status, max_seats, subscription_tier)
VALUES ('Platform Administration', 'platform-admin', 'ACTIVE', 999, 'ENTERPRISE');
```

---

## Testing Requirements

### 4.1 Unit Tests

Create test file: `backend/src/test/java/org/example/signer/repository/TenantRepositoryTest.java`

```java
@DataJpaTest
class TenantRepositoryTest {
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Test
    void shouldCreateTenant() {
        Tenant tenant = Tenant.builder()
            .name("Test Bank")
            .slug("test-bank")
            .status(Tenant.TenantStatus.ACTIVE)
            .maxSeats(10)
            .build();
        
        Tenant saved = tenantRepository.save(tenant);
        
        assertNotNull(saved.getId());
        assertNotNull(saved.getTenantUuid());
        assertEquals("test-bank", saved.getSlug());
    }
    
    @Test
    void shouldFindBySlug() {
        Tenant tenant = createTestTenant("acme-bank");
        
        Optional<Tenant> found = tenantRepository.findBySlug("acme-bank");
        
        assertTrue(found.isPresent());
        assertEquals(tenant.getId(), found.get().getId());
    }
    
    @Test
    void shouldEnforceUniqueSlug() {
        createTestTenant("duplicate");
        
        assertThrows(DataIntegrityViolationException.class, () -> {
            createTestTenant("duplicate");
        });
    }
}
```

Similar tests for `UserRepository` and `UserInvitationRepository`.

---

## Acceptance Criteria

- ✅ Database schema created with all tables and constraints
- ✅ Flyway migration runs successfully on clean database
- ✅ All entity classes mapped correctly with JPA annotations
- ✅ Repository interfaces created with required query methods
- ✅ Foreign key relationships enforced at database level
- ✅ Unique constraints prevent duplicate emails within tenant
- ✅ Default platform admin tenant created by migration
- ✅ Unit tests pass for all repository operations
- ✅ PostgreSQL data types (UUID, JSONB) work correctly
- ✅ Database indexes created for performance optimization

---

## Implementation Notes

1. **Run migrations first**: Test Flyway scripts on local PostgreSQL before implementing entities
2. **UUID generation**: Use database-level `gen_random_uuid()` for PostgreSQL compatibility
3. **Cascade deletes**: Tenant deletion cascades to users and invitations (use with caution)
4. **JSONB metadata**: Store arbitrary tenant configuration without schema changes
5. **Enum mapping**: Use `@Enumerated(EnumType.STRING)` for readability in database
6. **Timestamp handling**: Use `@CreationTimestamp` and `@UpdateTimestamp` for automatic tracking

---

## Next Phase

Once Phase 1 is complete and tested, proceed to:
**[Phase 2: Tenant-Aware Authentication & Authorization →](./phase-2-tenant-aware-authentication.md)**