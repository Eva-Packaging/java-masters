
## Core Design Notes (how to explain it)

* **Exceptions** are the main business object (a “trade break” / “case”).
* **Reference tables** (status, severity, category, resolution code) keep data normalized and filterable.
* **Assignment history** supports “who had it when” (audit + workload reporting).
* **Comments** and **attachments** support collaboration and evidence.
* **Audit events** provide immutable compliance traceability.
* **Users/Roles** enable JWT/RBAC alignment (ops vs compliance vs admin). 

---

# A) Identity & RBAC (Users, Roles)

## 1) `app_user`

**PK:** `user_id`
**Fields (sample):**

* `user_id` NUMBER(19)
* `username` VARCHAR2(80) UNIQUE NOT NULL
* `email` VARCHAR2(120) UNIQUE
* `display_name` VARCHAR2(120)
* `is_active` CHAR(1) DEFAULT 'Y'
* `created_at` TIMESTAMP DEFAULT SYSTIMESTAMP

**Supports:** assignment, “assigned_to” filters, audit “who did it”.

## 2) `app_role`

**PK:** `role_id`
**Fields:**

* `role_id` NUMBER(19)
* `role_name` VARCHAR2(50) UNIQUE NOT NULL  *(e.g., OPS_AGENT, COMPLIANCE, ADMIN)*
* `description` VARCHAR2(200)

## 3) `user_role`

**PK (composite):** (`user_id`, `role_id`)
**FKs:** `user_id → app_user.user_id`, `role_id → app_role.role_id`

**Relationship:** Many-to-many (Users ↔ Roles)

**Supports:** RBAC enforcement, admin-only screens, compliance read-only access.

---

# B) Reference/Lookup Tables (Normalized)

## 4) `exception_status`

**PK:** `status_id`
**Fields:**

* `status_id` NUMBER(19)
* `status_code` VARCHAR2(30) UNIQUE NOT NULL *(NEW, IN_PROGRESS, RESOLVED, REOPENED)*
* `is_terminal` CHAR(1) DEFAULT 'N'

## 5) `severity_level`

**PK:** `severity_id`
**Fields:**

* `severity_id` NUMBER(19)
* `severity_code` VARCHAR2(30) UNIQUE NOT NULL *(CRITICAL, HIGH, MEDIUM, LOW)*
* `sla_minutes` NUMBER(10) NOT NULL  *(e.g., 120, 480)*
* `default_priority` NUMBER(5)

**Supports:** SLA logic and queue prioritization.

## 6) `exception_category`

**PK:** `category_id`
**Fields:**

* `category_id` NUMBER(19)
* `category_code` VARCHAR2(50) UNIQUE NOT NULL *(REF_DATA_MISSING, SETTLEMENT_MISMATCH, ALLOCATION_ERROR...)*
* `description` VARCHAR2(200)

**Supports:** break reason grouping + filtering.

## 7) `resolution_code`

**PK:** `resolution_id`
**Fields:**

* `resolution_id` NUMBER(19)
* `resolution_code` VARCHAR2(50) UNIQUE NOT NULL *(FIXED_REF_DATA, MANUAL_OVERRIDE, CANCELLED_TRADE...)*
* `description` VARCHAR2(200)

**Supports:** consistent reporting on how breaks were resolved.

---

# C) Exception (Trade Break) Core Tables

## 8) `trade_exception`

**PK:** `exception_id`
**FKs:**

* `status_id → exception_status.status_id`
* `severity_id → severity_level.severity_id`
* `category_id → exception_category.category_id`
* `created_by → app_user.user_id`
* `current_assignee_id → app_user.user_id` *(nullable)*

**Fields (sample):**

* `exception_id` NUMBER(19)
* `external_trade_id` VARCHAR2(80) NOT NULL  *(id from upstream trade system)*
* `source_system` VARCHAR2(60) NOT NULL  *(e.g., OMS, PMS, SettlementEngine)*
* `product_type` VARCHAR2(40)  *(EQUITY, FX, IRS...)*
* `trade_date` DATE
* `settlement_date` DATE
* `status_id` NUMBER(19) NOT NULL
* `severity_id` NUMBER(19) NOT NULL
* `category_id` NUMBER(19) NOT NULL
* `priority` NUMBER(5) NOT NULL
* `summary` VARCHAR2(250)
* `details` CLOB  *(normalized alternative is a child table for key/value; see optional section)*
* `created_by` NUMBER(19) NOT NULL
* `current_assignee_id` NUMBER(19) NULL
* `created_at` TIMESTAMP DEFAULT SYSTIMESTAMP
* `updated_at` TIMESTAMP
* `version_no` NUMBER(10)  *(optimistic locking)*

**Supports features:**

* Queue filtering by status/severity/category/assignee
* Sorting by priority/date
* Concurrency control with `version_no`

**Relationship:**

* One `trade_exception` → many comments, attachments, assignments, SLA records, audit events.

---

# D) Assignment & Workflow History

## 9) `exception_assignment`

Tracks assignment changes over time (important for ops metrics + audit).

**PK:** `assignment_id`
**FKs:**

* `exception_id → trade_exception.exception_id`
* `assigned_to → app_user.user_id`
* `assigned_by → app_user.user_id`

**Fields:**

* `assignment_id` NUMBER(19)
* `exception_id` NUMBER(19) NOT NULL
* `assigned_to` NUMBER(19) NOT NULL
* `assigned_by` NUMBER(19) NOT NULL
* `assigned_at` TIMESTAMP DEFAULT SYSTIMESTAMP
* `unassigned_at` TIMESTAMP NULL
* `assignment_note` VARCHAR2(200)

**Supports:** “who owned this case when”, workload analytics, reassignment trails.

---

# E) Collaboration (Comments & Attachments)

## 10) `exception_comment`

**PK:** `comment_id`
**FKs:** `exception_id → trade_exception.exception_id`, `author_id → app_user.user_id`

**Fields:**

* `comment_id` NUMBER(19)
* `exception_id` NUMBER(19) NOT NULL
* `author_id` NUMBER(19) NOT NULL
* `comment_text` CLOB NOT NULL
* `created_at` TIMESTAMP DEFAULT SYSTIMESTAMP

**Supports:** ops notes, compliance notes, collaboration.

## 11) `exception_attachment`

Store metadata (file is in S3 / document store; DB stores pointer).

**PK:** `attachment_id`
**FKs:** `exception_id → trade_exception.exception_id`, `uploaded_by → app_user.user_id`

**Fields:**

* `attachment_id` NUMBER(19)
* `exception_id` NUMBER(19) NOT NULL
* `file_name` VARCHAR2(255) NOT NULL
* `content_type` VARCHAR2(100)
* `file_size_bytes` NUMBER(19)
* `storage_url` VARCHAR2(500) NOT NULL  *(S3 pre-signed URL or object key)*
* `uploaded_by` NUMBER(19) NOT NULL
* `uploaded_at` TIMESTAMP DEFAULT SYSTIMESTAMP

**Supports:** evidence for resolution + audits.

---

# F) SLA Tracking + Breach Visibility

## 12) `exception_sla`

You can compute SLA on the fly, but persisting enables fast queue filters (breached/not breached).

**PK:** `sla_id`
**FK:** `exception_id → trade_exception.exception_id`

**Fields:**

* `sla_id` NUMBER(19)
* `exception_id` NUMBER(19) UNIQUE NOT NULL
* `sla_start_at` TIMESTAMP NOT NULL
* `sla_due_at` TIMESTAMP NOT NULL
* `breached_at` TIMESTAMP NULL
* `is_breached` CHAR(1) DEFAULT 'N'

**Supports:** queue filter `is_breached=Y`, SLA dashboards, ops escalation.

---

# G) Resolution (When an exception is closed)

## 13) `exception_resolution`

**PK:** `exception_id` *(also FK to exception; 1-to-1)*
**FKs:**

* `exception_id → trade_exception.exception_id`
* `resolution_id → resolution_code.resolution_id`
* `resolved_by → app_user.user_id`

**Fields:**

* `exception_id` NUMBER(19)
* `resolution_id` NUMBER(19) NOT NULL
* `resolution_notes` CLOB
* `resolved_by` NUMBER(19) NOT NULL
* `resolved_at` TIMESTAMP DEFAULT SYSTIMESTAMP

**Relationship:** One-to-one (Exception ↔ Resolution)
**Supports:** closure reporting, consistent resolution categories, compliance evidence.

---

# H) Immutable Audit Trail (Compliance-grade)

## 14) `audit_event`

Immutable “who did what” record. (Even if you also publish events, this table persists them.)

**PK:** `audit_event_id`
**FKs:** `actor_user_id → app_user.user_id`, `exception_id → trade_exception.exception_id` *(nullable for non-exception events)*

**Fields:**

* `audit_event_id` NUMBER(19)
* `event_type` VARCHAR2(60) NOT NULL *(EXCEPTION_CREATED, STATUS_CHANGED, ASSIGNED, COMMENT_ADDED, RESOLVED...)*
* `exception_id` NUMBER(19) NULL
* `actor_user_id` NUMBER(19) NOT NULL
* `event_at` TIMESTAMP DEFAULT SYSTIMESTAMP
* `before_state` CLOB NULL  *(JSON snapshot of selected fields)*
* `after_state` CLOB NULL
* `correlation_id` VARCHAR2(80) NULL

**Supports:** compliance searches, timeline view, “prove who changed what”.

---

## Relationship Summary (what to say in interviews)

### One-to-Many

* `trade_exception` → `exception_comment`
* `trade_exception` → `exception_attachment`
* `trade_exception` → `exception_assignment`
* `trade_exception` → `audit_event`

### Many-to-Many

* `app_user` ↔ `app_role` via `user_role`

### One-to-One

* `trade_exception` ↔ `exception_resolution`
* `trade_exception` ↔ `exception_sla`

---

## Indexing Hints (for queue performance)

To support fast queue filters:

* Index `trade_exception(status_id, severity_id, category_id)`
* Index `trade_exception(current_assignee_id, priority, created_at)`
* Index `exception_sla(is_breached, sla_due_at)`
* Index `audit_event(exception_id, event_at)`

*(You can mention Oracle indexing/partitioning if the dataset is large.)*

---

## How the schema supports main features (mapping)

* **Queue filters:** `trade_exception` + lookup FKs + `exception_sla.is_breached`
* **Assignment workflow:** `trade_exception.current_assignee_id` + `exception_assignment` history
* **Collaboration:** `exception_comment`, `exception_attachment`
* **Resolution:** `exception_resolution` + `resolution_code`
* **Compliance/Audit timeline:** `audit_event` (plus assignment/comments also linkable)
* **RBAC:** `app_user`, `app_role`, `user_role`

