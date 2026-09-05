-- V3: Create immutable audit trail schema

CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,
    event_uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    tenant_id BIGINT,
    user_id BIGINT,
    event_type VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(255),
    status VARCHAR(20) DEFAULT 'SUCCESS' NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_id VARCHAR(100),
    metadata JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_audit_event_type CHECK (
        event_type IN (
            'AUTH',
            'USER_MANAGEMENT',
            'TENANT_MANAGEMENT',
            'ISO20022_OPERATION',
            'CONFIG_CHANGE',
            'IMPERSONATION',
            'DATA_ACCESS',
            'DATA_MODIFICATION'
        )
    ),
    CONSTRAINT chk_audit_status CHECK (
        status IN ('SUCCESS', 'FAILURE', 'PARTIAL')
    )
);

CREATE INDEX idx_audit_events_tenant_id ON audit_events(tenant_id);
CREATE INDEX idx_audit_events_user_id ON audit_events(user_id);
CREATE INDEX idx_audit_events_event_type ON audit_events(event_type);
CREATE INDEX idx_audit_events_created_at ON audit_events(created_at);
CREATE INDEX idx_audit_events_action ON audit_events(action);
CREATE INDEX idx_audit_events_resource ON audit_events(resource_type, resource_id);
CREATE INDEX idx_audit_events_request_id ON audit_events(request_id);
CREATE INDEX idx_audit_events_tenant_created ON audit_events(tenant_id, created_at DESC);
