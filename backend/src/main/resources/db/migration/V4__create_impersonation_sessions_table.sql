-- V4: Create impersonation sessions table for supervised support access

CREATE TABLE impersonation_sessions (
    id BIGSERIAL PRIMARY KEY,
    session_uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    support_user_id BIGINT NOT NULL,
    target_user_id BIGINT NOT NULL,
    target_tenant_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' NOT NULL,
    reason TEXT NOT NULL,
    approved_by BIGINT,
    approval_reason TEXT,
    max_duration_minutes INTEGER DEFAULT 240 NOT NULL,
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
    terminated_at TIMESTAMP,
    termination_reason TEXT,
    impersonation_token TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_impersonation_support_user FOREIGN KEY (support_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_impersonation_target_user FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_impersonation_target_tenant FOREIGN KEY (target_tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_impersonation_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_impersonation_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED', 'ACTIVE', 'EXPIRED', 'TERMINATED')
    ),
    CONSTRAINT chk_impersonation_max_duration CHECK (
        max_duration_minutes > 0 AND max_duration_minutes <= 240
    )
);

CREATE INDEX idx_impersonation_sessions_support_user ON impersonation_sessions(support_user_id);
CREATE INDEX idx_impersonation_sessions_target_user ON impersonation_sessions(target_user_id);
CREATE INDEX idx_impersonation_sessions_target_tenant ON impersonation_sessions(target_tenant_id);
CREATE INDEX idx_impersonation_sessions_status ON impersonation_sessions(status);
CREATE INDEX idx_impersonation_sessions_expires_at ON impersonation_sessions(expires_at);
