Below is a **detailed system design** for **Project #1: Claims Fraud Signal & Triage Dashboard**, built to **match Kevin’s resume stack** (Java, Spring Boot/Security, Hibernate/JPA, Angular/React, SQL, JWT/OAuth2, RBAC, AWS/Docker/K8s optional).

---

## 1) Frontend

### Tech stack

* **Angular or React** (both align with Kevin’s experience).
* TypeScript, HTML5, CSS3

### Frontend responsibilities

**A. Authentication + Role-based UI (RBAC)**

* Login flow obtains JWT (and refresh token if implemented).
* Hide/disable features by role:

    * **Adjuster**: create/modify claims, upload docs, view score/reasons, respond to requests
    * **Investigator**: triage queue, claim review, case notes, status transitions
    * **Admin**: manage rules/thresholds, user roles, reporting

**B. Triage Dashboard**

* Queue view: filter/sort by fraud score, status, SLA, claim type, region, date.
* “Claim Detail” view:

    * Fraud score trend + list of **fraud reasons/signals**
    * Timeline (events + decisions)
    * Linked documents/evidence

**C. Case Workflow UI**

* Assign/unassign investigators
* Add notes and recommendations
* Change status (Open → In Review → Escalated → Closed)
* Trigger “Request Documents” and show fulfillment status

**D. Audit visibility**

* Read-only “Activity Log” panel: who/what/when for key changes.

---

## 2) Backend structure (Spring Boot microservices)

### Services (microservices-friendly, interview-ready)

1. **Claim Service**

* Claim CRUD, document upload metadata, claim lifecycle
* Publishes claim events (created/updated/doc uploaded)

2. **Fraud Scoring Service**

* Consumes claim events
* Runs rules-based scoring engine
* Persists `fraud_score + reasons`
* Publishes `FraudScoreUpdated`

3. **Case Management Service**

* Creates an “Investigation Case” when score crosses threshold
* Owns queue views, assignments, notes, dispositions, SLAs

4. **Audit Service** (compliance)

* Append-only audit log for state changes + sensitive access
* Query endpoints for compliance reporting

5. **Rules Service** (optional but strong in interviews)

* Manage fraud rules/thresholds (enable/disable, versioning)

> This mirrors Kevin’s experience building secure REST APIs with Spring Boot/Security and maintaining audit logs.

---

### Internal layering (inside each service)

**Controller → Service (use-cases) → Domain → Repository**

* **Controller layer**

    * REST endpoints, DTO validation, HTTP status mapping
* **Service layer**

    * Orchestrates use-cases (create claim, score claim, open case, assign investigator)
    * Transaction boundaries
* **Domain layer**

    * Entities + invariants (allowed status transitions, SLA rules)
* **Repository layer**

    * Spring Data JPA / Hibernate repositories

### Security

* **Spring Security + JWT + RBAC**
* Optionally OAuth2 Resource Server if integrated with enterprise identity provider
* Method security (`@PreAuthorize`) for investigator/admin actions.

---

## 3) Database choice & justification

### Primary DB: PostgreSQL (or MySQL)

Kevin lists **MySQL/PostgreSQL/Oracle**, so choosing **PostgreSQL** is consistent.

**Why PostgreSQL**

* Strong relational integrity for workflows (claims ↔ cases ↔ assignments ↔ notes)
* Great indexing for triage queries (score, status, SLA, created_at)
* JSONB can store flexible “fraud reasons” payloads if you want
* Works cleanly with **Hibernate/JPA**

### Document storage (recommended)

* Store files in **object storage** (S3) and keep only metadata in DB:

    * file name, hash, type, uploaded_by, claim_id, storage_key

---

## 4) API gateway / microservices setup

### API Gateway

* **Spring Cloud Gateway** (simple, common, interview-friendly)

    * Route: `/claims/**` → Claim Service
    * Route: `/fraud/**` → Fraud Service
    * Route: `/cases/**` → Case Service
    * Route: `/audit/**` → Audit Service
    * Centralized concerns: CORS, rate limiting, auth token forwarding

### Service-to-service communication

**Best pattern here: hybrid**

* **Async events** for scoring + case creation (decouples, scales)

    * `ClaimCreated` → Fraud Scoring computes score
    * `FraudScoreUpdated` → Case Service opens/updates case
* **REST** for UI reads and commands

    * UI fetches claim details, queues, case actions

> If you want to keep the build lightweight, use REST calls between services; if you want “enterprise-grade,” add a broker (Kafka/RabbitMQ).

---

## 5) Deployment environment (Cloud, Docker, CI/CD)

### Containerization

* Package each service as a **Docker** image
* Local development: Docker Compose (gateway + services + postgres + broker optional)

### Cloud

Resume mentions **AWS** as a deployment option.
A clean AWS reference architecture:

* ECS Fargate (or EKS) for services
* RDS Postgres for database
* S3 for documents
* CloudWatch for logs/metrics

### CI/CD

Using typical tooling Kevin lists (Git/Maven/JUnit/Postman):
Pipeline stages (Jenkins or GitHub Actions):

1. Build + unit tests (`mvn test`)
2. Package (`mvn package`)
3. Build Docker image
4. Push to registry (ECR)
5. Deploy (ECS task update / Helm if EKS)
6. Smoke test (health endpoints)

---

## 6) How components interact (end-to-end flow)

### Claim intake → scoring → triage

1. **Frontend** submits claim → **API Gateway** → **Claim Service** (`POST /claims`)
2. Claim Service saves to Postgres; emits `ClaimCreated`
3. Fraud Service consumes event → calculates score (rules) → saves `fraud_score + reasons`
4. Fraud Service emits `FraudScoreUpdated`
5. Case Service consumes score event:

    * if score ≥ threshold → create investigation case + queue placement
6. Investigator UI calls Case Service to load the queue sorted by score/SLA

### Investigation workflow

1. Investigator opens a case → Case Service returns case + fraud reasons
2. UI fetches claim detail/docs metadata from Claim Service
3. Investigator adds notes / changes status → Case Service persists + emits audit event
4. Audit Service stores immutable audit record (who/what/when)
