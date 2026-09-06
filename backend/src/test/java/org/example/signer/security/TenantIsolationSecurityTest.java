package org.example.signer.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.message.CreateMessageRequestDto;
import org.example.signer.dto.message.UpdateSimulatorProfileDto;
import org.example.signer.entity.Iso20022Message;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.Iso20022MessageRepository;
import org.example.signer.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TenantIsolationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private Iso20022MessageRepository messageRepository;

    private Tenant tenant1;
    private User tenant1Admin;
    private String tenant1Token;

    private Tenant tenant2;
    private User tenant2Admin;
    private String tenant2Token;

    private Iso20022Message tenant1Message;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanAll();

        // 1. Setup Tenant 1 (First Bank)
        tenant1 = testDataFactory.createTenant("First Bank", "first-bank", Tenant.SubscriptionTier.PROFESSIONAL, 10);
        tenant1Admin = testDataFactory.createUser(tenant1.getId(), "admin@firstbank.com", "first_admin", User.UserRole.TENANT_ADMIN, "Secret123!");
        tenant1Token = testDataFactory.generateToken(tenant1Admin, tenant1.getSlug());
        testDataFactory.createSimulatorProfile(tenant1.getId(), "090004", "FBNINGLAXXX");

        // 2. Setup Tenant 2 (Zenith Bank)
        tenant2 = testDataFactory.createTenant("Zenith Bank", "zenith-bank", Tenant.SubscriptionTier.PROFESSIONAL, 10);
        tenant2Admin = testDataFactory.createUser(tenant2.getId(), "admin@zenithbank.com", "zenith_admin", User.UserRole.TENANT_ADMIN, "Secret123!");
        tenant2Token = testDataFactory.generateToken(tenant2Admin, tenant2.getSlug());
        testDataFactory.createSimulatorProfile(tenant2.getId(), "999057", "ZEBNNGLAXXX");

        // 3. Create message owned by Tenant 1
        tenant1Message = testDataFactory.createIsoMessage(
                tenant1.getId(),
                Iso20022Message.MessageType.PAYMENT_INITIATION,
                "pain.001.001.03",
                Iso20022Message.MessageDirection.OUTBOUND,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document><CstmrCdtTrfInitn><GrpHdr><MsgId>FBN-001</MsgId></GrpHdr></CstmrCdtTrfInitn></Document>"
        );
    }

    @Test
    @DisplayName("SEC-01: Tenant 2 cannot access Tenant 1's message via API (404 Not Found)")
    void sec01_cannotAccessOtherTenantMessage() throws Exception {
        mockMvc.perform(get("/api/v1/messages/" + tenant1Message.getMessageUuid())
                        .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("SEC-02: Tenant 2 cannot list Tenant 1's messages")
    void sec02_cannotListOtherTenantMessages() throws Exception {
        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("SEC-03: Tenant 2 cannot sign Tenant 1's message (404 Not Found)")
    void sec03_cannotSignOtherTenantMessage() throws Exception {
        mockMvc.perform(post("/api/v1/messages/" + tenant1Message.getMessageUuid() + "/sign")
                        .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("SEC-04: Tenant 2 cannot encrypt Tenant 1's message (404 Not Found)")
    void sec04_cannotEncryptOtherTenantMessage() throws Exception {
        mockMvc.perform(post("/api/v1/messages/" + tenant1Message.getMessageUuid() + "/encrypt?elementTagName=GrpHdr")
                        .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("SEC-05: Tenant 2 cannot access or mutate Tenant 1's simulator profile")
    void sec05_cannotAccessOrMutateOtherTenantSimulatorProfile() throws Exception {
        // Tenant 2 gets own profile
        mockMvc.perform(get("/api/v1/simulator/profile")
                        .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionCode").value("999057"))
                .andExpect(jsonPath("$.bic").value("ZEBNNGLAXXX"));

        // Tenant 2 updates own profile
        UpdateSimulatorProfileDto updateDto = UpdateSimulatorProfileDto.builder()
                .institutionCode("999059")
                .institutionName("Zenith Updated")
                .bic("ZEBNUPDXXX")
                .build();

        mockMvc.perform(put("/api/v1/simulator/profile")
                        .header("Authorization", "Bearer " + tenant2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionCode").value("999059"));

        // Verify Tenant 1 profile remains untouched
        mockMvc.perform(get("/api/v1/simulator/profile")
                        .header("Authorization", "Bearer " + tenant1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionCode").value("090004"))
                .andExpect(jsonPath("$.bic").value("FBNINGLAXXX"));
    }

    @Test
    @DisplayName("SEC-06: Tenant 2 cannot list Tenant 1's users")
    void sec06_cannotListOtherTenantUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value("admin@zenithbank.com"));
    }

    @Test
    @DisplayName("SEC-07: Tenant 2 cannot view Tenant 1's audit logs")
    void sec07_cannotViewOtherTenantAuditLogs() throws Exception {
        // Create an audit log for Tenant 1
        testDataFactory.createAuditEvent(tenant1.getId(), tenant1Admin.getId(), "MESSAGE_SIGN", "ISO_MESSAGE", tenant1Message.getMessageUuid().toString());

        // Tenant 2 views audit logs -> Tenant 1 event is not present
        mockMvc.perform(get("/api/v1/audit/events")
                        .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        // Tenant 1 views audit logs -> Tenant 1 event is present
        mockMvc.perform(get("/api/v1/audit/events")
                        .header("Authorization", "Bearer " + tenant1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].action").value("MESSAGE_SIGN"));
    }

    @Test
    @DisplayName("SEC-08: JWT token manipulation - altered token is rejected with 401")
    void sec08_manipulatedJwtTokenRejected() throws Exception {
        String manipulatedToken = tenant1Token.substring(0, tenant1Token.length() - 8) + "Tampered";

        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Bearer " + manipulatedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-09: Direct database repository queries strictly enforce tenant isolation")
    void sec09_directRepositoryEnforcesIsolation() {
        Page<Iso20022Message> t1Messages = messageRepository.findByTenantIdOrderByCreatedAtDesc(tenant1.getId(), PageRequest.of(0, 10));
        Page<Iso20022Message> t2Messages = messageRepository.findByTenantIdOrderByCreatedAtDesc(tenant2.getId(), PageRequest.of(0, 10));

        assertEquals(1, t1Messages.getTotalElements());
        assertEquals(0, t2Messages.getTotalElements());
        assertTrue(messageRepository.findByTenantIdAndMessageUuid(tenant2.getId(), tenant1Message.getMessageUuid()).isEmpty());
    }

    @Test
    @DisplayName("SEC-10: SQL injection attempt in query parameter fails safely")
    void sec10_sqlInjectionFailsSafely() throws Exception {
        String sqlInjection = "' OR '1'='1' --";

        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Bearer " + tenant1Token)
                        .param("direction", sqlInjection))
                .andExpect(status().isBadRequest());
    }
}
