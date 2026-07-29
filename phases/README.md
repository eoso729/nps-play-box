# NPS Play Box — Implementation Roadmap

This directory contains the detailed technical breakdown for each implementation phase, covering both the Spring Boot backend and the frontend workbench UI.

---

## Phase Summary

| Phase | File | Covers |
| :--- | :--- | :--- |
| Phase 1 | [phase-1-authentication-and-security.md](file:///Users/eoso/IdeaProjects/nps-play-box/phases/phase-1-authentication-and-security.md) | Registration, login, Microsoft OAuth 2.0 / Azure AD SSO, JWT session management |
| Phase 2 | [phase-2-backend-api-and-xml-pipeline.md](file:///Users/eoso/IdeaProjects/nps-play-box/phases/phase-2-backend-api-and-xml-pipeline.md) | Split API design: generate-only (`/api/generate/`) vs. full send (`/api/`), unified DTOs for all 11 message types |
| Phase 3 | [phase-3-frontend-ui-and-workbench.md](file:///Users/eoso/IdeaProjects/nps-play-box/phases/phase-3-frontend-ui-and-workbench.md) | Message form configurator, 3-tab output inspector (Plain XML / Signed XML / Service Response), XML diff checker |
| Phase 4 | [phase-4-testing-and-verification.md](file:///Users/eoso/IdeaProjects/nps-play-box/phases/phase-4-testing-and-verification.md) | End-to-end verification matrix for auth, XML generator, send pipeline, diff checker, and security |

---

## Frontend Technology Stack

| Concern | Choice |
| :--- | :--- |
| Framework | React 18 + TypeScript, Vite |
| Routing | React Router |
| State / Data Fetching | TanStack Query (React Query) |
| Forms | React Hook Form + Zod |
| Styling | Tailwind CSS |
| Syntax Highlighting | Shiki or Prism (`react-syntax-highlighter`) |
| HTTP Client | Axios / native `fetch` with JWT interceptor |
| Auth Token Storage | JWT in memory + `httpOnly` refresh cookie |

---

## Core Architecture

### Authentication Modes

```
Credential Login    ──> POST /api/auth/login  ──> JWT
Registration        ──> POST /api/auth/register ──> JWT
Microsoft OAuth     ──> GET  /oauth2/authorization/microsoft
                        GET  /login/oauth2/code/microsoft ──> JWT
```

### Message Processing: Two Distinct Pipelines

```
Fill Form Fields
      │
      ├──[ Generate XML ]──> POST /api/generate/{messageType}
      │                            │
      │                            ├── plainXml
      │                            └── signedXml
      │                           (no network dispatch)
      │
      └──[ Send Request ]──> POST /api/{messageType}
                                   │
                                   ├── plainXml
                                   ├── signedXml
                                   └── serviceResponse
                                        ├── statusCode
                                        ├── rawResponseBody
                                        └── executionTimeMs
```

### XML Diff Checker (Client-Side)

```
[ Generated Plain XML ] <── auto-populated from Generate or Send action
[ Reference XML       ] <── user pastes external XML

[ Run Diff ] ──> client-side normalise (pretty-print + trim whitespace)
                 ──> unified diff output with line-level highlighting
```

---

## Supported ISO 20022 Message Types

| Code | Description |
| :--- | :--- |
| `pain.013.001.11` | Payment Activation Request |
| `pain.001.001.11` | Payment Initiation |
| `pain.008.001.10` | Direct Debit Request |
| `pain.009.001.07` | Mandate Creation |
| `pain.010.001.07` | Mandate Amendment |
| `pain.011.001.07` | Mandate Cancellation |
| `pacs.008.001.10` | Credit Transfer |
| `pacs.003.001.09` | Customer Direct Debit |
| `pacs.004.001.11` | Payment Return |
| `acmt.023.001.03` | Name Verification |
| `camt.060.001.05` | Balance Enquiry |
