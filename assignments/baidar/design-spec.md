#  Baider (1-Week Project) — Design Spec (Entry-Level Friendly)

## 1) Project Overview (High-Level)

You will build a Spring Boot REST API called **SupportDesk API**.

SupportDesk is a simple ticketing system (like a mini ServiceNow/Jira backend).
It allows:

* Creating support tickets
* Assigning tickets to an agent
* Updating ticket status and priority
* Searching tickets using query parameters
* Viewing analytics using Java 8 Streams
* Using Spring Profiles for dev/prod
* Demonstrating bean scopes (singleton + prototype)
* Demonstrating dependency injection styles (constructor, field, setter)
* Demonstrating Predicate-based validation rules in service

Timebox: 7 days
Goal: A working REST API that demonstrates core Spring concepts and Java 8 features.

---

## 2) Required Tech Stack

Backend

* Java 8+
* Spring Boot
* Spring Web (REST)
* Spring Validation
* Spring Data JPA (Hibernate)
* H2 database (dev profile)

Build + Tools

* Maven
* Git
* Postman

Testing

* JUnit + Mockito (minimum 2 tests)

Optional (Bonus)

* Spring Actuator

---

## 3) Strict Implementation Rules (Must Follow)

### Rule A — Clean Layers (Required)

Controller layer

* Only HTTP mapping and DTO validation
* Must use @PostMapping, @RequestBody, @PathVariable, @RequestParam correctly
* No business logic

Service layer

* Business logic goes here
* Streams and Predicates must be used here
* Calls repositories

Repository layer

* Database access only
* No business logic

---

### Rule B — Dependency Injection Styles (Required)

You must demonstrate all three:

* Constructor injection (use in most services)
* Field injection (use once only, add a comment explaining why it’s discouraged)
* Setter injection (use for an optional dependency, explain in README)

@Autowired must be used correctly.

---

### Rule C — Predicate Usage (Required)

You must create a util class named TicketPredicates.

It must contain Predicate rules such as:

* title not blank
* description not blank
* priority not null
* status transition valid

These predicates must be used inside service methods before saving/updating.

---

### Rule D — Stream API Usage (Required)

You must implement an analytics endpoint that uses Streams:

* groupingBy + counting
* sorting
* min/max
* no loops in analytics method

Streams must be used in the service layer.

---

### Rule E — Bean Scopes (Required)

You must demonstrate:

* Singleton scope
* Prototype scope

Provide a debug endpoint that proves:

* singleton id stays the same across calls
* prototype id changes across calls

---

### Rule F — Spring Profiles (Required)

You must implement:

* dev profile: H2 DB, debug logs
* prod profile: Postgres placeholders, info logs

README must explain how to run both.

---

### Rule G — Spring Data JPA + Hibernate (Required)

You must use:

* Entities + relationships
* Repositories
* Correct JPA annotations

---

## 4) Required Folder Structure

Use this structure:

src/main/java/com/company/supportdesk

* controller/
* service/
* repository/
* domain/
* dto/
* config/
* exception/
* util/

---

## 5) Data Model (Required)

### Agent Entity

Fields:

* id
* name
* email
* createdAt

### Ticket Entity

Fields:

* id
* title
* description
* status (OPEN / IN_PROGRESS / RESOLVED / CLOSED)
* priority (LOW / MEDIUM / HIGH)
* createdAt
* updatedAt
* assignedAgent (Many-to-One relationship with Agent)

Relationships:

* One Agent can have many Tickets
* One Ticket can have only one Agent

---

## 6) Required REST API Endpoints

### 1) Create Agent

POST /api/agents

Request example:
{
"name": "Alex Agent",
"email": "[alex@company.com](mailto:alex@company.com)"
}

---

### 2) Create Ticket (PostMapping + RequestBody required)

POST /api/tickets

Request example:
{
"title": "Login not working",
"description": "User cannot login after password reset",
"priority": "HIGH"
}

Rules:

* Validate using TicketPredicates in service
* Default status = OPEN

---

### 3) Assign Ticket to Agent (PathVariable required)

PUT /api/tickets/{ticketId}/assign/{agentId}

Rules:

* Ticket must exist
* Agent must exist
* Update assignedAgent
* Update updatedAt

---

### 4) Update Ticket Status (Query parameter required)

PUT /api/tickets/{ticketId}/status?status=IN_PROGRESS

Rules:

* Must validate status transition using Predicate
* Example: CLOSED cannot go back to IN_PROGRESS (return 409)

---

### 5) Search Tickets (Query parameters required)

GET /api/tickets/search?status=OPEN&priority=HIGH&agentId=1

Rules:

* All params are optional
* Use @RequestParam
* Return matching tickets

---

### 6) Analytics Endpoint (Streams Required)

GET /api/tickets/analytics

Response must include:

* total tickets
* count by status
* count by priority
* top agent by assigned ticket count
* oldest OPEN ticket (by createdAt)

Streams required in service analytics method.
No loops allowed in analytics method.

---

## 7) Error Handling Requirements (Required)

You must implement @ControllerAdvice.

Error response format:
{
"code": "VALIDATION_ERROR | NOT_FOUND | CONFLICT | INTERNAL_ERROR",
"message": "Human readable message"
}

HTTP rules:

* 400 for validation errors
* 404 if ticket/agent not found
* 409 for invalid status transitions
* 500 for unexpected errors

---

## 8) Bean Scope Debug Endpoint (Required)

GET /api/debug/scopes

Response must show:

* singletonId (same every request)
* prototypeId (different every request)

---

## 9) Testing Requirements (Minimum 2 Tests)

You must include:

Test 1: Predicate validation

* Creating ticket with blank title must fail

Test 2: Analytics correctness

* Count by status is correct

---

## 10) Implementation Plan

### Phase 1:

* Setup project
* Entities + repositories
* H2 working

### Phase 2:

* Create Agent endpoint
* Create Ticket endpoint using @RequestBody + Predicate validation

### Phase 3:

* Assign ticket endpoint with @PathVariable
* Global exception handler

### Phase 4:

* Status update endpoint using query parameter + status transition predicate

### Phase 5:

* Search endpoint using optional @RequestParam

### Phase 6:

* Analytics endpoint using Streams (no loops)
* Bean scope debug endpoint

### Phase 7:

* Profiles dev/prod
* Tests
* README + Postman collection
* Final demo

---

## 11) Submission Checklist (Must Pass)

* Clean layer boundaries followed
* Predicate class exists and is used in service
* Streams used in analytics (no loops)
* Dependency injection styles demonstrated (constructor, field, setter)
* Bean scopes demonstrated (singleton + prototype)
* Profiles dev/prod exist and are documented
* JPA relationships implemented correctly
* Minimum 2 tests included
* README + Postman/curl examples included
