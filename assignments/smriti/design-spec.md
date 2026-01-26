# Smriti (1-Week Project)

## 1) Project Overview (High-Level)

You will build a Spring Boot REST API called **StudentPulse API**.

StudentPulse is a simple backend system that manages:

* Students
* Courses
* Student enrollments
* Progress tracking (basic)

This project is designed to improve your weakest areas:

* Java 8 features (0/10) → highest priority
* Spring Boot fundamentals (3/10)
* REST API basics (PostMapping, RequestBody, ResponseBody, Path/query params)
* Predicates + Streams
* Spring Data JPA implementation
* Spring Profiles
* Bean scopes (singleton + prototype)

Timebox: 7 days
Goal: A working REST API you can demo confidently in an interview.

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

### Rule A — Follow clean layers (Required)

You must follow this structure:

Controller layer

* Only handles HTTP requests and responses
* Must use annotations correctly (@PostMapping, @RequestBody, etc.)
* No business logic

Service layer

* Business logic goes here
* Streams + Predicates must be used here
* Calls repository

Repository layer

* Only database access
* No business logic

---

### Rule B — Java 8 Features Must Be Used (Required)

You must demonstrate in real code:

* Functional interfaces + lambdas
* Predicate usage
* Stream API usage

You must implement:

* A Predicate-based validation in the service layer
* A Stream-based analytics function in the service layer

---

### Rule C — REST API annotations must be used correctly (Required)

You must correctly use these in controllers:

* @PostMapping
* @RequestBody
* @ResponseBody (or use @RestController)
* @PathVariable
* @RequestParam (query parameter)

Incorrect usage = requirement not met.

---

### Rule D — Bean scopes must be demonstrated (Required)

You must show:

* Singleton scope (default)
* Prototype scope

You must create a small debug endpoint that proves the behavior.

---

### Rule E — Spring Data JPA must be used correctly (Required)

You must:

* Use entities with JPA annotations
* Use Spring Data repositories
* Use relationships (One-to-Many / Many-to-One)

---

### Rule F — Spring Profiles must be implemented (Required)

You must create:

* dev profile → H2 DB + debug logs
* prod profile → Postgres placeholders

You must explain how to run both profiles in README.

---

## 4) Required Folder Structure

Use this structure:

src/main/java/com/company/studentpulse

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

### Student Entity

Fields required:

* id
* name
* email
* createdAt

### Course Entity

Fields required:

* id
* title
* level (BEGINNER / INTERMEDIATE / ADVANCED)
* createdAt

### Enrollment Entity

Fields required:

* id
* student (Many-to-One)
* course (Many-to-One)
* status (ENROLLED / COMPLETED / DROPPED)
* progressPercent (0–100)
* enrolledAt

Relationships required:

* One Student can have many Enrollments
* One Course can have many Enrollments

---

## 6) Required REST API Endpoints

### 1) Create Student (PostMapping + RequestBody required)

POST /api/students

Request example:
{
"name": "Smriti",
"email": "[smriti@email.com](mailto:smriti@email.com)"
}

Rules:

* Validate name not blank
* Validate email not blank

---

### 2) Create Course (PostMapping + RequestBody required)

POST /api/courses

Request example:
{
"title": "Java 8 Streams",
"level": "BEGINNER"
}

---

### 3) Enroll Student into Course (PathVariable required)

POST /api/students/{studentId}/enroll/{courseId}

Rules:

* Create enrollment record
* Default progressPercent = 0
* Default status = ENROLLED

---

### 4) Update Progress (PathVariable + RequestBody required)

PUT /api/enrollments/{enrollmentId}/progress

Request example:
{
"progressPercent": 50
}

Rules:

* progressPercent must be 0–100
* If progressPercent = 100 → status becomes COMPLETED

---

### 5) Get Enrollments by Student (Query parameter required)

GET /api/enrollments?studentId=1

Rules:

* Must use @RequestParam

---

### 6) Analytics Endpoint (Streams Required)

GET /api/students/{studentId}/analytics

Response must include:

* total enrollments
* count by status (ENROLLED / COMPLETED / DROPPED)
* average progress percent
* list of course titles sorted alphabetically

Streams must be used in the service layer.
No loops allowed in analytics method.

---

## 7) Predicate Requirements (Required)

You must create a class:

* EnrollmentPredicates (or StudentPredicates)

Example predicate rules:

* progressPercent is between 0 and 100
* studentId is valid
* courseId is valid

You must call predicate validations inside service methods before saving.

---

## 8) Stream API Requirements (Required)

You must use Streams in:

* analytics method (required)
* at least one other method (filtering, sorting, mapping)

Examples:

* filter completed enrollments
* map enrollments to course titles
* compute average progress

---

## 9) Bean Scope Requirements (Required)

You must create:

* One singleton bean (default)
* One prototype bean

Expose endpoint:
GET /api/debug/scopes

Response must show:

* singleton instance id stays the same across requests
* prototype instance id changes each request

---

## 10) Spring Profile Requirements (Required)

dev profile

* H2 database
* show SQL enabled
* logging DEBUG

prod profile

* Postgres placeholders using env vars
* logging INFO

---

## 11) Error Handling Requirements (Required)

You must implement @ControllerAdvice.

Error response format:
```json
{
    "code": "VALIDATION_ERROR | NOT_FOUND | INTERNAL_ERROR",
    "message": "Human readable message"
}
```

HTTP rules:

* 400 for invalid input
* 404 if student/course/enrollment not found
* 500 for unexpected errors

---

## 12) Testing Requirements (Minimum 2 Tests)

You must include:

Test 1: Progress validation

* progressPercent must be between 0 and 100

Test 2: Analytics correctness

* count by status + average progress works

---

## 13) Implementation Plan

### Phase 1:

* Setup project
* Create entities + repositories
* Confirm H2 works

### Phase 2:

* Create Student + Course endpoints
* Validate DTOs

### Phase 3:

* Enrollment endpoint
* Get enrollments by student using query params

### Phase 4:

* Update progress endpoint
* Add predicate validations

### Phase 5:

* Analytics endpoint using Streams (no loops)

### Phase 6:

* Bean scope debug endpoint
* Profiles dev/prod setup

### Phase 7:

* Tests
* README
* Postman collection
* Final demo run

---

## 14) Submission Checklist (Must Pass)

Before submitting:

* PostMapping + RequestBody used correctly
* Path variables and query parameters used correctly
* Predicate validation exists and is used in service
* Streams used in analytics (no loops)
* Singleton + prototype scope demo endpoint works
* Spring Data JPA relationships implemented correctly
* Profiles dev and prod exist
* At least 2 tests written
* README + Postman/curl examples included
