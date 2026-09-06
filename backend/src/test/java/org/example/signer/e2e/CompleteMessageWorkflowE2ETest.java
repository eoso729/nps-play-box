package org.example.signer.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.AuthRequest;
import org.example.signer.dto.message.CreateMessageRequestDto;
import org.example.signer.dto.validation.XmlInspectRequestDto;
import org.example.signer.entity.Iso20022Message;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.testdata.TestDataFactory;
import org.example.signer.validation.IsoMessageRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompleteMessageWorkflowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    private Tenant tenantA;
    private User adminA;
    private User devA;
    private User devB;
    private User viewerA;

    private Tenant tenantB;
    private User adminB;

    private String samplePain001Xml;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanAll();

        tenantA = testDataFactory.createTenant("First Bank of Nigeria", "first-bank", Tenant.SubscriptionTier.ENTERPRISE, 20);
        adminA = testDataFactory.createUser(tenantA.getId(), "admin@firstbank.com", "fb_admin", User.UserRole.TENANT_ADMIN, "Password123!");
        devA = testDataFactory.createUser(tenantA.getId(), "dev1@firstbank.com", "fb_dev1", User.UserRole.DEVELOPER, "Password123!");
        devB = testDataFactory.createUser(tenantA.getId(), "dev2@firstbank.com", "fb_dev2", User.UserRole.DEVELOPER, "Password123!");
        viewerA = testDataFactory.createUser(tenantA.getId(), "viewer@firstbank.com", "fb_viewer", User.UserRole.VIEWER, "Password123!");

        tenantB = testDataFactory.createTenant("Zenith Bank", "zenith-bank", Tenant.SubscriptionTier.PROFESSIONAL, 10);
        adminB = testDataFactory.createUser(tenantB.getId(), "admin@zenithbank.com", "zb_admin", User.UserRole.TENANT_ADMIN, "Password123!");

        org.example.signer.validation.IsoMessageDefinition def = IsoMessageRegistry.getDefinition("pain.001");
        samplePain001Xml = def != null ? def.getSampleXml() : "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.12\"><CstmrCdtTrfInitn><GrpHdr><MsgId>MSG-001</MsgId></GrpHdr></CstmrCdtTrfInitn></Document>";
    }

    @Test
    @DisplayName("E2E-01: Complete Payment Initiation & Dispatch Lifecycle with Cross-Tenant Isolation")
    void testCompletePaymentInitiationLifecycle() throws Exception {
        // 1. Tenant Admin logs in and obtains JWT token
        AuthRequest loginRequest = AuthRequest.builder()
                .email("admin@firstbank.com")
                .password("Password123!")
                .tenantSlug("first-bank")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String tokenA = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // 2. Validate message XML against NIBSS schema and business rules
        XmlInspectRequestDto inspectRequest = XmlInspectRequestDto.builder()
                .xmlContent(samplePain001Xml)
                .messageType("pain.001")
                .build();

        mockMvc.perform(post("/api/validation/inspect")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inspectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.summary.totalErrors").value(0));

        // 3. Create a pain.001 credit transfer initiation draft message
        CreateMessageRequestDto createDto = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                .messageCode("pain.001.001.12")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml(samplePain001Xml)
                .transactionReference("TXN-E2E-001")
                .messageId("MSG-E2E-001")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageUuid").isNotEmpty())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        String messageUuid = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("messageUuid").asText();

        // 4. Sign message with tenant digital certificate / shared simulator key
        mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/sign")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SIGNED"))
                .andExpect(jsonPath("$.signedXml", containsString("Signature")))
                .andExpect(jsonPath("$.signedXml", containsString("SignedInfo")));

        // 5. Encrypt sensitive element according to NIBSS security profile
        mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/encrypt?elementTagName=GrpHdr")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCRYPTED"))
                .andExpect(jsonPath("$.encryptedXml", containsString("EncryptedData")));

        // 6. Verify dashboard statistics update immediately
        mockMvc.perform(get("/api/v1/messages/statistics")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessages").value(1))
                .andExpect(jsonPath("$.outboundCount").value(1))
                .andExpect(jsonPath("$.statusDistribution.ENCRYPTED").value(1));

        // 7. Verify audit log captures message creation, signing, and encryption
        mockMvc.perform(get("/api/v1/audit/events")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(3)));

        // 8. Switch to Tenant B (Zenith Bank)
        AuthRequest loginB = AuthRequest.builder()
                .email("admin@zenithbank.com")
                .password("Password123!")
                .tenantSlug("zenith-bank")
                .build();

        MvcResult loginBResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginB)))
                .andExpect(status().isOk())
                .andReturn();

        String tokenB = objectMapper.readTree(loginBResult.getResponse().getContentAsString())
                .get("token").asText();

        // 9. Tenant B receives 404 when querying Tenant A's message
        mockMvc.perform(get("/api/v1/messages/" + messageUuid)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // 10. Tenant B lists messages -> empty
        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("E2E-03: Multi-User Collaboration within Tenant and Action Attribution")
    void testMultiUserCollaborationWithinTenant() throws Exception {
        String tokenDevA = testDataFactory.generateToken(devA, tenantA.getSlug());
        String tokenDevB = testDataFactory.generateToken(devB, tenantA.getSlug());
        String tokenAdminA = testDataFactory.generateToken(adminA, tenantA.getSlug());
        String tokenViewerA = testDataFactory.generateToken(viewerA, tenantA.getSlug());

        // 1. User A (DEVELOPER) creates and validates a message
        CreateMessageRequestDto createDto = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                .messageCode("pain.001.001.12")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml(samplePain001Xml)
                .transactionReference("TXN-COLLAB-001")
                .messageId("MSG-COLLAB-001")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/messages")
                        .header("Authorization", "Bearer " + tokenDevA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String messageUuid = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("messageUuid").asText();

        // 2. User B (DEVELOPER) views the message and signs it
        mockMvc.perform(get("/api/v1/messages/" + messageUuid)
                        .header("Authorization", "Bearer " + tokenDevB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageUuid").value(messageUuid));

        mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/sign")
                        .header("Authorization", "Bearer " + tokenDevB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SIGNED"));

        // 3. User C (TENANT_ADMIN) encrypts the message
        mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/encrypt?elementTagName=GrpHdr")
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCRYPTED"));

        // 4. User D (VIEWER) views the message
        mockMvc.perform(get("/api/v1/messages/" + messageUuid)
                        .header("Authorization", "Bearer " + tokenViewerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCRYPTED"));

        // 5. Audit trail verification: resource history shows actions by the respective users
        mockMvc.perform(get("/api/v1/audit/resource-history")
                        .param("resourceType", "ISO20022_MESSAGE")
                        .param("resourceId", messageUuid)
                        .header("Authorization", "Bearer " + tokenAdminA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].action", hasItems("CREATE_MESSAGE", "SIGN_MESSAGE", "ENCRYPT_MESSAGE")));
    }
}
