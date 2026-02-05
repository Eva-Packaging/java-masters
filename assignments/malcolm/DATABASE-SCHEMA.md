
## 1) Core Tables (Case Lifecycle)

### `users`

Stores system users (agents, supervisors, auditors). If IAM is external (Okta/Cognito), this can store a local shadow profile.

* **PK:** `user_id` (BIGINT)
* Fields:

    * `user_id` BIGINT AUTO_INCREMENT
    * `external_subject` VARCHAR(128) UNIQUE  *(JWT sub / IdP user id)*
    * `email` VARCHAR(255) UNIQUE
    * `display_name` VARCHAR(120)
    * `status` ENUM('ACTIVE','INACTIVE') NOT NULL
    * `created_at` DATETIME NOT NULL
    * `updated_at` DATETIME NOT NULL

### `roles`

* **PK:** `role_id` (SMALLINT)
* Fields:

    * `role_id` SMALLINT AUTO_INCREMENT
    * `role_name` VARCHAR(40) UNIQUE  *(AGENT, SUPERVISOR, AUDITOR, ADMIN)*

### `user_roles` (join table)

Supports many-to-many users ↔ roles.

* **PK (composite):** (`user_id`, `role_id`)
* **FKs:** `user_id → users.user_id`, `role_id → roles.role_id`

**Relationships**

* `users (1) ↔ (M) user_roles (M) ↔ (1) roles`

---

## 2) Dispute Case + Reason Codes + SLA Rules

### `dispute_reason_codes`

Reason codes drive required evidence checklist + SLA rules.

* **PK:** `reason_code_id` (INT)
* Fields:

    * `reason_code_id` INT AUTO_INCREMENT
    * `code` VARCHAR(20) UNIQUE  *(ex: FRAUD_CARD_NOT_PRESENT, DUPLICATE_CHARGE)*
    * `description` VARCHAR(255)
    * `is_active` TINYINT(1) NOT NULL DEFAULT 1

### `sla_policies`

Defines SLA targets per reason code and priority.

* **PK:** `sla_policy_id` (INT)
* **FK:** `reason_code_id → dispute_reason_codes.reason_code_id`
* Fields:

    * `sla_policy_id` INT AUTO_INCREMENT
    * `reason_code_id` INT NOT NULL
    * `priority` ENUM('LOW','MEDIUM','HIGH') NOT NULL
    * `target_hours` INT NOT NULL  *(ex: 48, 72, 120)*
    * `created_at` DATETIME NOT NULL

**Relationships**

* `dispute_reason_codes (1) → (M) sla_policies`

---

## 3) Case Master Record

### `dispute_cases`

The main “case header”.

* **PK:** `case_id` (BIGINT)
* **FKs:**

    * `reason_code_id → dispute_reason_codes.reason_code_id`
    * `created_by → users.user_id`
    * `current_assignee_id → users.user_id` *(nullable)*
* Fields:

    * `case_id` BIGINT AUTO_INCREMENT
    * `case_number` VARCHAR(30) UNIQUE  *(human-friendly identifier)*
    * `customer_ref` VARCHAR(64)  *(tokenized customer id; avoid PII)*
    * `masked_card_last4` CHAR(4)
    * `transaction_ref` VARCHAR(64)  *(bank txn id)*
    * `merchant_name` VARCHAR(120)
    * `transaction_amount` DECIMAL(12,2) NOT NULL
    * `transaction_currency` CHAR(3) NOT NULL
    * `transaction_date` DATETIME NOT NULL
    * `reason_code_id` INT NOT NULL
    * `priority` ENUM('LOW','MEDIUM','HIGH') NOT NULL
    * `current_status` ENUM('NEW','IN_REVIEW','EVIDENCE_REQUESTED','CHARGEBACK_INITIATED','CHARGEBACK_APPROVED','RESOLVED','REJECTED','CLOSED') NOT NULL
    * `sla_due_at` DATETIME NULL  *(computed from SLA policy)*
    * `created_by` BIGINT NOT NULL
    * `current_assignee_id` BIGINT NULL
    * `created_at` DATETIME NOT NULL
    * `updated_at` DATETIME NOT NULL

**Relationships**

* `users (1) → (M) dispute_cases` via `created_by`
* `users (1) → (M) dispute_cases` via `current_assignee_id`
* `dispute_reason_codes (1) → (M) dispute_cases`

---

## 4) Status History + Workflow Integrity

### `case_status_history`

Immutable record of every status transition (helps audit + timeline UI).

* **PK:** `status_history_id` (BIGINT)
* **FKs:**

    * `case_id → dispute_cases.case_id`
    * `changed_by → users.user_id`
* Fields:

    * `status_history_id` BIGINT AUTO_INCREMENT
    * `case_id` BIGINT NOT NULL
    * `from_status` ENUM(...) NULL  *(null for first entry)*
    * `to_status` ENUM(...) NOT NULL
    * `change_reason` VARCHAR(255) NULL
    * `changed_by` BIGINT NOT NULL
    * `changed_at` DATETIME NOT NULL

**Relationship**

* `dispute_cases (1) → (M) case_status_history`

> **Why this supports features:** Case timeline view, SLA reporting by stage, compliance audits, “who changed what and when”.

---

## 5) Assignment History (Queue Management)

### `case_assignments`

Tracks assignment events over time (not just current assignee).

* **PK:** `assignment_id` (BIGINT)
* **FKs:**

    * `case_id → dispute_cases.case_id`
    * `assigned_to → users.user_id`
    * `assigned_by → users.user_id`
* Fields:

    * `assignment_id` BIGINT AUTO_INCREMENT
    * `case_id` BIGINT NOT NULL
    * `assigned_to` BIGINT NOT NULL
    * `assigned_by` BIGINT NOT NULL
    * `assigned_at` DATETIME NOT NULL
    * `unassigned_at` DATETIME NULL
    * `assignment_type` ENUM('MANUAL','AUTO','REASSIGN') NOT NULL

**Relationship**

* `dispute_cases (1) → (M) case_assignments`

> Supports “My Queue”, reassignment, supervisor load balancing, historical accountability.

---

## 6) Notes + Evidence Requests

### `case_notes`

Free-form notes added by agents/supervisors.

* **PK:** `note_id` (BIGINT)
* **FKs:** `case_id → dispute_cases.case_id`, `created_by → users.user_id`
* Fields:

    * `note_id` BIGINT AUTO_INCREMENT
    * `case_id` BIGINT NOT NULL
    * `note_type` ENUM('INTERNAL','CUSTOMER_VISIBLE') NOT NULL
    * `note_text` TEXT NOT NULL
    * `created_by` BIGINT NOT NULL
    * `created_at` DATETIME NOT NULL

**Relationship**

* `dispute_cases (1) → (M) case_notes`

### `evidence_requirements`

Configurable checklist per reason code.

* **PK:** `requirement_id` (INT)
* **FK:** `reason_code_id → dispute_reason_codes.reason_code_id`
* Fields:

    * `requirement_id` INT AUTO_INCREMENT
    * `reason_code_id` INT NOT NULL
    * `requirement_name` VARCHAR(120) NOT NULL *(ex: “Proof of authorization”, “Police report”)*
    * `is_mandatory` TINYINT(1) NOT NULL DEFAULT 1
    * `is_active` TINYINT(1) NOT NULL DEFAULT 1

### `case_evidence_requests`

When an agent requests evidence from the customer (or internal team).

* **PK:** `evidence_request_id` (BIGINT)
* **FKs:** `case_id → dispute_cases.case_id`, `requested_by → users.user_id`
* Fields:

    * `evidence_request_id` BIGINT AUTO_INCREMENT
    * `case_id` BIGINT NOT NULL
    * `requested_by` BIGINT NOT NULL
    * `requested_at` DATETIME NOT NULL
    * `due_at` DATETIME NULL
    * `status` ENUM('OPEN','FULFILLED','EXPIRED','CANCELLED') NOT NULL

### `case_evidence_request_items` (join table)

Maps a request to required checklist items.

* **PK (composite):** (`evidence_request_id`, `requirement_id`)
* **FKs:**

    * `evidence_request_id → case_evidence_requests.evidence_request_id`
    * `requirement_id → evidence_requirements.requirement_id`

**Relationships**

* `dispute_reason_codes (1) → (M) evidence_requirements`
* `dispute_cases (1) → (M) case_evidence_requests`
* `case_evidence_requests (1) ↔ (M) case_evidence_request_items (M) ↔ (1) evidence_requirements`

> Supports “required evidence checklist”, “request evidence” action, due dates, and tracking what’s still missing.

---

## 7) Evidence Metadata (Files in S3, Metadata in MySQL)

### `case_evidence_files`

Stores metadata only (objects stored in S3).

* **PK:** `evidence_file_id` (BIGINT)
* **FKs:**

    * `case_id → dispute_cases.case_id`
    * `uploaded_by → users.user_id`
    * `requirement_id → evidence_requirements.requirement_id` *(nullable, if file maps to a requirement)*
* Fields:

    * `evidence_file_id` BIGINT AUTO_INCREMENT
    * `case_id` BIGINT NOT NULL
    * `requirement_id` INT NULL
    * `file_name` VARCHAR(255) NOT NULL
    * `content_type` VARCHAR(120) NOT NULL
    * `file_size_bytes` BIGINT NOT NULL
    * `sha256_hash` CHAR(64) NULL
    * `storage_provider` ENUM('S3') NOT NULL
    * `storage_bucket` VARCHAR(128) NOT NULL
    * `storage_key` VARCHAR(512) NOT NULL
    * `uploaded_by` BIGINT NOT NULL
    * `uploaded_at` DATETIME NOT NULL
    * `is_deleted` TINYINT(1) NOT NULL DEFAULT 0

**Relationship**

* `dispute_cases (1) → (M) case_evidence_files`

> Supports evidence listing, downloading via presigned URLs, linking evidence to checklist items, and immutable history.

---

## 8) Compliance / Audit Logging (Immutable)

### `audit_log`

Append-only log capturing important actions across the system.

* **PK:** `audit_id` (BIGINT)
* **FKs:** `case_id → dispute_cases.case_id` *(nullable for non-case actions)*, `actor_user_id → users.user_id`
* Fields:

    * `audit_id` BIGINT AUTO_INCREMENT
    * `case_id` BIGINT NULL
    * `actor_user_id` BIGINT NOT NULL
    * `action_type` VARCHAR(60) NOT NULL *(CASE_CREATED, STATUS_CHANGED, EVIDENCE_UPLOADED, ASSIGNED, NOTE_ADDED, etc.)*
    * `action_summary` VARCHAR(255) NOT NULL
    * `entity_type` VARCHAR(60) NULL
    * `entity_id` VARCHAR(64) NULL
    * `ip_address` VARCHAR(45) NULL
    * `user_agent` VARCHAR(255) NULL
    * `correlation_id` VARCHAR(64) NULL
    * `created_at` DATETIME NOT NULL

**Relationship**

* `dispute_cases (1) → (M) audit_log` (optional link)

> Supports compliance, “who did what”, and debugging with correlation IDs.

---

## 9) Relationship Summary (Quick View)

**One-to-Many**

* `users → dispute_cases` (created_by)
* `users → dispute_cases` (current_assignee_id)
* `dispute_cases → case_status_history`
* `dispute_cases → case_notes`
* `dispute_cases → case_assignments`
* `dispute_cases → case_evidence_files`
* `dispute_cases → case_evidence_requests`
* `dispute_reason_codes → evidence_requirements`
* `dispute_reason_codes → sla_policies`

**Many-to-Many**

* `users ↔ roles` via `user_roles`
* `case_evidence_requests ↔ evidence_requirements` via `case_evidence_request_items`

---

## 10) How This Schema Supports Key Features

* **Case intake + queue:** `dispute_cases` with indexes on `current_status`, `priority`, `sla_due_at`, `current_assignee_id`
* **Workflow timeline:** `case_status_history` powers the timeline UI + audits
* **Assignment history + supervisor balancing:** `case_assignments` keeps reassignment trail
* **Evidence checklist per dispute type:** `evidence_requirements` tied to `reason_code`
* **Evidence requests:** `case_evidence_requests` + `case_evidence_request_items` show what’s missing + due dates
* **File management:** `case_evidence_files` stores S3 metadata + links to checklist items
* **Compliance:** `audit_log` provides immutable, searchable actions
