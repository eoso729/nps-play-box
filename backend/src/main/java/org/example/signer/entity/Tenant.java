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
    
    @Builder.Default
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
        if (maxSeats == null) {
            maxSeats = 5;
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
