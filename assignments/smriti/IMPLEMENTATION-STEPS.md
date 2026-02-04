
## Phase 1 — Initial Setup + Foundations (Repo, Tooling, Skeleton)

### Goal (end of week demo)

You can run: **Gateway + Exception Service + Auth Service** locally via Docker, hit a health endpoint, and login returns a JWT.

### Tasks

**Repo + structure**

* Create a mono-repo (or multi-repo) with folders:

    * `/services/auth-service`
    * `/services/exception-service`
    * `/services/audit-service` *(stub for now)*
    * `/gateway`
    * `/frontend`
    * `/infra/local` *(docker compose)*
* Decide build tooling:

    * Backend: Maven (standard) + Java 17/21
    * Frontend: React + TypeScript (Vite)

**Framework setup**

* Spring Boot starters:

    * Web, Validation, Security, Data JPA, Actuator
* Add Swagger/OpenAPI (springdoc) for auto API docs
* Add structured logging + correlationId filter (simple interceptor)

**Local infrastructure**

* Docker Compose: Oracle XE (or Postgres locally if Oracle is heavy—just explain Oracle in interviews), plus services
* Add Flyway/Liquibase (pick one) for migrations

**Definition of Done**

* `POST /auth/login` returns JWT
* `/exceptions/health` works
* OpenAPI UI renders

---

## Phase 2 — Database Schema + Exception CRUD (Queue + Detail)

### Goal (end of week demo)

You can **create exceptions** and view them in a **paginated queue** + **detail view** (backend first; basic frontend list optional).

### Backend milestones

* Implement normalized tables (core):

    * `trade_exception`, `exception_status`, `severity_level`, `exception_category`
* Seed reference data migrations (statuses, severities, categories)
* Implement endpoints:

    * `POST /exceptions`
    * `GET /exceptions` (filters + paging)
    * `GET /exceptions/{id}`

### Data design milestones

* Add indexes for queue queries (status/severity/category/assignee/createdAt)
* Add optimistic locking (`version` field)

### Definition of Done

* Queue filters work (status, severity, category, text search)
* Postman collection can create/search/detail reliably

---

## Phase 3 — Security (JWT + RBAC) + Assignment Workflow

### Goal (end of week demo)

You can login as **OPS vs COMPLIANCE** and see RBAC enforced; OPS can assign; compliance is read-only.

### Backend milestones

* Add user + role tables:

    * `app_user`, `app_role`, `user_role`
* Implement RBAC:

    * Method-level security (e.g., `@PreAuthorize("hasRole('OPS_AGENT')")`)
* Implement assignment:

    * Tables: `exception_assignment`
    * Endpoints:

        * `POST /exceptions/{id}/assignments`
        * `GET /exceptions/{id}/assignments`

### Gateway milestones

* Configure gateway routes:

    * `/api/v1/auth/**` → auth-service
    * `/api/v1/exceptions/**` → exception-service
* Enforce JWT at gateway (optional) or at each service (simpler initially)

### Definition of Done

* OPS can assign; COMPLIANCE gets `403` on restricted endpoints
* Assignment history shows who/when

---

## Phase 4 — Comments + Attachments (Collaboration Features)

### Goal (end of week demo)

Users can collaborate on an exception with **comments**, and attach evidence (metadata + storage pointer).

### Backend milestones

* Comments:

    * Table: `exception_comment`
    * Endpoints:

        * `POST /exceptions/{id}/comments`
        * `GET /exceptions/{id}/comments`
* Attachments (choose approach):

    * **Simple**: store `storage_url` in DB (simulate)
    * **Better**: presigned upload flow

        * `POST /exceptions/{id}/attachments/presign`
        * `POST /exceptions/{id}/attachments` (confirm metadata)
    * Table: `exception_attachment`

### Frontend start (lightweight)

* Implement:

    * Login page
    * Exceptions queue page
    * Exception detail page with comments list + add comment

### Definition of Done

* You can show an exception detail with comments + attachment list
* Evidence metadata persists and is retrievable

---

## Phase 5 — SLA Tracking + Resolution/Reopen State Machine

### Goal (end of week demo)

Exceptions have SLA due times, breach flags, and a clean workflow: **resolve** and **reopen** with validation.

### Backend milestones

* SLA:

    * Table: `exception_sla`
    * Compute `sla_due_at = created_at + severity.sla_minutes`
    * Add a scheduled job to mark breached (or compute on read; but persisted is more demo-friendly)
    * Endpoint: `GET /exceptions/{id}/sla`
    * Add queue filter `isBreached`
* Resolution:

    * Table: `exception_resolution` + lookup `resolution_code`
    * State machine rules:

        * `NEW/IN_PROGRESS/REOPENED` → `RESOLVED`
        * `RESOLVED` → `REOPENED`
    * Endpoints:

        * `POST /exceptions/{id}/resolve`
        * `POST /exceptions/{id}/reopen`

### Frontend milestones

* Add buttons: Assign, Resolve, Reopen (RBAC UI gating)
* Show SLA due time and breach indicator

### Definition of Done

* SLA shows due/breached
* Resolve/Reopen validates status transitions and enforces required fields

---

## Phase 6 — Audit Trail Service + Compliance Search + Exports

### Goal (end of week demo)

You can open an exception and view a **timeline**, and compliance can **search audit events** and export reports.

### Backend milestones

* Audit table: `audit_event`
* Implement audit creation strategy:

    * **Option A (simpler):** exception-service writes audit rows directly
    * **Option B (stronger design):** publish domain events; audit-service consumes and stores
* Endpoints:

    * `GET /exceptions/{id}/audit-events`
    * `GET /audit-events` (filters: eventType, actor, date range, correlationId)
* Export:

    * `GET /reports/exceptions.csv` (or `/reports/exceptions` returning JSON and export in UI)

### Frontend milestones

* Timeline component on exception detail
* Compliance view (read-only):

    * Audit search page
    * Export button

### Definition of Done

* Every key action (create/assign/comment/resolve/reopen) generates an audit event
* Compliance can search and export

---

## Phase 7 — Integration Polish (UI/UX, Metrics, Performance)

### Goal (end of week demo)

The app feels like a real ops console: fast queue, solid filters, KPI cards, and consistent error handling.

### Backend improvements

* Standardize error responses (validation, conflicts, forbidden)
* Add `GET /metrics/exceptions/summary` (counts by status/severity, breached count)
* Improve queue query performance (indexes, query tuning)

### Frontend improvements

* Better filtering UX (saved filters, clear filters)
* KPI cards on top (open, breached, critical)
* Toast notifications and error rendering from API error shape

### Definition of Done

* Smooth end-to-end demo:

    * login → queue → detail → assign/comment/resolve → audit shows changes

---

## Phase 8 — Testing Hardening + Deployment (Docker, K8s, CI/CD)

### Goal (end of week demo)

You can deploy to AWS (or at least a cloud-like environment) and show CI builds + basic delivery pipeline.

### Testing strategy (deliverables)

* **Unit tests (JUnit)**:

    * state transition rules
    * SLA computation logic
    * validation rules
* **Integration tests**:

    * repository tests with Testcontainers (DB)
    * controller tests (MockMvc)
* **Contract-ish tests**:

    * API schemas via OpenAPI
* **Smoke tests**:

    * Postman collection run (newman) in CI

### Deployment strategy (choose one “solo realistic” path)

* **Path A (simpler):** Docker Compose on a single VM (EC2)
* **Path B (stronger):** EKS Kubernetes

    * Helm charts per service
    * Secrets via AWS Secrets Manager
    * Ingress via ALB

### CI/CD

* GitHub Actions:

    * build + test
    * docker build/push to ECR
    * deploy (kubectl/helm) to EKS or ssh-compose to EC2
* Add environment configs:

    * `dev` (local), `staging` (cloud)

### Definition of Done

* A link or recorded demo of cloud deployment
* CI pipeline badge + reproducible deployment steps

---

# Testing Strategy Summary (what you say in interviews)

* “I unit-tested business rules like SLA calculations and workflow transitions.”
* “I used integration tests for repositories/controllers and ran Postman smoke tests in CI.”
* “I focused on preventing regressions in the highest-risk parts: state transitions, RBAC, and queue filters.”

