package org.example.signer.testdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.entity.*;
import org.example.signer.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@Profile("seed-test-data")
@RequiredArgsConstructor
public class TestDataGenerator implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantSimulatorProfileRepository profileRepository;
    private final Iso20022MessageRepository messageRepository;
    private final AuditEventRepository auditEventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (tenantRepository.count() > 1) {
            log.info("Test data already exists, skipping automatic seed.");
            return;
        }

        log.info("Seeding multi-tenant test data for local/container development...");

        String[][] banks = {
            {"First Bank of Nigeria", "first-bank", "090004", "FBNINGLAXXX"},
            {"Zenith Bank", "zenith-bank", "999057", "ZEBNNGLAXXX"},
            {"Access Bank", "access-bank", "090005", "ACCSNGLAXXX"},
            {"Guaranty Trust Bank", "gtbank", "090006", "GTBINLAXXX"},
            {"United Bank for Africa", "uba", "090007", "UNAFNGLAXXX"}
        };

        for (String[] bank : banks) {
            String name = bank[0];
            String slug = bank[1];
            String instCode = bank[2];
            String bic = bank[3];

            Tenant tenant = tenantRepository.save(Tenant.builder()
                    .name(name)
                    .slug(slug)
                    .status(Tenant.TenantStatus.ACTIVE)
                    .maxSeats(20)
                    .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                    .build());

            // Create admin user
            User admin = userRepository.save(User.builder()
                    .tenantId(tenant.getId())
                    .userUuid(UUID.randomUUID())
                    .email("admin@" + slug + ".test")
                    .username("admin_" + slug)
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .firstName(name)
                    .lastName("Admin")
                    .role(User.UserRole.TENANT_ADMIN)
                    .status(User.UserStatus.ACTIVE)
                    .authProvider("LOCAL")
                    .build());

            // Create developer user
            User dev = userRepository.save(User.builder()
                    .tenantId(tenant.getId())
                    .userUuid(UUID.randomUUID())
                    .email("dev@" + slug + ".test")
                    .username("dev_" + slug)
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .firstName(name)
                    .lastName("Developer")
                    .role(User.UserRole.DEVELOPER)
                    .status(User.UserStatus.ACTIVE)
                    .authProvider("LOCAL")
                    .build());

            // Create viewer user
            User viewer = userRepository.save(User.builder()
                    .tenantId(tenant.getId())
                    .userUuid(UUID.randomUUID())
                    .email("viewer@" + slug + ".test")
                    .username("viewer_" + slug)
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .firstName(name)
                    .lastName("Viewer")
                    .role(User.UserRole.VIEWER)
                    .status(User.UserStatus.ACTIVE)
                    .authProvider("LOCAL")
                    .build());

            // Create simulator profile
            profileRepository.save(TenantSimulatorProfile.builder()
                    .tenantId(tenant.getId())
                    .institutionCode(instCode)
                    .institutionName(name + " (Simulator)")
                    .bic(bic)
                    .schemeCode(instCode)
                    .defaultCurrency("NGN")
                    .defaultAccountNumber("0123456789")
                    .defaultAccountName(name + " Settlement")
                    .defaultBvn("22222222222")
                    .callbackUrl("https://" + slug + ".internal/callback")
                    .autoRespondInbound(true)
                    .build());

            // Create sample draft message
            messageRepository.save(Iso20022Message.builder()
                    .tenantId(tenant.getId())
                    .messageType(Iso20022Message.MessageType.TRANSFER)
                    .messageCode("pacs.008.001.02")
                    .direction(Iso20022Message.MessageDirection.OUTBOUND)
                    .status(Iso20022Message.MessageStatus.DRAFT)
                    .rawXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document><FIToFICstmrCdtTrf><GrpHdr><MsgId>" + instCode + "001</MsgId></GrpHdr></FIToFICstmrCdtTrf></Document>")
                    .messageId(instCode + "001")
                    .transactionReference("TXN-" + slug.toUpperCase() + "-001")
                    .createdBy(admin.getId())
                    .build());

            // Create sample audit log
            auditEventRepository.save(AuditEvent.builder()
                    .eventUuid(UUID.randomUUID())
                    .tenantId(tenant.getId())
                    .userId(admin.getId())
                    .eventType(AuditEvent.EventType.CONFIG_CHANGE)
                    .action("SIMULATOR_PROFILE_INITIALIZED")
                    .resourceType("SIMULATOR_PROFILE")
                    .resourceId(instCode)
                    .status(AuditEvent.EventStatus.SUCCESS)
                    .ipAddress("127.0.0.1")
                    .userAgent("TestDataGenerator/1.0")
                    .metadata(Map.of("institutionCode", instCode))
                    .build());

            log.info("Seeded tenant '{}' (instCode={}) with admin, developer, viewer, simulator profile and sample message.", name, instCode);
        }

        log.info("Multi-tenant test data seeding completed successfully.");
    }
}
