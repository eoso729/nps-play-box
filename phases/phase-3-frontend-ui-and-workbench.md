# Phase 3: Frontend UI — ISO 20022 Message Workbench

## Goal

Build a focused developer workbench UI for constructing, previewing, and dispatching ISO 20022 payment messages. The interface separates generation from dispatch, and provides a standalone XML diff tool for pre-send validation.

---

## Technology Stack

| Concern | Pick | Rationale |
| :--- | :--- | :--- |
| **Framework** | React 18 + TypeScript, bundled with Vite | Fast dev loop, no SSR overhead needed |
| **Routing** | React Router | Login → Dashboard → nested message-type routes |
| **State / Data Fetching** | TanStack Query (React Query) | The generate → sign → submit pipeline maps naturally to distinct async mutations with per-stage loading and error states |
| **Forms** | React Hook Form + Zod | The message configurator is a large multi-section form requiring per-field validation (IBAN format, BVN/NIN length, required fields) — RHF + Zod keeps it fast and fully typed |
| **Styling** | Tailwind CSS | Utility-driven; keeps the design token system (dark palette, emerald/indigo accents) consistent via CSS variables and Tailwind theme config |
| **Syntax Highlighting** | Shiki or Prism (`react-syntax-highlighter`) | Used for the three XML output panes with line numbers |
| **HTTP Client** | Axios or native `fetch` wrapped in a thin client module | Attaches JWT from Spring Security automatically via request interceptor |
| **Auth** | JWT stored in memory + `httpOnly` refresh cookie | Avoids `localStorage` for the access token; refresh token handled server-side via Spring Security |

---

## Layout Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  Header: NPS Play Box Engine    |   Gateway: NIBSS Sandbox   |   [User]  [Logout] │
├───────────────────┬─────────────────────────────────────────────────────────────┤
│                   │                                                             │
│  Message          │   [ Form Configurator Panel ]                               │
│  Navigator        │   Fieldsets for selected message type                       │
│                   │                                                             │
│  pain.013         ├─────────────────────────────────────────────────────────────┤
│  pain.001         │                                                             │
│  pain.009         │   [ Output Inspector ]                                      │
│  pain.010         │   Tabs: Plain XML | Signed XML | Service Response           │
│  pain.011         │                                                             │
│  pain.008         ├─────────────────────────────────────────────────────────────┤
│  pacs.008         │                                                             │
│  pacs.003         │   [ XML Diff Checker ]                                      │
│  pacs.004         │   Left: Generated Plain XML | Right: Pasted XML             │
│  acmt.023         │   Diff highlighted inline                                   │
│  camt.060         │                                                             │
│                   │                                                             │
└───────────────────┴─────────────────────────────────────────────────────────────┘
```

---

## Component Breakdown

### 1. Header & Navigation Bar

- Application title: "NPS Play Box Engine".
- Environment indicator badge: "NIBSS Sandbox" / "Production" (togglable, visually distinct).
- Session status: JWT validity, username, logout action.
- No emojis. Use minimal 1.5px stroke SVG icons (Lucide / Feather style) only where they clarify an action.

---

### 2. Message Type Navigator (Left Sidebar)

- Vertical list of supported ISO 20022 message specifications.
- Each entry displays: short label ("Payment Activation"), message code badge (`pain.013`).
- Active selection highlighted with left-border accent and background tint.
- Grouped by domain (Payment, Mandate, Debit, Account Services) with a small uppercase label separator — no numbered prefixes unless they carry meaning.

---

### 3. Form Configurator Panel

Dynamic fieldset driven by the selected message type. Fields map 1-to-1 with backend DTO properties.

**Fieldset Groups (example for `pain.013`):**

- **Group Header**
  - Message ID (`MsgId`) — auto-generated, editable override
  - Creation Date & Time (`CreDtTm`) — datetime picker, defaults to now
  - Initiating Party Name, Client ID

- **Payment Information**
  - Payment Info ID, Requested Execution Date
  - Debtor Name, Account Number (IBAN), Currency, Account Name
  - Debtor Agent BIC, Member ID

- **Transaction & Creditor Details**
  - End-to-End Reference, Instructed Amount, Currency
  - Creditor Name, Creditor IBAN, Creditor Account Name
  - Creditor Agent BIC, Member ID, Purpose

- **Supplementary Verification Data (NIBSS)**
  - Account Designation, ID Type (BVN / NIN), ID Value, Account Tier
  - Biometric Data, Address, Phone, Email (optional fields)
  - Creditor Info equivalents
  - Transaction Location coordinates, Channel Code, Mandate Category

**Action Bar (positioned below the form):**

| Action | Behaviour |
| :--- | :--- |
| Load Pre-filled Sample Data | Populates all fields with spec-compliant test values |
| Generate XML | Calls `/api/generate/...` — populates Plain XML and Signed XML tabs only. No network send. |
| Send Request | Calls `/api/...` full pipeline — populates all three output tabs including Service Response. |

The "Generate XML" and "Send Request" actions are visually distinct: Generate uses an outlined secondary style; Send uses a solid primary style.

---

### 4. Output Inspector Panel (3-Tab Viewer)

Positioned below or beside the form. Tabs switch between the three output stages.

**Tab 1: Plain XML**
- Syntax-highlighted, formatted ISO 20022 XML.
- Populated after both "Generate XML" and "Send Request" actions.
- Per-pane actions: Copy, Download `.xml`, Expand to full screen.

**Tab 2: Signed XML**
- Syntax-highlighted PKCS#7 / XMLDSig signed XML payload.
- Signature metadata row: Algorithm label, Key alias, Validity indicator ("Signature: Verified" / "Signature: Invalid").
- Populated after both "Generate XML" and "Send Request" actions.
- Per-pane actions: Copy, Download `.xml`, Expand.

**Tab 3: Service Response**
- Only populated after "Send Request".
- HTTP status pill: `200 OK` (emerald), `4xx` (amber), `5xx` (crimson).
- Execution latency badge: e.g. `142 ms`.
- Formatted raw response body (XML or JSON) from NIBSS/NPS service.
- Per-pane actions: Copy, Download.
- When empty (before a send is triggered): "No service response yet. Use Send Request to dispatch the signed XML." — not a vague blank state.

---

### 5. XML Diff Checker (Standalone Section / Tab)

A dedicated tool for validating generated XML against an external reference. Accessible from the sidebar or as a top-level section.

**Layout:**

```
┌──────────────────────────────────┬──────────────────────────────────┐
│  Generated Plain XML             │  Reference XML (Paste Here)      │
│  (auto-populated from last       │  Free-text area for user to      │
│   Generate or Send action)       │  paste external XML              │
│                                  │                                  │
│  Read-only, syntax-highlighted   │  Editable, syntax-highlighted    │
└──────────────────────────────────┴──────────────────────────────────┘
[ Run Diff ]                               [ Clear Reference ]
─────────────────────────────────────────────────────────────────────
  Diff Output — unified diff view with line-level addition/deletion
  highlighting. Matching lines shown in neutral. Changed lines in
  amber (modified), crimson (removed), emerald (added).
```

**Behaviour:**
- "Run Diff" triggers a client-side XML diff computation (e.g. using a JS diffing library such as `diff` or `jsdiff`).
- Diff output is rendered inline with line numbers.
- XML is normalized (pretty-printed, whitespace-trimmed) before diffing to avoid cosmetic noise.
- If no generated XML is present yet, prompt: "Generate or send a message first to populate the left pane."

---

## Design Tokens

| Token | Value | Usage |
| :--- | :--- | :--- |
| Background canvas | `#0b0f19` | Page background |
| Panel surface | `#111827` | Cards, sidebars, panes |
| Border | `#1f2937` | All 1px structural dividers |
| Text primary | `#f9fafb` | Headings, labels |
| Text muted | `#6b7280` | Subtitles, placeholders |
| Accent indigo | `#6366f1` | Active nav, signed XML badge |
| Accent emerald | `#10b981` | Success, 200 OK, Generate action |
| Accent amber | `#f59e0b` | Warning, modified diff lines |
| Accent crimson | `#ef4444` | Error, deleted diff lines |
| Code font | `IBM Plex Mono` or `JetBrains Mono` | All XML/code panes |
| UI font | `Inter` or `Plus Jakarta Sans` | All labels, forms, nav |
