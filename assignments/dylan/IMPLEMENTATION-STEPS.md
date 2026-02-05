
# Phase 1 — Initial Setup (Repo, Frameworks, Tooling)

**Goal:** working skeleton with auth, DB, and deployable containers.

**Backend setup**

* Create mono-repo (or 2 repos) with:

    * `consent-service` (Spring Boot)
    * `access-gateway-service` (Spring Boot)
    * *(optional later)* `audit-service` (can start embedded in gateway)
* Add dependencies (no code yet, just wiring):

    * Spring Web, Spring Security, Spring Data JPA, Validation
    * Flyway (schema migrations)
    * Actuator (health endpoints)
    * Test: JUnit5, Mockito, Testcontainers (add now even if used later)

**Frontend setup**

* Create `consent-ui` using **React + TypeScript**
* Add:

    * Router, React Query, basic UI library (MUI/AntD)
    * Auth client placeholder (mock login initially)

**Infrastructure/tooling**

* Local dev via Docker Compose:

    * MySQL container
    * consent-service + access-gateway-service
* Add consistent config patterns:

    * `.env` file, Spring profiles (`local`, `docker`, `prod`)
* CI baseline:

    * GitHub Actions pipeline: build + run unit tests

**Deliverables**

* Services start up successfully
* `/actuator/health` works for both
* DB migrations run on startup (empty schema is fine)

---

# Phase 2 — Core Data Model + Consent CRUD (Backend MVP)

**Goal:** “Consent Vault” works in isolation.

**Build**

* Implement normalized schema (customers, apps, scopes, consents, consent_scopes, accounts, consent_accounts)

    * Use Flyway migrations
* Implement Consent APIs (MVP endpoints):

    * `POST /api/v1/consents` (grant)
    * `GET /api/v1/customers/{id}/consents` (list)
    * `GET /api/v1/consents/{id}` (detail)
    * `POST /api/v1/consents/{id}/revoke`
* Add basic validations:

    * expiresAt must be future
    * scopes must be non-empty
    * prevent duplicate ACTIVE consent for same customer+app+scopes (your rule)

**Hints to research**

* Spring Boot `@Entity`, `@ManyToMany` vs join entities
* Flyway versioned migrations
* `@Valid` + Bean Validation annotations

**Deliverables**

* Postman collection proves CRUD works
* DB is normalized and populated with seed data for apps/scopes

---

# Phase 3 — Consent Validation API + Policy Rules

**Goal:** gateway can ask “Is this request allowed?”

**Build**

* Add `GET /api/v1/consents/validate` in consent-service
* Implement the rules:

    * consent must be ACTIVE
    * expiresAt > now
    * requested scopes must be subset of granted scopes
    * requested accountId(s) must be included if you enforce account-level consent
* Add “deny reason” taxonomy:

    * `CONSENT_MISSING`, `CONSENT_EXPIRED`, `SCOPE_NOT_GRANTED`, `ACCOUNT_NOT_GRANTED`

**Design for interview strength**

* Add indexing strategy (customer_id + app_id + status + expires_at)
* Add correlation id logging (`X-Request-Id`)

**Deliverables**

* One endpoint answers allow/deny clearly and consistently
* Unit tests for each rule/edge case

---

# Phase 4 — Access Gateway Service (End-to-End Enforcement)

**Goal:** third-party calls go through gateway and are enforced.

**Build**

* Implement protected gateway endpoints (MVP):

    * `GET /open-banking/v1/accounts`
    * `GET /open-banking/v1/accounts/{accountId}/transactions`
* JWT validation (simple version):

    * Accept JWT and extract `client_id` + scopes (can mock claims locally)
    * Enforce that token scopes include the resource scope request *and* consent is valid
* Implement call to consent-service validate endpoint (REST)
* Return correct HTTP outcomes:

    * 401 invalid token
    * 403 valid token but consent missing/invalid
* Response data:

    * For MVP, serve from local DB tables (`accounts`) and a simple `transactions` table or mock list

**Deliverables**

* Demo script:

    1. Create consent
    2. Call `/accounts` allowed
    3. Revoke consent
    4. Same call now denied (403 with reason)

---

# Phase 5 — Audit Logging + Admin Search

**Goal:** compliance story becomes real.

**Build**

* Add `audit_events` table + write events on:

    * consent grant/revoke
    * gateway allow/deny decisions
* Implement admin audit endpoint (MVP):

    * `GET /api/v1/audit-events?customerId&appClientId&decision&from&to&page&size`
* Add role-based access:

    * only admin role can query audit
* Add pagination + sorting by time desc

**Hints to research**

* Spring Data pagination (`Pageable`)
* Method security (`@PreAuthorize`)

**Deliverables**

* Admin can search and see full allow/deny timeline with request ids

---

# Phase 6 — Frontend Integration (Customer + Admin UI)

**Goal:** usable UI that demonstrates end-to-end flows.

**Customer UI**

* Login (simple mock or real OIDC if ready)
* Pages:

    * “My Consents” list
    * “Grant Consent” form (app + scopes + expiry + account selection)
    * “Revoke” action

**Admin UI**

* Audit search screen with filters
* Audit details modal (shows requestId, reason, scopes)

**Deliverables**

* Screen recording demo: grant → access allowed → revoke → access denied → audit shows both

---

# Phase 7 — Testing Strategy Upgrade (Confidence + Interview Credibility)

**Goal:** show real engineering maturity.

**Backend tests**

* Unit tests:

    * consent validation rules (most important)
    * revoke behavior idempotency (revoking twice)
* Integration tests (Testcontainers):

    * spin up MySQL container
    * run migrations
    * test “grant → validate → revoke → validate”
* Contract-ish tests:

    * gateway calling consent validate (mock server or wiremock)

**Frontend tests**

* Minimal but credible:

    * component tests for consent form validation
    * API mocking with MSW (optional)

**Deliverables**

* CI runs tests automatically and blocks merges on failures

---

# Phase 8 — Deployment (AWS + Docker + Kubernetes) + Hardening

**Goal:** “production-ready story”: deployable, observable, secure.

**Deployment**

* Docker images pushed to ECR
* Deploy to:

    * **EKS** (Helm charts) or ECS Fargate (simpler)
* RDS MySQL (or Aurora MySQL)
* API Gateway (optional) or ALB Ingress for EKS

**Operational hardening**

* Secrets in AWS Secrets Manager
* Rate limiting (basic)
* Observability:

    * structured logs
    * Actuator metrics
* Data retention policy for audit logs (document it)

**Deliverables**

* Live URL endpoints + basic health checks
* README with architecture + runbook

---

# Optional “Stretch Phases” (If you want extra differentiation)

### Phase 9 — Evidence Export Packs

* `POST /api/v1/audit-exports` async job
* Store export in S3, return signed URL

### Phase 10 — Developer Portal & App Onboarding

* Register third-party apps + redirect URIs
* Allowed scopes per app

---

## How to present this in interviews (simple narrative)

* **Phases 1–3:** built a robust consent core + validation rules
* **Phase 4:** added a policy enforcement gateway for real access control
* **Phases 5–6:** delivered auditability + UI workflows
* **Phases 7–8:** proved quality with tests + deployed with AWS + containers
