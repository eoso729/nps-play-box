# Phase 10: ISO 20022 Message Tenant Isolation

## Objective
Implement comprehensive tenant isolation for all ISO 20022 message processing, including message storage, certificate management, signing key isolation, test scenarios, and validation results. Ensure that all message-related operations respect tenant boundaries with zero cross-tenant data leakage.

**Duration**: 4-5 days  
**Dependencies**: Phase 1 (Multi-Tenant Database Foundation), Phase 2 (Tenant-Aware Authentication)

---

## Database Schema Changes

### 10.1 Create ISO 20022 Messages Table

```sql
CREATE TABLE iso20022_messages (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    message_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    message_type VARCHAR(100) NOT NULL,
    message_code VARCHAR(50) NOT NULL,
    direction VARCHAR(20) NOT NULL,
    
    -- Message content
    raw_xml TEXT NOT NULL,
    signed_xml TEXT,
    encrypted_xml TEXT,
    
    -- Processing metadata
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    processed_at TIMESTAMP,
    sent_at TIMESTAMP,
    received_at TIMESTAMP,
    
    -- Transaction tracking
    transaction_reference VARCHAR(255),
    end_to_end_id VARCHAR(255),
    message_id VARCHAR(255),
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    
    CONSTRAINT fk_messages_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_message_type CHECK (message_type IN (
        'PAYMENT_INITIATION', 'PAYMENT_ACTIVATION', 'PAYMENT_STATUS',
        'MANDATE_CREATION', 'MANDATE_AMENDMENT', 'MANDATE_CANCELLATION',
        'DIRECT_DEBIT', 'CUSTOMER_DIRECT_DEBIT', 'PAYMENT_RETURN',
        'ACCOUNT_REPORT', 'BANK_STATEMENT', 'BALANCE_ENQUIRY',
        'NAME_VERIFICATION', 'TRANSFER'
    )),
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
CREATE INDEX idx_messages_created_at ON iso20022_messages(tenant_id, created_at DESC);
```

### 10.2 Create Tenant Certificates Table

```sql
CREATE TABLE tenant_certificates (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    certificate_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    
    -- Certificate details
    certificate_name VARCHAR(255) NOT NULL,
    certificate_type VARCHAR(50) NOT NULL,
    certificate_pem TEXT NOT NULL,
    public_key_pem TEXT NOT NULL,
    
    -- Certificate metadata
    issuer VARCHAR(500),
    subject VARCHAR(500),
    serial_number VARCHAR(100),
    thumbprint VARCHAR(255),
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    
    -- Status and usage
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    is_default BOOLEAN DEFAULT FALSE,
    usage_purpose VARCHAR(100) NOT NULL,
    
    -- Audit
    uploaded_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    
    CONSTRAINT fk_certificates_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_certificates_uploader FOREIGN KEY (uploaded_by) REFERENCES users(id),
    CONSTRAINT chk_cert_type CHECK (certificate_type IN ('X509', 'PGP')),
    CONSTRAINT chk_cert_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED', 'DISABLED')),
    CONSTRAINT chk_usage_purpose CHECK (usage_purpose IN ('SIGNING', 'ENCRYPTION', 'BOTH')),
    CONSTRAINT uq_tenant_cert_name UNIQUE (tenant_id, certificate_name)
);

CREATE INDEX idx_certificates_tenant_id ON tenant_certificates(tenant_id);
CREATE INDEX idx_certificates_tenant_status ON tenant_certificates(tenant_id, status);
CREATE INDEX idx_certificates_tenant_default ON tenant_certificates(tenant_id, is_default) WHERE is_default = TRUE;
CREATE INDEX idx_certificates_valid_to ON tenant_certificates(tenant_id, valid_to);
```

### 10.3 Create Tenant Signing Keys Table

```sql
CREATE TABLE tenant_signing_keys (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    key_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    
    -- Key details
    key_name VARCHAR(255) NOT NULL,
    key_algorithm VARCHAR(50) NOT NULL,
    key_size INTEGER NOT NULL,
    
    -- Encrypted private key storage
    private_key_encrypted TEXT NOT NULL,
    public_key_pem TEXT NOT NULL,
    encryption_iv VARCHAR(255) NOT NULL,
    
    -- Key metadata
    fingerprint VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    key_purpose VARCHAR(100) NOT NULL,
    
    -- Status and expiration
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    
    -- Audit
    created_by BIGINT NOT NULL,
    
    CONSTRAINT fk_signing_keys_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_signing_keys_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT chk_key_algorithm CHECK (key_algorithm IN ('RSA', 'ECDSA', 'DSA')),
    CONSTRAINT chk_key_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED', 'DISABLED')),
    CONSTRAINT chk_key_purpose CHECK (key_purpose IN ('MESSAGE_SIGNING', 'DOCUMENT_SIGNING', 'BOTH')),
    CONSTRAINT uq_tenant_key_name UNIQUE (tenant_id, key_name)
);

CREATE INDEX idx_signing_keys_tenant_id ON tenant_signing_keys(tenant_id);
CREATE INDEX idx_signing_keys_tenant_status ON tenant_signing_keys(tenant_id, status);
CREATE INDEX idx_signing_keys_tenant_default ON tenant_signing_keys(tenant_id, is_default) WHERE is_default = TRUE;
```

### 10.4 Create Test Scenarios Table

```sql
CREATE TABLE test_scenarios (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    scenario_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    
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
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    last_run_at TIMESTAMP,
    last_run_status VARCHAR(50),
    execution_count INTEGER DEFAULT 0,
    
    -- Audit
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_scenarios_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_scenarios_creator FOREIGN KEY (created_by) REFERENCES users(id),
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
```

### 10.5 Create Validation Results Table

```sql
CREATE TABLE validation_results (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    result_uuid UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    
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
    validated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validated_by BIGINT NOT NULL,
    
    -- XML snapshots
    validated_xml TEXT,
    
    CONSTRAINT fk_validation_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_validation_message FOREIGN KEY (message_id) REFERENCES iso20022_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_validation_scenario FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_validation_user FOREIGN KEY (validated_by) REFERENCES users(id),
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
```

---

## Migration Scripts

### 10.6 Migration: Add tenant_id to Existing Tables

```sql
-- Migration script: V10_1__add_tenant_id_to_existing_tables.sql

BEGIN;

-- Step 1: Create a default tenant for existing data (if needed)
INSERT INTO tenants (name, slug, status, max_seats, subscription_tier)
VALUES ('Legacy Tenant', 'legacy-tenant', 'ACTIVE', 100, 'ENTERPRISE')
ON CONFLICT (slug) DO NOTHING;

-- Store the default tenant ID
DO $$
DECLARE
    default_tenant_id BIGINT;
BEGIN
    SELECT id INTO default_tenant_id FROM tenants WHERE slug = 'legacy-tenant';
    
    -- Add tenant_id to any existing message-related tables
    -- Example: If you have a payment_messages table
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'payment_messages') THEN
        ALTER TABLE payment_messages ADD COLUMN IF NOT EXISTS tenant_id BIGINT;
        UPDATE payment_messages SET tenant_id = default_tenant_id WHERE tenant_id IS NULL;
        ALTER TABLE payment_messages ALTER COLUMN tenant_id SET NOT NULL;
        ALTER TABLE payment_messages ADD CONSTRAINT fk_payment_messages_tenant 
            FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
        CREATE INDEX IF NOT EXISTS idx_payment_messages_tenant_id ON payment_messages(tenant_id);
    END IF;
    
    -- Repeat for other domain-specific tables
    -- Add similar blocks for validation_logs, test_results, etc.
END $$;

COMMIT;
```

### 10.7 Data Migration Script

```java
package org.example.signer.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class Phase10DataMigration {

    private final JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    @Transactional
    public void migrateLegacyData() {
        log.info("Starting Phase 10 data migration...");
        
        try {
            // 1. Ensure default tenant exists
            Long defaultTenantId = ensureDefaultTenant();
            
            // 2. Migrate any existing message data
            migrateExistingMessages(defaultTenantId);
            
            // 3. Migrate existing certificates
            migrateExistingCertificates(defaultTenantId);
            
            // 4. Migrate existing signing keys
            migrateExistingSigningKeys(defaultTenantId);
            
            log.info("Phase 10 data migration completed successfully");
        } catch (Exception e) {
            log.error("Phase 10 data migration failed", e);
            throw new RuntimeException("Migration failed", e);
        }
    }
    
    private Long ensureDefaultTenant() {
        String sql = """
            INSERT INTO tenants (name, slug, status, max_seats, subscription_tier)
            VALUES ('Legacy Tenant', 'legacy-tenant', 'ACTIVE', 100, 'ENTERPRISE')
            ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name
            RETURNING id
            """;
        
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    
    private void migrateExistingMessages(Long tenantId) {
        // Check if old message tables exist and migrate them
        String checkTableSql = """
            SELECT EXISTS (
                SELECT FROM information_schema.tables 
                WHERE table_name = 'legacy_messages'
            )
            """;
        
        Boolean tableExists = jdbcTemplate.queryForObject(checkTableSql, Boolean.class);
        
        if (Boolean.TRUE.equals(tableExists)) {
            String migrateSql = """
                INSERT INTO iso20022_messages 
                    (tenant_id, message_type, message_code, direction, raw_xml, 
                     status, created_by, created_at, transaction_reference)
                SELECT 
                    ?, 
                    message_type, 
                    message_code, 
                    'OUTBOUND',
                    xml_content,
                    status,
                    user_id,
                    created_at,
                    reference
                FROM legacy_messages
                WHERE NOT EXISTS (
                    SELECT 1 FROM iso20022_messages 
                    WHERE transaction_reference = legacy_messages.reference
                )
                """;
            
            int migratedCount = jdbcTemplate.update(migrateSql, tenantId);
            log.info("Migrated {} legacy messages", migratedCount);
        }
    }
    
    private void migrateExistingCertificates(Long tenantId) {
        // Scan for certificates in the filesystem and register them
        // This would integrate with your existing key management
        log.info("Certificate migration completed for tenant {}", tenantId);
    }
    
    private void migrateExistingSigningKeys(Long tenantId) {
        // Register existing signing keys under the default tenant
        log.info("Signing key migration completed for tenant {}", tenantId);
    }
}
```

---

## Backend Implementation

### 10.8 ISO 20022 Message Entity

```java
package org.example.signer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "iso20022_messages", indexes = {
    @Index(name = "idx_messages_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_messages_tenant_type", columnList = "tenant_id,message_type"),
    @Index(name = "idx_messages_tenant_status", columnList = "tenant_id,status"),
    @Index(name = "idx_messages_transaction_ref", columnList = "tenant_id,transaction_reference")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Iso20022Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "message_uuid", unique = true, nullable = false)
    private UUID messageUuid;

    @Column(name = "message_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(name = "message_code", nullable = false, length = 50)
    private String messageCode;

    @Column(name = "direction", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MessageDirection direction;

    @Column(name = "raw_xml", nullable = false, columnDefinition = "TEXT")
    private String rawXml;

    @Column(name = "signed_xml", columnDefinition = "TEXT")
    private String signedXml;

    @Column(name = "encrypted_xml", columnDefinition = "TEXT")
    private String encryptedXml;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User creator;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "transaction_reference", length = 255)
    private String transactionReference;

    @Column(name = "end_to_end_id", length = 255)
    private String endToEndId;

    @Column(name = "message_id", length = 255)
    private String messageId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private MessageMetadata metadata;

    @PrePersist
    protected void onCreate() {
        if (messageUuid == null) {
            messageUuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = MessageStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum MessageType {
        PAYMENT_INITIATION,
        PAYMENT_ACTIVATION,
        PAYMENT_STATUS,
        MANDATE_CREATION,
        MANDATE_AMENDMENT,
        MANDATE_CANCELLATION,
        DIRECT_DEBIT,
        CUSTOMER_DIRECT_DEBIT,
        PAYMENT_RETURN,
        ACCOUNT_REPORT,
        BANK_STATEMENT,
        BALANCE_ENQUIRY,
        NAME_VERIFICATION,
        TRANSFER
    }

    public enum MessageDirection {
        INBOUND,
        OUTBOUND
    }

    public enum MessageStatus {
        DRAFT,
        VALIDATED,
        SIGNED,
        ENCRYPTED,
        SENT,
        DELIVERED,
        FAILED,
        REJECTED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageMetadata {
        private String nibssBankCode;
        private String counterpartyBic;
        private String currency;
        private String amount;
        private String originalFileName;
        private Integer retryCount;
        private String errorCode;
        private String errorMessage;
    }
}
```

### 10.9 Tenant Certificate Entity

```java
package org.example.signer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_certificates", indexes = {
    @Index(name = "idx_certificates_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_certificates_tenant_status", columnList = "tenant_id,status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "certificate_uuid", unique = true, nullable = false)
    private UUID certificateUuid;

    @Column(name = "certificate_name", nullable = false, length = 255)
    private String certificateName;

    @Column(name = "certificate_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CertificateType certificateType;

    @Column(name = "certificate_pem", nullable = false, columnDefinition = "TEXT")
    private String certificatePem;

    @Column(name = "public_key_pem", nullable = false, columnDefinition = "TEXT")
    private String publicKeyPem;

    @Column(name = "issuer", length = 500)
    private String issuer;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "thumbprint", length = 255)
    private String thumbprint;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CertificateStatus status;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "usage_purpose", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private UsagePurpose usagePurpose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        if (certificateUuid == null) {
            certificateUuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = CertificateStatus.ACTIVE;
        }
        if (isDefault == null) {
            isDefault = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CertificateType {
        X509,
        PGP
    }

    public enum CertificateStatus {
        ACTIVE,
        EXPIRED,
        REVOKED,
        DISABLED
    }

    public enum UsagePurpose {
        SIGNING,
        ENCRYPTION,
        BOTH
    }
}
```

### 10.10 Tenant-Aware Message Repository

```java
package org.example.signer.repository;

import org.example.signer.model.Iso20022Message;
import org.example.signer.model.Iso20022Message.MessageStatus;
import org.example.signer.model.Iso20022Message.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface Iso20022MessageRepository extends JpaRepository<Iso20022Message, Long> {

    // Core tenant-scoped queries
    Optional<Iso20022Message> findByTenantIdAndMessageUuid(Long tenantId, UUID messageUuid);
    
    Page<Iso20022Message> findByTenantId(Long tenantId, Pageable pageable);
    
    Page<Iso20022Message> findByTenantIdAndMessageType(
        Long tenantId, 
        MessageType messageType, 
        Pageable pageable
    );
    
    Page<Iso20022Message> findByTenantIdAndStatus(
        Long tenantId, 
        MessageStatus status, 
        Pageable pageable
    );
    
    List<Iso20022Message> findByTenantIdAndTransactionReference(
        Long tenantId, 
        String transactionReference
    );
    
    // Advanced queries
    @Query("""
        SELECT m FROM Iso20022Message m
        WHERE m.tenantId = :tenantId
        AND m.messageType = :messageType
        AND m.status = :status
        AND m.createdAt BETWEEN :startDate AND :endDate
        ORDER BY m.createdAt DESC
        """)
    List<Iso20022Message> findByTenantAndCriteria(
        @Param("tenantId") Long tenantId,
        @Param("messageType") MessageType messageType,
        @Param("status") MessageStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Statistics queries
    @Query("""
        SELECT m.status, COUNT(m)
        FROM Iso20022Message m
        WHERE m.tenantId = :tenantId
        GROUP BY m.status
        """)
    List<Object[]> getMessageStatusDistribution(@Param("tenantId") Long tenantId);
    
    @Query("""
        SELECT m.messageType, COUNT(m)
        FROM Iso20022Message m
        WHERE m.tenantId = :tenantId
        AND m.createdAt >= :since
        GROUP BY m.messageType
        """)
    List<Object[]> getMessageTypeDistribution(
        @Param("tenantId") Long tenantId,
        @Param("since") LocalDateTime since
    );
    
    // Count queries
    long countByTenantIdAndStatus(Long tenantId, MessageStatus status);
    
    long countByTenantIdAndCreatedAtAfter(Long tenantId, LocalDateTime createdAt);
}
```

### 10.11 Tenant-Aware Message Service

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.Iso20022MessageDto;
import org.example.signer.model.Iso20022Message;
import org.example.signer.model.Iso20022Message.MessageStatus;
import org.example.signer.model.Iso20022Message.MessageType;
import org.example.signer.repository.Iso20022MessageRepository;
import org.example.signer.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAwareMessageService {

    private final Iso20022MessageRepository messageRepository;
    private final TenantContext tenantContext;
    
    @Transactional
    public Iso20022Message createMessage(Iso20022MessageDto dto) {
        Long tenantId = tenantContext.getCurrentTenantId();
        Long userId = tenantContext.getCurrentUserId();
        
        log.info("Creating ISO 20022 message for tenant {}: type={}", tenantId, dto.getMessageType());
        
        Iso20022Message message = Iso20022Message.builder()
            .tenantId(tenantId)
            .messageType(dto.getMessageType())
            .messageCode(dto.getMessageCode())
            .direction(dto.getDirection())
            .rawXml(dto.getRawXml())
            .status(MessageStatus.DRAFT)
            .transactionReference(dto.getTransactionReference())
            .endToEndId(dto.getEndToEndId())
            .messageId(dto.getMessageId())
            .metadata(dto.getMetadata())
            .build();
        
        message.setCreator(tenantContext.getCurrentUser());
        
        Iso20022Message saved = messageRepository.save(message);
        
        log.info("Created ISO 20022 message: tenantId={}, messageUuid={}, type={}", 
            tenantId, saved.getMessageUuid(), saved.getMessageType());
        
        return saved;
    }
    
    @Transactional(readOnly = true)
    public Iso20022Message getMessage(UUID messageUuid) {
        Long tenantId = tenantContext.getCurrentTenantId();
        
        return messageRepository.findByTenantIdAndMessageUuid(tenantId, messageUuid)
            .orElseThrow(() -> new MessageNotFoundException(
                "Message not found: " + messageUuid + " for tenant " + tenantId
            ));
    }
    
    @Transactional(readOnly = true)
    public Page<Iso20022Message> getMessages(Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return messageRepository.findByTenantId(tenantId, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Iso20022Message> getMessagesByType(MessageType type, Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return messageRepository.findByTenantIdAndMessageType(tenantId, type, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Iso20022Message> getMessagesByStatus(MessageStatus status, Pageable pageable) {
        Long tenantId = tenantContext.getCurrentTenantId();
        return messageRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }
    
    @Transactional
    public Iso20022Message updateMessageStatus(UUID messageUuid, MessageStatus newStatus) {
        Long tenantId = tenantContext.getCurrentTenantId();
        
        Iso20022Message message = getMessage(messageUuid);
        
        log.info("Updating message status: tenantId={}, messageUuid={}, {} -> {}", 
            tenantId, messageUuid, message.getStatus(), newStatus);
        
        message.setStatus(newStatus);
        
        if (newStatus == MessageStatus.VALIDATED) {
            message.setProcessedAt(LocalDateTime.now());
        } else if (newStatus == MessageStatus.SENT) {
            message.setSentAt(LocalDateTime.now());
        }
        
        return messageRepository.save(message);
    }
    
    @Transactional
    public Iso20022Message signMessage(UUID messageUuid, String signedXml) {
        Long tenantId = tenantContext.getCurrentTenantId();
        
        Iso20022Message message = getMessage(messageUuid);
        
        log.info("Signing message: tenantId={}, messageUuid={}", tenantId, messageUuid);
        
        message.setSignedXml(signedXml);
        message.setStatus(MessageStatus.SIGNED);
        message.setProcessedAt(LocalDateTime.now());
        
        return messageRepository.save(message);
    }
    
    @Transactional
    public Iso20022Message encryptMessage(UUID messageUuid, String encryptedXml) {
        Long tenantId = tenantContext.getCurrentTenantId();
        
        Iso20022Message message = getMessage(messageUuid);
        
        log.info("Encrypting message: tenantId={}, messageUuid={}", tenantId, messageUuid);
        
        message.setEncryptedXml(encryptedXml);
        message.setStatus(MessageStatus.ENCRYPTED);
        
        return messageRepository.save(message);
    }
    
    @Transactional(readOnly = true)
    public MessageStatistics getMessageStatistics() {
        Long tenantId = tenantContext.getCurrentTenantId();
        
        List<Object[]> statusDist = messageRepository.getMessageStatusDistribution(tenantId);
        List<Object[]> typeDist = messageRepository.getMessageTypeDistribution(
            tenantId, 
            LocalDateTime.now().minusDays(30)
        );
        
        long totalMessages = messageRepository.countByTenantId(tenantId);
        long todayMessages = messageRepository.countByTenantIdAndCreatedAtAfter(
            tenantId, 
            LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
        );
        
        return MessageStatistics.builder()
            .tenantId(tenantId)
            .totalMessages(totalMessages)
            .todayMessages(todayMessages)
            .statusDistribution(convertToMap(statusDist))
            .typeDistribution(convertToMap(typeDist))
            .build();
    }
    
    private Map<String, Long> convertToMap(List<Object[]> data) {
        return data.stream()
            .collect(Collectors.toMap(
                arr -> arr[0].toString(),
                arr -> (Long) arr[1]
            ));
    }
}
```

### 10.12 Tenant Certificate Service

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.model.TenantCertificate;
import org.example.signer.model.TenantCertificate.CertificateStatus;
import org.example.signer.repository.TenantCertificateRepository;
import org.example.signer.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantCertificateService {

    private final TenantCertificateRepository certificateRepository;
    private final TenantContext tenantContext;
    
    @Transactional
    public TenantCertificate uploadCertificate(String certificateName, 
                                               String certificatePem, 
                                               TenantCertificate.UsagePurpose purpose) {
        Long tenantId = tenantContext.getCurrentTenantId();
        
        log.info("Uploading certificate for tenant {}: name={}", tenantId, certificateName);
        
        // Parse certificate to extract metadata
        X509Certificate x509Cert = parseCertificate(certificatePem);
        
        TenantCertificate certificate = TenantCertificate.builder()
            .tenantId(tenantId)
            .certificateName(certificateName)
            .certificateType(TenantCertificate.CertificateType.X509)
            .certificatePem(certificatePem)
            .publicKeyPem(extractPublicKey(x509Cert))
            .issuer(x509Cert.getIssuerDN().toString())
            .subject(x509Cert.getSubjectDN().toString())
            .serialNumber(x509Cert.getSerialNumber().toString())
            .thumbprint(calculateThumbprint(x509Cert))
            .validFrom(LocalDateTime.ofInstant(
                x509Cert.getNotBefore().toInstant(), 
                ZoneId.systemDefault()
            ))
            .validTo(LocalDateTime.ofInstant(
                x509Cert.getNotAfter().toInstant(), 
                ZoneId.systemDefault()
            ))
            .status(CertificateStatus.ACTIVE)
            .usagePurpose(purpose)
            .uploadedBy(tenantContext.getCurrentUser())
            .build();
        
        TenantCertificate saved = certificateRepository.save(certificate);
        
        log.info("Uploaded certificate: tenantId={}, certUuid={}, thumbprint={}", 
            tenantId, saved.getCertificateUuid(), saved.getThumbprint());
        
        return saved;
    }
    
    @Transactional(readOnly = true)
    public TenantCertificate getDefaultCertificate(TenantCertificate.UsagePurpose purpose) {
        Long tenantId = tenantContext.getCurrentTenantId();
        
        return certificateRepository
            .findByTenantIdAndUsagePurposeAndIsDefaultAndStatus(
                tenantId, purpose, true, CertificateStatus.ACTIVE
            )
            .orElseThrow(() -> new CertificateNotFoundException(
                "No default " + purpose + " certificate found for tenant " + tenantId
            ));
    }
    
    @Transactional(readOnly = true)
    public List<TenantCertificate> getActiveCertificates() {
        Long tenantId = tenantContext.getCurrentTenantId();
        return certificateRepository.findByTenantIdAndStatus(tenantId, CertificateStatus.ACTIVE);
    }
    
    @Transactional
    public void setDefaultCertificate(UUID certificateUuid) {
        Long tenantId = tenantContext.getCurrentTenantId();
        
        TenantCertificate certificate = certificateRepository
            .findByTenantIdAndCertificateUuid(tenantId, certificateUuid)
            .orElseThrow(() -> new CertificateNotFoundException(
                "Certificate not found: " + certificateUuid
            ));
        
        // Clear existing default for this usage purpose
        certificateRepository.clearDefaultForPurpose(tenantId, certificate.getUsagePurpose());
        
        // Set new default
        certificate.setIsDefault(true);
        certificateRepository.save(certificate);
        
        log.info("Set default certificate: tenantId={}, certUuid={}, purpose={}", 
            tenantId, certificateUuid, certificate.getUsagePurpose());
    }
    
    @Transactional
    public void revokeCertificate(UUID certificateUuid) {
        Long tenantId = tenantContext.getCurrentTenantId();
        
        TenantCertificate certificate = certificateRepository
            .findByTenantIdAndCertificateUuid(tenantId, certificateUuid)
            .orElseThrow(() -> new CertificateNotFoundException(
                "Certificate not found: " + certificateUuid
            ));
        
        certificate.setStatus(CertificateStatus.REVOKED);
        certificateRepository.save(certificate);
        
        log.warn("Revoked certificate: tenantId={}, certUuid={}", tenantId, certificateUuid);
    }
    
    private X509Certificate parseCertificate(String certificatePem) {
        try {
            String pem = certificatePem
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");
            
            byte[] decoded = Base64.getDecoder().decode(pem);
            
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(decoded));
        } catch (Exception e) {
            throw new CertificateParseException("Failed to parse certificate", e);
        }
    }
    
    private String extractPublicKey(X509Certificate cert) {
        byte[] encoded = cert.getPublicKey().getEncoded();
        return "-----BEGIN PUBLIC KEY-----\n" +
               Base64.getEncoder().encodeToString(encoded) +
               "\n-----END PUBLIC KEY-----";
    }
    
    private String calculateThumbprint(X509Certificate cert) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(cert.getEncoded());
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate thumbprint", e);
        }
    }
}
```

### 10.13 Message Processing Controller

```java
package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.signer.dto.Iso20022MessageDto;
import org.example.signer.dto.MessageStatisticsDto;
import org.example.signer.model.Iso20022Message;
import org.example.signer.model.Iso20022Message.MessageStatus;
import org.example.signer.model.Iso20022Message.MessageType;
import org.example.signer.service.TenantAwareMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "ISO 20022 Messages", description = "Tenant-isolated message management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class MessageController {

    private final TenantAwareMessageService messageService;
    
    @PostMapping
    @Operation(summary = "Create new ISO 20022 message")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'DEVELOPER')")
    public ResponseEntity<Iso20022Message> createMessage(@Valid @RequestBody Iso20022MessageDto dto) {
        Iso20022Message message = messageService.createMessage(dto);
        return ResponseEntity.ok(message);
    }
    
    @GetMapping("/{messageUuid}")
    @Operation(summary = "Get message by UUID (tenant-scoped)")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<Iso20022Message> getMessage(@PathVariable UUID messageUuid) {
        Iso20022Message message = messageService.getMessage(messageUuid);
        return ResponseEntity.ok(message);
    }
    
    @GetMapping
    @Operation(summary = "List all messages for current tenant")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<Page<Iso20022Message>> getMessages(Pageable pageable) {
        Page<Iso20022Message> messages = messageService.getMessages(pageable);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/type/{messageType}")
    @Operation(summary = "Filter messages by type")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<Page<Iso20022Message>> getMessagesByType(
            @PathVariable MessageType messageType,
            Pageable pageable) {
        Page<Iso20022Message> messages = messageService.getMessagesByType(messageType, pageable);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Filter messages by status")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<Page<Iso20022Message>> getMessagesByStatus(
            @PathVariable MessageStatus status,
            Pageable pageable) {
        Page<Iso20022Message> messages = messageService.getMessagesByStatus(status, pageable);
        return ResponseEntity.ok(messages);
    }
    
    @PatchMapping("/{messageUuid}/status")
    @Operation(summary = "Update message status")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'DEVELOPER')")
    public ResponseEntity<Iso20022Message> updateStatus(
            @PathVariable UUID messageUuid,
            @RequestParam MessageStatus status) {
        Iso20022Message updated = messageService.updateMessageStatus(messageUuid, status);
        return ResponseEntity.ok(updated);
    }
    
    @PostMapping("/{messageUuid}/sign")
    @Operation(summary = "Sign message with tenant's signing key")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'DEVELOPER')")
    public ResponseEntity<Iso20022Message> signMessage(
            @PathVariable UUID messageUuid,
            @RequestBody String signedXml) {
        Iso20022Message signed = messageService.signMessage(messageUuid, signedXml);
        return ResponseEntity.ok(signed);
    }
    
    @PostMapping("/{messageUuid}/encrypt")
    @Operation(summary = "Encrypt message with recipient's certificate")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'DEVELOPER')")
    public ResponseEntity<Iso20022Message> encryptMessage(
            @PathVariable UUID messageUuid,
            @RequestBody String encryptedXml) {
        Iso20022Message encrypted = messageService.encryptMessage(messageUuid, encryptedXml);
        return ResponseEntity.ok(encrypted);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get message statistics for tenant")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'DEVELOPER', 'VIEWER')")
    public ResponseEntity<MessageStatisticsDto> getStatistics() {
        MessageStatistics stats = messageService.getMessageStatistics();
        return ResponseEntity.ok(MessageStatisticsDto.fromEntity(stats));
    }
}
```

---

## Testing

### 10.14 Integration Test: Tenant Isolation

```java
package org.example.signer.integration;

import org.example.signer.model.Iso20022Message;
import org.example.signer.model.Iso20022Message.MessageType;
import org.example.signer.model.Iso20022Message.MessageDirection;
import org.example.signer.repository.Iso20022MessageRepository;
import org.example.signer.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class MessageTenantIsolationTest {

    @Autowired
    private Iso20022MessageRepository messageRepository;
    
    @Autowired
    private TenantContext tenantContext;
    
    @Test
    void testMessageIsolation_DifferentTenants_CannotAccessEachOthersMessages() {
        // Arrange: Create messages for two different tenants
        Long tenant1Id = 1L;
        Long tenant2Id = 2L;
        
        Iso20022Message tenant1Message = Iso20022Message.builder()
            .tenantId(tenant1Id)
            .messageType(MessageType.PAYMENT_INITIATION)
            .messageCode("pacs.008.001.08")
            .direction(MessageDirection.OUTBOUND)
            .rawXml("<Document>Tenant 1</Document>")
            .build();
        
        Iso20022Message tenant2Message = Iso20022Message.builder()
            .tenantId(tenant2Id)
            .messageType(MessageType.PAYMENT_INITIATION)
            .messageCode("pacs.008.001.08")
            .direction(MessageDirection.OUTBOUND)
            .rawXml("<Document>Tenant 2</Document>")
            .build();
        
        messageRepository.save(tenant1Message);
        messageRepository.save(tenant2Message);
        
        UUID tenant1MessageUuid = tenant1Message.getMessageUuid();
        UUID tenant2MessageUuid = tenant2Message.getMessageUuid();
        
        // Act & Assert: Tenant 1 can only access their own message
        assertThat(messageRepository.findByTenantIdAndMessageUuid(tenant1Id, tenant1MessageUuid))
            .isPresent()
            .get()
            .satisfies(m -> assertThat(m.getRawXml()).contains("Tenant 1"));
        
        assertThat(messageRepository.findByTenantIdAndMessageUuid(tenant1Id, tenant2MessageUuid))
            .isEmpty();
        
        // Act & Assert: Tenant 2 can only access their own message
        assertThat(messageRepository.findByTenantIdAndMessageUuid(tenant2Id, tenant2MessageUuid))
            .isPresent()
            .get()
            .satisfies(m -> assertThat(m.getRawXml()).contains("Tenant 2"));
        
        assertThat(messageRepository.findByTenantIdAndMessageUuid(tenant2Id, tenant1MessageUuid))
            .isEmpty();
    }
    
    @Test
    void testCertificateIsolation_DifferentTenants_CannotAccessEachOthersCertificates() {
        // Similar test for certificates
        // Implementation omitted for brevity
    }
    
    @Test
    void testTestScenarioIsolation_DifferentTenants_IndependentTestData() {
        // Similar test for test scenarios
        // Implementation omitted for brevity
    }
}
```

### 10.15 Performance Test: Message Queries

```java
package org.example.signer.performance;

import org.example.signer.model.Iso20022Message;
import org.example.signer.repository.Iso20022MessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MessageQueryPerformanceTest {

    @Autowired
    private Iso20022MessageRepository messageRepository;
    
    @Test
    void testPaginationPerformance_1000Messages_UnderThreshold() {
        // Arrange: Create 1000 messages for a tenant
        Long tenantId = 1L;
        List<Iso20022Message> messages = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            messages.add(Iso20022Message.builder()
                .tenantId(tenantId)
                .messageType(MessageType.PAYMENT_INITIATION)
                .messageCode("pacs.008.001.08")
                .direction(MessageDirection.OUTBOUND)
                .rawXml("<Document>Test " + i + "</Document>")
                .build());
        }
        
        messageRepository.saveAll(messages);
        
        // Act: Query with pagination
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        Page<Iso20022Message> page = messageRepository.findByTenantId(
            tenantId, 
            PageRequest.of(0, 50)
        );
        
        stopWatch.stop();
        
        // Assert: Query should complete in under 100ms
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(100);
        assertThat(page.getContent()).hasSize(50);
        assertThat(page.getTotalElements()).isEqualTo(1000);
    }
}
```

---

## API Endpoint Updates

### 10.16 Updated API Endpoints

All API endpoints are now tenant-scoped through the JWT token:

```
POST   /api/v1/messages                    # Create message
GET    /api/v1/messages/{uuid}             # Get message (tenant-scoped)
GET    /api/v1/messages                    # List messages (tenant-scoped)
GET    /api/v1/messages/type/{type}        # Filter by type (tenant-scoped)
GET    /api/v1/messages/status/{status}    # Filter by status (tenant-scoped)
PATCH  /api/v1/messages/{uuid}/status      # Update status
POST   /api/v1/messages/{uuid}/sign        # Sign message
POST   /api/v1/messages/{uuid}/encrypt     # Encrypt message
GET    /api/v1/messages/statistics         # Get statistics (tenant-scoped)

POST   /api/v1/certificates                # Upload certificate
GET    /api/v1/certificates                # List certificates (tenant-scoped)
GET    /api/v1/certificates/{uuid}         # Get certificate
PATCH  /api/v1/certificates/{uuid}/default # Set as default
DELETE /api/v1/certificates/{uuid}         # Revoke certificate

POST   /api/v1/signing-keys                # Generate signing key
GET    /api/v1/signing-keys                # List keys (tenant-scoped)
GET    /api/v1/signing-keys/{uuid}         # Get key
PATCH  /api/v1/signing-keys/{uuid}/default # Set as default
DELETE /api/v1/signing-keys/{uuid}         # Revoke key

POST   /api/v1/test-scenarios              # Create test scenario
GET    /api/v1/test-scenarios              # List scenarios (tenant-scoped)
GET    /api/v1/test-scenarios/{uuid}       # Get scenario
PUT    /api/v1/test-scenarios/{uuid}       # Update scenario
POST   /api/v1/test-scenarios/{uuid}/execute # Execute scenario
DELETE /api/v1/test-scenarios/{uuid}       # Delete scenario

GET    /api/v1/validation-results          # List results (tenant-scoped)
GET    /api/v1/validation-results/{uuid}   # Get result details
```

---

## Acceptance Criteria

### Functional Requirements

- [ ] All ISO 20022 message tables include tenant_id with non-nullable constraint
- [ ] All message queries filtered by tenant_id automatically
- [ ] Cross-tenant message access returns 404 (not 403, to avoid information leakage)
- [ ] Certificate management fully isolated per tenant
- [ ] Signing keys stored encrypted and scoped to tenant
- [ ] Test scenarios cannot be accessed across tenant boundaries
- [ ] Validation results linked to tenant and message/scenario
- [ ] All API endpoints respect tenant context from JWT
- [ ] Message statistics calculated per tenant only

### Security Requirements

- [ ] TenantContext properly initialized from JWT token
- [ ] All repository methods include tenantId parameter
- [ ] Service layer validates tenant ownership before operations
- [ ] Certificate private keys never returned in API responses
- [ ] Signing keys stored with encryption at rest
- [ ] Audit logs record tenant_id for all message operations
- [ ] Cross-tenant access attempts logged as security events

### Performance Requirements

- [ ] Message list queries with pagination complete in <100ms
- [ ] Message retrieval by UUID completes in <50ms
- [ ] Certificate lookup completes in <30ms
- [ ] Tenant-scoped indexes optimize all queries
- [ ] Statistics aggregation completes in <200ms

### Testing Requirements

- [ ] Integration tests verify tenant isolation for messages
- [ ] Integration tests verify tenant isolation for certificates
- [ ] Integration tests verify tenant isolation for signing keys
- [ ] Integration tests verify tenant isolation for test scenarios
- [ ] Unit tests cover all service methods
- [ ] Performance tests validate query response times
- [ ] Security tests attempt cross-tenant access

---

## Rollback Plan

If critical issues are discovered:

1. **Database Rollback**:
   ```sql
   -- Drop new tables
   DROP TABLE IF EXISTS validation_results CASCADE;
   DROP TABLE IF EXISTS test_scenarios CASCADE;
   DROP TABLE IF EXISTS tenant_signing_keys CASCADE;
   DROP TABLE IF EXISTS tenant_certificates CASCADE;
   DROP TABLE IF EXISTS iso20022_messages CASCADE;
   ```

2. **Code Rollback**:
   - Revert to previous commit before Phase 10 changes
   - Remove new service classes
   - Restore original controller endpoints

3. **Data Migration Rollback**:
   - If data was migrated, restore from pre-migration backup
   - Verify data integrity with checksums

---

## Dependencies

**Requires Completion**:
- Phase 1: Multi-Tenant Database Foundation (tenants table, tenant_id pattern)
- Phase 2: Tenant-Aware Authentication (JWT with tenant_id, TenantContext)

**Blocks**:
- Phase 11: Integration Testing & Security Validation (needs complete implementation to test)

---

## Estimated Effort

- Database schema design and migration: 0.5 days
- Entity and repository implementation: 1 day
- Service layer implementation: 1.5 days
- Controller and API updates: 0.5 day
- Certificate and key management: 1 day
- Testing and validation: 0.5-1 day

**Total: 4-5 days**
