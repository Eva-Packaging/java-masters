# ConsentVault (1-Week Project) — Design Spec

## 1) Project Overview (High-Level)

You will build a Spring Boot REST API called ConsentVault.

ConsentVault manages customer consent in a banking-style system:

* Create a consent
* View consents
* Revoke a consent
* Auto-expire old consents
* Save audit logs for every action

This project is designed to help you improve:

* Java 8 Lambdas + Predicates
* Stream API
* Spring Boot REST APIs
* Dependency Injection (constructor injection)
* Spring Profiles
* Spring Actuator
* Async processing (basic multithreading)

Expected outcome: Working API + clean repo + demo-ready endpoints

---

## 2) Required Tech Stack

You must use the following:

Backend

* Java 8+
* Spring Boot
* Spring Web (REST)
* Spring Validation
* Spring Data JPA
* Spring Actuator

Database

* H2 database for dev profile (required)
* Postgres config placeholders for prod profile (required)

Build + Tools

* Maven
* Git
* Postman

Testing

* JUnit + Mockito (minimum 3 tests)

---

## 3) Strict Implementation Rules (Must Follow)

### Rule A — Clean Layered Architecture (Required)

Your code must be separated into these layers:

Controller layer

* Handles HTTP requests and responses only
* Calls the service layer
* Must not contain business logic

Service layer

* Contains business rules and decision making
* Contains Stream API logic (analytics)
* Calls repositories

Repository layer

* Database access only (Spring Data JPA)
* No business logic
* No Stream analytics logic

If you break these boundaries, the solution will be considered incomplete.

---

### Rule B — Constructor Injection Only (Required)

You must use constructor injection for all required dependencies.

Allowed

* Constructor injection (required dependencies)

Not allowed

* Field injection (forbidden)

Setter injection

* Only allowed for optional dependencies, and must be explained in README

---

### Rule C — Predicate Usage is Required (Java 8 Feature)

You must create a utility class named ConsentPredicates.

ConsentPredicates must include Predicate rules such as:

* customerId is not blank
* scope is not blank
* expiresAt is in the future

You must use these predicates inside the Service layer before saving a consent.

Important

* Validation annotations are required too, but they do not replace Predicate usage.
* Predicate usage must be visible in service methods.

---

### Rule D — Stream API Usage is Required (Analytics)

You must implement an analytics endpoint:

GET /api/consents/analytics?customerId=...

The analytics service method must use Stream API to compute:

* total consents
* counts by status (ACTIVE / REVOKED / EXPIRED)
* latest version per consent type
* nearest expiry date

No loops allowed inside analytics method:

* No for loops
* No while loops

Streams must be used in the Service layer, not the Controller.

---

### Rule E — Spring Profiles are Required

You must support two Spring profiles:

dev profile (default)

* Uses H2 database
* Debug logging enabled
* Fast local development setup

prod profile

* Uses Postgres config placeholders via environment variables
* Logging level set to INFO

You must include instructions in README for running both profiles.

---

### Rule F — Spring Actuator is Required (But Restricted)

You must enable Spring Actuator.

Only these endpoints are allowed to be exposed:

* /actuator/health
* /actuator/info
* /actuator/metrics

You must not expose all actuator endpoints.

---

### Rule G — Async Audit Logging is Required

Audit events must be saved asynchronously using:

* @EnableAsync
* @Async
* Custom executor named auditExecutor

Consent create/revoke/expire operations should not block waiting for audit persistence.

---

### Rule H — Scheduled Expiry Job is Required

You must implement a scheduled job that runs automatically:

* Finds ACTIVE consents where expiresAt < now
* Marks them as EXPIRED
* Writes an audit event asynchronously

---

## 4) Required Folder Structure

You must follow this structure:

src/main/java/com/company/consentvault

* controller/
* service/
* repository/
* domain/
* dto/
* config/
* exception/
* util/

---

## 5) Required Data Model

### Consent Entity (Required Fields)

Your Consent entity must include:

* id
* customerId
* consentType
* scope
* status (ACTIVE / REVOKED / EXPIRED)
* version
* expiresAt
* createdAt
* updatedAt
* createdBy

### AuditEvent Entity (Required Fields)

Your AuditEvent entity must include:

* id
* consentId
* customerId
* action (CREATED / REVOKED / EXPIRED)
* timestamp
* actor
* metadata

---

## 6) Required REST API Endpoints

### 1) Create Consent

POST /api/consents

Request example:
```json
{
    "customerId": "CUST-1001",
    "consentType": "ACCOUNT_ACCESS",
    "scope": "accounts.read transactions.read",
    "expiresAt": "2026-02-01T00:00:00Z"
}
```

Rules

* status must be ACTIVE
* version must be assigned correctly (see versioning rules)
* audit event must be created asynchronously

---

### 2) Get Consent By ID

GET /api/consents/{id}

Rules

* Return 200 if found
* Return 404 if not found

---

### 3) Get Consents By Customer

GET /api/consents?customerId=CUST-1001

Rules

* Return list of consents for that customer

---

### 4) Revoke Consent

PUT /api/consents/{id}/revoke

Rules

* If ACTIVE → change to REVOKED
* If already REVOKED or EXPIRED → return 409 conflict
* Must create audit event asynchronously

---

### 5) Analytics Endpoint (Streams Required)

GET /api/consents/analytics?customerId=CUST-1001

Response example:
```json
{
    "customerId": "CUST-1001",
    "totalConsents": 4,
    "countByStatus": {
        "ACTIVE": 2,
        "REVOKED": 1,
        "EXPIRED": 1
    },
    "latestVersionByType": {
        "ACCOUNT_ACCESS": 2,
        "KYC_SHARING": 1
    },
    "nearestExpiry": "2026-01-30T00:00:00Z"
}
```

Rules

* Must use Stream API in Service layer
* No loops allowed in analytics method

---

## 7) Business Rules (Important)

### Consent Versioning Rule (Required)

If a customer creates consent multiple times for the same consentType:

* First consent = version 1
* Second consent = version 2
* etc.

Example
Customer CUST-1001, type ACCOUNT_ACCESS

* Create #1 → version 1
* Create #2 → version 2

---

### Consent Status Rules

* New consent must start as ACTIVE
* Revoked consent must be REVOKED
* Expired consent must be EXPIRED

---

## 8) Error Handling Rules (Required)

You must implement a global error handler using @ControllerAdvice.

All errors must return JSON in this format:
{
"code": "VALIDATION_ERROR | NOT_FOUND | CONFLICT | INTERNAL_ERROR",
"message": "Human readable message"
}

Required HTTP codes:

* 400 Bad Request → invalid request body or validation error
* 404 Not Found → consent not found
* 409 Conflict → invalid action (example: revoke twice)
* 500 Internal Server Error → unexpected error

---

## 9) Actuator Requirements (Required)

Actuator must be enabled and restricted.

Allowed endpoints:

* /actuator/health
* /actuator/info
* /actuator/metrics

Info endpoint must include:

* app name
* version

---

## 10) Testing Requirements (Minimum)

You must write at least 3 tests:

Test 1: Version increases correctly

* Create consent twice for same customerId + consentType
* Ensure version increments

Test 2: Revoke conflict

* Revoke an ACTIVE consent
* Try revoking again
* Ensure conflict (409)

Test 3: Analytics correctness

* Create sample consents with different statuses/types
* Ensure counts and latest version are correct

---

## 11) Implementation Plan

### Phase 1:

* Setup project
* Create entities + repositories
* Confirm app runs

### Phase 2:

* Implement POST /api/consents
* Add validation annotations
* Add ConsentPredicates and use Predicate validation in service

### Phase 3:

* Implement GET endpoints
* Implement revoke endpoint
* Add global exception handling

### Phase 4:

* Refactor to clean services
* Ensure constructor injection only
* Confirm layering rules are followed

### Phase 5:

* Implement analytics endpoint
* Streams required, no loops allowed

### Phase 6:

* Add Spring Profiles dev + prod
* Add Actuator and restrict endpoints
* Update README instructions

### Phase 7:

* Add async audit logging
* Add scheduled expiry job
* Add tests
* Finalize README + Postman collection

---

## 12) Submission Checklist (Must Pass)

Before submitting, confirm:

* All endpoints work in Postman
* ConsentPredicates exists and is used in service
* Analytics uses Streams (no loops)
* Constructor injection only (no field injection)
* Profiles dev and prod exist and run
* Actuator endpoints are restricted to health/info/metrics
* Async audit logging implemented
* Scheduled expiry job implemented
* Minimum 3 tests included
* README included with setup + run instructions
