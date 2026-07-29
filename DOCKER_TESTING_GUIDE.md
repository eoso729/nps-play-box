# NPS Play Box — Docker Testing & Execution Guide

This guide provides step-by-step instructions to run, test, and verify the **NPS Play Box Engine** stack in Docker containers.

---

## 📋 Prerequisites

- **Docker**: Version 20.10+ installed and running.
- **Docker Compose**: Version 2.0+ (`docker compose` command available).
- **Git**: Initialized clean repository without tracked secret files.

---

## 🚀 Quick Start (Docker Environment)

### 1. Environment Configuration

Copy the sample environment file `.env.example` to `.env`:

```bash
cp .env.example .env
```

Review or edit `.env` if you wish to customize database passwords, JWT secrets, or Microsoft Azure AD OAuth keys.

---

### 2. Build & Launch the Containers

Run the following command from the root directory (`/Users/eoso/IdeaProjects/nps-play-box`):

```bash
docker compose up --build -d
```

This starts three services in order:
1. `postgres` (PostgreSQL 16 Database on port `5432`)
2. `backend` (Spring Boot API on port `8080`)
3. `frontend` (React + Nginx Web Portal on ports `80` and `3000`)

---

### 3. Check Container Health & Logs

Check status of all running containers:

```bash
docker compose ps
```

View live logs:

```bash
# All containers
docker compose logs -f

# Backend container logs only
docker compose logs -f backend

# Frontend container logs only
docker compose logs -f frontend

# Database container logs only
docker compose logs -f postgres
```

---

## 🧪 Testing the Phase 1 Implementation

### 1. Access the Frontend Portal

Open your web browser and navigate to:
- `http://localhost` or `http://localhost:3000`

You will see the **NPS Play Box Engine** sign-in portal styled in the dark green design theme (`#0b2818` / `#0f3a22`).

---

### 2. Test User Registration

1. On the frontend portal, click **Register Account** on the toggle bar.
2. Enter registration details:
   - **First Name**: Jane
   - **Last Name**: Developer
   - **Work Email**: `jane.dev@nibss-plc.com.ng`
   - **Organization**: ACME Financial Services
   - **Password**: `Secret123!`
3. Check the **Sandbox Terms of Use** checkbox.
4. Click **Create Account**.
5. **Expected Result**: Account is created via `POST /api/auth/register`, a JWT token is issued, and the Session API Token panel expands showing the Bearer token with a **Copy** button.

---

### 3. Test Credential Login

1. Click **Sign In** on the toggle bar.
2. Enter email/username (`jane.dev@nibss-plc.com.ng`) and password (`Secret123!`).
3. Click **Sign In**.
4. **Expected Result**: Authenticated via `POST /api/auth/login`, JWT Bearer token generated, and token displayed in the session panel.

---

### 4. Test Microsoft OAuth 2.0 / Azure AD SSO

1. On the Sign In screen, click **Sign in with Microsoft**.
2. **Expected Result**: Redirects to Microsoft Azure AD authorization endpoint (`/oauth2/authorization/microsoft`). Upon authentication, callback handles token exchange and redirects back to the portal session.

---

### 5. Backend REST API Direct Testing (cURL / Postman)

#### A. Health Check / Registration Endpoint

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "tester",
    "email": "tester@nibss-plc.com.ng",
    "password": "Password123!",
    "firstName": "Test",
    "lastName": "User",
    "organization": "NIBSS"
  }'
```

**Expected Response (`200 OK`)**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "tester",
    "email": "tester@nibss-plc.com.ng",
    "authProvider": "LOCAL",
    "role": "ROLE_USER"
  }
}
```

#### B. Login Endpoint

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "tester@nibss-plc.com.ng",
    "password": "Password123!"
  }'
```

#### C. Protected Route Test (`GET /api/auth/me`)

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_ACCESS_TOKEN"
```

---

## 🛠 Stopping & Cleaning Up

To stop all running services:

```bash
docker compose down
```

To stop containers and wipe persistent data volumes (resets database):

```bash
docker compose down -v
```

To rebuild containers from scratch without cache:

```bash
docker compose build --no-cache
```
