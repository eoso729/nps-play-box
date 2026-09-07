package org.example.signer.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.auth.RegisterRequestDto;
import org.example.signer.dto.message.CreateMessageRequestDto;
import org.example.signer.dto.message.UpdateSimulatorProfileDto;
import org.example.signer.dto.user.InviteUserRequest;
import org.example.signer.entity.Iso20022Message;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationAuthorizationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    private Tenant tenant;
    private User tenantAdmin;
    private User developer;
    private User viewer;

    private String adminToken;
    private String devToken;
    private String viewerToken;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanAll();

        tenant = testDataFactory.createTenant("Security Bank", "sec-bank", Tenant.SubscriptionTier.PROFESSIONAL, 10);

        tenantAdmin = testDataFactory.createUser(tenant.getId(), "admin@secbank.com", "sec_admin", User.UserRole.TENANT_ADMIN, "AdminPass123!");
        developer = testDataFactory.createUser(tenant.getId(), "dev@secbank.com", "sec_dev", User.UserRole.DEVELOPER, "DevPass123!");
        viewer = testDataFactory.createUser(tenant.getId(), "viewer@secbank.com", "sec_viewer", User.UserRole.VIEWER, "ViewerPass123!");

        adminToken = testDataFactory.generateToken(tenantAdmin, tenant.getSlug());
        devToken = testDataFactory.generateToken(developer, tenant.getSlug());
        viewerToken = testDataFactory.generateToken(viewer, tenant.getSlug());
    }

    @Test
    @DisplayName("SEC-AUTH-01: Unauthenticated request returns 401 Unauthorized")
    void secAuth01_unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/messages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-AUTH-02: Invalid/garbage token returns 401 Unauthorized")
    void secAuth02_invalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Bearer not-a-valid-jwt-token-at-all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-AUTH-03: Malformed authorization header returns 401 Unauthorized")
    void secAuth03_malformedAuthHeaderReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEC-AUTH-04: VIEWER cannot invite users (403 Forbidden)")
    void secAuth04_viewerCannotInviteUsers() throws Exception {
        InviteUserRequest invite = new InviteUserRequest();
        invite.setEmail("newuser@secbank.com");
        invite.setRole("DEVELOPER");

        mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-AUTH-05: DEVELOPER cannot invite users (403 Forbidden)")
    void secAuth05_developerCannotInviteUsers() throws Exception {
        InviteUserRequest invite = new InviteUserRequest();
        invite.setEmail("newuser@secbank.com");
        invite.setRole("DEVELOPER");

        mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + devToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invite)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-AUTH-06: TENANT_ADMIN cannot access platform admin endpoints (403 Forbidden)")
    void secAuth06_tenantAdminCannotAccessPlatformAdminEndpoints() throws Exception {
        // Platform admin only: GET /api/v1/tenants
        mockMvc.perform(get("/api/v1/tenants")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-AUTH-07: Registration requires valid payload and rejects blanks (400 Bad Request)")
    void secAuth07_registrationValidatesPayload() throws Exception {
        RegisterRequestDto badRequest = new RegisterRequestDto();
        badRequest.setEmail(""); // blank email
        badRequest.setPassword(""); // blank password

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SEC-AUTH-08: VIEWER cannot create messages (403 Forbidden)")
    void secAuth08_viewerCannotCreateMessage() throws Exception {
        CreateMessageRequestDto dto = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                .messageCode("pain.001.001.12")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml("<Document>test</Document>")
                .transactionReference("TXN-SEC-01")
                .messageId("MSG-SEC-01")
                .build();

        mockMvc.perform(post("/api/v1/messages")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-AUTH-09: VIEWER cannot sign messages (403 Forbidden)")
    void secAuth09_viewerCannotSignMessage() throws Exception {
        UUID fakeUuid = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/messages/" + fakeUuid + "/sign")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-AUTH-10: VIEWER cannot generate XML messages (403 Forbidden)")
    void secAuth10_viewerCannotGenerateXml() throws Exception {
        mockMvc.perform(post("/api/generate/payment-initiation-pain001")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-AUTH-11: VIEWER cannot run orchestrator flows (403 Forbidden)")
    void secAuth11_viewerCannotRunFlow() throws Exception {
        mockMvc.perform(post("/api/orchestrator/run-flow")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flowId\":\"direct-debit\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-AUTH-12: VIEWER can read messages (200 OK)")
    void secAuth12_viewerCanReadMessages() throws Exception {
        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-AUTH-13: DEVELOPER can create messages (201 Created)")
    void secAuth13_developerCanCreateMessage() throws Exception {
        CreateMessageRequestDto dto = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                .messageCode("pain.001.001.12")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml("<Document>test</Document>")
                .transactionReference("TXN-DEV-01")
                .messageId("MSG-DEV-01")
                .build();

        mockMvc.perform(post("/api/v1/messages")
                        .header("Authorization", "Bearer " + devToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageUuid").isNotEmpty());
    }

    @Test
    @DisplayName("SEC-AUTH-14: DEVELOPER cannot update tenant simulator profile (403 Forbidden)")
    void secAuth14_developerCannotUpdateSimulatorProfile() throws Exception {
        UpdateSimulatorProfileDto dto = UpdateSimulatorProfileDto.builder()
                .institutionCode("999")
                .institutionName("Test Bank")
                .bic("TESTNGLA")
                .build();

        mockMvc.perform(put("/api/v1/simulator/profile")
                        .header("Authorization", "Bearer " + devToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
