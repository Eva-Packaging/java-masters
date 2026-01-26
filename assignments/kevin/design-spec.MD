# Kevin (1-Week Project) — Candidate Design Spec (Entry-Level Friendly)

## 1) Project Overview (High-Level)

You will build a Spring Boot REST API called **TaskFlow API**.

TaskFlow is a simple **Task + User Management** system (similar to a lightweight Jira/Trello backend).
It allows users to:

* Create an account
* Create tasks
* Assign tasks
* Update task status
* View tasks with filtering + analytics
* Run a background job that updates tasks automatically
* Log audit events asynchronously

This project is designed to help you improve the areas you scored lowest in:

* Multithreading (very weak)
* Java 8 features (very weak)
* Coding fundamentals (very weak)
* Spring Boot fundamentals (weak)
* Hibernate + Spring Data JPA
* Dependency Injection styles
* Bean scopes
* Profiles + Actuator readiness (optional bonus)

Timebox: 7 days
Expected outcome: Working REST API + clean repo + demo-ready endpoints

---

## 2) Required Tech Stack (Use Resume Technologies)

Backend

* Java 8+
* Spring Boot
* Spring Web (REST)
* Spring Validation
* Spring Data JPA (Hibernate)
* H2 database (dev)

Build + Tools

* Maven
* Git
* Postman

Testing

* JUnit + Mockito (minimum 3 tests)

Optional (Bonus)

* Spring Actuator
* Docker

---

## 3) Strict Implementation Rules (Beginner Friendly)

### Rule A — Use clean layers (Required)

Your code must follow this structure:

Controller layer

* Handles HTTP requests/responses only
* Calls service methods
* No business logic inside controllers

Service layer

* Contains business logic
* Uses Streams and Java 8 features
* Calls repository methods

Repository layer

* Database access only
* No business logic

---

### Rule B — Use all 3 injection types (Required)

You must demonstrate:

* Constructor injection (main recommended)
* Field injection (show once only, explain why it’s not recommended)
* Setter injection (optional dependency example)

Important

* You must use constructor injection for most services
* You must include a short explanation in README about the differences

---

### Rule C — Bean scopes must be demonstrated (Required)

You must create a small demo component showing these scopes:

* Singleton (default)
* Prototype
* Request scope (only if you want bonus points)

You must include:

* A small endpoint that proves the scope behavior

---

### Rule D — Java 8 features must be used in real code (Required)

You must demonstrate:

* Functional interfaces + lambdas
* Predicate usage
* Stream API usage

You must include at least:

* 1 Predicate validation rule in service
* 1 Stream-based analytics method (no loops)

---

### Rule E — Multithreading must be used (Required)

You must implement at least 2 multithreading features:

1. Async logging (required)

* Use @Async with custom executor

2. Background scheduler (required)

* Use @Scheduled to auto-update tasks

No multithreading = project fails requirements.

---

### Rule F — Hibernate + Spring JPA must be used (Required)

You must use:

* Entities with relationships
* Spring Data JPA repositories
* JPA annotations correctly

You must implement at least:

* One-to-many relationship
* Many-to-one relationship

---

### Rule G — Spring Profiles must be used (Required)

You must create:

* dev profile → H2 database
* prod profile → Postgres placeholders

You must show in README how to run each profile.

---

## 4) Required Folder Structure

Use this structure:

src/main/java/com/company/taskflow

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

### User Entity

Required fields:

* id
* name
* email
* createdAt

### Task Entity

Required fields:

* id
* title
* description
* status (TODO / IN_PROGRESS / DONE)
* priority (LOW / MEDIUM / HIGH)
* dueDate
* createdAt
* updatedAt
* assignedTo (Many-to-One relationship with User)

### AuditLog Entity (Required for async logging)

Required fields:

* id
* eventType (TASK_CREATED, TASK_UPDATED, TASK_AUTO_EXPIRED)
* message
* createdAt
* threadName (store which thread logged it)

---

## 6) Required REST API Endpoints

### 1) Create User

POST /api/users

Request example:
```json
{
    "name": "Kevin",
    "email": "kevin@email.com"
}
```

---

### 2) Create Task

POST /api/tasks

Request example:
```json
{
    "title": "Finish Spring Boot project",
    "description": "Build TaskFlow API",
    "priority": "HIGH",
    "dueDate": "2026-02-01",
    "assignedUserId": 1
}
```

Rules

* Validate title not blank
* Validate dueDate in the future using Predicate in service
* Save audit log asynchronously

---

### 3) Get Task By ID

GET /api/tasks/{id}

Return

* 200 if found
* 404 if not found

---

### 4) Update Task Status

PUT /api/tasks/{id}/status?status=IN_PROGRESS

Rules

* Only valid statuses allowed
* Save audit log asynchronously

---

### 5) Get Tasks By User

GET /api/tasks?assignedUserId=1

---

### 6) Analytics Endpoint (Streams Required)

GET /api/tasks/analytics?assignedUserId=1

Must return:

* total tasks
* count by status
* count by priority
* nearest due date task

Must use Stream API in service.
No loops allowed in analytics method.

---

## 7) Coding Requirements (Very Important)

### Requirement 1 — Predicate Validation (Required)

You must create a util class:

* TaskPredicates

Example rules:

* title not blank
* dueDate in the future
* priority not null

TaskService must call these predicates before saving.

---

### Requirement 2 — Stream API (Required)

You must use Streams in:

* analytics method (required)
* at least one other method (filter tasks, sort tasks, etc.)

No loops inside analytics.

---

### Requirement 3 — Functional Interfaces + Lambdas (Required)

You must show:

* Predicate
* Function mapper (DTO → Entity)
* Consumer for logging or side effects

---

## 8) Multithreading Requirements (Required)

### Async Logging (Required)

* Every task create/update must write an AuditLog asynchronously
* Must use @Async and custom executor

### Scheduled Job (Required)

Create a job that runs every 60 seconds:

* Finds tasks that are overdue and not DONE
* Updates status to TODO or marks them as “OVERDUE” (your choice)
* Writes an AuditLog asynchronously

---

## 9) Bean Scopes Requirements (Required)

You must create a small component that proves bean scopes.

Example:

* A bean that generates a random UUID when created

Expose endpoint:
GET /api/debug/scopes

Return:

* singleton UUID stays same across calls
* prototype UUID changes across calls

---

## 10) Spring Profile Requirements (Required)

dev profile

* H2 database
* show SQL enabled

prod profile

* Postgres placeholders using env vars
* logging level INFO

---

## 11) Error Handling Requirements (Required)

You must use @ControllerAdvice.

Error response format:
```json
{
    "code": "VALIDATION_ERROR | NOT_FOUND | CONFLICT | INTERNAL_ERROR",
    "message": "Human readable message"
}
```

HTTP status rules:

* 400 for invalid input
* 404 for missing task/user
* 409 for invalid status updates
* 500 for unexpected errors

---

## 12) Testing Requirements (Minimum 3 Tests)

You must include at least:

Test 1: Create task validation

* dueDate must be in the future

Test 2: Stream analytics correctness

* counts by status and priority

Test 3: Scheduled job behavior (basic test)

* overdue tasks get updated

---

## 13) Implemention Plan

### Phase 1:

* Setup project + entities + repositories
* Confirm H2 works

### Phase 2:

* Create User API
* Create Task API
* Basic validation

### Phase 3:

* Update status endpoint
* Global exception handler

### Phase 4:

* Add Predicate validations + mapper functions
* Refactor to constructor injection mostly
* Add setter injection example

### Phase 5:

* Add analytics endpoint using Streams
* Add second Streams usage (sorting/filtering)

### Phase 6:

* Add async audit logging + custom executor
* Add scheduler job for overdue tasks

### Phase 7:

* Add bean scope demo endpoint
* Add profiles dev/prod
* Add tests + README + Postman collection

---

## 14) Submission Checklist (Must Pass)

Before submitting:

* All endpoints work in Postman
* Predicate validation exists and is used in service
* Streams used in analytics (no loops)
* Multithreading includes @Async + @Scheduled
* Constructor injection used for main services
* Bean scope demo endpoint works
* Hibernate relationships are correct
* Profiles dev and prod exist
* At least 3 tests written
* README + Postman/curl examples included
