package org.example.signer.service;

import org.example.signer.dto.message.TenantSimulatorProfileDto;
import org.example.signer.dto.message.UpdateSimulatorProfileDto;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.TenantSimulatorProfile;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.TenantSimulatorProfileRepository;
import org.example.signer.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantSimulatorProfileServiceTest {

    @Mock
    private TenantSimulatorProfileRepository profileRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantSimulatorProfileService profileService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(101L);
        TenantContext.setTenantSlug("bank-alpha");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should return existing simulator profile for tenant")
    void testGetExistingProfile() {
        TenantSimulatorProfile profile = TenantSimulatorProfile.builder()
                .id(1L)
                .tenantId(101L)
                .profileUuid(UUID.randomUUID())
                .institutionCode("090004")
                .institutionName("Bank Alpha Pseudo")
                .bic("ALPHNGLAXXX")
                .defaultCurrency("NGN")
                .autoRespondInbound(true)
                .build();

        when(profileRepository.findByTenantId(101L)).thenReturn(Optional.of(profile));

        TenantSimulatorProfileDto result = profileService.getCurrentProfile();

        assertNotNull(result);
        assertEquals("090004", result.getInstitutionCode());
        assertEquals("Bank Alpha Pseudo", result.getInstitutionName());
        verify(profileRepository).findByTenantId(101L);
    }

    @Test
    @DisplayName("Should auto-create default simulator profile if not yet configured")
    void testAutoCreateDefaultProfile() {
        when(profileRepository.findByTenantId(101L)).thenReturn(Optional.empty());
        Tenant tenant = Tenant.builder().id(101L).name("Bank Alpha").build();
        when(tenantRepository.findById(101L)).thenReturn(Optional.of(tenant));

        when(profileRepository.save(any(TenantSimulatorProfile.class))).thenAnswer(invocation -> {
            TenantSimulatorProfile p = invocation.getArgument(0);
            p.setId(1L);
            p.setProfileUuid(UUID.randomUUID());
            return p;
        });

        TenantSimulatorProfileDto result = profileService.getCurrentProfile();

        assertNotNull(result);
        assertEquals("999057", result.getInstitutionCode());
        assertEquals("Bank Alpha (Simulator)", result.getInstitutionName());
        verify(profileRepository).save(any(TenantSimulatorProfile.class));
    }

    @Test
    @DisplayName("Should update current simulator profile")
    void testUpdateProfile() {
        TenantSimulatorProfile existing = TenantSimulatorProfile.builder()
                .id(1L)
                .tenantId(101L)
                .profileUuid(UUID.randomUUID())
                .institutionCode("999057")
                .institutionName("Old Name")
                .bic("OLDNBICXXX")
                .defaultCurrency("NGN")
                .autoRespondInbound(true)
                .build();

        when(profileRepository.findByTenantId(101L)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(TenantSimulatorProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateSimulatorProfileDto updateDto = UpdateSimulatorProfileDto.builder()
                .institutionCode("090005")
                .institutionName("Updated Bank Pseudo")
                .bic("NEWBICXXX")
                .defaultAccountNumber("1234567890")
                .defaultAccountName("Test Account")
                .defaultBvn("22222222222")
                .callbackUrl("https://bank.example.com/callback")
                .autoRespondInbound(false)
                .build();

        TenantSimulatorProfileDto updated = profileService.updateCurrentProfile(updateDto);

        assertNotNull(updated);
        assertEquals("090005", updated.getInstitutionCode());
        assertEquals("Updated Bank Pseudo", updated.getInstitutionName());
        assertEquals("NEWBICXXX", updated.getBic());
        assertEquals("1234567890", updated.getDefaultAccountNumber());
        assertFalse(updated.getAutoRespondInbound());
    }
}
