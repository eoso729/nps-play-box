package org.example.signer.testdata;

import lombok.RequiredArgsConstructor;
import org.example.signer.entity.*;
import org.example.signer.repository.*;
import org.example.signer.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TestDataFactory {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantSimulatorProfileRepository profileRepository;
    private final Iso20022MessageRepository messageRepository;
    private final TestScenarioRepository scenarioRepository;
    private final ValidationResultRepository validationResultRepository;
    private final AuditEventRepository auditEventRepository;
    private final UserInvitationRepository userInvitationRepository;
    private final ImpersonationSessionRepository impersonationSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void cleanAll() {
        validationResultRepository.deleteAll();
        scenarioRepository.deleteAll();
        messageRepository.deleteAll();
        profileRepository.deleteAll();
        userInvitationRepository.deleteAll();
        impersonationSessionRepository.deleteAll();
        auditEventRepository.deleteAllInBatch();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    public Tenant createTenant(String name, String slug, Tenant.SubscriptionTier tier, int maxSeats) {
        Tenant tenant = Tenant.builder()
                .name(name)
                .slug(slug)
                .status(Tenant.TenantStatus.ACTIVE)
                .subscriptionTier(tier != null ? tier : Tenant.SubscriptionTier.PROFESSIONAL)
                .maxSeats(maxSeats > 0 ? maxSeats : 10)
                .build();
        return tenantRepository.save(tenant);
    }

    public User createUser(Long tenantId, String email, String username, User.UserRole role, String password) {
        User user = User.builder()
                .tenantId(tenantId)
                .userUuid(UUID.randomUUID())
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .firstName("Test")
                .lastName("User")
                .role(role)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build();
        return userRepository.save(user);
    }

    public String generateToken(User user, String tenantSlug) {
        return jwtService.generateToken(user, tenantSlug);
    }

    public TenantSimulatorProfile createSimulatorProfile(Long tenantId, String institutionCode, String bic) {
        TenantSimulatorProfile profile = TenantSimulatorProfile.builder()
                .tenantId(tenantId)
                .institutionCode(institutionCode)
                .institutionName("Bank " + institutionCode + " (Simulator)")
                .bic(bic)
                .schemeCode(institutionCode)
                .defaultCurrency("NGN")
                .defaultAccountNumber("0123456789")
                .defaultAccountName("Settlement Account")
                .defaultBvn("22222222222")
                .autoRespondInbound(true)
                .build();
        return profileRepository.save(profile);
    }

    public Iso20022Message createIsoMessage(
            Long tenantId,
            Iso20022Message.MessageType messageType,
            String messageCode,
            Iso20022Message.MessageDirection direction,
            String rawXml) {

        Iso20022Message message = Iso20022Message.builder()
                .tenantId(tenantId)
                .messageType(messageType)
                .messageCode(messageCode)
                .direction(direction != null ? direction : Iso20022Message.MessageDirection.OUTBOUND)
                .status(Iso20022Message.MessageStatus.DRAFT)
                .rawXml(rawXml != null ? rawXml : "<?xml version=\"1.0\"?><Document><Test>Payload</Test></Document>")
                .transactionReference("TXN-" + UUID.randomUUID())
                .messageId("MSG-" + UUID.randomUUID())
                .build();
        return messageRepository.save(message);
    }

    public AuditEvent createAuditEvent(Long tenantId, Long userId, String action, String resourceType, String resourceId) {
        AuditEvent event = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(tenantId)
                .userId(userId)
                .eventType(AuditEvent.EventType.DATA_MODIFICATION)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .status(AuditEvent.EventStatus.SUCCESS)
                .ipAddress("127.0.0.1")
                .userAgent("MockMvcTest/1.0")
                .metadata(Map.of("testKey", "testValue"))
                .build();
        return auditEventRepository.save(event);
    }
}
