package org.example.signer.repository;

import org.example.signer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Multi-tenant methods
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);
    Optional<User> findByUserUuid(UUID userUuid);
    List<User> findByTenantId(Long tenantId);
    org.springframework.data.domain.Page<User> findByTenantId(Long tenantId, org.springframework.data.domain.Pageable pageable);
    long countByTenantIdAndStatus(Long tenantId, User.UserStatus status);
    long countByTenantIdAndRoleAndStatus(Long tenantId, User.UserRole role, User.UserStatus status);
    boolean existsByTenantIdAndEmail(Long tenantId, String email);

    // Legacy and authentication methods
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrUsername(String email, String username);
    Optional<User> findByMicrosoftOid(String microsoftOid);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
