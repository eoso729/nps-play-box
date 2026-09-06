package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_simulator_profiles", indexes = {
    @Index(name = "idx_sim_profiles_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_sim_profiles_inst_code", columnList = "institution_code")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSimulatorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", unique = true, nullable = false)
    private Long tenantId;

    @Column(name = "profile_uuid", unique = true, nullable = false, updatable = false)
    private UUID profileUuid;

    @Column(name = "institution_code", nullable = false, length = 20)
    private String institutionCode;

    @Column(name = "institution_name", nullable = false, length = 255)
    private String institutionName;

    @Column(name = "bic", nullable = false, length = 50)
    private String bic;

    @Column(name = "scheme_code", length = 50)
    private String schemeCode;

    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;

    @Column(name = "default_account_number", length = 50)
    private String defaultAccountNumber;

    @Column(name = "default_account_name", length = 255)
    private String defaultAccountName;

    @Column(name = "default_bvn", length = 20)
    private String defaultBvn;

    @Column(name = "callback_url", length = 500)
    private String callbackUrl;

    @Column(name = "auto_respond_inbound", nullable = false)
    private Boolean autoRespondInbound;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (profileUuid == null) {
            profileUuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (defaultCurrency == null) {
            defaultCurrency = "NGN";
        }
        if (autoRespondInbound == null) {
            autoRespondInbound = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
