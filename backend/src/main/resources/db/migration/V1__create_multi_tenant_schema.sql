-- V1: Create multi-tenant foundation schema

-- 1. Create tenants table
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

-- 2. Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    username VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    auth_provider VARCHAR(50) DEFAULT 'LOCAL',
    microsoft_oid VARCHAR(255),
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

-- 3. Create user invitations table
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

-- 4. Seed default platform admin tenant
INSERT INTO tenants (name, slug, status, max_seats, subscription_tier)
VALUES ('Platform Administration', 'platform-admin', 'ACTIVE', 999, 'ENTERPRISE');

-- 5. Seed default platform admin user (default credentials: admin@npsbox.io / Admin@123)
INSERT INTO users (tenant_id, email, username, password_hash, first_name, last_name, role, status, auth_provider)
VALUES (
    (SELECT id FROM tenants WHERE slug = 'platform-admin'),
    'admin@npsbox.io',
    'admin',
    '$2a$10$wSuCD2gI6tUSXIFdEUWyJeKB.t19/NhL9ZaWYVEcYUj//DrOenBIy',
    'Platform',
    'Administrator',
    'PLATFORM_ADMIN',
    'ACTIVE',
    'LOCAL'
);
