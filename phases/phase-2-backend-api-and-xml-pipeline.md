# Phase 2: Backend API — XML Generation Pipeline & Send Pipeline

## Goal

Split the backend message processing into two distinct, independently callable pipelines:

1. **XML Generator Pipeline** — Generates and returns `plainXml` and `signedXml` without dispatching any network request to the NPS/NIBSS service.
2. **Send Pipeline** — Takes the already-generated (or user-provided) signed XML and dispatches it to the NPS/NIBSS service, returning the raw service response.

This separation allows users to inspect, validate, and diff XML before committing to a live send.

---

## API Design: Two Separate Endpoint Groups

### Group A — XML Generator Endpoints (No Network Send)

Each ISO 20022 message type gets a `/generate` variant that returns only the XML artifacts.

**Request**: Standard message DTO (same fields as before).
**Response**: `XmlGenerationResponseDto`

```java
public class XmlGenerationResponseDto {
    private String messageType;   // e.g. "pain.013.001.11"
    private String messageId;     // Auto-generated MsgId used in the XML
    private String plainXml;      // Raw unsigned ISO 20022 XML
    private String signedXml;     // PKCS#7 digitally signed and encrypted XML
    private String generatedAt;   // ISO-8601 timestamp
}
```

**Endpoints (examples):**

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/generate/payment-activation-pain013` | Generate `pain.013` plain + signed XML |
| `POST` | `/api/generate/payment-initiation-pain001` | Generate `pain.001` plain + signed XML |
| `POST` | `/api/generate/mandate-creation-pain009` | Generate `pain.009` plain + signed XML |
| `POST` | `/api/generate/mandate-amendment-pain010` | Generate `pain.010` plain + signed XML |
| `POST` | `/api/generate/mandate-cancellation-pain011` | Generate `pain.011` plain + signed XML |
| `POST` | `/api/generate/direct-debit-pain008` | Generate `pain.008` plain + signed XML |
| `POST` | `/api/generate/customer-direct-debit-pacs003` | Generate `pacs.003` plain + signed XML |
| `POST` | `/api/generate/transfer-pacs008` | Generate `pacs.008` plain + signed XML |
| `POST` | `/api/generate/payment-return-pacs004` | Generate `pacs.004` plain + signed XML |
| `POST` | `/api/generate/name-verification-acmt023` | Generate `acmt.023` plain + signed XML |
| `POST` | `/api/generate/balance-enquiry-camt060` | Generate `camt.060` plain + signed XML |

**Pipeline Logic (Generate-only):**
1. Validate DTO fields.
2. Call `XmlGenerator.generate(requestDto)` → `plainXml`.
3. Call `Signer.signXml(plainXml)` → `signedXml`.
4. Return `XmlGenerationResponseDto` — **no HTTP dispatch**.

---

### Group B — Send Pipeline Endpoints

Takes a request DTO, runs the full generate pipeline, then dispatches the signed XML to the NPS/NIBSS service and captures the response.

**Response**: `MessageSendResponseDto`

```java
public class MessageSendResponseDto {
    private String messageType;
    private String messageId;
    private String plainXml;
    private String signedXml;
    private ServicePushResult serviceResponse;
}

public class ServicePushResult {
    private int statusCode;          // e.g. 200, 500
    private String rawResponseBody;  // Raw XML/JSON from NIBSS endpoint
    private long executionTimeMs;    // Round-trip latency
    private boolean success;
    private String timestamp;        // ISO-8601 response timestamp
}
```

**Endpoints** follow the existing `/api/` prefix pattern (e.g. `/api/payment-activation-pain013`).

**Pipeline Logic (Full Send):**
1. Validate DTO fields.
2. Generate plain XML.
3. Sign XML.
4. Dispatch via `CurlSender.send(signedXml)`.
5. Capture and return full `MessageSendResponseDto`.

---

## Supported Message Catalog

| Message Code | Description | DTO Class |
| :--- | :--- | :--- |
| `pain.013.001.11` | Payment Activation Request | `PaymentActivationRequestDto` |
| `pain.001.001.11` | Payment Initiation | `PaymentInitiationRequestDto` |
| `pain.008.001.10` | Direct Debit Request | `DirectDebitRequestDto` |
| `pain.009.001.07` | Mandate Creation | `MandateCreationRequestDto` |
| `pain.010.001.07` | Mandate Amendment | `MandateAmendmentRequestDto` |
| `pain.011.001.07` | Mandate Cancellation | `MandateCancellationRequestDto` |
| `pacs.008.001.10` | Credit Transfer | `TransferRequestDto` |
| `pacs.003.001.09` | Customer Direct Debit | `CustomerDirectDebitRequestDto` |
| `pacs.004.001.11` | Payment Return | `PaymentReturnRequestDto` |
| `acmt.023.001.04` | Name Verification | `NameVerificationRequestDto` |
| `camt.060.001.05` | Balance Enquiry | `AccountReportingRequestDto` |

---

## Notes

- The `/generate` endpoints must be stateless — no side effects, no database writes.
- The `/api/` send endpoints should optionally log the execution event to an audit table.
- Both endpoint groups are protected by JWT (`JwtAuthenticationFilter`).
