# Phase 4: Testing, Verification & Validation

## Goal

Validate the full system end-to-end across all three operational modes: XML generation only, full request send, and XML diff checking.

---

## Verification Matrix

### 1. Authentication & Registration

| Test | Expected Outcome |
| :--- | :--- |
| Register with valid email + strong password | User created, JWT returned, redirect to dashboard |
| Register with duplicate email | `400 Bad Request` with clear field-level error message |
| Login with correct credentials | JWT issued, session persisted in storage |
| Login with incorrect password | `401 Unauthorized`, no token issued |
| Access protected `/api/**` route without token | `401 Unauthorized` |
| Sign in with Microsoft (valid Azure AD account) | OAuth flow completes, user upserted, JWT issued, redirect to dashboard |
| Sign in with Microsoft (account not in allowed tenant) | `403 Forbidden` or configured error redirect |

---

### 2. XML Generator Pipeline (Generate-Only)

**Test case: `pain.013` Payment Activation**

1. Fill form fields: Debtor BVN `11111111145`, Amount `3800.00`, Initiating Party `Ponmile Joy`, Creditor Agent BIC `999997`.
2. Click **Generate XML**.
3. Verify Plain XML tab:
   - Contains correct namespace: `xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:pain.013.001.11"`.
   - `<MsgId>` is non-empty and matches `messageId` in response.
   - `<InstdAmt Ccy="NGN">3800.00</InstdAmt>` present.
   - `<IdValue>11111111145</IdValue>` appears under `DebtorInfo`.
4. Verify Signed XML tab:
   - XML is wrapped with XMLDSig / PKCS#7 signature elements.
   - "Signature: Verified" badge is shown.
5. Verify Service Response tab:
   - Shows "No service response yet. Use Send Request to dispatch the signed XML." — **no network call was made**.

---

### 3. Full Send Pipeline

1. With form populated, click **Send Request**.
2. Verify all three output tabs are populated.
3. Verify Service Response tab shows:
   - HTTP `200 OK` pill.
   - Execution latency in milliseconds.
   - Non-empty raw response body from NIBSS/NPS service.
4. Test error path: set execution date to a past date on `pain.013`.
   - Verify crimson `5xx` status pill.
   - Verify raw error response body rendered in Service Response pane.
   - Verify error is specific, not vague.

---

### 4. XML Diff Checker

| Test | Expected Outcome |
| :--- | :--- |
| Generate XML, paste identical XML in right pane, run diff | Zero differences shown — clean state indicator |
| Generate XML, paste XML with a changed `<MsgId>` value, run diff | Line containing `<MsgId>` highlighted as modified (amber) |
| Generate XML, paste XML with an extra element, run diff | Added line shown in emerald |
| Generate XML, paste XML missing an element, run diff | Removed line shown in crimson |
| Run diff with no generated XML | Clear prompt: "Generate or send a message first to populate the left pane." |
| Run diff with no reference XML pasted | Prompt: "Paste a reference XML document in the right pane to compare." |
| Whitespace-only difference | Diff normalises whitespace before comparison — no spurious differences flagged |

---

### 5. Structural & Security Validation

- All `/api/**` and `/api/generate/**` endpoints reject unauthenticated requests.
- `/api/generate/**` endpoints produce no audit log entries and make zero outbound network calls (verifiable via test mocking of `CurlSender`).
- OAuth2 callback URL (`/login/oauth2/code/microsoft`) is not accessible without a valid state parameter (CSRF-safe).
- JWT expiry is enforced: expired tokens receive `401 Unauthorized`.
