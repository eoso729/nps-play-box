package org.example.signer.repository;

import org.example.signer.entity.SeatRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRequestRepository extends JpaRepository<SeatRequest, Long> {
    List<SeatRequest> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<SeatRequest> findByStatusOrderByCreatedAtDesc(SeatRequest.RequestStatus status);
    List<SeatRequest> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, SeatRequest.RequestStatus status);
}
