package org.example.signer.repository;

import org.example.signer.entity.TenantSimulatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantSimulatorProfileRepository extends JpaRepository<TenantSimulatorProfile, Long> {

    Optional<TenantSimulatorProfile> findByTenantId(Long tenantId);

    Optional<TenantSimulatorProfile> findByTenantIdAndProfileUuid(Long tenantId, UUID profileUuid);

    Optional<TenantSimulatorProfile> findByInstitutionCode(String institutionCode);

    boolean existsByTenantId(Long tenantId);
}
