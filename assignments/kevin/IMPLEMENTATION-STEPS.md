## Phase 1 — Initial Setup (repo, frameworks, tooling)

**Goal:** working skeleton + CI + local environment.

* **Repo**

    * Monorepo (recommended): `/backend` + `/frontend` + `/infra`
    * Branch strategy: `main` + `dev` + short-lived feature branches
* **Backend setup**

    * Spring Boot 3, Java 17/21
    * Modules (start as modular monolith to move fast): `claims`, `fraud`, `cases`, `audit`, `auth`
    * Dependencies: Spring Web, Spring Security, Validation, Spring Data JPA, Flyway, Lombok, Postgres driver, Actuator, OpenAPI (springdoc)
* **Frontend setup**

    * React + TS (or Angular if you prefer)
    * Router + UI kit (MUI/Material)
    * API client scaffolding (Axios/fetch wrapper)
* **Tooling**

    * Docker Compose: Postgres + backend + frontend
    * Code quality: Spotless/Checkstyle, ESLint/Prettier
    * GitHub Actions: build + unit tests on PR
* **Deliverable:** “Hello world” UI + backend health endpoint + DB migrations running locally.

---

## Phase 2 — Auth + RBAC + Base Entities

**Goal:** secure endpoints and role-based screens.

* **Backend**

    * JWT auth (login endpoint + token validation filter)
    * RBAC roles: `ADJUSTER`, `INVESTIGATOR`, `ADMIN`
    * Core schema + migrations: `users`, `roles`, `user_roles`
    * Seed data for demo accounts
* **Frontend**

    * Login page
    * Route guards / protected routes
    * Basic layout + navigation based on role
* **Deliverable:** login works, roles enforced, user sees correct navigation.

---

## Phase 3 — Claims Service (CRUD + search + status history)

**Goal:** adjuster can create and manage claims.

* **Backend**

    * Endpoints:

        * `POST /claims`, `GET /claims/{id}`, `GET /claims` (search/paginate)
        * `PATCH /claims/{id}/status`
        * `GET /claims/{id}/history`
    * Tables + migrations: `policies` (minimal), `claimants`, `claims`, `claim_status_history`
    * Validation + error handling (global exception handler)
* **Frontend**

    * Claim create form (minimal fields)
    * Claim list + filters + claim detail screen
* **Deliverable:** claims fully usable end-to-end.

---

## Phase 4 — Document Upload (metadata + presigned URL flow)

**Goal:** attach evidence to claims.

* **Backend**

    * `POST /claims/{id}/documents` returns upload instructions (presigned URL stub or local storage for dev)
    * `GET /claims/{id}/documents`
    * `GET /documents/{docId}/download-url`
    * Table: `claim_documents`
* **Frontend**

    * Upload UI on claim detail
    * Document list + download button
* **Deliverable:** users upload and retrieve documents tied to a claim.

---

## Phase 5 — Fraud Scoring v1 (rules engine + explanations)

**Goal:** generate fraud score + reasons and show them.

* **Backend**

    * Fraud scoring logic (rules-based):

        * Example rules: high loss amount, repeat claims in window, mismatch indicators, duplicate address/phone (basic)
    * Endpoints:

        * `GET /fraud/claims/{claimId}/score`
        * `POST /fraud/claims/{claimId}/rescore`
    * Tables: `fraud_scores`, `fraud_signals`
    * Trigger scoring:

        * fastest: score synchronously after claim create/update
        * better: async via in-app event publisher (still same repo)
* **Frontend**

    * Display fraud score + risk level + reasons on claim detail
* **Deliverable:** every claim has a score + “why” list.

---

## Phase 6 — Case Management (queues, assignment, notes, status workflow)

**Goal:** investigators get a triage queue and can work cases.

* **Backend**

    * Auto-create case when score ≥ threshold
    * Endpoints:

        * `GET /cases` (queue filters, sort by score/SLA)
        * `GET /cases/{caseId}`
        * `POST /cases/{caseId}/assignments`
        * `POST /cases/{caseId}/notes`
        * `PATCH /cases/{caseId}/status`
    * Tables: `investigation_cases`, `case_assignments`, `case_notes`, `case_status_history`
* **Frontend**

    * Investigator queue screen (filters + sort)
    * Case detail with assignment + notes + status actions
* **Deliverable:** full triage workflow works end-to-end.

---

## Phase 7 — Integration polish (gateway-style routing + audit events)

**Goal:** make it feel “enterprise”: consistent APIs, auditability.

* **Backend**

    * Add “API Gateway” style routing (optional): Spring Cloud Gateway OR keep as single backend with consistent `/api/v1`
    * Implement audit logging for:

        * claim created/status changed
        * document uploaded
        * case assigned/status changed
        * fraud rescored
    * Endpoints:

        * `GET /audit/events` with filters
    * Table: `audit_events`
* **Frontend**

    * Audit panel on claim/case detail (read-only)
    * Better UX: loading states, empty states, form validation messages
* **Deliverable:** traceability + clean end-to-end story for interviews.

---

## Phase 8 — Testing strategy hardening + Deployment

**Goal:** production-ready demo deployment + confidence.

### Testing Strategy (implement throughout; finish here)

* **Unit tests (fast)**

    * Services: scoring logic, status transition rules, permission checks
* **Repository tests**

    * JPA slice tests for key queries (queue filters, history)
* **API integration tests**

    * Spring Boot `@SpringBootTest` + Testcontainers (Postgres)
    * Auth + RBAC tests: 401/403 coverage
* **Frontend tests**

    * Component tests for key screens (React Testing Library / Angular TestBed)
    * Mock API tests for filters and role-based UI
* **Contract sanity**

    * Generate OpenAPI and verify examples match (smoke)

### Deployment

* **Option A (fastest demo)**

    * Backend: Render/Fly.io + managed Postgres
    * Frontend: Vercel/Netlify
* **Option B (matches resume: AWS)**

    * Backend: ECS Fargate + RDS Postgres
    * Docs: S3
    * Frontend: S3 + CloudFront (or Amplify)
* **CI/CD**

    * Build, test, push image, deploy on merge to `main`
* **Deliverable:** public demo URL + README with architecture diagram + sample credentials.

---

## What to say in interviews (how this roadmap helps)

* You can clearly describe **incremental delivery**, **security first**, **auditability**, and **scalability path** (modular monolith → microservices/events later).
