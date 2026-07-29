# Phase 1: Authentication, Registration & Identity Management

## Goal

Build a multi-mode authentication system that supports:
- Standard credential-based registration and login (email + password)
- Microsoft OAuth 2.0 / Azure AD SSO for enterprise users
- JWT session management with role-based access control across all protected API routes

---

## Backend Tasks

### 1. User Entity & Repository

- `User` model fields: `id`, `username`, `email`, `passwordHash` (nullable for OAuth users), `authProvider` (`LOCAL` | `MICROSOFT`), `microsoftOid` (nullable), `role` (`ADMIN` | `USER`), `createdAt`, `lastLoginAt`.
- `UserRepository` with Spring Data JPA.

### 2. Credential-Based Auth Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new user with email and password |
| `POST` | `/api/auth/login` | Authenticate and issue JWT |
| `GET` | `/api/auth/me` | Return current session details from JWT |

- DTOs: `RegisterRequestDto` (`username`, `email`, `password`), `LoginRequestDto` (`email`, `password`)
- Response: `AuthResponseDto` (`accessToken`, `tokenType: Bearer`, `expiresIn`, `user`)

**Registration Logic:**
1. Validate uniqueness of `email`.
2. Hash password with BCrypt.
3. Persist user with `authProvider: LOCAL`.
4. Issue JWT and return `AuthResponseDto`.

### 3. Microsoft OAuth 2.0 / Azure AD SSO

- Dependency: `spring-boot-starter-oauth2-client` + `spring-security-oauth2-jose`.
- Configuration: `application.yml` Azure AD client registration (`client-id`, `client-secret`, `tenant-id`).
- Endpoints:
  - `GET /oauth2/authorization/microsoft` — Redirect user to Microsoft login.
  - `GET /login/oauth2/code/microsoft` — OAuth2 callback, handled by Spring Security.
- Post-authentication `OAuth2SuccessHandler`:
  1. Extract `oid`, `email`, `name` from Microsoft ID token claims.
  2. Upsert user (`authProvider: MICROSOFT`, `microsoftOid`).
  3. Issue application-level JWT and redirect to frontend with token in query param or secure cookie.

### 4. Spring Security Configuration

- `SecurityFilterChain`:
  - Permit: `/api/auth/**`, `/oauth2/**`, `/login/oauth2/**`, static frontend assets.
  - Protect: `/api/**` with `JwtAuthenticationFilter`.
- `JwtAuthenticationFilter`: validate bearer token on all protected routes, attach `SecurityContext`.
- `CorsConfig`: whitelist frontend origin and expose `Authorization` response header.

---

## Frontend Requirements

### Auth Views

1. **Login Screen**
   - Email + Password inputs with inline validation.
   - "Sign in with Microsoft" button — opens Microsoft OAuth popup or redirect.
   - Link to registration screen.

2. **Registration Screen**
   - Username, Email, Password, Confirm Password inputs.
   - Password strength indicator.
   - Inline validation (email format, password match, minimum length).
   - On success: auto-login and redirect to dashboard.

3. **Session Persistence**
   - Store JWT in `localStorage` (or HTTP-only cookie if backend sets it).
   - Auto-redirect authenticated users away from auth screens.
   - Token expiry check on app load with automatic logout.

### Microsoft OAuth Button Design

- Follows Microsoft brand guidelines: white button, Microsoft logo SVG, sentence case label "Sign in with Microsoft".
- No emoji or custom icon substitution.
