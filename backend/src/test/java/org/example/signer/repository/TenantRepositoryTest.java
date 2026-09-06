package org.example.signer.repository;

import org.example.signer.entity.Tenant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    @DisplayName("Should create tenant and generate UUID and timestamps")
    void shouldCreateTenant() {
        Tenant tenant = Tenant.builder()
                .name("Apex Bank")
                .slug("apex-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                .build();

        Tenant saved = tenantRepository.saveAndFlush(tenant);

        assertNotNull(saved.getId());
        assertNotNull(saved.getTenantUuid());
        assertEquals("apex-bank", saved.getSlug());
        assertEquals(10, saved.getMaxSeats());
        assertEquals(Tenant.TenantStatus.ACTIVE, saved.getStatus());
        assertEquals(Tenant.SubscriptionTier.PROFESSIONAL, saved.getSubscriptionTier());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("Should find tenant by slug")
    void shouldFindBySlug() {
        Tenant tenant = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Zenith Trust")
                .slug("zenith-trust")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        Optional<Tenant> found = tenantRepository.findBySlug("zenith-trust");

        assertTrue(found.isPresent());
        assertEquals(tenant.getId(), found.get().getId());
        assertEquals("Zenith Trust", found.get().getName());
    }

    @Test
    @DisplayName("Should find tenant by UUID")
    void shouldFindByTenantUuid() {
        Tenant tenant = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Starlight Capital")
                .slug("starlight-capital")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        Optional<Tenant> found = tenantRepository.findByTenantUuid(tenant.getTenantUuid());

        assertTrue(found.isPresent());
        assertEquals(tenant.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Should check existence by slug")
    void shouldCheckExistsBySlug() {
        tenantRepository.saveAndFlush(Tenant.builder()
                .name("First National")
                .slug("first-national")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        assertTrue(tenantRepository.existsBySlug("first-national"));
        assertFalse(tenantRepository.existsBySlug("non-existent"));
    }

    @Test
    @DisplayName("Should enforce unique slug constraint")
    void shouldEnforceUniqueSlug() {
        tenantRepository.saveAndFlush(Tenant.builder()
                .name("Duplicate Test A")
                .slug("duplicate-slug")
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        Tenant duplicate = Tenant.builder()
                .name("Duplicate Test B")
                .slug("duplicate-slug")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            tenantRepository.saveAndFlush(duplicate);
        });
    }
}
