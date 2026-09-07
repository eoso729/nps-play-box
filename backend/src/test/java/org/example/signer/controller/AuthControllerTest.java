package org.example.signer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.AuthRequest;
import org.example.signer.dto.AuthResponse;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.TenantRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Tenant testTenant;
    private User testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        testTenant = tenantRepository.save(Tenant.builder()
                .name("Apex Bank")
                .slug("apex-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                .build());

        testUser = userRepository.save(User.builder()
                .tenantId(testTenant.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@apex-bank.com")
                .username("apexadmin")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .firstName("Apex")
                .lastName("Admin")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());
    }

    @Test
    @DisplayName("Should authenticate valid user with tenant slug")
    void shouldAuthenticateValidUser() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .email("admin@apex-bank.com")
                .password("Password123!")
                .tenantSlug("apex-bank")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("admin@apex-bank.com"))
                .andExpect(jsonPath("$.user.role").value("TENANT_ADMIN"))
                .andExpect(jsonPath("$.user.tenant.slug").value("apex-bank"))
                .andExpect(jsonPath("$.user.tenant.name").value("Apex Bank"));
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void shouldRejectInvalidCredentials() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .email("admin@apex-bank.com")
                .password("WrongPassword")
                .tenantSlug("apex-bank")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject user when specifying non-existent tenant")
    void shouldRejectUserFromDifferentTenant() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .email("admin@apex-bank.com")
                .password("Password123!")
                .tenantSlug("different-tenant")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject inactive user account")
    void shouldRejectInactiveUser() throws Exception {
        testUser.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(testUser);

        AuthRequest request = AuthRequest.builder()
                .email("admin@apex-bank.com")
                .password("Password123!")
                .tenantSlug("apex-bank")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject user from suspended tenant")
    void shouldRejectSuspendedTenant() throws Exception {
        testTenant.setStatus(Tenant.TenantStatus.SUSPENDED);
        tenantRepository.save(testTenant);

        AuthRequest request = AuthRequest.builder()
                .email("admin@apex-bank.com")
                .password("Password123!")
                .tenantSlug("apex-bank")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should successfully refresh token with valid refresh token")
    void shouldRefreshToken() throws Exception {
        String refreshToken = jwtService.generateRefreshToken(testUser, testTenant.getSlug());

        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        AuthResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        assertNotNull(response.getToken());
        assertTrue(jwtService.validateToken(response.getToken()));
        assertEquals(testTenant.getId(), jwtService.extractTenantId(response.getToken()));
    }

    @Test
    @DisplayName("Should reject refresh request when using access token instead of refresh token")
    void shouldRejectAccessTokenOnRefresh() throws Exception {
        String accessToken = jwtService.generateToken(testUser, testTenant.getSlug());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fetch current user details via /me with valid JWT")
    void shouldGetCurrentUser() throws Exception {
        String accessToken = jwtService.generateToken(testUser, testTenant.getSlug());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@apex-bank.com"))
                .andExpect(jsonPath("$.role").value("TENANT_ADMIN"))
                .andExpect(jsonPath("$.tenant.slug").value("apex-bank"));
    }

    @Test
    @DisplayName("Should support legacy /api/auth/login route")
    void shouldSupportLegacyLogin() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .email("admin@apex-bank.com")
                .password("Password123!")
                .tenantSlug("apex-bank")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Should authenticate valid user when tenantSlug is omitted by auto-resolving tenant from email")
    void shouldAuthenticateUserWithoutTenantSlug() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .email("admin@apex-bank.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("admin@apex-bank.com"))
                .andExpect(jsonPath("$.user.tenant.slug").value("apex-bank"))
                .andExpect(jsonPath("$.user.role").value("TENANT_ADMIN"));
    }
}
