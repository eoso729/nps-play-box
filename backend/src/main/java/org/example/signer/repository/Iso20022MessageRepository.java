package org.example.signer.repository;

import org.example.signer.entity.Iso20022Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface Iso20022MessageRepository extends JpaRepository<Iso20022Message, Long>, JpaSpecificationExecutor<Iso20022Message> {

    Optional<Iso20022Message> findByTenantIdAndMessageUuid(Long tenantId, UUID messageUuid);

    Page<Iso20022Message> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<Iso20022Message> findByTenantIdAndMessageTypeOrderByCreatedAtDesc(
            Long tenantId, Iso20022Message.MessageType messageType, Pageable pageable);

    Page<Iso20022Message> findByTenantIdAndStatusOrderByCreatedAtDesc(
            Long tenantId, Iso20022Message.MessageStatus status, Pageable pageable);

    Page<Iso20022Message> findByTenantIdAndDirectionOrderByCreatedAtDesc(
            Long tenantId, Iso20022Message.MessageDirection direction, Pageable pageable);

    Optional<Iso20022Message> findByTenantIdAndTransactionReference(Long tenantId, String transactionReference);

    Optional<Iso20022Message> findByTenantIdAndMessageId(Long tenantId, String messageId);

    Optional<Iso20022Message> findByTenantIdAndEndToEndId(Long tenantId, String endToEndId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, Iso20022Message.MessageStatus status);

    long countByTenantIdAndDirection(Long tenantId, Iso20022Message.MessageDirection direction);

    @Query("SELECT m.status, COUNT(m) FROM Iso20022Message m WHERE m.tenantId = :tenantId GROUP BY m.status")
    List<Object[]> countByStatusGrouped(@Param("tenantId") Long tenantId);

    @Query("SELECT m.messageType, COUNT(m) FROM Iso20022Message m WHERE m.tenantId = :tenantId GROUP BY m.messageType")
    List<Object[]> countByMessageTypeGrouped(@Param("tenantId") Long tenantId);
}
