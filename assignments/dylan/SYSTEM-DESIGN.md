# Consent Vault + Third-Party Access Gateway

## 1) Frontend (React) tech stack + responsibilities

**Tech stack**

* **React + TypeScript**
* **React Router** (routing)
* **State/data:** React Query (or Redux Toolkit if you prefer global state)
* **UI:** Material UI / Ant Design
* **Auth:** OAuth2/OIDC login (e.g., Cognito Hosted UI) → stores access token in memory (preferred) and refreshes via secure flow

**Primary user personas**

1. **Customer** (self-service): grants/revokes consent to a third-party app
2. **Compliance/Admin**: searches audit trails, reviews access attempts, exports reports
3. **Third-Party Developer Portal (optional UI)**: registers apps, gets client credentials, manages redirect URIs

**Frontend responsibilities**

* Consent management UI:

    * View active consents (by third-party, data scopes, expiry)
    * Grant consent (scopes + duration + accounts involved)
    * Revoke consent immediately
* Admin/compliance dashboards:

    * Search audit logs by customer/app/scope/date/result
    * View “access attempt timeline” (allowed/blocked)
    * Export evidence packs (CSV/PDF)
* Optional: app registration screens (if you implement a lightweight developer portal)

---

## 2) Backend structure (services, controllers, layers)

**Backend approach: Spring Boot microservices (2–4 services)**
You can start with 2 services for speed, then split further as an “evolution” story in interviews.

### A) **Consent Service**

**Responsibility:** source of truth for consent lifecycle

* Create consent (grant)
* Update consent version (terms changes)
* Revoke consent
* Check whether consent is valid *now* for a given customer/app/scope/account set

**Key layers**

* **Controller layer**: REST endpoints (`/consents`, `/customers/{id}/consents`)
* **Service layer**: business rules (expiry, scope rules, revocation rules)
* **Repository layer**: Spring Data JPA repositories
* **Domain layer**: entities + value objects (Consent, Scope, ConsentStatus)
* **Security layer**: Spring Security + method security (`@PreAuthorize`)

### B) **Access Gateway / Policy Enforcement Service**

**Responsibility:** enforce consent in front of protected resources
This service behaves like a **policy enforcement point (PEP)**:

* Receives API calls from third parties
* Validates JWT (client identity)
* Calls Consent Service to verify valid consent
* Logs allow/deny decision
* Proxies request to internal resource APIs (or returns policy error)

**Key layers**

* **Controller / Proxy layer**: endpoints that mimic resource APIs (ex: `/openbanking/accounts`, `/transactions`)
* **Policy service**: consent validation, scope checks, rate limiting hooks
* **Audit publisher**: write audit records (DB or async event)

### C) **Audit Service** (can be separate or inside gateway initially)

**Responsibility:** immutable audit log + search/reporting

* Store access attempts and consent events
* Provide query APIs for compliance UI
* Export evidence (batch job)

### D) **Developer Portal Service (optional)**

**Responsibility:** third-party app onboarding

* Register app, redirect URIs, allowed scopes
* Issue client credentials (or integrate with an IdP like Cognito/Keycloak)

**Cross-cutting backend concerns**

* Validation: Bean Validation (Jakarta)
* Mapping: MapStruct (optional)
* Observability: structured logs, metrics (Micrometer), tracing (OpenTelemetry)

---

## 3) Database choice + justification

**Default DB: MySQL (RDS on AWS)**

* Strong relational modeling for consent objects:

    * customers ↔ consents ↔ scopes ↔ accounts
* Good fit for:

    * transactional integrity (grant/revoke must be consistent)
    * indexing for “is consent valid” queries (customer+app+scope+status+expiry)
* Cost-effective + common in enterprise stacks

**Where NoSQL can complement later (optional enhancement)**

* Audit logs can get large. You can start in MySQL, then evolve to:

    * **OpenSearch** for fast search + dashboards
    * or **S3 + Athena** for low-cost long-term audit storage

---

## 4) API gateway / microservices setup

**Recommended setup**

* **External API Gateway** (AWS API Gateway) in front of everything
* Route to:

    * `access-gateway-service` (third-party traffic)
    * `consent-service` (customer/admin traffic)
    * `audit-service` (admin traffic)

**Why this setup fits**

* Separation of concerns:

    * Consent is authoritative
    * Gateway enforces policy
    * Audit is immutable + reportable
* Scales independently:

    * Access gateway needs higher throughput
    * Consent service needs strong consistency
    * Audit service needs storage/search optimization

**Service-to-service communication**

* Synchronous REST calls (simple + easy to explain)
* Optional improvement:

    * publish audit events asynchronously (SQS/SNS or Kafka) to decouple gateway from audit storage

---

## 5) Deployment environment (AWS + Docker + Kubernetes + CI/CD)

**Containerization**

* Each service packaged as a **Docker image**
* Config via environment variables / AWS Secrets Manager

**Runtime**

* **EKS (Kubernetes)** for orchestration 
* Alternatives you can mention:

    * ECS Fargate for simpler ops
    * CloudWatch logs + X-Ray tracing

**Core AWS services**

* **EKS** (or ECS)
* **RDS MySQL**
* **API Gateway** (edge routing)
* **Cognito** (OIDC login) *or* Keycloak if self-managed
* **Secrets Manager / Parameter Store**
* **CloudWatch** (logs/metrics)
* **S3** (evidence pack exports)

**CI/CD (GitHub Actions or Jenkins)**

* Pipeline stages:

    1. Lint + unit tests
    2. Build JARs (Maven/Gradle)
    3. Build Docker images
    4. Push to ECR
    5. Deploy to EKS (Helm/Kustomize)
    6. Run smoke tests (hit health endpoints + basic flows)

---

## 6) How components interact (end-to-end flows)

### Flow A: Customer grants consent

1. Customer logs into React UI via **OIDC** (Cognito)
2. UI calls **Consent Service** `POST /consents`
3. Consent Service stores:

    * consent record (status=ACTIVE, expiry, version)
    * consent scopes + target accounts
4. Consent Service emits an **audit event** (“CONSENT_GRANTED”)

### Flow B: Third-party calls an API (policy enforcement)

1. Third-party app calls **API Gateway** → routed to **Access Gateway Service**
2. Access Gateway:

    * validates JWT (client identity + requested scopes)
    * calls Consent Service `GET /consents/validate?customerId=...&clientId=...&scopes=...`
3. If valid:

    * logs “ALLOW”
    * forwards/proxies to internal resource service (or returns mock data in MVP)
4. If invalid:

    * returns **403** with reason (`CONSENT_MISSING`, `CONSENT_EXPIRED`, `SCOPE_NOT_GRANTED`)
    * logs “DENY” to Audit

### Flow C: Compliance searches audit evidence

1. Admin logs into React Admin UI
2. UI calls Audit Service `GET /audit-events?...filters...`
3. Audit Service returns paginated events + optional export job:

    * `POST /audit-exports` → generates file → stores in S3 → returns download link
