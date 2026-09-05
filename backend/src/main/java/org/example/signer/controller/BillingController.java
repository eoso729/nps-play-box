package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.billing.SeatUsageResponse;
import org.example.signer.security.RequirePlatformAdmin;
import org.example.signer.service.BillingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Tag(name = "Billing Management", description = "APIs for billing and seat usage reporting")
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @Operation(summary = "Get seat usage for all tenants", description = "Platform administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Seat usage retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequirePlatformAdmin
    @GetMapping("/seat-usage")
    public ResponseEntity<List<SeatUsageResponse>> getAllSeatUsage(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime periodStart,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime periodEnd) {

        if (periodStart == null) {
            periodStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        if (periodEnd == null) {
            periodEnd = LocalDateTime.now();
        }

        return ResponseEntity.ok(
                billingService.getAllTenantsSeatUsage(periodStart, periodEnd));
    }

    @Operation(summary = "Get seat usage for specific tenant", description = "Platform administrators only")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tenant seat usage retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
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
            periodStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
        if (periodEnd == null) {
            periodEnd = LocalDateTime.now();
        }

        return ResponseEntity.ok(
                billingService.getTenantSeatUsage(tenantId, periodStart, periodEnd));
    }
}
