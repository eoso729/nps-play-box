package org.example.signer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.user.*;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.TenantRepository;
import org.example.signer.repository.UserInvitationRepository;
import org.example.signer.repository.UserRepository;
import org.example.signer.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInvitationRepository invitationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Tenant testTenant;
    private User tenantAdminUser;
    private String tenantAdminToken;

    private User developerUser;
    private String developerToken;

    private User otherUser;

    @BeforeEach
    void setup() {
        invitationRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
        invitationRepository.flush();
        userRepository.flush();
        tenantRepository.flush();

        testTenant = tenantRepository.save(Tenant.builder()
                .name("Acme Bank")
                .slug("acme-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                .build());

        tenantAdminUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@acmebank.com")
                .username("acmeadmin")
                .passwordHash(passwordEncoder.encode("AdminPass123!"))
                .firstName("Acme")
                .lastName("Admin")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        tenantAdminToken = jwtService.generateToken(tenantAdminUser, testTenant.getSlug());

        developerUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("dev@acmebank.com")
                .username("acmedev")
                .passwordHash(passwordEncoder.encode("DevPass123!"))
                .firstName("Dev")
                .lastName("User")
                .role(User.UserRole.DEVELOPER)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        developerToken = jwtService.generateToken(developerUser, testTenant.getSlug());

        otherUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("other@acmebank.com")
                .username("otheruser")
                .passwordHash(passwordEncoder.encode("OtherPass123!"))
                .firstName("Other")
                .lastName("User")
                .role(User.UserRole.VIEWER)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());
    }

    @Test
    @DisplayName("Tenant admin can invite a user to their tenant")
    void tenantAdminShouldInviteUser() throws Exception {
        InviteUserRequest request = InviteUserRequest.builder()
                .email("newhire@acmebank.com")
                .role("DEVELOPER")
                .firstName("John")
                .lastName("Smith")
                .customMessage("Welcome aboard!")
                .build();

        mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + tenantAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newhire@acmebank.com"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"))
                .andExpect(jsonPath("$.invitationToken").isNotEmpty())
                .andExpect(jsonPath("$.expired").value(false));
    }

    @Test
    @DisplayName("Public user can accept invitation without authentication")
    void publicShouldAcceptInvitationWithoutAuth() throws Exception {
        // Invite a user first
        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email("acceptme@acmebank.com")
                .role("DEVELOPER")
                .build();

        MvcResult inviteResult = mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + tenantAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteReq)))
                .andExpect(status().isCreated())
                .andReturn();

        InvitationResponse inv = objectMapper.readValue(
                inviteResult.getResponse().getContentAsString(), InvitationResponse.class);

        // Accept invitation publicly
        AcceptInvitationRequest acceptReq = AcceptInvitationRequest.builder()
                .token(inv.getInvitationToken())
                .firstName("Accept")
                .lastName("User")
                .password("StrongPass123!")
                .build();

        mockMvc.perform(post("/api/v1/users/accept-invitation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("acceptme@acmebank.com"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.tenant.slug").value("acme-bank"));
    }

    @Test
    @DisplayName("Reject accept invitation with invalid token")
    void shouldRejectInvalidInvitationToken() throws Exception {
        AcceptInvitationRequest acceptReq = AcceptInvitationRequest.builder()
                .token("invalid-token-12345")
                .firstName("Fake")
                .lastName("User")
                .password("StrongPass123!")
                .build();

        mockMvc.perform(post("/api/v1/users/accept-invitation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Tenant admin can list users in their organization")
    void tenantAdminShouldListUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("User can view their own profile")
    void userCanViewSelf() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + developerUser.getId())
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("dev@acmebank.com"))
                .andExpect(jsonPath("$.role").value("DEVELOPER"));
    }

    @Test
    @DisplayName("Developer cannot view another user profile (403 Forbidden)")
    void developerCannotViewAnotherUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + otherUser.getId())
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Developer cannot invite users (403 Forbidden)")
    void developerCannotInviteUsers() throws Exception {
        InviteUserRequest request = InviteUserRequest.builder()
                .email("test@acmebank.com")
                .role("VIEWER")
                .build();

        mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tenant admin can update user role")
    void tenantAdminCanUpdateUserRole() throws Exception {
        UpdateRoleRequest request = UpdateRoleRequest.builder()
                .role("VIEWER")
                .build();

        mockMvc.perform(patch("/api/v1/users/" + developerUser.getId() + "/role")
                        .header("Authorization", "Bearer " + tenantAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }

    @Test
    @DisplayName("Tenant admin can deactivate and reactivate user")
    void tenantAdminCanDeactivateAndReactivateUser() throws Exception {
        // Deactivate
        mockMvc.perform(patch("/api/v1/users/" + developerUser.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        // Reactivate
        mockMvc.perform(patch("/api/v1/users/" + developerUser.getId() + "/reactivate")
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Tenant admin can soft-delete user (status becomes INACTIVE)")
    void tenantAdminCanDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + developerUser.getId())
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isNoContent());

        // Verify status is now INACTIVE
        mockMvc.perform(get("/api/v1/users/" + developerUser.getId())
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("Tenant admin can fetch seat utilization metrics")
    void tenantAdminCanGetSeatUtilization() throws Exception {
        mockMvc.perform(get("/api/v1/users/seats/utilization")
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxSeats").value(10))
                .andExpect(jsonPath("$.usedSeats").value(3))
                .andExpect(jsonPath("$.availableSeats").value(7))
                .andExpect(jsonPath("$.utilizationPercentage").value(30.0));
    }

    @Test
    @DisplayName("Tenant admin can list and cancel pending invitations")
    void tenantAdminCanListAndCancelInvitations() throws Exception {
        InviteUserRequest inviteReq = InviteUserRequest.builder()
                .email("cancelme@acmebank.com")
                .role("VIEWER")
                .build();

        MvcResult inviteResult = mockMvc.perform(post("/api/v1/users/invite")
                        .header("Authorization", "Bearer " + tenantAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteReq)))
                .andExpect(status().isCreated())
                .andReturn();

        InvitationResponse inv = objectMapper.readValue(
                inviteResult.getResponse().getContentAsString(), InvitationResponse.class);

        // List invitations
        mockMvc.perform(get("/api/v1/users/invitations")
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("cancelme@acmebank.com"));

        // Cancel invitation
        mockMvc.perform(delete("/api/v1/users/invitations/" + inv.getId())
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isNoContent());

        // Verify list is empty
        mockMvc.perform(get("/api/v1/users/invitations")
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
