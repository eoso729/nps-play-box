package org.example.signer.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.signer.dto.AuthRequest;
import org.example.signer.dto.message.CreateMessageRequestDto;
import org.example.signer.dto.validation.XmlInspectRequestDto;
import org.example.signer.entity.Iso20022Message;
import org.example.signer.entity.Tenant;
import org.example.signer.entity.User;
import org.example.signer.repository.Iso20022MessageRepository;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiResponseTimePerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private Iso20022MessageRepository messageRepository;

    private String sampleXml;

    @BeforeEach
    void setUp() {
        testDataFactory.cleanAll();

        var def = IsoMessageRegistry.getDefinition("pain.001");
        sampleXml = def != null ? def.getSampleXml() : "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.12\"><CstmrCdtTrfInitn><GrpHdr><MsgId>MSG-001</MsgId></GrpHdr></CstmrCdtTrfInitn></Document>";
    }

    @Test
    @DisplayName("Concurrent Tenant Operations: 10 tenants concurrently create and sign messages with zero race conditions or cross-talk")
    void testConcurrentTenantsMessageSigning() throws Exception {
        int tenantCount = 10;
        List<Tenant> tenants = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<String> tokens = new ArrayList<>();

        for (int i = 0; i < tenantCount; i++) {
            Tenant t = testDataFactory.createTenant("Bank " + i, "bank-" + i, Tenant.SubscriptionTier.PROFESSIONAL, 10);
            User u = testDataFactory.createUser(t.getId(), "admin@bank" + i + ".com", "admin_bank_" + i, User.UserRole.TENANT_ADMIN, "Pass1234!");
            String token = testDataFactory.generateToken(u, t.getSlug());
            tenants.add(t);
            users.add(u);
            tokens.add(token);
        }

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(tenantCount);
        List<Throwable> exceptions = new CopyOnWriteArrayList<>();
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 0; i < tenantCount; i++) {
            final int index = i;
            final String token = tokens.get(index);
            final Tenant tenant = tenants.get(index);

            executor.submit(() -> {
                try {
                    startLatch.await();

                    // 1. Create message for this tenant
                    CreateMessageRequestDto createDto = CreateMessageRequestDto.builder()
                            .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                            .messageCode("pain.001.001.12")
                            .direction(Iso20022Message.MessageDirection.OUTBOUND)
                            .rawXml(sampleXml)
                            .transactionReference("TXN-" + index)
                            .messageId("MSG-" + index)
                            .build();

                    MvcResult createRes = mockMvc.perform(post("/api/v1/messages")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(createDto)))
                            .andExpect(status().isCreated())
                            .andReturn();

                    String messageUuid = objectMapper.readTree(createRes.getResponse().getContentAsString())
                            .get("messageUuid").asText();

                    // 2. Sign message for this tenant
                    mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/sign")
                                    .header("Authorization", "Bearer " + token))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.status").value("SIGNED"))
                            .andExpect(jsonPath("$.signedXml", containsString("Signature")));

                    // 3. Verify statistics query for this tenant
                    mockMvc.perform(get("/api/v1/messages/statistics")
                                    .header("Authorization", "Bearer " + token))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.totalMessages").value(1));

                    successCounter.incrementAndGet();
                } catch (Throwable t) {
                    exceptions.add(t);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Fire all threads concurrently
        startLatch.countDown();
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Concurrent operations timed out");
        assertTrue(exceptions.isEmpty(), "Exceptions during concurrent tenant operations: " + exceptions);
        assertEquals(tenantCount, successCounter.get(), "All tenant operations should succeed");

        // Verify strictly isolated message storage: each tenant has exactly 1 message
        for (Tenant tenant : tenants) {
            long count = messageRepository.countByTenantId(tenant.getId());
            assertEquals(1, count, "Tenant " + tenant.getId() + " must have exactly 1 isolated message");
        }
    }

    @Test
    @DisplayName("API Response Time Validation: P95 latency bounds verified across core operations")
    void testApiResponseTimeP95Bounds() throws Exception {
        Tenant tenant = testDataFactory.createTenant("Latency Test Bank", "latency-bank", Tenant.SubscriptionTier.ENTERPRISE, 20);
        User user = testDataFactory.createUser(tenant.getId(), "latency@bank.com", "latency_user", User.UserRole.TENANT_ADMIN, "SecretPass123!");
        String token = testDataFactory.generateToken(user, tenant.getSlug());

        // Pre-warm Spring DispatcherServlet and JAXB / crypto context
        mockMvc.perform(get("/api/v1/messages/statistics").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        int iterations = 20;

        // 1. Benchmark Message Signing (p95 < 500ms)
        List<Long> signLatencies = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            CreateMessageRequestDto createDto = CreateMessageRequestDto.builder()
                    .messageType(Iso20022Message.MessageType.PAYMENT_INITIATION)
                    .messageCode("pain.001.001.12")
                    .direction(Iso20022Message.MessageDirection.OUTBOUND)
                    .rawXml(sampleXml)
                    .transactionReference("TXN-LATENCY-" + i)
                    .messageId("MSG-LATENCY-" + i)
                    .build();

            MvcResult createResult = mockMvc.perform(post("/api/v1/messages")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andReturn();

            String messageUuid = objectMapper.readTree(createResult.getResponse().getContentAsString())
                    .get("messageUuid").asText();

            long start = System.currentTimeMillis();
            mockMvc.perform(post("/api/v1/messages/" + messageUuid + "/sign")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            long elapsed = System.currentTimeMillis() - start;
            signLatencies.add(elapsed);
        }

        long p95Sign = calculateP95(signLatencies);
        assertTrue(p95Sign < 500, "Message signing p95 (" + p95Sign + "ms) must be < 500ms");

        // 2. Benchmark Dashboard Statistics Query (p95 < 500ms)
        List<Long> statsLatencies = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(get("/api/v1/messages/statistics")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            statsLatencies.add(System.currentTimeMillis() - start);
        }

        long p95Stats = calculateP95(statsLatencies);
        assertTrue(p95Stats < 500, "Dashboard statistics query p95 (" + p95Stats + "ms) must be < 500ms");

        // 3. Benchmark Schema / XML Validation (p95 < 500ms)
        XmlInspectRequestDto inspectDto = XmlInspectRequestDto.builder()
                .xmlContent(sampleXml)
                .messageType("pain.001")
                .build();

        List<Long> validateLatencies = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(post("/api/validation/inspect")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inspectDto)))
                    .andExpect(status().isOk());
            validateLatencies.add(System.currentTimeMillis() - start);
        }

        long p95Validate = calculateP95(validateLatencies);
        assertTrue(p95Validate < 500, "Validation inspection p95 (" + p95Validate + "ms) must be < 500ms");

        // 4. Benchmark Audit Events Query (p95 < 500ms)
        List<Long> auditLatencies = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(get("/api/v1/audit/events")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            auditLatencies.add(System.currentTimeMillis() - start);
        }

        long p95Audit = calculateP95(auditLatencies);
        assertTrue(p95Audit < 500, "Audit log query p95 (" + p95Audit + "ms) must be < 500ms");

        // 5. Benchmark Authentication / Login (p95 < 500ms)
        AuthRequest loginReq = AuthRequest.builder()
                .email("latency@bank.com")
                .password("SecretPass123!")
                .tenantSlug("latency-bank")
                .build();

        List<Long> authLatencies = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginReq)))
                    .andExpect(status().isOk());
            authLatencies.add(System.currentTimeMillis() - start);
        }

        long p95Auth = calculateP95(authLatencies);
        assertTrue(p95Auth < 500, "Authentication login p95 (" + p95Auth + "ms) must be < 500ms");
    }

    private long calculateP95(List<Long> latencies) {
        if (latencies == null || latencies.isEmpty()) return 0;
        List<Long> sorted = new ArrayList<>(latencies);
        Collections.sort(sorted);
        int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }
}
