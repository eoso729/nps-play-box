package org.example.signer.repository;

import org.example.signer.entity.ImpersonationSession;
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
public interface ImpersonationSessionRepository extends JpaRepository<ImpersonationSession, Long> {

    Optional<ImpersonationSession> findBySessionUuid(UUID sessionUuid);

    Optional<ImpersonationSession> findByImpersonationToken(String token);

    Page<ImpersonationSession> findBySupportUserIdOrderByCreatedAtDesc(
            Long supportUserId, Pageable pageable);

    Page<ImpersonationSession> findByStatusOrderByCreatedAtDesc(
            ImpersonationSession.SessionStatus status, Pageable pageable);

    Page<ImpersonationSession> findByTargetTenantIdOrderByCreatedAtDesc(
            Long targetTenantId, Pageable pageable);

    @Query("SELECT s FROM ImpersonationSession s WHERE s.status = 'ACTIVE' AND s.expiresAt < :now")
    List<ImpersonationSession> findExpiredActiveSessions(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM ImpersonationSession s WHERE s.supportUserId = :supportUserId " +
           "AND s.targetUserId = :targetUserId AND s.status = 'ACTIVE'")
    Optional<ImpersonationSession> findActiveSession(
            @Param("supportUserId") Long supportUserId,
            @Param("targetUserId") Long targetUserId);
}
