package org.example.signer.repository;

import org.example.signer.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long>, JpaSpecificationExecutor<AuditEvent> {

    Page<AuditEvent> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<AuditEvent> findByTenantIdAndEventTypeOrderByCreatedAtDesc(
            Long tenantId, AuditEvent.EventType eventType, Pageable pageable);

    Page<AuditEvent> findByTenantIdAndUserIdOrderByCreatedAtDesc(
            Long tenantId, Long userId, Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.tenantId = :tenantId " +
           "AND a.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY a.createdAt DESC")
    Page<AuditEvent> findByTenantIdAndDateRange(
            @Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT a FROM AuditEvent a WHERE a.tenantId = :tenantId " +
           "AND a.resourceType = :resourceType AND a.resourceId = :resourceId " +
           "ORDER BY a.createdAt DESC")
    List<AuditEvent> findResourceHistory(
            @Param("tenantId") Long tenantId,
            @Param("resourceType") String resourceType,
            @Param("resourceId") String resourceId);

    @Query("SELECT COUNT(a) FROM AuditEvent a WHERE a.tenantId = :tenantId " +
           "AND a.eventType = :eventType AND a.status = :status " +
           "AND a.createdAt >= :since")
    long countByTenantAndTypeAndStatusSince(
            @Param("tenantId") Long tenantId,
            @Param("eventType") AuditEvent.EventType eventType,
            @Param("status") AuditEvent.EventStatus status,
            @Param("since") LocalDateTime since);
}
