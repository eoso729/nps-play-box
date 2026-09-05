package org.example.signer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "current_seats", nullable = false)
    private Integer currentSeats;

    @Column(name = "requested_additional_seats", nullable = false)
    private Integer requestedAdditionalSeats;

    @Column(name = "approved_seats")
    private Integer approvedSeats;

    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;

    @Column(name = "expected_growth", columnDefinition = "TEXT")
    private String expectedGrowth;

    @Column(name = "contact_email")
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "denial_reason", columnDefinition = "TEXT")
    private String denialReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }

    public enum RequestStatus {
        PENDING, APPROVED, DENIED
    }
}
