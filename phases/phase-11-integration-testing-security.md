# Phase 11: Integration Testing & Security Validation

## Objective
Conduct comprehensive end-to-end integration testing and security validation of the complete multi-tenant NPS Play Box platform. Verify tenant isolation, validate performance under load, ensure audit trail completeness, and perform security penetration testing to certify production readiness.

**Duration**: 5-7 days  
**Dependencies**: All phases (1-10)

---

## Test Strategy Overview

### Test Pyramid

```
                    E2E Tests (20%)
                  /               \
              Integration Tests (30%)
            /                       \
        Unit Tests (50%)
```

### Test Categories

1. **Functional Testing**: Verify all features work as specified
2. **Integration Testing**: Test component interactions
3. **Security Testing**: Penetration testing and vulnerability assessment
4. **Performance Testing**: Load, stress, and scalability testing
5. **Compliance Testing**: Audit trail and regulatory requirements
6. **User Acceptance Testing**: Real-world scenario validation

---

## Test Environment Setup

### 11.1 Test Environment Configuration

```yaml
# docker-compose.test.yml
version: '3.8'

services:
  postgres-test:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: nps_test
      POSTGRES_USER: test_user
      POSTGRES_PASSWORD: test_password
    ports:
      - "5433:5432"
    volumes:
      - ./test-data:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U test_user"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend-test:
    build:
      context: ./backend
      dockerfile: Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: test
      DATABASE_URL: jdbc:postgresql://postgres-test:5432/nps_test
      DATABASE_USERNAME: test_user
      DATABASE_PASSWORD: test_password
      JWT_SECRET: test-secret-key-minimum-256-bits-long
    ports:
      - "8081:8080"
    depends_on:
      postgres-test:
        condition: service_healthy

  frontend-test:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    environment:
      VITE_API_BASE_URL: http://backend-test:8080
    ports:
      - "3001:80"
    depends_on:
      - backend-test

  selenium-hub:
    image: selenium/hub:4.15.0
    ports:
      - "4444:4444"

  selenium-chrome:
    image: selenium/node-chrome:4.15.0
    environment:
      SE_EVENT_BUS_HOST: selenium-hub
      SE_EVENT_BUS_PUBLISH_PORT: 4442
      SE_EVENT_BUS_SUBSCRIBE_PORT: 4443
    depends_on:
      - selenium-hub

  selenium-firefox:
    image: selenium/node-firefox:4.15.0
    environment:
      SE_EVENT_BUS_HOST: selenium-hub
      SE_EVENT_BUS_PUBLISH_PORT: 4442
      SE_EVENT_BUS_SUBSCRIBE_PORT: 4443
    depends_on:
      - selenium-hub
```

### 11.2 Test Data Generation Script

```java
package org.example.signer.testdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.signer.model.*;
import org.example.signer.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestDataGenerator implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final Iso20022MessageRepository messageRepository;
    private final TenantSimulatorProfileRepository profileRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final Random random = new Random();

    @Override
    public void run(String... args) {
        log.info("Generating test data...");
        
        List<Tenant> tenants = generateTenants(5);
        
        for (int i = 0; i < tenants.size(); i++) {
            Tenant tenant = tenants.get(i);
            List<User> users = generateUsersForTenant(tenant, 10);
            generateMessagesForTenant(tenant, users, 100);
            generateSimulatorProfileForTenant(tenant, i);
            generateAuditLogsForTenant(tenant, users);
        }
        
        log.info("Test data generation completed");
    }
    
    private List<Tenant> generateTenants(int count) {
        List<Tenant> tenants = new ArrayList<>();
        
        String[] bankNames = {
            "First Bank of Nigeria",
            "Access Bank",
            "GTBank",
            "Zenith Bank",
            "UBA"
        };
        
        String[] tiers = {"TRIAL", "STANDARD", "PROFESSIONAL", "ENTERPRISE"};
        
        for (int i = 0; i < count; i++) {
            Tenant tenant = Tenant.builder()
                .name(bankNames[i])
                .slug(bankNames[i].toLowerCase().replace(" ", "-"))
                .status("ACTIVE")
                .maxSeats(random.nextInt(50) + 10)
                .subscriptionTier(tiers[random.nextInt(tiers.length)])
                .build();
            
            tenants.add(tenantRepository.save(tenant));
            log.info("Created tenant: {}", tenant.getName());
        }
        
        return tenants;
    }
    
    private List<User> generateUsersForTenant(Tenant tenant, int count) {
        List<User> users = new ArrayList<>();
        
        String[] roles = {"TENANT_ADMIN", "DEVELOPER", "DEVELOPER", "VIEWER", "VIEWER"};
        String[] firstNames = {"John", "Jane", "Michael", "Sarah", "David", "Emily", "Robert", "Lisa"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        
        for (int i = 0; i < count; i++) {
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                          i + "@" + tenant.getSlug() + ".test";
            
            User user = User.builder()
                .tenantId(tenant.getId())
                .email(email)
                .passwordHash(passwordEncoder.encode("Password123!"))
                .firstName(firstName)
                .lastName(lastName)
                .role(roles[i % roles.length])
                .status("ACTIVE")
                .build();
            
            users.add(userRepository.save(user));
        }
        
        log.info("Created {} users for tenant {}", users.size(), tenant.getName());
        return users;
    }
    
    private void generateMessagesForTenant(Tenant tenant, List<User> users, int count) {
        Iso20022Message.MessageType[] types = Iso20022Message.MessageType.values();
        Iso20022Message.MessageStatus[] statuses = Iso20022Message.MessageStatus.values();
        
        for (int i = 0; i < count; i++) {
            User creator = users.get(random.nextInt(users.size()));
            
            Iso20022Message message = Iso20022Message.builder()
                .tenantId(tenant.getId())
                .messageType(types[random.nextInt(types.length)])
                .messageCode("pacs.008.001.08")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml("<Document>Sample XML " + UUID.randomUUID() + "</Document>")
                .status(statuses[random.nextInt(statuses.length)])
                .creator(creator)
                .transactionReference("TXN-" + System.currentTimeMillis() + "-" + i)
                .endToEndId("E2E-" + UUID.randomUUID())
                .messageId("MSG-" + UUID.randomUUID())
                .build();
            
            messageRepository.save(message);
        }
        
        log.info("Created {} messages for tenant {}", count, tenant.getName());
    }
    
    private void generateSimulatorProfileForTenant(Tenant tenant, int index) {
        String[] instCodes = {"090004", "999057", "999058", "090005", "090006"};
        String code = instCodes[index % instCodes.length];

        TenantSimulatorProfile profile = TenantSimulatorProfile.builder()
            .tenantId(tenant.getId())
            .institutionCode(code)
            .institutionName(tenant.getName() + " (Simulator)")
            .bic(tenant.getSlug().toUpperCase().replace("-", "") + "NGLAXXX")
            .schemeCode(code)
            .defaultCurrency("NGN")
            .defaultAccountNumber("10" + String.format("%08d", index + 1))
            .defaultAccountName(tenant.getName() + " Settlement Acct")
            .defaultBvn("22" + String.format("%09d", index + 1))
            .autoRespondInbound(true)
            .build();

        profileRepository.save(profile);
        log.info("Created simulator profile for tenant {} with instCode {}", tenant.getName(), code);
    }
    
    private void generateAuditLogsForTenant(Tenant tenant, List<User> users) {
        String[] actions = {
            "USER_LOGIN", "USER_LOGOUT", "MESSAGE_CREATED", "MESSAGE_VALIDATED",
            "MESSAGE_SIGNED", "MESSAGE_SENT", "SIMULATOR_PROFILE_UPDATED", "USER_CREATED"
        };
        
        for (int i = 0; i < 50; i++) {
            User user = users.get(random.nextInt(users.size()));
            
            AuditLog log = AuditLog.builder()
                .tenantId(tenant.getId())
                .userId(user.getId())
                .action(actions[random.nextInt(actions.length)])
                .resourceType("Message")
                .resourceId(UUID.randomUUID().toString())
                .ipAddress("192.168.1." + (random.nextInt(255) + 1))
                .userAgent("TestClient/1.0")
                .status("SUCCESS")
                .build();
            
            auditLogRepository.save(log);
        }
        
        log.info("Created audit logs for tenant {}", tenant.getName());
    }
}
```

---

## End-to-End Test Scenarios

### 11.3 E2E Test: Complete Message Workflow

```java
package org.example.signer.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.example.signer.dto.auth.LoginRequest;
import org.example.signer.dto.auth.LoginResponse;
import org.example.signer.dto.Iso20022MessageDto;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CompleteMessageWorkflowE2ETest {

    @LocalServerPort
    private int port;
    
    private static String tenant1Token;
    private static String tenant2Token;
    private static String messageUuid;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @Order(1)
    @DisplayName("E2E-01: Tenant 1 user logs in successfully")
    void tenant1UserLogin() {
        LoginRequest request = new LoginRequest(
            "john.smith@first-bank.test",
            "Password123!"
        );
        
        LoginResponse response = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("user.email", equalTo(request.getEmail()))
            .body("user.tenantSlug", equalTo("first-bank"))
        .extract()
            .as(LoginResponse.class);
        
        tenant1Token = response.getToken();
    }

    @Test
    @Order(2)
    @DisplayName("E2E-02: Tenant 2 user logs in successfully")
    void tenant2UserLogin() {
        LoginRequest request = new LoginRequest(
            "jane.johnson@access-bank.test",
            "Password123!"
        );
        
        LoginResponse response = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("user.tenantSlug", equalTo("access-bank"))
        .extract()
            .as(LoginResponse.class);
        
        tenant2Token = response.getToken();
    }

    @Test
    @Order(3)
    @DisplayName("E2E-03: Tenant 1 creates a payment initiation message")
    void tenant1CreatesMessage() {
        Iso20022MessageDto request = Iso20022MessageDto.builder()
            .messageType("PAYMENT_INITIATION")
            .messageCode("pacs.008.001.08")
            .direction("OUTBOUND")
            .rawXml("<Document><FIToFICstmrCdtTrf>...</FIToFICstmrCdtTrf></Document>")
            .transactionReference("TXN-E2E-001")
            .endToEndId("E2E-E2E-001")
            .build();
        
        String uuid = given()
            .header("Authorization", "Bearer " + tenant1Token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/messages")
        .then()
            .statusCode(200)
            .body("messageUuid", notNullValue())
            .body("messageType", equalTo("PAYMENT_INITIATION"))
            .body("status", equalTo("DRAFT"))
            .body("transactionReference", equalTo("TXN-E2E-001"))
        .extract()
            .path("messageUuid");
        
        messageUuid = uuid;
    }

    @Test
    @Order(4)
    @DisplayName("E2E-04: Tenant 1 validates the message")
    void tenant1ValidatesMessage() {
        given()
            .header("Authorization", "Bearer " + tenant1Token)
        .when()
            .post("/api/v1/validation/validate/{messageUuid}", messageUuid)
        .then()
            .statusCode(200)
            .body("isValid", equalTo(true))
            .body("errorCount", equalTo(0));
        
        // Verify message status updated
        given()
            .header("Authorization", "Bearer " + tenant1Token)
        .when()
            .get("/api/v1/messages/{messageUuid}", messageUuid)
        .then()
            .statusCode(200)
            .body("status", equalTo("VALIDATED"));
    }

    @Test
    @Order(5)
    @DisplayName("E2E-05: Tenant 1 signs the message")
    void tenant1SignsMessage() {
        given()
            .header("Authorization", "Bearer " + tenant1Token)
        .when()
            .post("/api/v1/messages/{messageUuid}/sign", messageUuid)
        .then()
            .statusCode(200)
            .body("status", equalTo("SIGNED"))
            .body("signedXml", notNullValue());
    }

    @Test
    @Order(6)
    @DisplayName("E2E-06: Tenant 1 encrypts the message")
    void tenant1EncryptsMessage() {
        given()
            .header("Authorization", "Bearer " + tenant1Token)
        .when()
            .post("/api/v1/messages/{messageUuid}/encrypt", messageUuid)
        .then()
            .statusCode(200)
            .body("status", equalTo("ENCRYPTED"))
            .body("encryptedXml", notNullValue());
    }

    @Test
    @Order(7)
    @DisplayName("E2E-07: Tenant 1 sends the message")
    void tenant1SendsMessage() {
        given()
            .header("Authorization", "Bearer " + tenant1Token)
        .when()
            .post("/api/v1/messages/{messageUuid}/send", messageUuid)
        .then()
            .statusCode(200)
            .body("status", equalTo("SENT"))
            .body("sentAt", notNullValue());
    }

    @Test
    @Order(8)
    @DisplayName("E2E-08: Tenant 2 CANNOT access Tenant 1's message")
    void tenant2CannotAccessTenant1Message() {
        given()
            .header("Authorization", "Bearer " + tenant2Token)
        .when()
            .get("/api/v1/messages/{messageUuid}", messageUuid)
        .then()
            .statusCode(404); // Not 403, to avoid information leakage
    }

    @Test
    @Order(9)
    @DisplayName("E2E-09: Tenant 1 retrieves audit logs for the workflow")
    void tenant1RetrievesAuditLogs() {
        given()
            .header("Authorization", "Bearer " + tenant1Token)
            .queryParam("resourceId", messageUuid)
        .when()
            .get("/api/v1/audit-logs")
        .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)))
            .body("content[0].action", isOneOf(
                "MESSAGE_CREATED", "MESSAGE_VALIDATED", 
                "MESSAGE_SIGNED", "MESSAGE_ENCRYPTED", "MESSAGE_SENT"
            ))
            .body("content[0].userId", notNullValue())
            .body("content[0].ipAddress", notNullValue());
    }

    @Test
    @Order(10)
    @DisplayName("E2E-10: Tenant 1 views message statistics")
    void tenant1ViewsStatistics() {
        given()
            .header("Authorization", "Bearer " + tenant1Token)
        .when()
            .get("/api/v1/messages/statistics")
        .then()
            .statusCode(200)
            .body("totalMessages", greaterThan(0))
            .body("statusDistribution.SENT", greaterThan(0))
            .body("typeDistribution.PAYMENT_INITIATION", greaterThan(0));
    }
}
```

### 11.4 E2E Test: Multi-User Team Management

```java
package org.example.signer.e2e;

import io.restassured.RestAssured;
import org.example.signer.dto.InviteUserRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TeamManagementE2ETest {

    @LocalServerPort
    private int port;
    
    private static String adminToken;
    private static String invitationToken;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @Test
    @Order(1)
    @DisplayName("E2E-TM-01: Tenant admin invites a new developer")
    void tenantAdminInvitesUser() {
        // Login as tenant admin
        adminToken = loginAs("admin@first-bank.test", "Password123!");
        
        InviteUserRequest request = new InviteUserRequest(
            "newdev@first-bank.test",
            "DEVELOPER"
        );
        
        String token = given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
            .body(request)
        .when()
            .post("/api/v1/users/invite")
        .then()
            .statusCode(200)
            .body("email", equalTo(request.getEmail()))
            .body("role", equalTo("DEVELOPER"))
            .body("invitationToken", notNullValue())
        .extract()
            .path("invitationToken");
        
        invitationToken = token;
    }

    @Test
    @Order(2)
    @DisplayName("E2E-TM-02: New user accepts invitation and sets password")
    void newUserAcceptsInvitation() {
        given()
            .contentType("application/json")
            .body(Map.of(
                "invitationToken", invitationToken,
                "password", "NewPassword123!",
                "firstName", "New",
                "lastName", "Developer"
            ))
        .when()
            .post("/api/v1/auth/accept-invitation")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("user.email", equalTo("newdev@first-bank.test"))
            .body("user.role", equalTo("DEVELOPER"));
    }

    @Test
    @Order(3)
    @DisplayName("E2E-TM-03: Admin checks seat utilization")
    void adminChecksSeatUtilization() {
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/api/v1/tenants/current")
        .then()
            .statusCode(200)
            .body("currentSeats", greaterThan(0))
            .body("maxSeats", greaterThan(0))
            .body("seatUtilizationPercentage", lessThanOrEqualTo(100));
    }

    @Test
    @Order(4)
    @DisplayName("E2E-TM-04: Admin cannot exceed seat quota")
    void adminCannotExceedSeatQuota() {
        // First, fill all seats
        // Then attempt one more invitation
        
        InviteUserRequest request = new InviteUserRequest(
            "overflow@first-bank.test",
            "VIEWER"
        );
        
        given()
            .header("Authorization", "Bearer " + adminToken)
            .contentType("application/json")
            .body(request)
        .when()
            .post("/api/v1/users/invite")
        .then()
            .statusCode(422)
            .body("error", containsString("seat quota"))
            .body("currentSeats", notNullValue())
            .body("maxSeats", notNullValue());
    }

    @Test
    @Order(5)
    @DisplayName("E2E-TM-05: Admin deactivates a user")
    void adminDeactivatesUser() {
        String userUuid = "..."; // UUID of user to deactivate
        
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .patch("/api/v1/users/{userUuid}/deactivate", userUuid)
        .then()
            .statusCode(200)
            .body("status", equalTo("INACTIVE"));
        
        // Verify seat count decreased
        given()
            .header("Authorization", "Bearer " + adminToken)
        .when()
            .get("/api/v1/tenants/current")
        .then()
            .body("currentSeats", lessThan(previousSeatCount));
    }

    private String loginAs(String email, String password) {
        return given()
            .contentType("application/json")
            .body(Map.of("email", email, "password", password))
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
        .extract()
            .path("token");
    }
}
```

---

## Cross-Tenant Isolation Tests (Penetration Testing)

### 11.5 Security Test: Tenant Isolation

```java
package org.example.signer.security;

import io.restassured.RestAssured;
import org.example.signer.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TenantIsolationSecurityTest {

    @LocalServerPort
    private int port;
    
    @Autowired
    private Iso20022MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TenantSimulatorProfileRepository profileRepository;
    
    private String tenant1Token;
    private String tenant2Token;
    private UUID tenant1MessageUuid;
    private UUID tenant2MessageUuid;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        
        // Setup test data
        tenant1Token = loginAs("user1@tenant1.test", "Password123!");
        tenant2Token = loginAs("user2@tenant2.test", "Password123!");
        
        // Create resources for both tenants
        tenant1MessageUuid = createMessageAs(tenant1Token);
        tenant2MessageUuid = createMessageAs(tenant2Token);
    }

    @Test
    @DisplayName("SEC-01: Tenant 2 cannot access Tenant 1's message via API")
    void cannotAccessOtherTenantMessageViaApi() {
        given()
            .header("Authorization", "Bearer " + tenant2Token)
        .when()
            .get("/api/v1/messages/{uuid}", tenant1MessageUuid)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("SEC-02: Tenant 2 cannot list Tenant 1's messages")
    void cannotListOtherTenantMessages() {
        given()
            .header("Authorization", "Bearer " + tenant2Token)
        .when()
            .get("/api/v1/messages")
        .then()
            .statusCode(200)
            .body("content", everyItem(
                not(hasEntry("messageUuid", tenant1MessageUuid.toString()))
            ));
    }

    @Test
    @DisplayName("SEC-03: Tenant 2 cannot modify Tenant 1's message")
    void cannotModifyOtherTenantMessage() {
        given()
            .header("Authorization", "Bearer " + tenant2Token)
            .contentType("application/json")
            .body(Map.of("status", "VALIDATED"))
        .when()
            .patch("/api/v1/messages/{uuid}/status", tenant1MessageUuid)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("SEC-04: Tenant 2 cannot sign Tenant 1's message")
    void cannotSignOtherTenantMessage() {
        given()
            .header("Authorization", "Bearer " + tenant2Token)
        .when()
            .post("/api/v1/messages/{uuid}/sign", tenant1MessageUuid)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("SEC-05: Tenant 2 cannot access or mutate Tenant 1's simulator profile")
    void cannotAccessOtherTenantSimulatorProfile() {
        given()
            .header("Authorization", "Bearer " + tenant2Token)
        .when()
            .get("/api/v1/simulator/profile")
        .then()
            .statusCode(200)
            .body("institutionName", not(containsString("Tenant 1")));
    }

    @Test
    @DisplayName("SEC-06: Tenant 2 cannot list Tenant 1's users")
    void cannotListOtherTenantUsers() {
        given()
            .header("Authorization", "Bearer " + tenant2Token)
        .when()
            .get("/api/v1/users")
        .then()
            .statusCode(200)
            .body("content", everyItem(
                hasEntry("tenantSlug", "tenant2")
            ));
    }

    @Test
    @DisplayName("SEC-07: Tenant 2 cannot view Tenant 1's audit logs")
    void cannotViewOtherTenantAuditLogs() {
        given()
            .header("Authorization", "Bearer " + tenant2Token)
        .when()
            .get("/api/v1/audit-logs")
        .then()
            .statusCode(200)
            .body("content", everyItem(
                not(hasEntry("resourceId", tenant1MessageUuid.toString()))
            ));
    }

    @Test
    @DisplayName("SEC-08: JWT token manipulation - altered tenant_id rejected")
    void jwtTokenManipulation_AlteredTenantId_Rejected() {
        // Attempt to manipulate JWT token to access another tenant
        String manipulatedToken = tenant1Token.substring(0, tenant1Token.length() - 10) + "XXXXXXXXXX";
        
        given()
            .header("Authorization", "Bearer " + manipulatedToken)
        .when()
            .get("/api/v1/messages")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("SEC-09: Direct database query respects tenant isolation")
    void directDatabaseQuery_RespectsTenantIsolation() {
        // This tests the repository layer directly
        Long tenant1Id = 1L;
        Long tenant2Id = 2L;
        
        // Tenant 1 cannot see Tenant 2's messages
        var tenant1Messages = messageRepository.findByTenantId(tenant1Id, Pageable.unpaged());
        var tenant2Messages = messageRepository.findByTenantId(tenant2Id, Pageable.unpaged());
        
        assertThat(tenant1Messages.getContent())
            .extracting(Iso20022Message::getTenantId)
            .containsOnly(tenant1Id);
        
        assertThat(tenant2Messages.getContent())
            .extracting(Iso20022Message::getTenantId)
            .containsOnly(tenant2Id);
    }

    @Test
    @DisplayName("SEC-10: SQL injection attempt in tenant context fails safely")
    void sqlInjectionAttempt_FailsSafely() {
        String maliciousInput = "' OR '1'='1' --";
        
        given()
            .header("Authorization", "Bearer " + tenant1Token)
            .queryParam("search", maliciousInput)
        .when()
            .get("/api/v1/messages")
        .then()
            .statusCode(anyOf(is(200), is(400)))
            .body("content", everyItem(
                hasEntry("tenantId", 1)
            ));
    }

    private String loginAs(String email, String password) {
        return given()
            .contentType("application/json")
            .body(Map.of("email", email, "password", password))
        .when()
            .post("/api/v1/auth/login")
        .extract()
            .path("token");
    }

    private UUID createMessageAs(String token) {
        return UUID.fromString(
            given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of(
                    "messageType", "PAYMENT_INITIATION",
                    "messageCode", "pacs.008.001.08",
                    "direction", "OUTBOUND",
                    "rawXml", "<Document>Test</Document>"
                ))
            .when()
                .post("/api/v1/messages")
            .extract()
                .path("messageUuid")
        );
    }
}
```

### 11.6 Security Test: Authentication & Authorization

```java
package org.example.signer.security;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationAuthorizationSecurityTest {

    @Test
    @DisplayName("SEC-AUTH-01: Unauthenticated request returns 401")
    void unauthenticatedRequest_Returns401() {
        given()
        .when()
            .get("/api/v1/messages")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("SEC-AUTH-02: Invalid token returns 401")
    void invalidToken_Returns401() {
        given()
            .header("Authorization", "Bearer invalid-token")
        .when()
            .get("/api/v1/messages")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("SEC-AUTH-03: Expired token returns 401")
    void expiredToken_Returns401() {
        String expiredToken = generateExpiredToken();
        
        given()
            .header("Authorization", "Bearer " + expiredToken)
        .when()
            .get("/api/v1/messages")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("SEC-AUTH-04: VIEWER cannot create messages")
    void viewerCannotCreateMessages() {
        String viewerToken = loginAs("viewer@tenant1.test", "Password123!");
        
        given()
            .header("Authorization", "Bearer " + viewerToken)
            .contentType("application/json")
            .body(Map.of(
                "messageType", "PAYMENT_INITIATION",
                "messageCode", "pacs.008.001.08",
                "direction", "OUTBOUND",
                "rawXml", "<Document>Test</Document>"
            ))
        .when()
            .post("/api/v1/messages")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("SEC-AUTH-05: DEVELOPER cannot invite users")
    void developerCannotInviteUsers() {
        String developerToken = loginAs("developer@tenant1.test", "Password123!");
        
        given()
            .header("Authorization", "Bearer " + developerToken)
            .contentType("application/json")
            .body(Map.of(
                "email", "newuser@tenant1.test",
                "role", "VIEWER"
            ))
        .when()
            .post("/api/v1/users/invite")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("SEC-AUTH-06: TENANT_ADMIN cannot access platform admin endpoints")
    void tenantAdminCannotAccessPlatformAdminEndpoints() {
        String tenantAdminToken = loginAs("admin@tenant1.test", "Password123!");
        
        given()
            .header("Authorization", "Bearer " + tenantAdminToken)
        .when()
            .get("/api/v1/platform/tenants")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("SEC-AUTH-07: Password must meet complexity requirements")
    void passwordComplexityEnforced() {
        given()
            .contentType("application/json")
            .body(Map.of(
                "email", "newuser@tenant1.test",
                "password", "weak",
                "firstName", "Test",
                "lastName", "User"
            ))
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(400)
            .body("errors", hasItem(containsString("password")));
    }

    @Test
    @DisplayName("SEC-AUTH-08: Account locked after 5 failed login attempts")
    void accountLockAfterFailedAttempts() {
        String email = "locktest@tenant1.test";
        
        // Attempt 5 failed logins
        for (int i = 0; i < 5; i++) {
            given()
                .contentType("application/json")
                .body(Map.of("email", email, "password", "wrongpassword"))
            .when()
                .post("/api/v1/auth/login")
            .then()
                .statusCode(401);
        }
        
        // 6th attempt should indicate account locked
        given()
            .contentType("application/json")
            .body(Map.of("email", email, "password", "wrongpassword"))
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(423)
            .body("error", containsString("account locked"));
    }

    private String loginAs(String email, String password) {
        return given()
            .contentType("application/json")
            .body(Map.of("email", email, "password", password))
        .when()
            .post("/api/v1/auth/login")
        .extract()
            .path("token");
    }

    private String generateExpiredToken() {
        // Generate a JWT token with past expiration date
        // Implementation depends on your JWT library
        return "expired.jwt.token";
    }
}
```

---

## Performance Testing

### 11.7 Performance Test: Load Testing (100 Concurrent Users)

```java
package org.example.signer.performance;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class LoadPerformanceTest {

    private static StandardJMeterEngine jmeter;

    @BeforeAll
    static void setupJMeter() {
        File jmeterHome = new File("/usr/local/apache-jmeter");
        File jmeterProperties = new File(jmeterHome, "bin/jmeter.properties");
        
        JMeterUtils.setJMeterHome(jmeterHome.getAbsolutePath());
        JMeterUtils.loadJMeterProperties(jmeterProperties.getAbsolutePath());
        JMeterUtils.initLocale();
        
        jmeter = new StandardJMeterEngine();
    }

    @Test
    @DisplayName("PERF-01: 100 concurrent users - Message list API < 500ms p95")
    void loadTest_100ConcurrentUsers_MessageListUnder500ms() {
        // Configure test plan
        TestPlan testPlan = new TestPlan("Message List Load Test");
        
        // Configure thread group (100 users)
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(100);
        threadGroup.setRampUp(10); // 10 seconds ramp-up
        
        LoopController loopController = new LoopController();
        loopController.setLoops(10); // Each user makes 10 requests
        loopController.setFirst(true);
        threadGroup.setSamplerController(loopController);
        
        // Configure HTTP sampler
        HTTPSamplerProxy httpSampler = new HTTPSamplerProxy();
        httpSampler.setDomain("localhost");
        httpSampler.setPort(8080);
        httpSampler.setPath("/api/v1/messages");
        httpSampler.setMethod("GET");
        httpSampler.addNonEncodedArgument("page", "0", "=");
        httpSampler.addNonEncodedArgument("size", "20", "=");
        
        // Add Authorization header
        httpSampler.getHeaderManager().add(new Header("Authorization", "Bearer " + getValidToken()));
        
        // Configure results collector
        Summariser summariser = new Summariser("summary");
        ResultCollector resultCollector = new ResultCollector(summariser);
        resultCollector.setFilename("target/jmeter-results.jtl");
        
        // Build test plan tree
        HashTree testPlanTree = new HashTree();
        testPlanTree.add(testPlan);
        HashTree threadGroupTree = testPlanTree.add(testPlan, threadGroup);
        threadGroupTree.add(httpSampler);
        threadGroupTree.add(resultCollector);
        
        // Run test
        jmeter.configure(testPlanTree);
        jmeter.run();
        
        // Assert results
        LoadTestResults results = analyzeResults("target/jmeter-results.jtl");
        
        assertThat(results.getP95ResponseTime())
            .as("95th percentile response time should be under 500ms")
            .isLessThan(500);
        
        assertThat(results.getErrorRate())
            .as("Error rate should be under 1%")
            .isLessThan(1.0);
        
        assertThat(results.getThroughput())
            .as("Throughput should be at least 100 requests/second")
            .isGreaterThan(100.0);
    }

    @Test
    @DisplayName("PERF-02: 100 concurrent users - Message creation API < 1000ms p95")
    void loadTest_100ConcurrentUsers_MessageCreationUnder1000ms() {
        // Similar setup but for POST /api/v1/messages
        // Implementation omitted for brevity
    }

    @Test
    @DisplayName("PERF-03: Database connection pool handles 100 concurrent connections")
    void loadTest_DatabaseConnectionPool_Handles100Connections() {
        // Test database connection pool under load
        // Implementation omitted for brevity
    }

    private String getValidToken() {
        // Return a valid JWT token for testing
        return "valid.jwt.token";
    }

    private LoadTestResults analyzeResults(String resultsFile) {
        // Parse JMeter results file and extract metrics
        // Implementation omitted for brevity
        return new LoadTestResults();
    }

    static class LoadTestResults {
        private double p95ResponseTime;
        private double errorRate;
        private double throughput;
        
        // Getters...
    }
}
```

### 11.8 Gatling Load Test Script

```scala
// src/test/scala/simulations/MessageWorkflowLoadTest.scala
package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class MessageWorkflowLoadTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // Authentication
  val login = exec(http("Login")
    .post("/api/v1/auth/login")
    .body(StringBody("""{"email":"perf@tenant1.test","password":"Password123!"}"""))
    .check(status.is(200))
    .check(jsonPath("$.token").saveAs("authToken"))
  )

  // Create message
  val createMessage = exec(http("Create Message")
    .post("/api/v1/messages")
    .header("Authorization", "Bearer ${authToken}")
    .body(StringBody("""{
      "messageType":"PAYMENT_INITIATION",
      "messageCode":"pacs.008.001.08",
      "direction":"OUTBOUND",
      "rawXml":"<Document>Performance Test</Document>",
      "transactionReference":"PERF-${__UUID()}"
    }"""))
    .check(status.is(200))
    .check(jsonPath("$.messageUuid").saveAs("messageUuid"))
  )

  // Validate message
  val validateMessage = exec(http("Validate Message")
    .post("/api/v1/validation/validate/${messageUuid}")
    .header("Authorization", "Bearer ${authToken}")
    .check(status.is(200))
  )

  // Sign message
  val signMessage = exec(http("Sign Message")
    .post("/api/v1/messages/${messageUuid}/sign")
    .header("Authorization", "Bearer ${authToken}")
    .check(status.is(200))
  )

  // List messages
  val listMessages = exec(http("List Messages")
    .get("/api/v1/messages?page=0&size=20")
    .header("Authorization", "Bearer ${authToken}")
    .check(status.is(200))
  )

  // Complete workflow scenario
  val completeWorkflow = scenario("Complete Message Workflow")
    .exec(login)
    .pause(1.second)
    .exec(createMessage)
    .pause(500.milliseconds)
    .exec(validateMessage)
    .pause(500.milliseconds)
    .exec(signMessage)
    .pause(1.second)
    .exec(listMessages)

  // Browse messages scenario
  val browseMessages = scenario("Browse Messages")
    .exec(login)
    .pause(1.second)
    .repeat(5) {
      exec(listMessages)
        .pause(2.seconds)
    }

  // Setup load test
  setUp(
    completeWorkflow.inject(
      rampUsers(50) during (30.seconds),
      constantUsersPerSec(10) during (60.seconds)
    ),
    browseMessages.inject(
      rampUsers(50) during (30.seconds),
      constantUsersPerSec(20) during (60.seconds)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.percentile(95).lt(500),
     global.successfulRequests.percent.gt(99)
   )
}
```

---

## Security Audit Checklist

### 11.9 Comprehensive Security Audit

```markdown
# Security Audit Checklist

## Authentication & Authorization
- [ ] JWT tokens contain tenant_id claim
- [ ] JWT tokens have appropriate expiration (15 minutes for access, 7 days for refresh)
- [ ] JWT secret key is at least 256 bits
- [ ] Passwords hashed with bcrypt (cost factor >= 12)
- [ ] Password complexity enforced (min 8 chars, uppercase, lowercase, number, special)
- [ ] Account lockout after 5 failed login attempts
- [ ] Role-based access control enforced at controller level
- [ ] Service layer validates user permissions before operations
- [ ] No hardcoded credentials in source code
- [ ] Sensitive configuration in environment variables

## Tenant Isolation
- [ ] All database tables include tenant_id column
- [ ] All queries filtered by tenant_id
- [ ] Repository layer prevents cross-tenant queries
- [ ] Service layer validates tenant ownership
- [ ] Cross-tenant access attempts return 404 (not 403)
- [ ] JWT tenant_id validated against requested resources
- [ ] No tenant_id manipulation possible via API
- [ ] Database foreign keys cascade deletes properly
- [ ] Tenant context properly initialized from JWT
- [ ] Strict tenant data isolation for messages, test scenarios, validation results, and simulator profiles (shared simulator keys are platform runtime only)

## Data Protection
- [ ] Sensitive data encrypted at rest (signing keys, passwords)
- [ ] HTTPS enforced for all API endpoints (in production)
- [ ] Simulator private keys never exposed via API endpoints or client payloads
- [ ] Database connection uses SSL/TLS
- [ ] No sensitive data in application logs
- [ ] XML content sanitized before storage
- [ ] File uploads validated and size-limited
- [ ] No SQL injection vulnerabilities
- [ ] No XSS vulnerabilities in frontend
- [ ] CSRF protection enabled

## Audit Trail
- [ ] All authentication events logged
- [ ] All message operations logged
- [ ] All user management operations logged
- [ ] All simulator profile updates and message operations logged
- [ ] Support impersonation sessions logged
- [ ] Audit logs immutable (append-only)
- [ ] Audit logs include IP address and user agent
- [ ] Audit log retention policy defined
- [ ] Audit logs accessible only to authorized users
- [ ] Failed authorization attempts logged

## API Security
- [ ] Rate limiting configured (per tenant and per user)
- [ ] Input validation on all endpoints
- [ ] Output encoding prevents injection attacks
- [ ] Pagination limits enforced (max 100 items)
- [ ] File upload size limits enforced (max 10MB)
- [ ] CORS configured restrictively
- [ ] Security headers configured (CSP, X-Frame-Options, etc.)
- [ ] API versioning implemented
- [ ] Deprecated endpoints properly sunset
- [ ] Error messages don't leak sensitive information

## Infrastructure Security
- [ ] Database access restricted by firewall
- [ ] Application runs as non-root user
- [ ] Docker images scanned for vulnerabilities
- [ ] Dependencies regularly updated
- [ ] Security patches applied promptly
- [ ] Secrets managed securely (not in version control)
- [ ] Backup encryption configured
- [ ] Disaster recovery plan documented
- [ ] Monitoring and alerting configured
- [ ] Security incident response plan documented

## Compliance
- [ ] Audit logs meet regulatory requirements
- [ ] Data retention policy implemented
- [ ] User consent for data processing obtained
- [ ] Privacy policy documented
- [ ] Terms of service documented
- [ ] GDPR compliance (if applicable)
- [ ] Data breach notification process defined
- [ ] Third-party security assessments conducted
- [ ] Security training for development team completed
- [ ] Penetration testing conducted and issues remediated
```

---

## Audit Trail Completeness Verification

### 11.10 Test: Audit Trail Verification

```java
package org.example.signer.audit;

import org.example.signer.model.AuditLog;
import org.example.signer.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditTrailCompletenessTest {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("AUDIT-01: User login creates audit log entry")
    void userLogin_CreatesAuditLog() {
        // Perform login
        loginAs("audit@tenant1.test", "Password123!");
        
        // Verify audit log created
        List<AuditLog> logs = auditLogRepository.findByActionAndCreatedAtAfter(
            "USER_LOGIN",
            LocalDateTime.now().minusMinutes(1)
        );
        
        assertThat(logs)
            .isNotEmpty()
            .anySatisfy(log -> {
                assertThat(log.getAction()).isEqualTo("USER_LOGIN");
                assertThat(log.getUserId()).isNotNull();
                assertThat(log.getTenantId()).isNotNull();
                assertThat(log.getIpAddress()).isNotNull();
                assertThat(log.getUserAgent()).isNotNull();
                assertThat(log.getStatus()).isEqualTo("SUCCESS");
            });
    }

    @Test
    @DisplayName("AUDIT-02: Failed login creates audit log entry")
    void failedLogin_CreatesAuditLog() {
        // Attempt failed login
        attemptLogin("audit@tenant1.test", "wrongpassword");
        
        // Verify audit log created
        List<AuditLog> logs = auditLogRepository.findByActionAndCreatedAtAfter(
            "USER_LOGIN_FAILED",
            LocalDateTime.now().minusMinutes(1)
        );
        
        assertThat(logs)
            .isNotEmpty()
            .anySatisfy(log -> {
                assertThat(log.getAction()).isEqualTo("USER_LOGIN_FAILED");
                assertThat(log.getStatus()).isEqualTo("FAILURE");
                assertThat(log.getMetadata())
                    .containsKey("reason")
                    .containsValue("Invalid credentials");
            });
    }

    @Test
    @DisplayName("AUDIT-03: Message creation creates audit log entry")
    void messageCreation_CreatesAuditLog() {
        String token = loginAs("audit@tenant1.test", "Password123!");
        
        // Create message
        UUID messageUuid = createMessage(token);
        
        // Verify audit log created
        List<AuditLog> logs = auditLogRepository.findByResourceIdAndAction(
            messageUuid.toString(),
            "MESSAGE_CREATED"
        );
        
        assertThat(logs)
            .hasSize(1)
            .first()
            .satisfies(log -> {
                assertThat(log.getAction()).isEqualTo("MESSAGE_CREATED");
                assertThat(log.getResourceType()).isEqualTo("Message");
                assertThat(log.getResourceId()).isEqualTo(messageUuid.toString());
                assertThat(log.getUserId()).isNotNull();
                assertThat(log.getTenantId()).isNotNull();
            });
    }

    @Test
    @DisplayName("AUDIT-04: Message signing creates audit log entry")
    void messageSigning_CreatesAuditLog() {
        String token = loginAs("audit@tenant1.test", "Password123!");
        UUID messageUuid = createMessage(token);
        
        // Sign message
        signMessage(token, messageUuid);
        
        // Verify audit log created
        List<AuditLog> logs = auditLogRepository.findByResourceIdAndAction(
            messageUuid.toString(),
            "MESSAGE_SIGNED"
        );
        
        assertThat(logs).hasSize(1);
    }

    @Test
    @DisplayName("AUDIT-05: User invitation creates audit log entry")
    void userInvitation_CreatesAuditLog() {
        String adminToken = loginAs("admin@tenant1.test", "Password123!");
        
        // Invite user
        inviteUser(adminToken, "newuser@tenant1.test", "DEVELOPER");
        
        // Verify audit log created
        List<AuditLog> logs = auditLogRepository.findByActionAndCreatedAtAfter(
            "USER_INVITED",
            LocalDateTime.now().minusMinutes(1)
        );
        
        assertThat(logs)
            .isNotEmpty()
            .anySatisfy(log -> {
                assertThat(log.getMetadata())
                    .containsEntry("invitedEmail", "newuser@tenant1.test")
                    .containsEntry("role", "DEVELOPER");
            });
    }

    @Test
    @DisplayName("AUDIT-06: Simulator profile update creates audit log entry")
    void simulatorProfileUpdate_CreatesAuditLog() {
        String token = loginAs("admin@tenant1.test", "Password123!");
        
        // Update simulator profile
        given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body(Map.of(
                "institutionCode", "090004",
                "institutionName", "Bank One (Simulator)",
                "bic", "BONEUS33XXX"
            ))
        .when()
            .put("/api/v1/simulator/profile")
        .then()
            .statusCode(200);
        
        // Verify audit log created
        List<AuditLog> logs = auditLogRepository.findByTenantIdAndAction(
            1L,
            "SIMULATOR_PROFILE_UPDATED"
        );
        
        assertThat(logs).isNotEmpty();
    }

    @Test
    @DisplayName("AUDIT-07: Support impersonation creates audit log entries")
    void supportImpersonation_CreatesAuditLogs() {
        String platformAdminToken = loginAsPlatformAdmin();
        
        // Start impersonation
        String impersonationToken = startImpersonation(
            platformAdminToken,
            "target@tenant1.test",
            "Investigate bug report #123"
        );
        
        // Perform action as impersonated user
        createMessage(impersonationToken);
        
        // End impersonation
        endImpersonation(impersonationToken);
        
        // Verify audit logs created
        List<AuditLog> logs = auditLogRepository.findByCreatedAtAfter(
            LocalDateTime.now().minusMinutes(5)
        );
        
        assertThat(logs)
            .extracting(AuditLog::getAction)
            .contains(
                "IMPERSONATION_STARTED",
                "MESSAGE_CREATED",
                "IMPERSONATION_ENDED"
            );
        
        // Verify impersonation context in message creation log
        assertThat(logs)
            .filteredOn(log -> log.getAction().equals("MESSAGE_CREATED"))
            .first()
            .satisfies(log -> {
                assertThat(log.getImpersonatorId()).isNotNull();
                assertThat(log.getImpersonationSessionId()).isNotNull();
                assertThat(log.getMetadata())
                    .containsKey("impersonationJustification");
            });
    }

    @Test
    @DisplayName("AUDIT-08: Audit logs are immutable")
    void auditLogs_AreImmutable() {
        // Create an audit log
        AuditLog log = AuditLog.builder()
            .tenantId(1L)
            .userId(1L)
            .action("TEST_ACTION")
            .resourceType("Test")
            .resourceId("test-123")
            .ipAddress("127.0.0.1")
            .userAgent("Test")
            .status("SUCCESS")
            .build();
        
        AuditLog saved = auditLogRepository.save(log);
        
        // Attempt to modify
        saved.setStatus("MODIFIED");
        
        // Verify modification rejected or detected
        assertThatThrownBy(() -> auditLogRepository.save(saved))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Audit logs are immutable");
    }

    @Test
    @DisplayName("AUDIT-09: Audit log export includes all required fields")
    void auditLogExport_IncludesAllFields() {
        String adminToken = loginAs("admin@tenant1.test", "Password123!");
        
        // Export audit logs
        String csv = exportAuditLogs(adminToken);
        
        // Verify CSV contains all required columns
        assertThat(csv)
            .contains("timestamp")
            .contains("tenant_id")
            .contains("user_id")
            .contains("action")
            .contains("resource_type")
            .contains("resource_id")
            .contains("ip_address")
            .contains("user_agent")
            .contains("status");
    }

    @Test
    @DisplayName("AUDIT-10: Failed authorization attempts are logged")
    void failedAuthorization_IsLogged() {
        String viewerToken = loginAs("viewer@tenant1.test", "Password123!");
        
        // Attempt unauthorized action (create message as viewer)
        attemptCreateMessage(viewerToken);
        
        // Verify audit log created
        List<AuditLog> logs = auditLogRepository.findByActionAndCreatedAtAfter(
            "AUTHORIZATION_FAILED",
            LocalDateTime.now().minusMinutes(1)
        );
        
        assertThat(logs)
            .isNotEmpty()
            .anySatisfy(log -> {
                assertThat(log.getStatus()).isEqualTo("FAILURE");
                assertThat(log.getMetadata())
                    .containsEntry("requiredRole", "DEVELOPER")
                    .containsEntry("actualRole", "VIEWER");
            });
    }
}
```

---

## API Response Time Validation

### 11.11 Test: API Response Time Requirements

```java
package org.example.signer.performance;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiResponseTimeTest {

    @LocalServerPort
    private int port;
    
    private String authToken;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        authToken = loginAndGetToken();
    }

    @Test
    @DisplayName("PERF-RT-01: GET /api/v1/messages responds in < 500ms")
    void getMessages_RespondsUnder500ms() {
        Response response = given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/api/v1/messages?page=0&size=20")
        .then()
            .statusCode(200)
        .extract()
            .response();
        
        long responseTime = response.getTime();
        assertThat(responseTime)
            .as("Message list should respond in under 500ms")
            .isLessThan(500);
    }

    @Test
    @DisplayName("PERF-RT-02: GET /api/v1/messages/{uuid} responds in < 200ms")
    void getMessage_RespondsUnder200ms() {
        UUID messageUuid = createTestMessage();
        
        Response response = given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/api/v1/messages/{uuid}", messageUuid)
        .then()
            .statusCode(200)
        .extract()
            .response();
        
        long responseTime = response.getTime();
        assertThat(responseTime)
            .as("Message retrieval should respond in under 200ms")
            .isLessThan(200);
    }

    @Test
    @DisplayName("PERF-RT-03: POST /api/v1/messages responds in < 1000ms")
    void createMessage_RespondsUnder1000ms() {
        Response response = given()
            .header("Authorization", "Bearer " + authToken)
            .contentType("application/json")
            .body("""
                {
                    "messageType": "PAYMENT_INITIATION",
                    "messageCode": "pacs.008.001.08",
                    "direction": "OUTBOUND",
                    "rawXml": "<Document>Test</Document>"
                }
                """)
        .when()
            .post("/api/v1/messages")
        .then()
            .statusCode(200)
        .extract()
            .response();
        
        long responseTime = response.getTime();
        assertThat(responseTime)
            .as("Message creation should respond in under 1000ms")
            .isLessThan(1000);
    }

    @Test
    @DisplayName("PERF-RT-04: POST /api/v1/auth/login responds in < 500ms")
    void login_RespondsUnder500ms() {
        Response response = given()
            .contentType("application/json")
            .body("""
                {
                    "email": "perf@tenant1.test",
                    "password": "Password123!"
                }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
        .extract()
            .response();
        
        long responseTime = response.getTime();
        assertThat(responseTime)
            .as("Login should respond in under 500ms")
            .isLessThan(500);
    }

    @Test
    @DisplayName("PERF-RT-05: GET /api/v1/audit-logs responds in < 500ms")
    void getAuditLogs_RespondsUnder500ms() {
        Response response = given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/api/v1/audit-logs?page=0&size=50")
        .then()
            .statusCode(200)
        .extract()
            .response();
        
        long responseTime = response.getTime();
        assertThat(responseTime)
            .as("Audit log retrieval should respond in under 500ms")
            .isLessThan(500);
    }

    @Test
    @DisplayName("PERF-RT-06: GET /api/v1/messages/statistics responds in < 500ms")
    void getStatistics_RespondsUnder500ms() {
        Response response = given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/api/v1/messages/statistics")
        .then()
            .statusCode(200)
        .extract()
            .response();
        
        long responseTime = response.getTime();
        assertThat(responseTime)
            .as("Statistics should respond in under 500ms")
            .isLessThan(500);
    }

    private String loginAndGetToken() {
        return given()
            .contentType("application/json")
            .body("""
                {
                    "email": "perf@tenant1.test",
                    "password": "Password123!"
                }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
        .extract()
            .path("token");
    }

    private UUID createTestMessage() {
        return UUID.fromString(
            given()
                .header("Authorization", "Bearer " + authToken)
                .contentType("application/json")
                .body("""
                    {
                        "messageType": "PAYMENT_INITIATION",
                        "messageCode": "pacs.008.001.08",
                        "direction": "OUTBOUND",
                        "rawXml": "<Document>Test</Document>"
                    }
                    """)
            .when()
                .post("/api/v1/messages")
            .extract()
                .path("messageUuid")
        );
    }
}
```

---

## Final Acceptance Testing

### 11.12 User Acceptance Test Plan

```markdown
# User Acceptance Test (UAT) Plan

## UAT-01: Complete Multi-Tenant Workflow

**Objective**: Verify that two different financial institutions can independently use the platform without interference.

**Participants**: 
- Bank A: Admin + 2 developers
- Bank B: Admin + 2 developers

**Steps**:
1. Bank A admin creates account and invites 2 developers
2. Bank B admin creates account and invites 2 developers
3. Both banks configure their pseudo-bank simulator profiles (Institution Code, BIC, accounts) independently without uploading cryptographic keys
4. Both banks create, validate, sign, and send ISO 20022 messages (automated signing via shared simulator keys)
5. Both banks verify they can only see their own data
6. Both banks export their audit logs

**Expected Results**:
- [ ] Each bank sees only their own users
- [ ] Each bank sees only their own messages
- [ ] Each bank sees only their own simulator profiles
- [ ] Each bank sees only their own audit logs
- [ ] Automated signing succeeds seamlessly using system simulator keys without cross-tenant collision
- [ ] No errors or performance degradation
- [ ] All messages process successfully

---

## UAT-02: Seat Quota Enforcement

**Objective**: Verify seat quota limits are enforced correctly.

**Participants**: Tenant admin

**Steps**:
1. Create tenant with 5-seat quota
2. Invite 5 users successfully
3. Attempt to invite 6th user
4. Verify invitation blocked with clear error
5. Deactivate 1 user
6. Successfully invite new user (now within quota)

**Expected Results**:
- [ ] Cannot exceed seat quota
- [ ] Clear error message when quota reached
- [ ] Deactivating user frees up seat
- [ ] Real-time seat count is accurate

---

## UAT-03: Support Impersonation

**Objective**: Verify support can safely impersonate users for troubleshooting.

**Participants**: Platform admin, support engineer, tenant user

**Steps**:
1. Tenant user reports an issue
2. Support engineer requests impersonation access
3. Platform admin approves request
4. Support engineer logs in as tenant user
5. Support engineer investigates issue
6. Impersonation session expires after time limit
7. Tenant admin reviews impersonation audit logs

**Expected Results**:
- [ ] Impersonation requires approval
- [ ] Impersonation has time limit
- [ ] All actions during impersonation are logged
- [ ] Tenant admin is notified
- [ ] Session auto-expires
- [ ] Complete audit trail maintained

---

## UAT-04: Message Processing Performance

**Objective**: Verify message processing meets performance requirements.

**Participants**: Multiple developers

**Steps**:
1. Create 10 payment initiation messages
2. Validate all messages in batch
3. Sign all messages in batch
4. Encrypt all messages in batch
5. Send all messages in batch
6. Verify all operations complete quickly

**Expected Results**:
- [ ] Batch validation completes in < 10 seconds
- [ ] Batch signing completes in < 20 seconds
- [ ] Batch encryption completes in < 20 seconds
- [ ] No errors during processing
- [ ] All status updates reflected immediately

---

## UAT-05: Audit Trail Completeness

**Objective**: Verify all required actions are captured in audit logs.

**Participants**: Compliance officer

**Steps**:
1. Perform various actions (login, create message, invite user, etc.)
2. Export audit logs
3. Verify all actions are logged
4. Verify log format meets requirements
5. Attempt to modify or delete audit logs

**Expected Results**:
- [ ] All actions captured in audit logs
- [ ] Logs include timestamp, user, IP, action, result
- [ ] Logs are exportable in CSV/JSON format
- [ ] Logs cannot be modified or deleted
- [ ] Logs are searchable and filterable

---

## UAT-06: Role-Based Access Control

**Objective**: Verify different roles have appropriate access levels.

**Participants**: Users with different roles (Admin, Developer, Viewer)

**Steps**:
1. Login as Viewer
2. Attempt various operations (view only should succeed)
3. Login as Developer
4. Attempt various operations (view + create should succeed)
5. Login as Tenant Admin
6. Attempt various operations (all tenant operations should succeed)

**Expected Results**:
- [ ] Viewer can view but not modify
- [ ] Developer can create and modify messages
- [ ] Tenant Admin can manage users and settings
- [ ] Unauthorized operations return 403
- [ ] Failed authorization attempts are logged

---

## UAT-07: Pseudo-Bank Profile Configuration & Automated Simulator Signing

**Objective**: Verify pseudo-bank identity configuration, default test accounts, automated transparent signing via shared simulator keys, and inbound callback correlation.

**Participants**: Tenant admin, developer

**Steps**:
1. Configure tenant's pseudo-bank profile (Institution Code e.g. `090004`, BIC, Bank Name, Default Account/BVN)
2. Generate an ISO 20022 message (`pain.001` or `pain.013`) and verify tenant's institution code is injected
3. Execute automated signing and verify envelope signature is created using shared simulator private key without requiring manual key upload
4. Verify message status transitions to `SIGNED`
5. Simulate dispatch to mock NIBSS switch
6. Verify inbound simulator response correlates back to the originating tenant via `OrgnlMsgId` / `OrgnlEndToEndId`

**Expected Results**:
- [ ] Profile attributes save and retrieve strictly within tenant boundary
- [ ] Messages generated with tenant's configured pseudo-bank parameters
- [ ] Signing succeeds automatically with system simulator keys
- [ ] Zero onboarding friction (no manual certificate or key uploads required)
- [ ] Inbound counterparty callbacks correlate accurately to originating tenant

---

## UAT-08: Cross-Browser Compatibility

**Objective**: Verify platform works across major browsers.

**Participants**: Any user

**Steps**:
1. Test complete workflow on Chrome
2. Test complete workflow on Firefox
3. Test complete workflow on Safari
4. Test complete workflow on Edge

**Expected Results**:
- [ ] UI renders correctly in all browsers
- [ ] All features work in all browsers
- [ ] No console errors
- [ ] Performance is acceptable in all browsers
```

---

## Acceptance Criteria

### Overall System

- [ ] All 10 phases implemented and integrated
- [ ] All unit tests passing (>80% code coverage)
- [ ] All integration tests passing
- [ ] All E2E tests passing
- [ ] All security tests passing
- [ ] All performance tests passing
- [ ] All UAT scenarios completed successfully

### Functional Requirements

- [ ] Multi-tenant isolation verified (zero cross-tenant data leakage)
- [ ] Team and user management working correctly
- [ ] Seat quota enforcement working correctly
- [ ] ISO 20022 message processing working correctly
- [ ] Audit trail complete and immutable
- [ ] Support impersonation working correctly

### Performance Requirements

- [ ] API response times meet targets (<500ms for reads, <1000ms for writes)
- [ ] System handles 100 concurrent users
- [ ] Database queries optimized with proper indexes
- [ ] No N+1 query problems
- [ ] Page load times <2 seconds

### Security Requirements

- [ ] All security audit checklist items completed
- [ ] Penetration testing completed with no critical issues
- [ ] Cross-tenant isolation verified
- [ ] Authentication and authorization working correctly
- [ ] Sensitive data encrypted
- [ ] No SQL injection vulnerabilities
- [ ] No XSS vulnerabilities
- [ ] CSRF protection enabled

### Compliance Requirements

- [ ] Audit logs meet regulatory requirements
- [ ] Data retention policy implemented
- [ ] Privacy policy documented
- [ ] Terms of service documented
- [ ] Security incident response plan documented

### Documentation

- [ ] API documentation complete (Swagger UI)
- [ ] User guides written
- [ ] Admin guides written
- [ ] Deployment guides written
- [ ] Troubleshooting guides written
- [ ] Architecture diagrams created

---

## Rollback Plan

If critical issues are discovered during integration testing:

1. **Stop Deployment**: Immediately halt any production deployment
2. **Document Issues**: Create detailed bug reports with reproduction steps
3. **Assess Severity**: Categorize issues as blocker, critical, or major
4. **Fix or Rollback**: 
   - If fixable within 2 days: Fix and retest
   - If not fixable quickly: Rollback to previous stable version
5. **Communicate**: Notify all stakeholders of status and timeline

---

## Dependencies

**Requires Completion**: All phases (1-10)

**Blocks**: Production deployment

---

## Estimated Effort

- Test environment setup: 0.5 days
- Test data generation: 0.5 days
- E2E test implementation: 1.5 days
- Security testing: 1 day
- Performance testing: 1 day
- Audit trail verification: 0.5 days
- UAT execution: 1 day
- Bug fixes and retesting: 1 day

**Total: 5-7 days**

---

## Success Metrics

- **Test Coverage**: >80% code coverage
- **Test Pass Rate**: 100% of tests passing
- **Performance**: All APIs meet response time targets
- **Security**: Zero critical or high-severity vulnerabilities
- **Stability**: Zero data corruption or data loss issues
- **User Satisfaction**: UAT participants rate the system 4/5 or higher

---

## Post-Testing Activities

After successful completion of all tests:

1. **Generate Test Report**: Comprehensive report covering all test results
2. **Security Certification**: Document that security requirements are met
3. **Performance Baseline**: Document performance metrics for future comparison
4. **Production Readiness Review**: Final go/no-go decision with stakeholders
5. **Deploy to Production**: Execute deployment plan
6. **Post-Deployment Verification**: Smoke tests in production environment
7. **Monitor**: Continuous monitoring for first 48 hours post-deployment
