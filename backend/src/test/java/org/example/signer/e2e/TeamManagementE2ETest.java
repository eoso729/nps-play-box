package org.example.signer.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.AuthRequest;
import org.example.signer.dto.message.CreateMessageRequestDto;
import org.example.signer.dto.user.AcceptInvitationRequest;
import org.example.signer.dto.user.InviteUserRequest;
import org.example.signer.dto.user.UpdateRoleRequest;
import org.example.signer.entity.Iso20022Message;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.UserRepository;
import org.example.signer.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TeamManagementE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private UserRepository userRepository;

    private Tenant tenant;
    private User admin;
    private String adminToken;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanAll();

        // Create tenant with capacity for 3 seats
        tenant = testDataFactory.createTenant("Guaranty Trust Bank", "gt-bank", Tenant.SubscriptionTier.STANDARD, 3);
        admin = testDataFactory.createUser(tenant.getId(), "admin@gtbank.com", "gt_admin", User.UserRole.TENANT_ADMIN, "AdminPass123!");
        adminToken = testDataFactory.generateToken(admin, tenant.getSlug());
    }

    @Test
    @DisplayName("E2E-02: Complete User Invitation, Role Lifecycle, and Seat Quota Enforcement")
    void testUserInvitationAndLifecycle() throws Exception {
        // 1. Verify initial seat utilization: 1 seat used (admin), 2 available
        mockMvc.perform(get("/api/v1/users/seats/utilization")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxSeats").value(3))
                .andExpect(jsonPath("$.activeUsers").value(1))
                .andExpect(jsonPath("$.availableSeats").value(2));

        // 2. Admin invites a new user with DEVELOPER role
        InviteUserRequest inviteRequest = new InviteUserRequest();
        inviteRequest.setEmail("dev@gtbank.com");
        inviteRequest.setFirstName("Chidi");
        inviteRequest.setLastName("Developer");
        inviteRequest.setRole("DEVELOPER");

        MvcResult inviteResult = mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invitationToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("dev@gtbank.com"))
                .andReturn();

        String invitationToken = objectMapper.readTree(inviteResult.getResponse().getContentAsString())
                .get("invitationToken").asText();

        // 3. Verify invitation shows in pending list
        mockMvc.perform(get("/api/v1/users/invitations")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("dev@gtbank.com"));

        // 4. New user accepts invitation, sets password
        AcceptInvitationRequest acceptRequest = new AcceptInvitationRequest();
        acceptRequest.setToken(invitationToken);
        acceptRequest.setPassword("NewUserPass123!");
        acceptRequest.setFirstName("Chidi");
        acceptRequest.setLastName("Developer");

        MvcResult acceptResult = mockMvc.perform(post("/api/v1/users/accept-invitation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("dev@gtbank.com"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        Long newUserId = objectMapper.readTree(acceptResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 5. Verify seat utilization updated: 2 seats used, 1 available
        mockMvc.perform(get("/api/v1/users/seats/utilization")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeUsers").value(2))
                .andExpect(jsonPath("$.availableSeats").value(1));

        // 6. New user logs in with DEVELOPER role
        AuthRequest devLogin = AuthRequest.builder()
                .email("dev@gtbank.com")
                .password("NewUserPass123!")
                .tenantSlug("gt-bank")
                .build();

        MvcResult devLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("DEVELOPER"))
                .andReturn();

        String devToken = objectMapper.readTree(devLoginResult.getResponse().getContentAsString())
                .get("token").asText();

        // 7. DEVELOPER can create messages
        CreateMessageRequestDto msgDto = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                .messageCode("pain.001.001.12")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml("<Document><CstmrCdtTrfInitn><GrpHdr><MsgId>DEV-001</MsgId></GrpHdr></CstmrCdtTrfInitn></Document>")
                .transactionReference("TXN-DEV-001")
                .messageId("DEV-001")
                .build();

        mockMvc.perform(post("/api/v1/messages")
                        .header("Authorization", "Bearer " + devToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgDto)))
                .andExpect(status().isCreated());

        // 8. DEVELOPER cannot invite users (403 Forbidden)
        InviteUserRequest unauthorizedInvite = new InviteUserRequest();
        unauthorizedInvite.setEmail("another@gtbank.com");
        unauthorizedInvite.setFirstName("Unauthorized");
        unauthorizedInvite.setLastName("User");
        unauthorizedInvite.setRole("DEVELOPER");

        mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + devToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unauthorizedInvite)))
                .andExpect(status().isForbidden());

        // 9. Admin demotes user to VIEWER role
        UpdateRoleRequest roleRequest = new UpdateRoleRequest();
        roleRequest.setRole("VIEWER");

        mockMvc.perform(patch("/api/v1/users/" + newUserId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("VIEWER"));

        // Refresh dev user to get updated token
        User refreshedDev = userRepository.findById(newUserId).orElseThrow();
        String viewerToken = testDataFactory.generateToken(refreshedDev, tenant.getSlug());

        // 10. VIEWER can view messages
        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        // 11. Admin deactivates the user
        mockMvc.perform(patch("/api/v1/users/" + newUserId + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        // 12. Deactivated user cannot log in (403 Forbidden)
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devLogin)))
                .andExpect(status().isForbidden());

        // 13. Seat utilization reflects deactivation: 1 active user (admin), 2 available
        mockMvc.perform(get("/api/v1/users/seats/utilization")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeUsers").value(1))
                .andExpect(jsonPath("$.availableSeats").value(2));
    }

    @Test
    @DisplayName("Seat Quota Enforcement: Exceeding maxSeats throws 400 Bad Request")
    void testSeatQuotaEnforcement() throws Exception {
        // Tenant has maxSeats = 3. Admin is seat 1.
        // Invite 2 users to reach capacity (seats 2 and 3)
        InviteUserRequest user2 = new InviteUserRequest();
        user2.setEmail("user2@gtbank.com");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setRole("DEVELOPER");

        MvcResult res2 = mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated())
                .andReturn();

        String token2 = objectMapper.readTree(res2.getResponse().getContentAsString()).get("invitationToken").asText();
        AcceptInvitationRequest acc2 = new AcceptInvitationRequest();
        acc2.setToken(token2);
        acc2.setPassword("Pass1234!");
        acc2.setFirstName("User");
        acc2.setLastName("Two");
        mockMvc.perform(post("/api/v1/users/accept-invitation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acc2)))
                .andExpect(status().isOk());

        InviteUserRequest user3 = new InviteUserRequest();
        user3.setEmail("user3@gtbank.com");
        user3.setFirstName("User");
        user3.setLastName("Three");
        user3.setRole("DEVELOPER");

        MvcResult res3 = mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user3)))
                .andExpect(status().isCreated())
                .andReturn();

        String token3 = objectMapper.readTree(res3.getResponse().getContentAsString()).get("invitationToken").asText();
        AcceptInvitationRequest acc3 = new AcceptInvitationRequest();
        acc3.setToken(token3);
        acc3.setPassword("Pass1234!");
        acc3.setFirstName("User");
        acc3.setLastName("Three");
        mockMvc.perform(post("/api/v1/users/accept-invitation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acc3)))
                .andExpect(status().isOk());

        // Max capacity (3 active users) reached! Attempting to invite 4th user fails with 400
        InviteUserRequest user4 = new InviteUserRequest();
        user4.setEmail("user4@gtbank.com");
        user4.setFirstName("User");
        user4.setLastName("Four");
        user4.setRole("DEVELOPER");

        mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user4)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SEAT_QUOTA_EXCEEDED"));
    }
}
