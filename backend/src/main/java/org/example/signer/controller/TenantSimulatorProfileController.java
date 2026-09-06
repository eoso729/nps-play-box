package org.example.signer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.dto.message.TenantSimulatorProfileDto;
import org.example.signer.dto.message.UpdateSimulatorProfileDto;
import org.example.signer.service.TenantSimulatorProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Simulator Profile", description = "Tenant pseudo-bank profile, institution code, and simulator routing")
@RestController
@RequestMapping("/api/v1/simulator/profile")
@RequiredArgsConstructor
public class TenantSimulatorProfileController {

    private final TenantSimulatorProfileService profileService;

    @Operation(summary = "Get current simulator profile", description = "Retrieves pseudo-bank identification and default accounts for current tenant")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @GetMapping
    public ResponseEntity<TenantSimulatorProfileDto> getProfile() {
        return ResponseEntity.ok(profileService.getCurrentProfile());
    }

    @Operation(summary = "Update simulator profile", description = "Updates pseudo-bank codes, routing callback, and default mock accounts")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid profile payload")
    })
    @PutMapping
    public ResponseEntity<TenantSimulatorProfileDto> updateProfile(@Valid @RequestBody UpdateSimulatorProfileDto dto) {
        return ResponseEntity.ok(profileService.updateCurrentProfile(dto));
    }
}
