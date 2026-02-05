## Phase 1 — Initial Setup (Repo, Frameworks, Tooling)

### Goals (end of phase demo)

* You can run **backend + MySQL + frontend** locally with one command.
* JWT-secured “hello” endpoint + basic React app shell.

### Tasks

**Repo**

* Create a mono-repo with clear structure:

    * `/backend` (Spring Boot)
    * `/frontend` (React + TS)
    * `/infra` (docker-compose, k8s manifests later)
    * `/docs` (architecture + API notes)

**Backend setup**

* Spring Boot starters: Web, Validation, Security, Data JPA, Actuator
* MySQL connector, Flyway/Liquibase (choose one)
* Basic project standards:

    * global exception handler
    * request logging + correlation-id filter
    * OpenAPI/Swagger for API docs

**Frontend setup**

* React + TypeScript, routing, layout, auth placeholder
* API client wrapper (fetch/axios) with base URL + auth header support

**Local tooling**

* Docker Compose: MySQL + backend (+ optional frontend)
* Code quality: Spotless/Checkstyle, basic linting in frontend
* GitHub Actions (or Jenkins) skeleton pipeline: build + unit tests

---

## Phase 2 — Database + Core Case Domain (Create + Read)

### Goals (demo)

* Create a case, view case details, list cases (simple queue).

### Tasks

**Database**

* Implement the core tables:

    * `dispute_cases`
    * `dispute_reason_codes`
    * `case_status_history`
    * `users/roles/user_roles` (minimal)
* Seed reference data: reason codes + a few users/roles

**Backend**

* Implement:

    * `POST /cases` (create)
    * `GET /cases/{id}` (read)
    * `GET /cases` (basic list/pagination)
* Add server-side validation + consistent error responses

**Frontend**

* Minimal screens:

    * Case Intake form (basic fields)
    * Case list page
    * Case detail page (header fields only)

---

## Phase 3 — Workflow + Status Transitions + Timeline

### Goals (demo)

* Move a case through statuses with role checks and see a timeline.

### Tasks

**Backend**

* Add workflow endpoints:

    * `POST /cases/{id}/actions/start-review`
    * `POST /cases/{id}/actions/initiate-chargeback`
    * `POST /cases/{id}/actions/close`
* Enforce rules:

    * allowed transitions table/enum
    * `@PreAuthorize` checks
    * optimistic locking (`version` field)
* Expose timeline:

    * `GET /cases/{id}/status-history`

**Frontend**

* Case detail page:

    * action buttons (Start Review, Initiate Chargeback, Close)
    * status history timeline component
    * role-based button visibility (based on JWT claims)

---

## Phase 4 — Assignment + Work Queue Filters (Ops-Ready)

### Goals (demo)

* “My Queue” works: self-assign, supervisor reassign, filters & SLA sorting.

### Tasks

**Database**

* Add:

    * `case_assignments`
* Add indexes for queue:

    * `(current_status, priority, sla_due_at)`
    * `(current_assignee_id, current_status)`

**Backend**

* Assignment APIs:

    * `POST /cases/{id}/assignments/self`
    * `POST /cases/{id}/assignments` (supervisor)
* Queue enhancements:

    * filtering by status/assignee/priority/reason code
    * sort by `slaDueAt`

**Frontend**

* Queue page:

    * filters UI (status, priority, assignee=ME)
    * “Assign to me” button from list + detail
* Supervisor-only reassign UI (simple dropdown)

---

## Phase 5 — Evidence Checklist + Evidence Requests (No Files Yet)

### Goals (demo)

* Create evidence requests tied to a reason code and track what’s missing.

### Tasks

**Database**

* Add:

    * `evidence_requirements`
    * `case_evidence_requests`
    * `case_evidence_request_items`

**Backend**

* Reference:

    * `GET /reference/reason-codes/{code}/evidence-requirements`
* Evidence request APIs:

    * `POST /cases/{id}/evidence-requests`
    * `GET /cases/{id}/evidence-requests`
* Add business logic:

    * when evidence requested → optionally set status `EVIDENCE_REQUESTED`

**Frontend**

* Case detail:

    * evidence checklist panel (required items)
    * create evidence request modal (choose requirements + due date)
    * show open requests + pending items

---

## Phase 6 — Evidence Upload (S3-style) + Metadata (End-to-End)

### Goals (demo)

* Upload evidence (locally or S3), store metadata, download via a link.

### Tasks

**Storage approach (choose)**

* Local dev: store files in a local folder via docker volume
* “Production-like”: S3 + presigned URLs (best interview story)

**Database**

* Add:

    * `case_evidence_files`

**Backend**

* Evidence APIs:

    * `POST /evidence/upload-intents` (returns presigned url or local upload token)
    * `POST /evidence/files` (confirm + store metadata)
    * `GET /cases/{id}/evidence/files`
    * `GET /evidence/files/{fileId}/download-url`

**Frontend**

* Case detail:

    * file uploader tied to a requirement
    * evidence list with “download” action

---

## Phase 7 — Notes + Audit Log + Reporting (Compliance Angle)

### Goals (demo)

* Notes, immutable audit trail, and a supervisor dashboard metric.

### Tasks

**Database**

* Add:

    * `case_notes`
    * `audit_log`

**Backend**

* Notes APIs:

    * `POST /cases/{id}/notes`
    * `GET /cases/{id}/notes`
* Audit logging:

    * log on key actions (create case, assign, status change, evidence upload)
* Supervisor view:

    * basic endpoint for counts:

        * cases by status
        * SLA breached/at-risk

**Frontend**

* Notes tab on case detail
* Audit tab (read-only)
* Simple dashboard page (cards + tables)

---

## Phase 8 — Testing Hardening + Production Deployment

### Goals (demo)

* CI passing, realistic test coverage, deployed environment reachable.

### Testing Strategy (this phase focus)

**Backend**

* Unit tests: service-level workflow rules
* Controller tests: validation + security constraints
* Integration tests:

    * Testcontainers MySQL
    * happy path workflows: create → assign → start review → request evidence → upload meta → close
* Contract-ish tests:

    * verify error response shape across endpoints

**Frontend**

* Unit tests:

    * form validation + key components
* API mocking tests (MSW or similar)
* One end-to-end smoke test (Playwright/Cypress):

    * create case → status transition → upload evidence metadata

### Deployment

**Containerization**

* Dockerfiles for frontend + backend
* Compose for local; images built in CI

**AWS**

* “Good enough” solo deployment choices:

    1. **ECS Fargate** (simpler than EKS for solo) + RDS MySQL + S3
       or
    2. **EKS** (if you want Kubernetes interview depth)

**CI/CD**

* Pipeline:

    * build + test
    * docker build/push (ECR)
    * deploy (Terraform or simple task definition update / kubectl apply)

---

## Optional Phase 9 — Polish + Interview Readiness (if you have time)

* Add search (case number, transaction ref)
* Add rate limiting at gateway
* Add OpenAPI docs + “demo script” doc
* Add “data masking” rules and log redaction (banking credibility)

---

### How to present this roadmap in interviews (one-liner)

“I built it incrementally like a real enterprise delivery—starting with secure case intake and a normalized schema, then adding workflow controls, queue/assignment, evidence management, audit logging, and finally CI/CD + cloud deployment.”
