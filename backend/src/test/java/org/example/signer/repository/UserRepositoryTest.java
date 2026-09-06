package org.example.signer.repository;

import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenantA;
    private Tenant tenantB;

    @BeforeEach
    void setUp() {
        tenantA = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Bank A")
                .slug("bank-a-" + System.currentTimeMillis())
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        tenantB = tenantRepository.saveAndFlush(Tenant.builder()
                .name("Bank B")
                .slug("bank-b-" + System.currentTimeMillis())
                .status(Tenant.TenantStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("Should create user linked to tenant with generated UUID")
    void shouldCreateUserLinkedToTenant() {
        User user = User.builder()
                .tenantId(tenantA.getId())
                .email("user@banka.com")
                .passwordHash("hashed_pw")
                .firstName("John")
                .lastName("Doe")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .build();

        User saved = userRepository.saveAndFlush(user);

        assertNotNull(saved.getId());
        assertNotNull(saved.getUserUuid());
        assertEquals(tenantA.getId(), saved.getTenantId());
        assertEquals("user@banka.com", saved.getEmail());
        assertEquals(User.UserRole.DEVELOPER, saved.getRole());
        assertEquals(User.UserStatus.ACTIVE, saved.getStatus());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    @DisplayName("Should find user by email and tenant ID")
    void shouldFindByEmailAndTenantId() {
        userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("alice@banka.com")
                .passwordHash("hashed_pw")
                .role(User.UserRole.TENANT_ADMIN)
                .build());

        Optional<User> found = userRepository.findByEmailAndTenantId("alice@banka.com", tenantA.getId());

        assertTrue(found.isPresent());
        assertEquals("alice@banka.com", found.get().getEmail());
        assertEquals(tenantA.getId(), found.get().getTenantId());

        // Should NOT find in tenant B
        Optional<User> notFoundInB = userRepository.findByEmailAndTenantId("alice@banka.com", tenantB.getId());
        assertFalse(notFoundInB.isPresent());
    }

    @Test
    @DisplayName("Should find user by user UUID")
    void shouldFindByUserUuid() {
        User saved = userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("bob@banka.com")
                .passwordHash("hashed_pw")
                .build());

        Optional<User> found = userRepository.findByUserUuid(saved.getUserUuid());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Should find all users by tenant ID")
    void shouldFindByTenantId() {
        userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("user1@banka.com")
                .passwordHash("hashed_pw")
                .build());

        userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("user2@banka.com")
                .passwordHash("hashed_pw")
                .build());

        userRepository.saveAndFlush(User.builder()
                .tenantId(tenantB.getId())
                .email("user1@bankb.com")
                .passwordHash("hashed_pw")
                .build());

        List<User> tenantAUsers = userRepository.findByTenantId(tenantA.getId());
        assertEquals(2, tenantAUsers.size());

        List<User> tenantBUsers = userRepository.findByTenantId(tenantB.getId());
        assertEquals(1, tenantBUsers.size());
    }

    @Test
    @DisplayName("Should count users by tenant ID and status")
    void shouldCountByTenantIdAndStatus() {
        userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("active1@banka.com")
                .passwordHash("hashed_pw")
                .status(User.UserStatus.ACTIVE)
                .build());

        userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("active2@banka.com")
                .passwordHash("hashed_pw")
                .status(User.UserStatus.ACTIVE)
                .build());

        userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("inactive@banka.com")
                .passwordHash("hashed_pw")
                .status(User.UserStatus.INACTIVE)
                .build());

        long activeCount = userRepository.countByTenantIdAndStatus(tenantA.getId(), User.UserStatus.ACTIVE);
        long inactiveCount = userRepository.countByTenantIdAndStatus(tenantA.getId(), User.UserStatus.INACTIVE);

        assertEquals(2, activeCount);
        assertEquals(1, inactiveCount);
    }

    @Test
    @DisplayName("Should enforce unique constraint on (tenant_id, email)")
    void shouldEnforceUniqueTenantEmail() {
        userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("same@banka.com")
                .passwordHash("hashed_pw")
                .build());

        User duplicate = User.builder()
                .tenantId(tenantA.getId())
                .email("same@banka.com")
                .passwordHash("other_pw")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicate);
        });
    }

    @Test
    @DisplayName("Should allow same email in different tenants")
    void shouldAllowSameEmailInDifferentTenants() {
        User userInA = userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("consultant@external.com")
                .passwordHash("hashed_pw_a")
                .build());

        User userInB = userRepository.saveAndFlush(User.builder()
                .tenantId(tenantB.getId())
                .email("consultant@external.com")
                .passwordHash("hashed_pw_b")
                .build());

        assertNotNull(userInA.getId());
        assertNotNull(userInB.getId());
        assertNotEquals(userInA.getId(), userInB.getId());
        assertEquals(userInA.getEmail(), userInB.getEmail());
        assertNotEquals(userInA.getTenantId(), userInB.getTenantId());
    }

    @Test
    @DisplayName("Should check existence by tenant ID and email")
    void shouldCheckExistsByTenantIdAndEmail() {
        userRepository.saveAndFlush(User.builder()
                .tenantId(tenantA.getId())
                .email("exists@banka.com")
                .passwordHash("hashed_pw")
                .build());

        assertTrue(userRepository.existsByTenantIdAndEmail(tenantA.getId(), "exists@banka.com"));
        assertFalse(userRepository.existsByTenantIdAndEmail(tenantB.getId(), "exists@banka.com"));
    }
}
