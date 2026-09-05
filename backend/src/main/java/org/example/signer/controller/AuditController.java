package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.audit.AuditEventResponse;
import org.example.signer.dto.audit.AuditEventSearchRequest;
import org.example.signer.entity.User;
import org.example.signer.security.RequirePlatformAdmin;
import org.example.signer.security.RequireTenantAdmin;
import org.example.signer.security.TenantContext;
import org.example.signer.security.TenantUserDetails;
import org.example.signer.service.AuditExportService;
import org.example.signer.service.AuditQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Tag(name = "Immutable Audit Trail", description = "APIs for querying, filtering, and exporting compliance audit trails")
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditQueryService auditQueryService;
    private final AuditExportService auditExportService;

    @Operation(summary = "Search audit events within tenant", description = "Tenant administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit events retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping("/events")
    public ResponseEntity<Page<AuditEventResponse>> searchTenantEvents(
            @ModelAttribute AuditEventSearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        Page<AuditEventResponse> events = auditQueryService.searchAuditEvents(tenantId, searchRequest, pageable);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Get audit history for a specific resource", description = "Tenant administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource audit history retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping("/resource-history")
    public ResponseEntity<List<AuditEventResponse>> getResourceHistory(
            @RequestParam String resourceType,
            @RequestParam String resourceId,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        List<AuditEventResponse> history = auditQueryService.getResourceHistory(tenantId, resourceType, resourceId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Export tenant audit events as JSON", description = "Tenant administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit events exported as JSON file"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportTenantJson(
            @ModelAttribute AuditEventSearchRequest searchRequest,
            @RequestParam(defaultValue = "10000") int maxRecords,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) throws IOException {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        byte[] data = auditExportService.exportAsJson(tenantId, searchRequest, maxRecords);
        String filename = "audit-events-" + tenantId + "-" + timestamp() + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @Operation(summary = "Export tenant audit events as CSV", description = "Tenant administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit events exported as CSV file"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequireTenantAdmin
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportTenantCsv(
            @ModelAttribute AuditEventSearchRequest searchRequest,
            @RequestParam(defaultValue = "10000") int maxRecords,
            @RequestAttribute(value = "tenantId", required = false) Long requestTenantId,
            Authentication authentication) {

        Long tenantId = resolveTenantId(requestTenantId, authentication);
        byte[] data = auditExportService.exportAsCsv(tenantId, searchRequest, maxRecords);
        String filename = "audit-events-" + tenantId + "-" + timestamp() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    // Platform Admin Endpoints

    @Operation(summary = "Search audit events across platform or any tenant", description = "Platform administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform audit events retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequirePlatformAdmin
    @GetMapping("/admin/events")
    public ResponseEntity<Page<AuditEventResponse>> searchPlatformEvents(
            @ModelAttribute AuditEventSearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditEventResponse> events = auditQueryService.searchAuditEvents(
                searchRequest != null ? searchRequest.getTenantId() : null,
                searchRequest,
                pageable);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Export platform audit events as JSON", description = "Platform administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform audit events exported as JSON file"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequirePlatformAdmin
    @GetMapping("/admin/export/json")
    public ResponseEntity<byte[]> exportPlatformJson(
            @ModelAttribute AuditEventSearchRequest searchRequest,
            @RequestParam(defaultValue = "10000") int maxRecords) throws IOException {

        Long targetTenantId = searchRequest != null ? searchRequest.getTenantId() : null;
        byte[] data = auditExportService.exportAsJson(targetTenantId, searchRequest, maxRecords);
        String filename = "audit-events-platform-" + timestamp() + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @Operation(summary = "Export platform audit events as CSV", description = "Platform administrators only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Platform audit events exported as CSV file"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @RequirePlatformAdmin
    @GetMapping("/admin/export/csv")
    public ResponseEntity<byte[]> exportPlatformCsv(
            @ModelAttribute AuditEventSearchRequest searchRequest,
            @RequestParam(defaultValue = "10000") int maxRecords) {

        Long targetTenantId = searchRequest != null ? searchRequest.getTenantId() : null;
        byte[] data = auditExportService.exportAsCsv(targetTenantId, searchRequest, maxRecords);
        String filename = "audit-events-platform-" + timestamp() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    private Long resolveTenantId(Long requestTenantId, Authentication authentication) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) return tenantId;
        if (requestTenantId != null) return requestTenantId;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof TenantUserDetails tud) {
                return tud.getTenantId();
            } else if (authentication.getPrincipal() instanceof User u) {
                return u.getTenantId();
            }
        }
        throw new AccessDeniedException("No tenant context available");
    }

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }
}
