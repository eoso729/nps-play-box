package org.example.signer.repository;

import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.entity.UserInvitation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserInvitationRepositoryTest {

    @Autowired
    private UserInvitationRepository userInvitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant tenant;
    private User inviter;

    @BeforeEach
    void setUp() {
        tenant = tenantRepository.saveAndFlush(Tenant.builder()
                .name("First Bank")
                .slug("first-bank-" + System.currentTimeMillis())
                .status(Tenant.TenantStatus.ACTIVE)
                .build());

        inviter = userRepository.saveAndFlush(User.builder()
                .tenantId(tenant.getId())
                .email("inviter@firstbank.com")
                .passwordHash("hashed_pw")
                .role(User.UserRole.TENANT_ADMIN)
                .build());
    }

    @Test
    @DisplayName("Should create user invitation and find by token")
    void shouldCreateAndFindByToken() {
        String token = UUID.randomUUID().toString();
        UserInvitation invitation = UserInvitation.builder()
                .tenantId(tenant.getId())
                .email("newuser@firstbank.com")
                .role(User.UserRole.DEVELOPER)
                .invitationToken(token)
                .invitedBy(inviter.getId())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        UserInvitation saved = userInvitationRepository.saveAndFlush(invitation);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertFalse(saved.isExpired());
        assertFalse(saved.isAccepted());

        Optional<UserInvitation> found = userInvitationRepository.findByInvitationToken(token);
        assertTrue(found.isPresent());
        assertEquals("newuser@firstbank.com", found.get().getEmail());
        assertEquals(tenant.getId(), found.get().getTenantId());
    }

    @Test
    @DisplayName("Should find pending invitations by tenant ID")
    void shouldFindPendingInvitations() {
        UserInvitation pending = UserInvitation.builder()
                .tenantId(tenant.getId())
                .email("pending@firstbank.com")
                .role(User.UserRole.DEVELOPER)
                .invitationToken(UUID.randomUUID().toString())
                .invitedBy(inviter.getId())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        UserInvitation accepted = UserInvitation.builder()
                .tenantId(tenant.getId())
                .email("accepted@firstbank.com")
                .role(User.UserRole.VIEWER)
                .invitationToken(UUID.randomUUID().toString())
                .invitedBy(inviter.getId())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .acceptedAt(LocalDateTime.now())
                .build();

        userInvitationRepository.saveAndFlush(pending);
        userInvitationRepository.saveAndFlush(accepted);

        List<UserInvitation> pendingList = userInvitationRepository.findByTenantIdAndAcceptedAtIsNull(tenant.getId());
        assertEquals(1, pendingList.size());
        assertEquals("pending@firstbank.com", pendingList.get(0).getEmail());
    }

    @Test
    @DisplayName("Should delete expired and unaccepted invitations")
    void shouldDeleteExpiredUnacceptedInvitations() {
        UserInvitation expired = UserInvitation.builder()
                .tenantId(tenant.getId())
                .email("expired@firstbank.com")
                .role(User.UserRole.VIEWER)
                .invitationToken(UUID.randomUUID().toString())
                .invitedBy(inviter.getId())
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        UserInvitation valid = UserInvitation.builder()
                .tenantId(tenant.getId())
                .email("valid@firstbank.com")
                .role(User.UserRole.VIEWER)
                .invitationToken(UUID.randomUUID().toString())
                .invitedBy(inviter.getId())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        userInvitationRepository.saveAndFlush(expired);
        userInvitationRepository.saveAndFlush(valid);

        userInvitationRepository.deleteByExpiresAtBeforeAndAcceptedAtIsNull(LocalDateTime.now());
        userInvitationRepository.flush();

        List<UserInvitation> remaining = userInvitationRepository.findAll();
        assertEquals(1, remaining.size());
        assertEquals("valid@firstbank.com", remaining.get(0).getEmail());
    }
}
