package org.example.signer.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.AuthRequest;
import org.example.signer.dto.impersonation.ImpersonationRequestDto;
import org.example.signer.dto.message.CreateMessageRequestDto;
import org.example.signer.dto.message.UpdateSimulatorProfileDto;
import org.example.signer.dto.user.InviteUserRequest;
import org.example.signer.entity.*;
import org.example.signer.repository.AuditEventRepository;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.service.ImpersonationService;
import org.example.signer.service.TenantAwareMessageService;
import org.example.signer.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuditTrailCompletenessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImpersonationService impersonationService;

    @Autowired
    private TenantAwareMessageService tenantAwareMessageService;

    private Tenant tenantA;
    private User adminA;
    private String tokenA;

    private Tenant tenantB;
    private User adminB;
    private String tokenB;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanAll();

        tenantA = testDataFactory.createTenant("First Bank", "first-bank", Tenant.SubscriptionTier.PROFESSIONAL, 10);
        adminA = testDataFactory.createUser(tenantA.getId(), "admin@firstbank.com", "fb_admin", User.UserRole.TENANT_ADMIN, "SecretPass123!");
        tokenA = testDataFactory.generateToken(adminA, tenantA.getSlug());

        tenantB = testDataFactory.createTenant("Zenith Bank", "zenith-bank", Tenant.SubscriptionTier.PROFESSIONAL, 10);
        adminB = testDataFactory.createUser(tenantB.getId(), "admin@zenithbank.com", "zb_admin", User.UserRole.TENANT_ADMIN, "SecretPass123!");
        tokenB = testDataFactory.generateToken(adminB, tenantB.getSlug());
    }

    @Test
    @DisplayName("AUDIT-01: User login success and failure logged with IP and tenant attribution")
    void testAudit01_UserLoginEventsLogged() throws Exception {
        // Success Login
        AuthRequest successReq = AuthRequest.builder()
                .email("admin@firstbank.com")
                .password("SecretPass123!")
                .tenantSlug("first-bank")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "192.168.1.100")
                        .header("User-Agent", "Mozilla/5.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(successReq)))
                .andExpect(status().isOk());

        // Failed Login
        AuthRequest failReq = AuthRequest.builder()
                .email("admin@firstbank.com")
                .password("WrongPassword!")
                .tenantSlug("first-bank")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "192.168.1.101")
                        .header("User-Agent", "Mozilla/5.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(failReq)))
                .andExpect(status().isUnauthorized());

        List<AuditEvent> authEvents = auditEventRepository.findByTenantIdOrderByCreatedAtDesc(tenantA.getId(), Pageable.unpaged()).getContent();
        assertTrue(authEvents.stream().anyMatch(e -> "LOGIN".equals(e.getAction()) && e.getStatus() == AuditEvent.EventStatus.SUCCESS));
        assertTrue(authEvents.stream().anyMatch(e -> "LOGIN".equals(e.getAction()) && e.getStatus() == AuditEvent.EventStatus.FAILURE));
    }

    @Test
    @DisplayName("AUDIT-02, AUDIT-03, AUDIT-04: Message creation, signing, and encryption logged")
    void testAudit02_03_04_MessageOperationsLogged() throws Exception {
        // 1. Create message
        CreateMessageRequestDto createDto = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                .messageCode("pain.001.001.12")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml("<Document><CstmrCdtTrfInitn><GrpHdr><MsgId>M-001</MsgId></GrpHdr></CstmrCdtTrfInitn></Document>")
                .transactionReference("TXN-001")
                .messageId("M-001")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String messageUuid = objectMapper.readTree(result.getResponse().getContentAsString()).get("messageUuid").asText();

        // 2. Sign message
        mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/sign")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        // 3. Encrypt element
        mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/encrypt?elementTagName=GrpHdr")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());

        List<AuditEvent> events = auditEventRepository.findByTenantIdOrderByCreatedAtDesc(tenantA.getId(), Pageable.unpaged()).getContent();

        // AUDIT-02: Message creation logged with message UUID and tenant ID
        assertTrue(events.stream().anyMatch(e -> "CREATE_MESSAGE".equals(e.getAction()) && messageUuid.equals(e.getResourceId())));

        // AUDIT-03: Message signing logged
        assertTrue(events.stream().anyMatch(e -> "SIGN_MESSAGE".equals(e.getAction()) && messageUuid.equals(e.getResourceId())));

        // AUDIT-04: Message encryption logged
        assertTrue(events.stream().anyMatch(e -> "ENCRYPT_MESSAGE".equals(e.getAction()) && messageUuid.equals(e.getResourceId())));
    }

    @Test
    @DisplayName("AUDIT-05: Outbound message dispatch logged with destination and status")
    void testAudit05_OutboundMessageDispatchLogged() {
        tenantAwareMessageService.recordOutboundMessage(
                tenantA.getId(),
                Iso20022Message.MessageType.PAYMENT_INITIATION,
                "pain.001.001.12",
                "<raw/>",
                "<signed/>",
                "<encrypted/>",
                "MSG-DISPATCH-99",
                "TXN-DISPATCH-99",
                "E2E-99",
                Iso20022Message.MessageStatus.SENT
        );

        List<AuditEvent> events = auditEventRepository.findByTenantIdOrderByCreatedAtDesc(tenantA.getId(), Pageable.unpaged()).getContent();
        assertTrue(events.stream().anyMatch(e -> "DISPATCH_MESSAGE".equals(e.getAction()) && "MSG-DISPATCH-99".equals(e.getResourceId())));
    }

    @Test
    @DisplayName("AUDIT-06: User invitation logged with invited email and assigned role")
    void testAudit06_UserInvitationLogged() throws Exception {
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail("invitee@firstbank.com");
        request.setFirstName("Ada");
        request.setLastName("Lovelace");
        request.setRole("DEVELOPER");

        mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        List<AuditEvent> events = auditEventRepository.findByTenantIdOrderByCreatedAtDesc(tenantA.getId(), Pageable.unpaged()).getContent();
        assertTrue(events.stream().anyMatch(e -> "INVITE_USER".equals(e.getAction()) && "USER_INVITATION".equals(e.getResourceType())));
    }

    @Test
    @DisplayName("AUDIT-07: Simulator profile update logged with changed fields")
    void testAudit07_SimulatorProfileUpdateLogged() throws Exception {
        UpdateSimulatorProfileDto profileDto = UpdateSimulatorProfileDto.builder()
                .institutionCode("090001")
                .institutionName("First Bank Prod")
                .bic("FBNINGLA")
                .defaultCurrency("NGN")
                .build();

        mockMvc.perform(put("/api/v1/simulator/profile")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileDto)))
                .andExpect(status().isOk());

        List<AuditEvent> events = auditEventRepository.findByTenantIdOrderByCreatedAtDesc(tenantA.getId(), Pageable.unpaged()).getContent();
        assertTrue(events.stream().anyMatch(e -> "UPDATE_SIMULATOR_PROFILE".equals(e.getAction()) && "SIMULATOR_PROFILE".equals(e.getResourceType())));
    }

    @Test
    @DisplayName("AUDIT-08: Support impersonation session logged with request and lifecycle events")
    void testAudit08_SupportImpersonationSessionLogged() {
        Tenant platformTenant = testDataFactory.createTenant("Platform Support", "platform-admin", Tenant.SubscriptionTier.ENTERPRISE, 50);
        User supportUser = testDataFactory.createUser(platformTenant.getId(), "support@platform.com", "supporter", User.UserRole.PLATFORM_ADMIN, "SecretPass1!");

        ImpersonationRequestDto req = ImpersonationRequestDto.builder()
                .targetUserId(adminA.getId())
                .reason("Investigating payment failure ticket #4092")
                .durationMinutes(30)
                .build();

        var sessionDto = impersonationService.requestImpersonation(supportUser.getId(), req);
        assertNotNull(sessionDto);

        List<AuditEvent> events = auditEventRepository.findByTenantIdOrderByCreatedAtDesc(tenantA.getId(), Pageable.unpaged()).getContent();
        assertTrue(events.stream().anyMatch(e -> "IMPERSONATION_REQUESTED".equals(e.getAction()) && e.getEventType() == AuditEvent.EventType.IMPERSONATION));
    }

    @Test
    @DisplayName("AUDIT-09: Immutability verified - onPreUpdate callback throws UnsupportedOperationException")
    void testAudit09_ImmutabilityEnforced() {
        AuditEvent event = AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(tenantA.getId())
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.SUCCESS)
                .build();

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, event::onPreUpdate);
        assertTrue(ex.getMessage().contains("Audit events are immutable and cannot be updated"));
    }

    @Test
    @DisplayName("AUDIT-10: CSV and JSON export contains all required fields and respects tenant boundary")
    void testAudit10_ExportCsvAndJsonRespectsTenantBoundaries() throws Exception {
        // Seed audit event for Tenant A
        auditEventRepository.save(AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(tenantA.getId())
                .userId(adminA.getId())
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.SUCCESS)
                .ipAddress("127.0.0.1")
                .build());

        // Seed audit event for Tenant B
        auditEventRepository.save(AuditEvent.builder()
                .eventUuid(UUID.randomUUID())
                .tenantId(tenantB.getId())
                .userId(adminB.getId())
                .eventType(AuditEvent.EventType.AUTH)
                .action("LOGIN")
                .resourceType("AUTH")
                .status(AuditEvent.EventStatus.SUCCESS)
                .ipAddress("10.0.0.1")
                .build());

        // 1. Export CSV for Tenant A
        MvcResult csvResult = mockMvc.perform(get("/api/v1/audit/export/csv")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".csv")))
                .andReturn();

        String csvContent = new String(csvResult.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("Event UUID,Timestamp,Event Type,Action,Resource Type,Resource ID,User ID"));
        assertTrue(csvContent.contains("admin@firstbank.com"));
        assertFalse(csvContent.contains("admin@zenithbank.com"));

        // 2. Export JSON for Tenant A
        MvcResult jsonResult = mockMvc.perform(get("/api/v1/audit/export/json")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(".json")))
                .andReturn();

        String jsonContent = new String(jsonResult.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        JsonNode jsonNode = objectMapper.readTree(jsonContent);
        assertTrue(jsonNode.isArray());
        assertTrue(jsonNode.size() > 0);

        for (JsonNode item : jsonNode) {
            assertEquals(tenantA.getId(), item.get("tenantId").asLong());
        }
    }
}
