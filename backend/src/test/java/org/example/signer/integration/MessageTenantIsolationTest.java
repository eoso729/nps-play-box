package org.example.signer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.message.CreateMessageRequestDto;
import org.example.signer.dto.message.CreateTestScenarioDto;
import org.example.signer.dto.message.UpdateSimulatorProfileDto;
import org.example.signer.entity.Iso20022Message;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.TestScenario;
import org.example.signer.entity.User;
import org.example.signer.repository.*;
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

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MessageTenantIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Iso20022MessageRepository messageRepository;

    @Autowired
    private TenantSimulatorProfileRepository profileRepository;

    @Autowired
    private TestScenarioRepository scenarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Tenant tenantA;
    private User userA;
    private String tokenA;

    private Tenant tenantB;
    private User userB;
    private String tokenB;

    @BeforeEach
    void setUp() {
        scenarioRepository.deleteAll();
        messageRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        // Tenant A: First Bank Simulator
        tenantA = tenantRepository.save(Tenant.builder()
                .name("First Bank")
                .slug("first-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                .build());

        userA = userRepository.save(User.builder()
                .tenantId(tenantA.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@firstbank.com")
                .username("firstbank_admin")
                .passwordHash(passwordEncoder.encode("Secret123!"))
                .firstName("First")
                .lastName("Bank")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        tokenA = jwtService.generateToken(userA, tenantA.getSlug());

        // Tenant B: Zenith Bank Simulator
        tenantB = tenantRepository.save(Tenant.builder()
                .name("Zenith Bank")
                .slug("zenith-bank")
                .status(Tenant.TenantStatus.ACTIVE)
                .maxSeats(10)
                .subscriptionTier(Tenant.SubscriptionTier.PROFESSIONAL)
                .build());

        userB = userRepository.save(User.builder()
                .tenantId(tenantB.getId())
                .userUuid(UUID.randomUUID())
                .email("admin@zenithbank.com")
                .username("zenith_admin")
                .passwordHash(passwordEncoder.encode("Secret123!"))
                .firstName("Zenith")
                .lastName("Bank")
                .role(User.UserRole.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .authProvider("LOCAL")
                .build());

        tokenB = jwtService.generateToken(userB, tenantB.getSlug());
    }

    @Test
    @DisplayName("Tenant A creates a message; Tenant B receives 404 when querying Tenant A's message")
    void testMessageCrossTenantIsolation() throws Exception {
        // 1. Tenant A creates an ISO 20022 message
        CreateMessageRequestDto createDto = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                .messageCode("pain.001.001.03")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document><CstmrCdtTrfInitn><GrpHdr><MsgId>MSG-A-001</MsgId></GrpHdr></CstmrCdtTrfInitn></Document>")
                .transactionReference("TX-REF-001")
                .messageId("MSG-A-001")
                .build();

        MvcResult resultA = mockMvc.perform(post("/api/v1/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageUuid").isNotEmpty())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        String messageUuid = objectMapper.readTree(resultA.getResponse().getContentAsString())
                .get("messageUuid").asText();

        // 2. Tenant A can access its own message
        mockMvc.perform(get("/api/v1/messages/" + messageUuid)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageUuid").value(messageUuid))
                .andExpect(jsonPath("$.messageId").value("MSG-A-001"));

        // 3. Tenant B attempts to access Tenant A's message -> 404 NOT_FOUND
        mockMvc.perform(get("/api/v1/messages/" + messageUuid)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // 4. Tenant B lists messages -> empty
        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        // 5. Tenant A lists messages -> contains 1 message
        mockMvc.perform(get("/api/v1/messages")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Digital signing and encryption use shared SimulatorKeyProvider transparently")
    void testMessageSigningAndEncryptionWithSimulatorKeys() throws Exception {
        // Create draft message for Tenant A
        CreateMessageRequestDto createDto = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.TRANSFER)
                .messageCode("pacs.008.001.02")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document><FIToFICstmrCdtTrf><GrpHdr><MsgId>M123</MsgId></GrpHdr><CdtTrfTxInf><IntrBkSttlmAmt>1000.00</IntrBkSttlmAmt></CdtTrfTxInf></FIToFICstmrCdtTrf></Document>")
                .messageId("M123")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/messages")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String messageUuid = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("messageUuid").asText();

        // Sign message using shared simulator key
        mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/sign")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SIGNED"))
                .andExpect(jsonPath("$.signedXml").isNotEmpty())
                .andExpect(jsonPath("$.signedXml", containsString("Signature")))
                .andExpect(jsonPath("$.signedXml", containsString("SignedInfo")));

        // Encrypt element using shared simulator public key
        mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/encrypt?elementTagName=CdtTrfTxInf")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCRYPTED"))
                .andExpect(jsonPath("$.encryptedXml").isNotEmpty())
                .andExpect(jsonPath("$.encryptedXml", containsString("EncryptedData")));
    }

    @Test
    @DisplayName("Tenant simulator profiles are isolated and can be customized independently")
    void testTenantSimulatorProfilesIsolation() throws Exception {
        // Tenant A fetches default profile
        mockMvc.perform(get("/api/v1/simulator/profile")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionCode").value("999057"))
                .andExpect(jsonPath("$.defaultCurrency").value("NGN"));

        // Tenant A updates its profile
        UpdateSimulatorProfileDto updateDtoA = UpdateSimulatorProfileDto.builder()
                .institutionCode("090004")
                .institutionName("First Bank Pseudo")
                .bic("FBNINGLAXXX")
                .schemeCode("090004")
                .defaultAccountNumber("0123456789")
                .defaultAccountName("First Bank Settlement")
                .defaultBvn("11111111111")
                .autoRespondInbound(true)
                .build();

        mockMvc.perform(put("/api/v1/simulator/profile")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDtoA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionCode").value("090004"))
                .andExpect(jsonPath("$.institutionName").value("First Bank Pseudo"));

        // Tenant B fetches profile -> unaffected by Tenant A
        mockMvc.perform(get("/api/v1/simulator/profile")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionCode").value("999057"))
                .andExpect(jsonPath("$.institutionName").value("Zenith Bank (Simulator)"));
    }

    @Test
    @DisplayName("Tenant statistics are isolated to the querying tenant")
    void testTenantMessageStatisticsIsolation() throws Exception {
        // Create 2 messages for Tenant A
        CreateMessageRequestDto msgA1 = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                .messageCode("pain.001")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml("<xml>1</xml>")
                .build();

        CreateMessageRequestDto msgA2 = CreateMessageRequestDto.builder()
                .messageType(Iso20022Message.MessageType.TRANSFER)
                .messageCode("pacs.008")
                .direction(Iso20022Message.MessageDirection.OUTBOUND)
                .rawXml("<xml>2</xml>")
                .build();

        mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msgA1)));

        mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(msgA2)));

        // Stats for Tenant A
        mockMvc.perform(get("/api/v1/messages/statistics")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessages").value(2))
                .andExpect(jsonPath("$.outboundCount").value(2))
                .andExpect(jsonPath("$.inboundCount").value(0));

        // Stats for Tenant B -> zero
        mockMvc.perform(get("/api/v1/messages/statistics")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessages").value(0))
                .andExpect(jsonPath("$.outboundCount").value(0));
    }

    @Test
    @DisplayName("Test scenarios are strictly isolated by tenant")
    void testTestScenariosIsolation() throws Exception {
        CreateTestScenarioDto scenarioDto = CreateTestScenarioDto.builder()
                .scenarioName("Direct Credit Positive Flow")
                .scenarioDescription("Verifies positive pacs.008 processing")
                .messageType("pacs.008")
                .testCategory(TestScenario.TestCategory.FUNCTIONAL)
                .inputData(Map.of("amount", 5000, "currency", "NGN"))
                .expectedOutput(Map.of("responseCode", "00"))
                .build();

        MvcResult res = mockMvc.perform(post("/api/v1/test-scenarios")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scenarioDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scenarioUuid").isNotEmpty())
                .andReturn();

        String scenarioUuid = objectMapper.readTree(res.getResponse().getContentAsString())
                .get("scenarioUuid").asText();

        // Tenant A can get the scenario
        mockMvc.perform(get("/api/v1/test-scenarios/" + scenarioUuid)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenarioName").value("Direct Credit Positive Flow"));

        // Tenant B gets 404
        mockMvc.perform(get("/api/v1/test-scenarios/" + scenarioUuid)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }
}
