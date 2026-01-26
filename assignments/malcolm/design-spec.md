# Malcolm (1-Week Project) — Design Spec (Spring-Focused)

## 1) Project Overview (High-Level)

You will build a Spring Boot REST API called **OrderTrack API**.

OrderTrack is a simple backend service that manages:

* Customers
* Orders
* Order items
* Order status updates

This project is designed to **strengthen Spring fundamentals**, while reinforcing Java 8 features and clean REST API design.

Based on your feedback, the main focus areas are:

* Spring concepts (primary focus)
* RESTful API coding
* @Controller vs REST behavior
* Path variables and query parameters
* Dependency injection styles (@Autowired)
* Java 8 functional interfaces, lambdas, and predicates
* Spring Data JPA + Hibernate

Timebox: 7 days
Goal: A working Spring Boot application that clearly demonstrates core Spring knowledge.

---

## 2) Required Tech Stack

Backend

* Java 8+
* Spring Boot
* Spring Web (MVC)
* Spring Validation
* Spring Data JPA (Hibernate)
* H2 database (dev profile)

Build + Tools

* Maven
* Git
* Postman

Testing

* JUnit + Mockito (minimum 2 tests)

---

## 3) Strict Implementation Rules (Spring-Focused)

### Rule A — Use Spring MVC Correctly (Required)

You must use Spring MVC annotations correctly.

Controller rules:

* Use @Controller (not @RestController)
* Use @ResponseBody on methods that return JSON
* Use @PostMapping, @GetMapping, @PutMapping correctly
* Do not put business logic in controllers

This rule exists to ensure you understand how Spring MVC works internally.

---

### Rule B — RESTful API Design (Required)

Your APIs must follow REST principles:

* Use proper HTTP methods (GET, POST, PUT)
* Use nouns for URLs
* Use HTTP status codes correctly

Example patterns:

* GET /api/orders/{orderId}
* GET /api/orders?customerId=1
* POST /api/orders
* PUT /api/orders/{orderId}/status

---

### Rule C — Dependency Injection Styles (Required)

You must demonstrate all three injection styles:

1. Constructor injection (preferred)

* Use this in most services

2. Field injection

* Use once in a simple component
* Add a comment explaining why it is not recommended

3. Setter injection

* Use for an optional dependency
* Explain its use in README

@Autowired must be used correctly in all three cases.

---

### Rule D — Java 8 Features Must Be Used (Required)

You must demonstrate:

* Functional interfaces
* Lambda expressions
* Predicate usage

These must appear in real business logic, not just demo classes.

---

### Rule E — Spring Data JPA + Hibernate (Required)

You must use:

* @Entity, @Id, @GeneratedValue
* @ManyToOne, @OneToMany relationships
* Spring Data JPA repositories

No JDBC or native SQL required.

---

## 4) Required Folder Structure

Use this structure:

src/main/java/com/company/ordertrack

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

### Customer Entity

Required fields:

* id
* name
* email
* createdAt

### Order Entity

Required fields:

* id
* orderNumber
* status (CREATED / PAID / SHIPPED / CANCELLED)
* createdAt
* customer (Many-to-One relationship)

### OrderItem Entity

Required fields:

* id
* productName
* quantity
* price
* order (Many-to-One relationship)

Relationships:

* One Customer → Many Orders
* One Order → Many OrderItems

---

## 6) Required REST API Endpoints

### 1) Create Customer

POST /api/customers

Request example:
```json
{
    "name": "Malcolm",
    "email": "malcolm@email.com"
}
```

---

### 2) Create Order (PathVariable required)

POST /api/customers/{customerId}/orders

Rules:

* Create order with status CREATED
* Associate order with customer

---

### 3) Add Item to Order

POST /api/orders/{orderId}/items

Request example:
```json
{
    "productName": "Laptop",
    "quantity": 1,
    "price": 1200
}
```

---

### 4) Update Order Status (Query parameter required)

PUT /api/orders/{orderId}/status?status=PAID

Rules:

* Only valid status transitions allowed
* Use Predicate to validate status change

---

### 5) Get Orders (Query parameter required)

GET /api/orders?customerId=1

---

### 6) Analytics Endpoint (Streams Required)

GET /api/orders/analytics?customerId=1

Response must include:

* total orders
* count by status
* total order value
* highest value order

Stream API must be used in the service layer.
No loops allowed in analytics method.

---

## 7) Predicate Requirements (Required)

You must create a util class:

* OrderPredicates

Required predicates:

* validOrderStatusTransition
* quantityGreaterThanZero
* priceGreaterThanZero

These predicates must be called in the service layer before saving or updating data.

---

## 8) Java 8 Stream API Requirements (Required)

You must use Streams in:

* analytics endpoint (required)
* at least one other service method (filtering or aggregation)

Examples:

* filter orders by status
* sum order item prices
* find max order value

---

## 9) Spring Dependency Injection Requirements (Required)

You must clearly demonstrate:

* Constructor injection (main usage)
* Field injection (once, with explanation)
* Setter injection (optional dependency)

Your README must include:

* When to use constructor injection
* Why field injection is discouraged
* When setter injection makes sense

---

## 10) Error Handling Requirements (Required)

You must use @ControllerAdvice.

Error response format:
```json
{
    "code": "VALIDATION_ERROR | NOT_FOUND | CONFLICT | INTERNAL_ERROR",
    "message": "Human readable message"
}
```

HTTP rules:

* 400 for validation errors
* 404 for missing customer/order
* 409 for invalid order status transition
* 500 for unexpected errors

---

## 11) Spring Profile Requirements (Required)

dev profile

* H2 database
* show SQL enabled
* logging DEBUG

prod profile

* Postgres placeholders using environment variables
* logging INFO

README must explain how to run both profiles.

---

## 12) Testing Requirements (Minimum 2 Tests)

You must include at least:

Test 1: Order status validation

* Invalid status transition should fail

Test 2: Analytics correctness

* Count by status and total value must be correct

---

## 13) Implementation Plan

### Phase 1:

* Setup project
* Create entities + repositories
* Confirm H2 works

### Phase 2:

* Customer + Order creation APIs
* Practice @Controller + @ResponseBody

### Phase 3:

* Add OrderItem API
* Use @PathVariable correctly

### Phase 4:

* Update order status using query params
* Add Predicate validation

### Phase 5:

* Implement analytics endpoint using Streams
* Add second Stream use-case

### Phase 6:

* Refactor injection styles
* Ensure @Autowired used correctly
* Add global exception handling

### Phase 7:

* Add profiles dev/prod
* Add tests
* Final README + Postman collection

---

## 14) Submission Checklist (Must Pass)

Before submitting:

* Controllers use @Controller and @ResponseBody correctly
* Path variables and query parameters used correctly
* Predicate validation exists and is used in services
* Streams used in analytics (no loops)
* Constructor, field, and setter injection demonstrated
* JPA entities and relationships implemented correctly
* Profiles dev and prod exist
* At least 2 tests included
* README + Postman/curl examples included

---

If you want, I can also generate a **starter project skeleton** (empty classes + TODO comments) so Malcolm can focus purely on learning Spring concepts instead of setup.
