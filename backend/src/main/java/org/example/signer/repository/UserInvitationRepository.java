package org.example.signer.repository;

import org.example.signer.entity.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByInvitationToken(String token);
    List<UserInvitation> findByTenantIdAndAcceptedAtIsNull(Long tenantId);
    void deleteByExpiresAtBeforeAndAcceptedAtIsNull(LocalDateTime cutoffDate);
}
