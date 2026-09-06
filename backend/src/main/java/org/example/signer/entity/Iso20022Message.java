package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "iso20022_messages", indexes = {
    @Index(name = "idx_messages_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_messages_tenant_type", columnList = "tenant_id,message_type"),
    @Index(name = "idx_messages_tenant_status", columnList = "tenant_id,status"),
    @Index(name = "idx_messages_transaction_ref", columnList = "tenant_id,transaction_reference"),
    @Index(name = "idx_messages_msg_id", columnList = "message_id"),
    @Index(name = "idx_messages_end_to_end", columnList = "end_to_end_id"),
    @Index(name = "idx_messages_created_at", columnList = "tenant_id,created_at DESC")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Iso20022Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "message_uuid", unique = true, nullable = false, updatable = false)
    private UUID messageUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 100)
    private MessageType messageType;

    @Column(name = "message_code", nullable = false, length = 50)
    private String messageCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 20)
    private MessageDirection direction;

    @Column(name = "raw_xml", nullable = false, columnDefinition = "TEXT")
    private String rawXml;

    @Column(name = "signed_xml", columnDefinition = "TEXT")
    private String signedXml;

    @Column(name = "encrypted_xml", columnDefinition = "TEXT")
    private String encryptedXml;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private MessageStatus status;

    @Column(name = "created_by")
    private Long createdBy;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

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
        if (direction == null) {
            direction = MessageDirection.OUTBOUND;
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
}
