# Phase 5: Seat Quota Enforcement

## Objective
Implement robust seat quota enforcement to prevent user creation beyond allocated limits, provide real-time availability checks, establish seat request workflows, and create billing integration endpoints for seat usage reporting.

**Duration**: 3-4 days  
**Dependencies**: Phase 3 (Tenant Management), Phase 4 (User Management)

---

## API Endpoints

### 5.1 Quota Enforcement Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/v1/quota/availability` | Check seat availability | Tenant Admin |
| GET | `/api/v1/quota/status` | Get current quota status | Tenant Admin |
| POST | `/api/v1/quota/request` | Request additional seats | Tenant Admin |
| GET | `/api/v1/quota/requests` | List seat requests | Tenant Admin |
| PATCH | `/api/v1/quota/requests/{id}/approve` | Approve seat request | Platform Admin |
| PATCH | `/api/v1/quota/requests/{id}/deny` | Deny seat request | Platform Admin |
| GET | `/api/v1/billing/seat-usage` | Get seat usage for billing | Platform Admin |
| GET | `/api/v1/billing/seat-usage/{tenantId}` | Get tenant seat usage | Platform Admin |

---

## Backend Implementation

### 1.1 Create DTOs

**File**: `backend/src/main/java/org/example/signer/dto/quota/SeatAvailabilityResponse.java`

```java
package org.example.signer.dto.quota;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatAvailabilityResponse {
    private boolean available;
    private Integer maxSeats;
    private Integer usedSeats;
    private Integer availableSeats;
    private Integer pendingInvitations;
    private Integer requestedSeats;
    private String message;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/quota/QuotaStatusResponse.java`

```java
package org.example.signer.dto.quota;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuotaStatusResponse {
    private Long tenantId;
    private String tenantName;
    private Integer maxSeats;
    private Integer usedSeats;
    private Integer availableSeats;
    private Integer activeUsers;
    private Integer inactiveUsers;
    private Integer pendingInvitations;
    private Double utilizationPercentage;
    private String subscriptionTier;
    private boolean quotaExceeded;
    private boolean nearingLimit; // >80% utilization
    private LocalDateTime lastUpdated;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/quota/SeatRequestRequest.java`

```java
package org.example.signer.dto.quota;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeatRequestRequest {
    
    @NotNull(message = "Additional seats count is required")
    @Min(value = 1, message = "Must request at least 1 additional seat")
    private Integer additionalSeats;
    
    @NotBlank(message = "Business justification is required")
    private String justification;
    
    private String expectedGrowth;
    
    private String contactEmail;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/quota/SeatRequestResponse.java`

```java
package org.example.signer.dto.quota;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SeatRequestResponse {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private Integer currentSeats;
    private Integer requestedAdditionalSeats;
    private Integer newTotalSeats;
    private String justification;
    private String expectedGrowth;
    private String contactEmail;
    private String status; // PENDING, APPROVED, DENIED
    private String requestedByName;
    private String requestedByEmail;
    private LocalDateTime requestedAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String denialReason;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/quota/ApproveRequestRequest.java`

```java
package org.example.signer.dto.quota;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ApproveRequestRequest {
    
    @Min(value = 1, message = "Must approve at least 1 seat")
    private Integer approvedSeats; // Can be different from requested
    
    private String notes;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/quota/DenyRequestRequest.java`

```java
package org.example.signer.dto.quota;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DenyRequestRequest {
    
    @NotBlank(message = "Denial reason is required")
    private String reason;
}
```

**File**: `backend/src/main/java/org/example/signer/dto/billing/SeatUsageResponse.java`

```java
package org.example.signer.dto.billing;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SeatUsageResponse {
    private Long tenantId;
    private String tenantName;
    private String tenantSlug;
    private String subscriptionTier;
    private Integer maxSeats;
    private Integer usedSeats;
    private Integer billableSeats; // Typically max of (usedSeats, committedSeats)
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private List<DailyUsage> dailyUsage;
    
    @Data
    @Builder
    public static class DailyUsage {
        private LocalDateTime date;
        private Integer activeSeats;
        private Integer peakSeats;
    }
}
```

### 1.2 Create Database Entity for Seat Requests

**File**: `backend/src/main/java/org/example/signer/entity/SeatRequest.java`

```java
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
```

### 1.3 Create Repository

**File**: `backend/src/main/java/org/example/signer/repository/SeatRequestRepository.java`

```java
package org.example.signer.repository;

import org.example.signer.entity.SeatRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRequestRepository extends JpaRepository<SeatRequest, Long> {
    List<SeatRequest> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<SeatRequest> findByStatusOrderByCreatedAtDesc(SeatRequest.RequestStatus status);
    List<SeatRequest> findByTenantIdAndStatusOrderByCreatedAtDesc(
            Long tenantId, SeatRequest.RequestStatus status);
}
```

### 1.4 Create Quota Service

**File**: `backend/src/main/java/org/example/signer/service/QuotaService.java`

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.quota.*;
import org.example.signer.entity.SeatRequest;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.exception.QuotaExceededException;
import org.example.signer.repository.SeatRequestRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    private static final double NEARING_LIMIT_THRESHOLD = 0.80; // 80%
    
    /**
     * Check if a tenant has available seats for new user creation
     */
    public SeatAvailabilityResponse checkAvailability(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
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
    public QuotaStatusResponse getQuotaStatus(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
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
                .subscriptionTier(tenant.getSubscriptionTier().name())
                .quotaExceeded(quotaExceeded)
                .nearingLimit(nearingLimit)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
    
    /**
     * Validate and enforce quota before user creation
     * @throws QuotaExceededException if quota is exceeded
     */
    public void enforceQuotaForUserCreation(Long tenantId) throws QuotaExceededException {
        SeatAvailabilityResponse availability = checkAvailability(tenantId);
        
        if (!availability.getAvailable()) {
            log.warn("Quota exceeded for tenant {}: {}/{} seats used", 
                    tenantId, availability.getUsedSeats(), availability.getMaxSeats());
            throw new QuotaExceededException(
                    "Seat quota exceeded. " + availability.getMessage());
        }
    }
    
    /**
     * Create a seat increase request
     */
    @Transactional
    public SeatRequestResponse requestAdditionalSeats(
            Long tenantId, Long requestedBy, SeatRequestRequest request) {
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        User requester = userRepository.findById(requestedBy)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if there's already a pending request
        List<SeatRequest> pendingRequests = seatRequestRepository
                .findByTenantIdAndStatusOrderByCreatedAtDesc(
                        tenantId, SeatRequest.RequestStatus.PENDING);
        
        if (!pendingRequests.isEmpty()) {
            throw new IllegalStateException(
                    "A seat request is already pending for your organization");
        }
        
        SeatRequest seatRequest = SeatRequest.builder()
                .tenantId(tenantId)
                .currentSeats(tenant.getMaxSeats())
                .requestedAdditionalSeats(request.getAdditionalSeats())
                .justification(request.getJustification())
                .expectedGrowth(request.getExpectedGrowth())
                .contactEmail(request.getContactEmail() != null ? 
                        request.getContactEmail() : requester.getEmail())
                .status(SeatRequest.RequestStatus.PENDING)
                .requestedBy(requestedBy)
                .build();
        
        seatRequest = seatRequestRepository.save(seatRequest);
        
        log.info("Seat request created: tenantId={}, requestId={}, additionalSeats={}", 
                tenantId, seatRequest.getId(), request.getAdditionalSeats());
        
        // TODO: Send notification to platform admins
        
        return mapToResponse(seatRequest, tenant, requester, null);
    }
    
    /**
     * Get all seat requests for a tenant
     */
    public List<SeatRequestResponse> getTenantRequests(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
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
    @Transactional
    public SeatRequestResponse approveRequest(
            Long requestId, Long reviewedBy, ApproveRequestRequest request) {
        
        SeatRequest seatRequest = seatRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Seat request not found"));
        
        if (seatRequest.getStatus() != SeatRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved");
        }
        
        Tenant tenant = tenantRepository.findById(seatRequest.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        User reviewer = userRepository.findById(reviewedBy)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        
        // Apply the approved seats
        int approvedSeats = request.getApprovedSeats() != null ? 
                request.getApprovedSeats() : seatRequest.getRequestedAdditionalSeats();
        int newMaxSeats = seatRequest.getCurrentSeats() + approvedSeats;
        
        tenant.setMaxSeats(newMaxSeats);
        tenantRepository.save(tenant);
        
        seatRequest.setStatus(SeatRequest.RequestStatus.APPROVED);
        seatRequest.setApprovedSeats(approvedSeats);
        seatRequest.setReviewedBy(reviewedBy);
        seatRequest.setReviewedAt(LocalDateTime.now());
        seatRequest.setNotes(request.getNotes());
        seatRequest = seatRequestRepository.save(seatRequest);
        
        log.info("Seat request approved: requestId={}, tenantId={}, approvedSeats={}, newTotal={}", 
                requestId, tenant.getId(), approvedSeats, newMaxSeats);
        
        // TODO: Send notification to requester
        
        User requester = userRepository.findById(seatRequest.getRequestedBy()).orElse(null);
        return mapToResponse(seatRequest, tenant, requester, reviewer);
    }
    
    /**
     * Deny a seat request (Platform Admin)
     */
    @Transactional
    public SeatRequestResponse denyRequest(
            Long requestId, Long reviewedBy, DenyRequestRequest request) {
        
        SeatRequest seatRequest = seatRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Seat request not found"));
        
        if (seatRequest.getStatus() != SeatRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be denied");
        }
        
        User reviewer = userRepository.findById(reviewedBy)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        
        seatRequest.setStatus(SeatRequest.RequestStatus.DENIED);
        seatRequest.setDenialReason(request.getReason());
        seatRequest.setReviewedBy(reviewedBy);
        seatRequest.setReviewedAt(LocalDateTime.now());
        seatRequest = seatRequestRepository.save(seatRequest);
        
        log.info("Seat request denied: requestId={}, tenantId={}, reason={}", 
                requestId, seatRequest.getTenantId(), request.getReason());
        
        // TODO: Send notification to requester
        
        Tenant tenant = tenantRepository.findById(seatRequest.getTenantId()).orElse(null);
        User requester = userRepository.findById(seatRequest.getRequestedBy()).orElse(null);
        return mapToResponse(seatRequest, tenant, requester, reviewer);
    }
    
    private SeatRequestResponse mapToResponse(
            SeatRequest request, Tenant tenant, User requester, User reviewer) {
        
        return SeatRequestResponse.builder()
                .id(request.getId())
                .tenantId(request.getTenantId())
                .tenantName(tenant != null ? tenant.getName() : "Unknown")
                .currentSeats(request.getCurrentSeats())
                .requestedAdditionalSeats(request.getRequestedAdditionalSeats())
                .newTotalSeats(request.getCurrentSeats() + 
                        (request.getApprovedSeats() != null ? 
                                request.getApprovedSeats() : request.getRequestedAdditionalSeats()))
                .justification(request.getJustification())
                .expectedGrowth(request.getExpectedGrowth())
                .contactEmail(request.getContactEmail())
                .status(request.getStatus().name())
                .requestedByName(requester != null ? 
                        requester.getFirstName() + " " + requester.getLastName() : "Unknown")
                .requestedByEmail(requester != null ? requester.getEmail() : "Unknown")
                .requestedAt(request.getCreatedAt())
                .reviewedBy(reviewer != null ? 
                        reviewer.getFirstName() + " " + reviewer.getLastName() : null)
                .reviewedAt(request.getReviewedAt())
                .denialReason(request.getDenialReason())
                .build();
    }
}
```

### 1.5 Create Custom Exception

**File**: `backend/src/main/java/org/example/signer/exception/QuotaExceededException.java`

```java
package org.example.signer.exception;

public class QuotaExceededException extends RuntimeException {
    
    public QuotaExceededException(String message) {
        super(message);
    }
    
    public QuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 1.6 Create Billing Service

**File**: `backend/src/main/java/org/example/signer/service/BillingService.java`

```java
package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.billing.SeatUsageResponse;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {
    
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    
    /**
     * Get seat usage for all tenants (for billing purposes)
     */
    public List<SeatUsageResponse> getAllTenantsSeatUsage(
            LocalDateTime periodStart, LocalDateTime periodEnd) {
        
        List<Tenant> tenants = tenantRepository.findAll();
        List<SeatUsageResponse> usageList = new ArrayList<>();
        
        for (Tenant tenant : tenants) {
            SeatUsageResponse usage = getTenantSeatUsage(
                    tenant.getId(), periodStart, periodEnd);
            usageList.add(usage);
        }
        
        return usageList;
    }
    
    /**
     * Get seat usage for specific tenant
     */
    public SeatUsageResponse getTenantSeatUsage(
            Long tenantId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        long activeSeats = userRepository.countByTenantIdAndStatus(
                tenantId, User.UserStatus.ACTIVE);
        
        // For billing, we typically charge for max seats allocated
        // Alternative: charge for peak usage during period
        int billableSeats = tenant.getMaxSeats();
        
        // TODO: If you want to track daily usage, implement audit logging
        // and query historical data for the period
        List<SeatUsageResponse.DailyUsage> dailyUsage = new ArrayList<>();
        
        return SeatUsageResponse.builder()
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .tenantSlug(tenant.getSlug())
                .subscriptionTier(tenant.getSubscriptionTier().name())
                .maxSeats(tenant.getMaxSeats())
                .usedSeats((int) activeSeats)
                .billableSeats(billableSeats)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .dailyUsage(dailyUsage)
                .build();
    }
}
```

### 1.7 Update User Service to Enforce Quota

**File**: `backend/src/main/java/org/example/signer/service/UserService.java`

Add quota enforcement to the `inviteUser` method:

```java
@Transactional
public InvitationResponse inviteUser(Long tenantId, Long invitedBy, InviteUserRequest request) {
    // Enforce quota BEFORE creating invitation
    quotaService.enforceQuotaForUserCreation(tenantId);
    
    // ... rest of the existing logic
}
```

And to the `acceptInvitation` method:

```java
@Transactional
public UserResponse acceptInvitation(AcceptInvitationRequest request) {
    // Find invitation
    UserInvitation invitation = invitationRepository
            .findByInvitationToken(request.getToken())
            .orElseThrow(() -> new RuntimeException("Invalid invitation token"));
    
    // ... existing validation
    
    // Enforce quota BEFORE creating user
    quotaService.enforceQuotaForUserCreation(invitation.getTenantId());
    
    // ... rest of the existing logic
}
```

### 1.8 Create Controllers

**File**: `backend/src/main/java/org/example/signer/controller/QuotaController.java`

```java
package org.example.signer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.signer.dto.quota.*;
import org.example.signer.security.RequirePlatformAdmin;
import org.example.signer.security.RequireTenantAdmin;
import org.example.signer.service.QuotaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quota")
@RequiredArgsConstructor
public class QuotaController {
    
    private final QuotaService quotaService;
    
    @RequireTenantAdmin
    @GetMapping("/availability")
    public ResponseEntity<SeatAvailabilityResponse> checkAvailability(
            @RequestAttribute("tenantId") Long tenantId) {
        return ResponseEntity.ok(quotaService.checkAvailability(tenantId));
    }
    
    @RequireTenantAdmin
    @GetMapping("/status")
    public ResponseEntity<QuotaStatusResponse> getQuotaStatus(
            @RequestAttribute("tenantId") Long tenantId) {
        return ResponseEntity.ok(quotaService.getQuotaStatus(tenantId));
    }
    
    @RequireTenantAdmin
    @PostMapping("/request")
    public ResponseEntity<SeatRequestResponse> requestAdditionalSeats(
            @RequestAttribute("tenantId") Long tenantId,
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody SeatRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quotaService.requestAdditionalSeats(tenantId, userId, request));
    }
    
    @RequireTenantAdmin
    @GetMapping("/requests")
    public ResponseEntity<List<SeatRequestResponse>> getTenantRequests(
            @RequestAttribute("tenantId") Long tenantId) {
        return ResponseEntity.ok(quotaService.getTenantRequests(tenantId));
    }
    
    @RequirePlatformAdmin
    @GetMapping("/requests/all")
    public ResponseEntity<List<SeatRequestResponse>> getAllPendingRequests() {
        return ResponseEntity.ok(quotaService.getAllPendingRequests());
    }
    
    @RequirePlatformAdmin
    @PatchMapping("/requests/{id}/approve")
    public ResponseEntity<SeatRequestResponse> approveRequest(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ApproveRequestRequest request) {
        return ResponseEntity.ok(quotaService.approveRequest(id, userId, request));
    }
    
    @RequirePlatformAdmin
    @PatchMapping("/requests/{id}/deny")
    public ResponseEntity<SeatRequestResponse> denyRequest(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody DenyRequestRequest request) {
        return ResponseEntity.ok(quotaService.denyRequest(id, userId, request));
    }
}
```

**File**: `backend/src/main/java/org/example/signer/controller/BillingController.java`

```java
package org.example.signer.controller;

import lombok.RequiredArgsConstructor;
import org.example.signer.dto.billing.SeatUsageResponse;
import org.example.signer.security.RequirePlatformAdmin;
import org.example.signer.service.BillingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {
    
    private final BillingService billingService;
    
    @RequirePlatformAdmin
    @GetMapping("/seat-usage")
    public ResponseEntity<List<SeatUsageResponse>> getAllSeatUsage(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime periodStart,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime periodEnd) {
        
        // Default to current month if not specified
        if (periodStart == null) {
            periodStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        }
        if (periodEnd == null) {
            periodEnd = LocalDateTime.now();
        }
        
        return ResponseEntity.ok(
                billingService.getAllTenantsSeatUsage(periodStart, periodEnd));
    }
    
    @RequirePlatformAdmin
    @GetMapping("/seat-usage/{tenantId}")
    public ResponseEntity<SeatUsageResponse> getTenantSeatUsage(
            @PathVariable Long tenantId,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime periodStart,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime periodEnd) {
        
        if (periodStart == null) {
            periodStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        }
        if (periodEnd == null) {
            periodEnd = LocalDateTime.now();
        }
        
        return ResponseEntity.ok(
                billingService.getTenantSeatUsage(tenantId, periodStart, periodEnd));
    }
}
```

### 1.9 Create Global Exception Handler

**File**: `backend/src/main/java/org/example/signer/exception/GlobalExceptionHandler.java`

```java
package org.example.signer.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<Map<String, Object>> handleQuotaExceeded(
            QuotaExceededException ex) {
        
        log.warn("Quota exceeded: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Quota Exceeded");
        body.put("message", ex.getMessage());
        body.put("code", "SEAT_QUOTA_EXCEEDED");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
    
    // Add other exception handlers as needed
}
```

---

## Database Migration

### 2.1 Create Migration for Seat Requests Table

**File**: `backend/src/main/resources/db/migration/V4__create_seat_requests_table.sql`

```sql
-- V4: Create seat requests table for quota management

CREATE TABLE seat_requests (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    current_seats INTEGER NOT NULL,
    requested_additional_seats INTEGER NOT NULL,
    approved_seats INTEGER,
    justification TEXT,
    expected_growth TEXT,
    contact_email VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    requested_by BIGINT NOT NULL,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    denial_reason TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_seat_requests_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_seat_requests_requester FOREIGN KEY (requested_by) REFERENCES users(id),
    CONSTRAINT fk_seat_requests_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id),
    CONSTRAINT chk_request_status CHECK (status IN ('PENDING', 'APPROVED', 'DENIED'))
);

CREATE INDEX idx_seat_requests_tenant_id ON seat_requests(tenant_id);
CREATE INDEX idx_seat_requests_status ON seat_requests(status);
CREATE INDEX idx_seat_requests_created_at ON seat_requests(created_at);
```

---

## Testing Requirements

### 3.1 Service Layer Tests

**File**: `backend/src/test/java/org/example/signer/service/QuotaServiceTest.java`

```java
@SpringBootTest
@Transactional
class QuotaServiceTest {
    
    @Autowired
    private QuotaService quotaService;
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private Tenant testTenant;
    private User adminUser;
    
    @BeforeEach
    void setup() {
        testTenant = tenantRepository.save(Tenant.builder()
                .name("Test Corp")
                .slug("test-corp")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(5)
                .subscriptionTier(Tenant.SubscriptionTier.STANDARD)
                .build());
        
        adminUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .email("admin@test.com")
                .passwordHash("hashed")
                .firstName("Admin")
                .lastName("User")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
    }
    
    @Test
    void shouldCheckAvailability() {
        SeatAvailabilityResponse response = quotaService.checkAvailability(
                testTenant.getId());
        
        assertTrue(response.getAvailable());
        assertEquals(5, response.getMaxSeats());
        assertEquals(1, response.getUsedSeats());
        assertEquals(4, response.getAvailableSeats());
    }
    
    @Test
    void shouldDetectQuotaExceeded() {
        // Fill up seats
        for (int i = 0; i < 4; i++) {
            userRepository.save(User.builder()
                    .tenantId(testTenant.getId())
                    .email("user" + i + "@test.com")
                    .passwordHash("hashed")
                    .firstName("User")
                    .lastName(String.valueOf(i))
                    .role(User.UserRole.DEVELOPER)
                    .status(User.UserStatus.ACTIVE)
                    .build());
        }
        
        SeatAvailabilityResponse response = quotaService.checkAvailability(
                testTenant.getId());
        
        assertFalse(response.getAvailable());
        assertEquals(5, response.getUsedSeats());
        assertEquals(0, response.getAvailableSeats());
    }
    
    @Test
    void shouldEnforceQuota() {
        // Fill up seats to max
        for (int i = 0; i < 4; i++) {
            userRepository.save(User.builder()
                    .tenantId(testTenant.getId())
                    .email("user" + i + "@test.com")
                    .passwordHash("hashed")
                    .firstName("User")
                    .lastName(String.valueOf(i))
                    .role(User.UserRole.DEVELOPER)
                    .status(User.UserStatus.ACTIVE)
                    .build());
        }
        
        // Attempt to create one more should throw exception
        assertThrows(QuotaExceededException.class, () ->
                quotaService.enforceQuotaForUserCreation(testTenant.getId()));
    }
    
    @Test
    void shouldCreateSeatRequest() {
        SeatRequestRequest request = new SeatRequestRequest();
        request.setAdditionalSeats(10);
        request.setJustification("Team expansion for Q4");
        request.setExpectedGrowth("20% growth expected");
        
        SeatRequestResponse response = quotaService.requestAdditionalSeats(
                testTenant.getId(), adminUser.getId(), request);
        
        assertNotNull(response.getId());
        assertEquals(5, response.getCurrentSeats());
        assertEquals(10, response.getRequestedAdditionalSeats());
        assertEquals(15, response.getNewTotalSeats());
        assertEquals("PENDING", response.getStatus());
    }
    
    @Test
    void shouldRejectDuplicatePendingRequest() {
        SeatRequestRequest request = new SeatRequestRequest();
        request.setAdditionalSeats(5);
        request.setJustification("Need more seats");
        
        quotaService.requestAdditionalSeats(
                testTenant.getId(), adminUser.getId(), request);
        
        assertThrows(IllegalStateException.class, () ->
                quotaService.requestAdditionalSeats(
                        testTenant.getId(), adminUser.getId(), request));
    }
    
    @Test
    void shouldApproveRequest() {
        // Create request
        SeatRequestRequest createReq = new SeatRequestRequest();
        createReq.setAdditionalSeats(10);
        createReq.setJustification("Growth");
        SeatRequestResponse created = quotaService.requestAdditionalSeats(
                testTenant.getId(), adminUser.getId(), createReq);
        
        // Create platform admin
        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform")
                .slug("platform")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .build());
        User platformAdmin = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .email("platform@admin.com")
                .passwordHash("hashed")
                .firstName("Platform")
                .lastName("Admin")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
        
        // Approve
        ApproveRequestRequest approveReq = new ApproveRequestRequest();
        approveReq.setApprovedSeats(10);
        approveReq.setNotes("Approved for Q4 growth");
        
        SeatRequestResponse approved = quotaService.approveRequest(
                created.getId(), platformAdmin.getId(), approveReq);
        
        assertEquals("APPROVED", approved.getStatus());
        
        // Verify tenant max seats updated
        Tenant updated = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertEquals(15, updated.getMaxSeats());
    }
    
    @Test
    void shouldDenyRequest() {
        // Create request
        SeatRequestRequest createReq = new SeatRequestRequest();
        createReq.setAdditionalSeats(100);
        createReq.setJustification("Big expansion");
        SeatRequestResponse created = quotaService.requestAdditionalSeats(
                testTenant.getId(), adminUser.getId(), createReq);
        
        // Create platform admin
        Tenant platformTenant = tenantRepository.save(Tenant.builder()
                .name("Platform")
                .slug("platform")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(999)
                .build());
        User platformAdmin = userRepository.save(User.builder()
                .tenantId(platformTenant.getId())
                .email("platform@admin.com")
                .passwordHash("hashed")
                .firstName("Platform")
                .lastName("Admin")
                .role(User.UserRole.PLATFORM_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build());
        
        // Deny
        DenyRequestRequest denyReq = new DenyRequestRequest();
        denyReq.setReason("Request exceeds tier limits");
        
        SeatRequestResponse denied = quotaService.denyRequest(
                created.getId(), platformAdmin.getId(), denyReq);
        
        assertEquals("DENIED", denied.getStatus());
        assertEquals("Request exceeds tier limits", denied.getDenialReason());
        
        // Verify tenant max seats NOT updated
        Tenant notUpdated = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertEquals(5, notUpdated.getMaxSeats());
    }
    
    @Test
    void shouldGetQuotaStatus() {
        QuotaStatusResponse status = quotaService.getQuotaStatus(testTenant.getId());
        
        assertEquals(testTenant.getId(), status.getTenantId());
        assertEquals(5, status.getMaxSeats());
        assertEquals(1, status.getUsedSeats());
        assertEquals(4, status.getAvailableSeats());
        assertFalse(status.isQuotaExceeded());
        assertFalse(status.isNearingLimit());
    }
    
    @Test
    void shouldDetectNearingLimit() {
        // Add users to reach 85% capacity (4 of 5 seats)
        for (int i = 0; i < 3; i++) {
            userRepository.save(User.builder()
                    .tenantId(testTenant.getId())
                    .email("user" + i + "@test.com")
                    .passwordHash("hashed")
                    .firstName("User")
                    .lastName(String.valueOf(i))
                    .role(User.UserRole.DEVELOPER)
                    .status(User.UserStatus.ACTIVE)
                    .build());
        }
        
        QuotaStatusResponse status = quotaService.getQuotaStatus(testTenant.getId());
        
        assertTrue(status.isNearingLimit());
        assertEquals(80.0, status.getUtilizationPercentage());
    }
}
```

### 3.2 API Integration Tests

**File**: `backend/src/test/java/org/example/signer/controller/QuotaControllerTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class QuotaControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String tenantAdminToken;
    private String platformAdminToken;
    
    @BeforeEach
    void setup() {
        tenantAdminToken = createTenantAdminAndGetToken();
        platformAdminToken = createPlatformAdminAndGetToken();
    }
    
    @Test
    void shouldCheckAvailability() throws Exception {
        mockMvc.perform(get("/api/v1/quota/availability")
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").isBoolean())
                .andExpect(jsonPath("$.maxSeats").isNumber())
                .andExpect(jsonPath("$.usedSeats").isNumber())
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void shouldGetQuotaStatus() throws Exception {
        mockMvc.perform(get("/api/v1/quota/status")
                .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").exists())
                .andExpect(jsonPath("$.maxSeats").exists())
                .andExpect(jsonPath("$.utilizationPercentage").exists())
                .andExpect(jsonPath("$.quotaExceeded").isBoolean());
    }
    
    @Test
    void shouldRequestAdditionalSeats() throws Exception {
        SeatRequestRequest request = new SeatRequestRequest();
        request.setAdditionalSeats(10);
        request.setJustification("Need seats for new team members");
        request.setExpectedGrowth("Adding 8 developers next quarter");
        
        mockMvc.perform(post("/api/v1/quota/request")
                .header("Authorization", "Bearer " + tenantAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.requestedAdditionalSeats").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
    
    @Test
    void platformAdminShouldApproveSeatRequest() throws Exception {
        // First create a request as tenant admin
        // ... (create request logic)
        
        ApproveRequestRequest approveReq = new ApproveRequestRequest();
        approveReq.setApprovedSeats(10);
        approveReq.setNotes("Approved");
        
        mockMvc.perform(patch("/api/v1/quota/requests/1/approve")
                .header("Authorization", "Bearer " + platformAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
```

---

## Acceptance Criteria

- ✅ Real-time seat availability checks before user creation/invitation
- ✅ User invitation blocked when quota exceeded with clear error message
- ✅ User acceptance of invitation blocked if quota reached
- ✅ Quota status endpoint shows utilization percentage and warnings
- ✅ Tenant admins can request additional seats with justification
- ✅ Only one pending seat request allowed per tenant at a time
- ✅ Platform admins can view all pending seat requests
- ✅ Platform admins can approve requests (with modified seat count)
- ✅ Platform admins can deny requests with reason
- ✅ Tenant max_seats updated immediately upon approval
- ✅ Tenant admins receive notifications on request approval/denial
- ✅ Billing API provides seat usage data for all tenants
- ✅ Billing API supports date range filtering for usage periods
- ✅ Quota enforcement consistent across invitation and acceptance flows
- ✅ Error responses include actionable messages and error codes

---

## Implementation Notes

1. **Race Conditions**: Use database transactions to prevent race conditions during quota checks
2. **Caching**: Consider caching seat counts with TTL for high-traffic scenarios
3. **Notifications**: Implement email notifications for seat request status changes
4. **Metrics**: Track quota exceeded events for capacity planning
5. **Soft Limits**: Consider implementing soft limits (warnings at 80%) vs hard limits
6. **Grace Period**: Optional grace period after quota exceeded before hard blocking
7. **Billing Integration**: Extend billing API to integrate with Stripe/payment systems
8. **Historical Tracking**: Implement audit log for seat usage history (Phase 6)
9. **Auto-Approval**: Consider auto-approval rules for specific tiers or amounts
10. **Request Expiry**: Add expiry to pending seat requests (e.g., 30 days)

---

## Performance Considerations

1. **Seat Count Queries**: Index `tenant_id` and `status` columns for fast counting
2. **Concurrent Invitations**: Use optimistic locking to handle concurrent invitation attempts
3. **Quota Cache**: Cache seat utilization data with 5-minute TTL to reduce DB load
4. **Batch Operations**: Provide batch invitation endpoint that checks quota once upfront

---

## Security Considerations

1. **Authorization**: Strictly enforce tenant admin role for quota operations
2. **Input Validation**: Validate seat request amounts to prevent abuse
3. **Rate Limiting**: Implement rate limiting on seat request endpoints
4. **Audit Logging**: Log all quota-related operations for compliance

---

## Next Steps

After Phase 5 completion:
- **Phase 6**: Audit logging and compliance tracking
- **Phase 7**: Advanced analytics and reporting
- **Phase 8**: Multi-region support and data residency
- **Phase 9**: API rate limiting and throttling
- **Phase 10**: Advanced security features (2FA, SSO, SAML)
