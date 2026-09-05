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
    
    @Column
    private String username;
    
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
    
    @Column(name = "auth_provider")
    private String authProvider;
    
    @Column(name = "microsoft_oid")
    private String microsoftOid;
    
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
        if (tenantId == null) {
            tenantId = 1L;
        }
        if (role == null) {
            role = UserRole.VIEWER;
        }
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
        if (authProvider == null) {
            authProvider = "LOCAL";
        }
        if (username == null) {
            username = email;
        }
    }
    
    public enum UserRole {
        PLATFORM_ADMIN, TENANT_ADMIN, DEVELOPER, VIEWER
    }
    
    public enum UserStatus {
        ACTIVE, INACTIVE, PENDING
    }
}
