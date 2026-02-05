
## 1) Frontend: Tech Stack + Responsibilities

### Tech Stack (choose based on your comfort)

* **React + TypeScript** (preferred if both are possible)
* UI: Material UI / Ant Design (pick one)
* State: React Query (server-state) + lightweight store (Zustand/Redux Toolkit if needed)
* Auth: OIDC/OAuth2 client flow (redirect or PKCE) → receives **JWT** from Auth service
* File upload: multipart form upload + progress indicator

### Frontend Responsibilities

**Core screens**

1. **Dispute Intake Form**

    * Capture dispute details (txn, reason code, amount, merchant, notes)
    * Validate inputs + show required evidence checklist by reason code
2. **Case Queue (Operations/Agent Worklist)**

    * Filters: status, age/SLA, reason, assignment, priority
    * Sort/paginate (server-side)
3. **Case Detail View**

    * Timeline of events (status transitions + notes + evidence)
    * Evidence list (download links, metadata)
    * Actions based on role: “Assign to me”, “Request evidence”, “Approve chargeback”, “Close case”
4. **Supervisor Dashboard**

    * SLA breaches, workload by team, cases by status/reason
5. **Audit Viewer (Read-only)**

    * Immutable log entries for compliance

**Frontend cross-cutting**

* RBAC-aware UI: hide/disable actions based on role claims in JWT
* Token refresh handling, global error toasts, correlation-id display (optional)
* Accessibility + audit-friendly UX: consistent timestamps, status tags, searchable timeline

---

## 2) Backend: Structure (Services, Controllers, Layers)

### Recommended Architecture

**Microservices** (clean interview story + scalable), behind an API gateway:

1. **Dispute Case Service** (core domain)
2. **Evidence Service** (file metadata + storage orchestration)
3. **Workflow/SLA Service** (optional, can be internal module first)
4. **Notification Service** (optional, email/SMS/Slack integration later)
5. **Auth/IAM** (could be external: Okta/Cognito/Azure AD; or internal for demo)

If you want to keep it simpler for implementation, you can start with **a modular monolith** (same structure but as modules) and describe how it *could* be split into services later.

---

### Backend Layers (per service)

**Controller Layer**

* REST endpoints
* DTO validation (Bean Validation)
* Maps DTO ↔ domain models (MapStruct or manual mapping)

**Service Layer**

* Business rules: status transitions, assignment logic, SLA checks
* Transaction boundaries (`@Transactional`)
* Calls other services (Evidence, Notifications)

**Domain Layer**

* Entities: Case, DisputeItem, StatusHistory, CaseNote, Assignment, etc.
* Domain enums: status, reason codes, priority, SLA buckets
* Domain policies: allowed transitions

**Repository/Data Access Layer**

* Spring Data JPA repositories
* Query methods + custom queries for queue filtering (Specification / Criteria API)

**Security Layer**

* Spring Security resource server
* JWT validation (issuer, audience)
* Method-level security: `@PreAuthorize("hasRole('SUPERVISOR')")`

**Observability Layer**

* Structured logs + correlation id
* Metrics (Prometheus) + tracing (OpenTelemetry)

---

## 3) Database Choice + Justification

### Default Choice: **MySQL**

**Why MySQL works well here**

* Strong fit for **workflow/case-management** (relational, transactional)
* Clean support for:

    * status histories
    * assignments
    * SLAs and reporting queries
    * audit trails (append-only tables)
* Familiar and common in banking internal apps
* Easy to run locally with Docker and scale in AWS (RDS MySQL)

**Where you might add a second store**

* Evidence file storage: **S3** (objects) + **MySQL** for metadata
* Search: Elasticsearch/OpenSearch later for full-text case notes

---

## 4) API Gateway / Microservices Setup

### API Gateway (recommended)

* **Spring Cloud Gateway** (or AWS API Gateway if you want managed)
  **Gateway responsibilities**
* Route `/cases/**` to Dispute Case Service
* Route `/evidence/**` to Evidence Service
* Centralized concerns:

    * JWT auth verification / token relay
    * Rate limiting (basic)
    * Request/response logging
    * CORS handling
    * Correlation ID injection

### Service-to-service communication

* **REST (synchronous)** for core operations

    * Case Service → Evidence Service: get evidence list, create upload intent, etc.
* **Async events (optional but strong interview add)**

    * Case status changed → publish event (Kafka/SNS/SQS)
    * Notification Service subscribes
    * SLA Service subscribes to track breach risks

For interviews: mention you started with REST for simplicity and introduced events for decoupling as the system grew.

---

## 5) Deployment Environment (AWS + Docker + Kubernetes + CI/CD)

### Local Dev

* Docker Compose:

    * case-service
    * evidence-service
    * gateway
    * mysql
    * (optional) localstack / minio for S3-like

### AWS (practical + interview-ready)

* **EKS (Kubernetes)** running services as Deployments
* **RDS MySQL** for relational data
* **S3** for evidence objects
* **IAM Roles for Service Accounts (IRSA)** to allow evidence-service to write to S3 securely
* **CloudWatch** for logs + alarms
* **ALB Ingress Controller** to expose gateway

### CI/CD (GitHub Actions or Jenkins)

Pipeline stages:

1. **Build & Test**

    * Maven build, unit tests, integration tests (Testcontainers for MySQL)
2. **Security**

    * Dependency scan (OWASP / Snyk)
3. **Containerize**

    * Build Docker images, push to **ECR**
4. **Deploy**

    * Helm/Kustomize apply to EKS (dev → staging → prod)
5. **Quality gates**

    * Checkstyle/SpotBugs + minimum coverage threshold

---

## 6) How Components Interact (End-to-End Flows)

### Flow A: Create a new dispute case (Intake)

1. **User (Agent)** logs in → gets **JWT**
2. React app calls **Gateway**: `POST /cases`
3. Gateway validates JWT and routes to **Case Service**
4. Case Service:

    * validates request
    * creates `Case` + initial `StatusHistory` (“NEW”)
    * calculates SLA target date based on reason code
    * stores in **MySQL**
5. Response returns new `caseId` + initial status

**Outcome:** consistent intake, auditable creation, SLA clock starts immediately.

---

### Flow B: Upload evidence (S3-backed)

1. Agent opens Case Detail → requests upload
2. React calls: `POST /evidence/upload-intent?caseId=...`
3. Evidence Service returns:

    * pre-signed S3 URL (or upload token)
    * required metadata fields
4. Browser uploads file directly to **S3**
5. Evidence Service stores metadata record in **MySQL** (file name, hash, size, uploader, timestamps)
6. Case Service records a `CaseEvent`: “EVIDENCE_ADDED”

**Outcome:** secure, scalable evidence storage without routing large files through your core service.

---

### Flow C: Status transition + notifications

1. Agent clicks “Approve Chargeback” in UI
2. React calls: `POST /cases/{id}/actions/approve-chargeback`
3. Case Service checks:

    * user role
    * allowed transition (INVESTIGATING → CHARGEBACK_APPROVED)
4. Case Service writes:

    * status update
    * status history entry
    * audit log entry
5. Case Service publishes event: `CaseStatusChanged`
6. Notification Service sends message to relevant parties (optional)

**Outcome:** controlled transitions, role-gated operations, compliant audit trail.

---

## Architecture Diagram (in words)

* **React UI** → hits **API Gateway**
* Gateway routes to:

    * **Case Service** (case lifecycle + SLA + assignment + audit)
    * **Evidence Service** (evidence metadata + presigned URLs)
* Both services store workflow data in **RDS MySQL**
* Evidence objects stored in **S3**
* Optional: events through **Kafka/SNS/SQS** for notifications + analytics
* Deployed as containers on **EKS** with CI/CD pushing images to **ECR**
