-- V5: Create ISO 20022 messages, tenant simulator profiles, test scenarios, and validation results tables

-- 1. Create ISO 20022 Messages Table
CREATE TABLE iso20022_messages (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    message_uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    message_type VARCHAR(100) NOT NULL,
    message_code VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    
    -- Message content
    raw_xml TEXT NOT NULL,
    signed_xml TEXT,
    encrypted_xml TEXT,
    
    -- Processing metadata
    status VARCHAR(50) DEFAULT 'DRAFT' NOT NULL,
    created_by BIGINT,
    processed_at TIMESTAMP,
    sent_at TIMESTAMP,
    received_at TIMESTAMP,
    
    -- Transaction tracking & correlation
    transaction_reference VARCHAR(255),
    end_to_end_id VARCHAR(255),
    message_id VARCHAR(255),
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    metadata JSONB,
    
    CONSTRAINT fk_messages_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_direction CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    CONSTRAINT chk_status CHECK (status IN (
        'DRAFT', 'VALIDATED', 'SIGNED', 'ENCRYPTED', 
        'SENT', 'DELIVERED', 'FAILED', 'REJECTED'
    ))
);

CREATE INDEX idx_messages_tenant_id ON iso20022_messages(tenant_id);
CREATE INDEX idx_messages_tenant_type ON iso20022_messages(tenant_id, message_type);
CREATE INDEX idx_messages_tenant_status ON iso20022_messages(tenant_id, status);
CREATE INDEX idx_messages_transaction_ref ON iso20022_messages(tenant_id, transaction_reference);
CREATE INDEX idx_messages_msg_id ON iso20022_messages(message_id);
CREATE INDEX idx_messages_end_to_end ON iso20022_messages(end_to_end_id);
CREATE INDEX idx_messages_created_at ON iso20022_messages(tenant_id, created_at DESC);

-- 2. Create Tenant Simulator Profiles Table
CREATE TABLE tenant_simulator_profiles (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT UNIQUE NOT NULL,
    profile_uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Pseudo-Bank Identification
    institution_code VARCHAR(20) NOT NULL,
    institution_name VARCHAR(255) NOT NULL,
    bic VARCHAR(50) NOT NULL,
    scheme_code VARCHAR(50),
    
    -- Default Account Settings for Mock Flows
    default_currency VARCHAR(3) DEFAULT 'NGN' NOT NULL,
    default_account_number VARCHAR(50),
    default_account_name VARCHAR(255),
    default_bvn VARCHAR(20),
    
    -- Dispatch & Simulator Routing
    callback_url VARCHAR(500),
    auto_respond_inbound BOOLEAN DEFAULT TRUE NOT NULL,
    
    -- Audit
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_sim_profiles_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_sim_profiles_updater FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_sim_profiles_tenant_id ON tenant_simulator_profiles(tenant_id);
CREATE INDEX idx_sim_profiles_inst_code ON tenant_simulator_profiles(institution_code);

-- 3. Create Test Scenarios Table
CREATE TABLE test_scenarios (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scenario_uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Scenario details
    scenario_name VARCHAR(255) NOT NULL,
    scenario_description TEXT,
    message_type VARCHAR(100) NOT NULL,
    test_category VARCHAR(100) NOT NULL,
    
    -- Test data
    input_data JSONB NOT NULL,
    expected_output JSONB,
    validation_rules JSONB,
    
    -- Status and results
    status VARCHAR(50) DEFAULT 'DRAFT' NOT NULL,
    last_run_at TIMESTAMP,
    last_run_status VARCHAR(50),
    execution_count INTEGER DEFAULT 0,
    
    -- Audit
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_scenarios_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_scenarios_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_scenario_status CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_test_category CHECK (test_category IN (
        'FUNCTIONAL', 'VALIDATION', 'INTEGRATION', 
        'PERFORMANCE', 'SECURITY', 'REGRESSION'
    )),
    CONSTRAINT uq_tenant_scenario_name UNIQUE (tenant_id, scenario_name)
);

CREATE INDEX idx_scenarios_tenant_id ON test_scenarios(tenant_id);
CREATE INDEX idx_scenarios_tenant_type ON test_scenarios(tenant_id, message_type);
CREATE INDEX idx_scenarios_tenant_category ON test_scenarios(tenant_id, test_category);
CREATE INDEX idx_scenarios_tenant_status ON test_scenarios(tenant_id, status);

-- 4. Create Validation Results Table
CREATE TABLE validation_results (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    result_uuid UUID DEFAULT gen_random_uuid() NOT NULL UNIQUE,
    
    -- Associated entities
    message_id BIGINT,
    scenario_id BIGINT,
    
    -- Validation details
    validation_type VARCHAR(100) NOT NULL,
    validation_engine VARCHAR(100) NOT NULL,
    
    -- Results
    is_valid BOOLEAN NOT NULL,
    error_count INTEGER DEFAULT 0,
    warning_count INTEGER DEFAULT 0,
    errors JSONB,
    warnings JSONB,
    
    -- Processing info
    execution_time_ms INTEGER,
    validated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    validated_by BIGINT,
    
    -- XML snapshots
    validated_xml TEXT,
    
    CONSTRAINT fk_validation_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_validation_message FOREIGN KEY (message_id) REFERENCES iso20022_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_validation_scenario FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_validation_user FOREIGN KEY (validated_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_validation_type CHECK (validation_type IN (
        'SCHEMA', 'BUSINESS_RULES', 'NIBSS_RULES', 
        'SECURITY', 'COMPREHENSIVE'
    )),
    CONSTRAINT chk_validation_engine CHECK (validation_engine IN (
        'XSD', 'NIBSS_VALIDATOR', 'CUSTOM', 'COMBINED'
    ))
);

CREATE INDEX idx_validation_tenant_id ON validation_results(tenant_id);
CREATE INDEX idx_validation_message_id ON validation_results(tenant_id, message_id);
CREATE INDEX idx_validation_scenario_id ON validation_results(tenant_id, scenario_id);
CREATE INDEX idx_validation_tenant_type ON validation_results(tenant_id, validation_type);
CREATE INDEX idx_validation_tenant_valid ON validation_results(tenant_id, is_valid);
CREATE INDEX idx_validation_created_at ON validation_results(tenant_id, validated_at DESC);

-- 5. Seed default simulator profiles for existing tenants
INSERT INTO tenant_simulator_profiles (tenant_id, institution_code, institution_name, bic, scheme_code, default_currency)
SELECT 
    t.id,
    '999057',
    t.name || ' (Simulator)',
    'SIMUNGLAXXX',
    '999057',
    'NGN'
FROM tenants t
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_simulator_profiles p WHERE p.tenant_id = t.id
);
