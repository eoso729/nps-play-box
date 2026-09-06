-- V2: Create seat requests table for quota management

CREATE TABLE seat_requests (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    current_seats INTEGER NOT NULL,
    requested_additional_seats INTEGER NOT NULL,
    approved_seats INTEGER,
    justification TEXT,
    expected_growth TEXT,
    contact_email VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PENDING' NOT NULL,
    requested_by BIGINT NOT NULL,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    denial_reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_seat_requests_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_seat_requests_requester FOREIGN KEY (requested_by) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_seat_requests_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_request_status CHECK (status IN ('PENDING', 'APPROVED', 'DENIED'))
);

CREATE INDEX idx_seat_requests_tenant_id ON seat_requests(tenant_id);
CREATE INDEX idx_seat_requests_status ON seat_requests(status);
CREATE INDEX idx_seat_requests_created_at ON seat_requests(created_at);
