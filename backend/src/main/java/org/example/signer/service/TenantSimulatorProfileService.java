package org.example.signer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.audit.Auditable;
import org.example.signer.dto.message.TenantSimulatorProfileDto;
import org.example.signer.dto.message.UpdateSimulatorProfileDto;
import org.example.signer.entity.AuditEvent;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.TenantSimulatorProfile;
import org.example.signer.exception.ResourceNotFoundException;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.TenantSimulatorProfileRepository;
import org.example.signer.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantSimulatorProfileService {

    private final TenantSimulatorProfileRepository profileRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public TenantSimulatorProfileDto getCurrentProfile() {
        Long tenantId = resolveTenantId();
        return TenantSimulatorProfileDto.fromEntity(getOrCreateProfile(tenantId));
    }

    @Transactional
    public TenantSimulatorProfileDto getProfileForTenant(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        return TenantSimulatorProfileDto.fromEntity(getOrCreateProfile(tenantId));
    }

    @Auditable(eventType = AuditEvent.EventType.CONFIG_CHANGE, action = "UPDATE_SIMULATOR_PROFILE", resourceType = "SIMULATOR_PROFILE", resourceId = "#result != null ? #result.institutionCode : null")
    @Transactional
    public TenantSimulatorProfileDto updateCurrentProfile(UpdateSimulatorProfileDto dto) {
        Long tenantId = resolveTenantId();
        TenantSimulatorProfile profile = getOrCreateProfile(tenantId);

        if (dto.getInstitutionCode() != null) profile.setInstitutionCode(dto.getInstitutionCode());
        if (dto.getInstitutionName() != null) profile.setInstitutionName(dto.getInstitutionName());
        if (dto.getBic() != null) profile.setBic(dto.getBic());
        if (dto.getSchemeCode() != null) profile.setSchemeCode(dto.getSchemeCode());
        if (dto.getDefaultCurrency() != null) profile.setDefaultCurrency(dto.getDefaultCurrency());
        if (dto.getDefaultAccountNumber() != null) profile.setDefaultAccountNumber(dto.getDefaultAccountNumber());
        if (dto.getDefaultAccountName() != null) profile.setDefaultAccountName(dto.getDefaultAccountName());
        if (dto.getDefaultBvn() != null) profile.setDefaultBvn(dto.getDefaultBvn());
        if (dto.getCallbackUrl() != null) profile.setCallbackUrl(dto.getCallbackUrl());
        if (dto.getAutoRespondInbound() != null) profile.setAutoRespondInbound(dto.getAutoRespondInbound());

        profile.setUpdatedAt(LocalDateTime.now());
        TenantSimulatorProfile saved = profileRepository.save(profile);
        log.info("Updated simulator profile for tenant {}: institutionCode={}", tenantId, saved.getInstitutionCode());

        return TenantSimulatorProfileDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Optional<TenantSimulatorProfile> findByInstitutionCode(String institutionCode) {
        return profileRepository.findByInstitutionCode(institutionCode);
    }

    @Transactional(readOnly = true)
    public Optional<TenantSimulatorProfile> findByTenantId(Long tenantId) {
        return profileRepository.findByTenantId(tenantId);
    }

    private TenantSimulatorProfile getOrCreateProfile(Long tenantId) {
        return profileRepository.findByTenantId(tenantId).orElseGet(() -> {
            String tenantName = tenantRepository.findById(tenantId)
                    .map(Tenant::getName)
                    .orElse("Tenant " + tenantId);

            TenantSimulatorProfile newProfile = TenantSimulatorProfile.builder()
                    .tenantId(tenantId)
                    .institutionCode("999057")
                    .institutionName(tenantName + " (Simulator)")
                    .bic("SIMUNGLAXXX")
                    .schemeCode("999057")
                    .defaultCurrency("NGN")
                    .autoRespondInbound(true)
                    .build();

            log.info("Creating default simulator profile for tenant {}: {}", tenantId, tenantName);
            return profileRepository.save(newProfile);
        });
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new ResourceNotFoundException("Tenant", "context", "No tenant context available in request");
        }
        return tenantId;
    }
}
