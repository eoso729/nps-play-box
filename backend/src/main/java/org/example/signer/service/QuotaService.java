package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.audit.Auditable;
import org.example.signer.dto.quota.*;
import org.example.signer.entity.AuditEvent;
import org.example.signer.entity.SeatRequest;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.exception.QuotaExceededException;
import org.example.signer.exception.TenantNotFoundException;
import org.example.signer.repository.SeatRequestRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final UserInvitationRepository invitationRepository;
    private final SeatRequestRepository seatRequestRepository;
    private final EmailService emailService;

    private static final double NEARING_LIMIT_THRESHOLD = 0.80; // 80%

    /**
     * Check if a tenant has available seats for new user creation
     */
    @Transactional(readOnly = true)
    public SeatAvailabilityResponse checkAvailability(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        long usedSeats = userRepository.countByTenantIdAndStatus(
                tenantId, User.UserStatus.ACTIVE);
        long pendingInvitations = invitationRepository
                .findByTenantIdAndAcceptedAtIsNull(tenantId)
                .stream()
                .filter(inv -> !inv.isExpired())
                .count();

        int maxSeats = tenant.getMaxSeats();
        int currentUsed = (int) usedSeats;
        int available = maxSeats - currentUsed;
        boolean hasAvailability = available > 0;

        String message;
        if (!hasAvailability) {
            message = "No seats available. Current usage: " + currentUsed + "/" + maxSeats +
                    ". Please request additional seats.";
        } else if (available <= 2) {
            message = "Limited seats available (" + available + " remaining). Consider requesting more.";
        } else {
            message = "Seats available: " + available + " of " + maxSeats;
        }

        return SeatAvailabilityResponse.builder()
                .available(hasAvailability)
                .maxSeats(maxSeats)
                .usedSeats(currentUsed)
                .availableSeats(available)
                .pendingInvitations((int) pendingInvitations)
                .message(message)
                .build();
    }

    /**
     * Get detailed quota status
     */
    @Transactional(readOnly = true)
    public QuotaStatusResponse getQuotaStatus(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        long activeUsers = userRepository.countByTenantIdAndStatus(
                tenantId, User.UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByTenantIdAndStatus(
                tenantId, User.UserStatus.INACTIVE);
        long pendingInvitations = invitationRepository
                .findByTenantIdAndAcceptedAtIsNull(tenantId)
                .stream()
                .filter(inv -> !inv.isExpired())
                .count();

        int maxSeats = tenant.getMaxSeats();
        int usedSeats = (int) activeUsers;
        int availableSeats = maxSeats - usedSeats;
        double utilization = maxSeats > 0 ? (usedSeats * 100.0 / maxSeats) : 0.0;

        boolean quotaExceeded = usedSeats >= maxSeats;
        boolean nearingLimit = utilization >= (NEARING_LIMIT_THRESHOLD * 100);

        return QuotaStatusResponse.builder()
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .maxSeats(maxSeats)
                .usedSeats(usedSeats)
                .availableSeats(availableSeats)
                .activeUsers((int) activeUsers)
                .inactiveUsers((int) inactiveUsers)
                .pendingInvitations((int) pendingInvitations)
                .utilizationPercentage(Math.round(utilization * 100.0) / 100.0)
                .subscriptionTier(tenant.getSubscriptionTier() != null ? tenant.getSubscriptionTier().name() : "STANDARD")
                .quotaExceeded(quotaExceeded)
                .nearingLimit(nearingLimit)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Validate and enforce quota before user creation
     * @throws QuotaExceededException if quota is exceeded
     */
    @Transactional(readOnly = true)
    public void enforceQuotaForUserCreation(Long tenantId) throws QuotaExceededException {
        SeatAvailabilityResponse availability = checkAvailability(tenantId);

        if (!Boolean.TRUE.equals(availability.getAvailable())) {
            log.warn("Quota exceeded for tenant {}: {}/{} seats used",
                    tenantId, availability.getUsedSeats(), availability.getMaxSeats());
            throw new QuotaExceededException(
                    "Seat quota exceeded. " + availability.getMessage());
        }
    }

    /**
     * Create a seat increase request
     */
    @Auditable(eventType = AuditEvent.EventType.CONFIG_CHANGE, action = "REQUEST_SEATS", resourceType = "SEAT_REQUEST", resourceId = "#result.id")
    @Transactional
    public SeatRequestResponse requestAdditionalSeats(
            Long tenantId, Long requestedBy, SeatRequestRequest request) {

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        User requester = userRepository.findById(requestedBy)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + requestedBy));

        // Check if there's already a pending request
        List<SeatRequest> pendingRequests = seatRequestRepository
                .findByTenantIdAndStatusOrderByCreatedAtDesc(
                        tenantId, SeatRequest.RequestStatus.PENDING);

        if (!pendingRequests.isEmpty()) {
            throw new IllegalStateException(
                    "A seat request is already pending for your organization");
        }

        String contactEmail = (request != null && StringUtils.hasText(request.getContactEmail())) ?
                request.getContactEmail().trim() : requester.getEmail();

        SeatRequest seatRequest = SeatRequest.builder()
                .tenantId(tenantId)
                .currentSeats(tenant.getMaxSeats())
                .requestedAdditionalSeats(request.getAdditionalSeats())
                .justification(request.getJustification())
                .expectedGrowth(request.getExpectedGrowth())
                .contactEmail(contactEmail)
                .status(SeatRequest.RequestStatus.PENDING)
                .requestedBy(requestedBy)
                .build();

        seatRequest = seatRequestRepository.save(seatRequest);

        log.info("Seat request created: tenantId={}, requestId={}, additionalSeats={}",
                tenantId, seatRequest.getId(), request.getAdditionalSeats());

        return mapToResponse(seatRequest, tenant, requester, null);
    }

    /**
     * Get all seat requests for a tenant
     */
    @Transactional(readOnly = true)
    public List<SeatRequestResponse> getTenantRequests(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        List<SeatRequest> requests = seatRequestRepository
                .findByTenantIdOrderByCreatedAtDesc(tenantId);

        return requests.stream()
                .map(req -> {
                    User requester = userRepository.findById(req.getRequestedBy()).orElse(null);
                    User reviewer = req.getReviewedBy() != null ?
                            userRepository.findById(req.getReviewedBy()).orElse(null) : null;
                    return mapToResponse(req, tenant, requester, reviewer);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all pending seat requests (Platform Admin)
     */
    @Transactional(readOnly = true)
    public List<SeatRequestResponse> getAllPendingRequests() {
        List<SeatRequest> requests = seatRequestRepository
                .findByStatusOrderByCreatedAtDesc(SeatRequest.RequestStatus.PENDING);

        return requests.stream()
                .map(req -> {
                    Tenant tenant = tenantRepository.findById(req.getTenantId()).orElse(null);
                    User requester = userRepository.findById(req.getRequestedBy()).orElse(null);
                    return mapToResponse(req, tenant, requester, null);
                })
                .collect(Collectors.toList());
    }

    /**
     * Approve a seat request (Platform Admin)
     */
    @Auditable(eventType = AuditEvent.EventType.CONFIG_CHANGE, action = "APPROVE_SEAT_REQUEST", resourceType = "SEAT_REQUEST", resourceId = "#requestId")
    @Transactional
    public SeatRequestResponse approveRequest(
            Long requestId, Long reviewedBy, ApproveRequestRequest request) {

        SeatRequest seatRequest = seatRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Seat request not found: " + requestId));

        if (seatRequest.getStatus() != SeatRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved");
        }

        Long targetTenantId = seatRequest.getTenantId();
        Tenant tenant = tenantRepository.findById(targetTenantId)
                .orElseThrow(() -> new TenantNotFoundException(targetTenantId));

        User reviewer = userRepository.findById(reviewedBy)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewedBy));

        // Apply the approved seats
        int approvedSeats = (request != null && request.getApprovedSeats() != null) ?
                request.getApprovedSeats() : seatRequest.getRequestedAdditionalSeats();
        int newMaxSeats = seatRequest.getCurrentSeats() + approvedSeats;

        tenant.setMaxSeats(newMaxSeats);
        tenantRepository.save(tenant);

        seatRequest.setStatus(SeatRequest.RequestStatus.APPROVED);
        seatRequest.setApprovedSeats(approvedSeats);
        seatRequest.setReviewedBy(reviewedBy);
        seatRequest.setReviewedAt(LocalDateTime.now());
        if (request != null && request.getNotes() != null) {
            seatRequest.setNotes(request.getNotes());
        }
        seatRequest = seatRequestRepository.save(seatRequest);

        log.info("Seat request approved: requestId={}, tenantId={}, approvedSeats={}, newTotal={}",
                requestId, tenant.getId(), approvedSeats, newMaxSeats);

        User requester = userRepository.findById(seatRequest.getRequestedBy()).orElse(null);
        String recipientEmail = seatRequest.getContactEmail() != null ? seatRequest.getContactEmail() :
                (requester != null ? requester.getEmail() : null);
        if (recipientEmail != null) {
            emailService.sendSeatRequestStatusEmail(
                    recipientEmail,
                    tenant.getName(),
                    true,
                    approvedSeats,
                    seatRequest.getNotes()
            );
        }

        return mapToResponse(seatRequest, tenant, requester, reviewer);
    }

    /**
     * Deny a seat request (Platform Admin)
     */
    @Auditable(eventType = AuditEvent.EventType.CONFIG_CHANGE, action = "DENY_SEAT_REQUEST", resourceType = "SEAT_REQUEST", resourceId = "#requestId")
    @Transactional
    public SeatRequestResponse denyRequest(
            Long requestId, Long reviewedBy, DenyRequestRequest request) {

        SeatRequest seatRequest = seatRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Seat request not found: " + requestId));

        if (seatRequest.getStatus() != SeatRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be denied");
        }

        User reviewer = userRepository.findById(reviewedBy)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found: " + reviewedBy));

        Tenant tenant = tenantRepository.findById(seatRequest.getTenantId()).orElse(null);

        seatRequest.setStatus(SeatRequest.RequestStatus.DENIED);
        seatRequest.setDenialReason(request != null ? request.getReason() : "Request denied");
        seatRequest.setReviewedBy(reviewedBy);
        seatRequest.setReviewedAt(LocalDateTime.now());
        seatRequest = seatRequestRepository.save(seatRequest);

        log.info("Seat request denied: requestId={}, tenantId={}, reason={}",
                requestId, seatRequest.getTenantId(), seatRequest.getDenialReason());

        User requester = userRepository.findById(seatRequest.getRequestedBy()).orElse(null);
        String recipientEmail = seatRequest.getContactEmail() != null ? seatRequest.getContactEmail() :
                (requester != null ? requester.getEmail() : null);
        if (recipientEmail != null && tenant != null) {
            emailService.sendSeatRequestStatusEmail(
                    recipientEmail,
                    tenant.getName(),
                    false,
                    0,
                    seatRequest.getDenialReason()
            );
        }

        return mapToResponse(seatRequest, tenant, requester, reviewer);
    }

    private SeatRequestResponse mapToResponse(
            SeatRequest request, Tenant tenant, User requester, User reviewer) {

        String requesterName = requester != null ?
                ((requester.getFirstName() != null ? requester.getFirstName() : "") + " " +
                 (requester.getLastName() != null ? requester.getLastName() : "")).trim() : "Unknown";
        if (requesterName.isEmpty()) {
            requesterName = "Unknown";
        }

        String reviewerName = null;
        if (reviewer != null) {
            reviewerName = ((reviewer.getFirstName() != null ? reviewer.getFirstName() : "") + " " +
                            (reviewer.getLastName() != null ? reviewer.getLastName() : "")).trim();
            if (reviewerName.isEmpty()) {
                reviewerName = "Platform Admin";
            }
        }

        int newTotal = request.getCurrentSeats() +
                (request.getApprovedSeats() != null ? request.getApprovedSeats() : request.getRequestedAdditionalSeats());

        return SeatRequestResponse.builder()
                .id(request.getId())
                .tenantId(request.getTenantId())
                .tenantName(tenant != null ? tenant.getName() : "Unknown")
                .currentSeats(request.getCurrentSeats())
                .requestedAdditionalSeats(request.getRequestedAdditionalSeats())
                .approvedSeats(request.getApprovedSeats())
                .newTotalSeats(newTotal)
                .justification(request.getJustification())
                .expectedGrowth(request.getExpectedGrowth())
                .contactEmail(request.getContactEmail())
                .status(request.getStatus().name())
                .requestedByName(requesterName)
                .requestedByEmail(requester != null ? requester.getEmail() : "Unknown")
                .requestedAt(request.getCreatedAt())
                .reviewedBy(reviewerName)
                .reviewedAt(request.getReviewedAt())
                .denialReason(request.getDenialReason())
                .notes(request.getNotes())
                .build();
    }
}
