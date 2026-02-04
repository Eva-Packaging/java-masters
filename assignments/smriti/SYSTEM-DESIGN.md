
## 1) High-Level Architecture (described in words)

**Users (Ops / Compliance / Support)** use a **React web app**.
The web app calls an **API Gateway** (single entry point).
Gateway routes requests to Spring Boot microservices:

* **Auth/Identity Service** (JWT, roles)
* **Exception Management Service** (trade breaks lifecycle)
* **Reference Data Service** (optional but realistic: instruments, accounts, counterparties)
* **Notification Service** (email/Slack/queue alerts)
* **Audit Service** (immutable history of actions)

All services persist to **Oracle** (separate schemas per service or separate DBs depending on maturity).
Events (optional but strong) are published to a **message broker** for notifications and audit trails.

---

## 2) Frontend (React) Tech Stack + Responsibilities

### Tech Stack

* **React + TypeScript**
* UI: Material UI / Ant Design (pick one)
* State: React Query (server state) + Context/Zustand for local UI state
* Auth: JWT stored in memory/session storage + refresh token approach
* Charts/metrics: simple KPI cards (breaks by status, SLA breaches)

### Frontend Responsibilities

**Core screens**

1. **Exception Queue (Workbench)**

   * Filters: status, severity, product, date, assignedTo, SLA breach
   * Sort/paging for large lists
   * Bulk actions (assign, change priority)

2. **Exception Detail View**

   * Trade summary + break reason
   * Timeline (audit trail)
   * Comments + attachments metadata
   * Actions: assign, change status, request info, resolve, reopen

3. **Rules & Categories (Admin)**

   * Manage break categories, severity rules, SLA thresholds
   * Role-based access: Admin only

4. **Compliance/Audit Dashboard**

   * Read-only audit log search
   * Export report (CSV)

**Security UX**

* On login, decode token claims for `roles` and show/hide actions (RBAC at UI layer)
* Still enforce RBAC on backend (UI is convenience, not security)

---

## 3) Backend Structure (Spring Boot) – Services, Controllers, Layers

### Recommended Microservices (interview-friendly)

1. **Auth Service**

   * Handles login, token issuance, user roles
   * Integrates with LDAP/SSO in “real enterprise” story; for demo use internal users table

2. **Exception Service (core)**

   * Owns exception lifecycle: create, assign, comment, resolve, reopen
   * Owns SLA calculation + priority workflow
   * Publishes events: `ExceptionCreated`, `ExceptionAssigned`, `ExceptionResolved`

3. **Audit Service**

   * Writes immutable audit records (who did what, when, before/after)
   * Query endpoints for compliance searches

4. **Notification Service**

   * Subscribes to events and notifies via email/Slack (or stores notifications in DB)

*(If you want fewer services for implementation, combine Audit+Notification into Exception Service, but still describe them as separable components.)*

### Layering (inside each service)

* **Controller layer**: REST endpoints, request validation
* **Service layer**: business rules (state transitions, SLA logic, assignment rules)
* **Repository layer**: Spring Data JPA repositories
* **Domain layer**: entities + domain enums (Status, Severity, Role)
* **Integration layer** (optional): message broker publisher/consumer, external data calls
* **Security layer**: Spring Security filters, method-level security `@PreAuthorize`

### Example Controller Responsibilities (no raw code, but how you’d explain)

* `ExceptionController`

  * `GET /exceptions` search queue (filters + paging)
  * `GET /exceptions/{id}` details
  * `POST /exceptions` create exception (from upstream or manual)
  * `POST /exceptions/{id}/assign` assign user/team
  * `POST /exceptions/{id}/resolve` resolve + resolution code
  * `POST /exceptions/{id}/comments` add comment

### Business Logic You Can Mention

* **State machine** for status transitions:

  * `NEW -> IN_PROGRESS -> RESOLVED`
  * `RESOLVED -> REOPENED -> IN_PROGRESS`
  * Block invalid transitions (e.g., can’t resolve without required fields)

* **SLA** calculation:

  * severity defines SLA window (e.g., Critical 2h, High 8h)
  * compute breach flag in query (or precompute field updated via scheduled job)

* **Optimistic locking**:

  * prevent two agents from resolving same exception simultaneously
  * mention JPA `@Version`

---

## 4) Database Choice + Justification

### Choice: **Oracle**

**Why Oracle fits (Nomura realism)**

* Common in banking/back-office systems
* Strong transaction guarantees, mature indexing, partitioning
* Works well with **JPA/Hibernate** + SQL reporting needs
* Plays nicely with audit/reporting queries and large datasets

### How you’d structure data

* Each microservice can own its schema:

  * `EXC_*` tables in exception schema
  * `AUD_*` tables in audit schema
* Use strict foreign keys within service boundary; across services use IDs/events

*(If asked why not NoSQL: exception workflows require relational integrity, query-heavy filters, and audit/reporting.)*

---

## 5) API Gateway / Microservices Setup

### API Gateway (recommended)

* **Spring Cloud Gateway** (or AWS API Gateway if you want more AWS)
* Responsibilities:

  * routing (`/api/exceptions/**` → Exception Service)
  * token validation at edge (optional)
  * rate limiting (optional)
  * centralized logging/correlation IDs

### Service-to-Service Communication

* Synchronous: REST (simple) for reference lookups
* Async: event-driven for audit + notification

  * When exception changes state → publish event
  * Audit service stores immutable record
  * Notification service sends alerts

### Why this is a strong interview story

* Clear separation of concerns
* Scales with teams and compliance needs
* Reduces coupling with event-based audit/notifications

---

## 6) Deployment Environment (AWS + Docker + Kubernetes + CI/CD)

### Containerization

* Each service packaged as a **Docker image**
* Config via environment variables / secrets

### Orchestration

* **Kubernetes** (EKS on AWS)

  * Deployment per service
  * Horizontal Pod Autoscaler for Exception Service (queue traffic spikes)
  * ConfigMaps for non-secret config, Secrets for credentials

### AWS Components (practical + explainable)

* **EKS** for Kubernetes
* **RDS Oracle** (or managed Oracle setup depending on org constraints)
* **ALB Ingress** to expose gateway
* **CloudWatch** for logs/metrics
* **Secrets Manager** for DB credentials, JWT signing keys

### CI/CD (example pipeline)

* GitHub Actions / Jenkins:

  1. Build + unit tests (JUnit)
  2. Static checks (basic)
  3. Build Docker image
  4. Push to ECR
  5. Deploy to EKS (Helm or kubectl apply)
  6. Smoke tests (Postman/newman)

---

## 7) How Components Interact (end-to-end flow)

### Flow A: Exception Created + Assigned

1. Upstream system (or manual user) submits `POST /exceptions`
2. **Gateway** routes to **Exception Service**
3. Exception Service:

   * validates payload
   * writes exception row to Oracle
   * publishes `ExceptionCreated` event
4. **Audit Service** consumes event → writes immutable audit entry
5. **Notification Service** consumes event → notifies ops channel/email
6. React UI refreshes queue via polling or websockets (optional)

### Flow B: Ops Agent Resolves Exception

1. Agent opens exception detail page
2. React calls `GET /exceptions/{id}` + `GET /exceptions/{id}/audit`
3. Agent clicks Resolve → `POST /exceptions/{id}/resolve`
4. Exception Service enforces:

   * RBAC: only Ops role can resolve
   * Valid transition + required resolution fields
5. Saves status change + publishes `ExceptionResolved`
6. Audit logs and notifications happen asynchronously again

---

## Interview-friendly “why” statements you can use

* “We used **RBAC** because ops and compliance permissions are different, and we needed audit-grade control.”
* “We chose **Oracle** because exception triage is query-heavy with filtering, reporting, and strong relational integrity.”
* “We used a **Gateway** to centralize routing and security concerns and keep services focused.”
* “We made audit + notifications **event-driven** so the core workflow stays fast and we reduce coupling.”
