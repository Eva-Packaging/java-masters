## Core Identity & RBAC

### `users`

* **PK:** `user_id UUID`
* Fields:

    * `email VARCHAR(255) UNIQUE NOT NULL`
    * `full_name VARCHAR(200) NOT NULL`
    * `password_hash VARCHAR(255) NOT NULL` *(or external IdP)*
    * `is_active BOOLEAN NOT NULL DEFAULT TRUE`
    * `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

### `roles`

* **PK:** `role_id UUID`
* Fields:

    * `role_name VARCHAR(50) UNIQUE NOT NULL` *(ADJUSTER, INVESTIGATOR, ADMIN)*
    * `description VARCHAR(200)`

### `user_roles` (many-to-many)

* **PK (composite):** `(user_id, role_id)`
* **FKs:** `user_id → users.user_id`, `role_id → roles.role_id`
* Fields:

    * `assigned_at TIMESTAMPTZ NOT NULL DEFAULT now()`

**Relationships**

* `users (1) ↔ (M) user_roles (M) ↔ (1) roles`

---

## Policy & Claim Data

### `policies`

* **PK:** `policy_id UUID`
* Fields:

    * `policy_number VARCHAR(40) UNIQUE NOT NULL`
    * `policy_type VARCHAR(50) NOT NULL` *(AUTO, HOME, etc.)*
    * `status VARCHAR(20) NOT NULL`
    * `effective_date DATE NOT NULL`
    * `expiration_date DATE NOT NULL`
    * `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

### `claimants`

* **PK:** `claimant_id UUID`
* Fields:

    * `first_name VARCHAR(80) NOT NULL`
    * `last_name VARCHAR(80) NOT NULL`
    * `dob DATE`
    * `phone VARCHAR(30)`
    * `email VARCHAR(255)`
    * `address_line1 VARCHAR(120)`
    * `city VARCHAR(80)`
    * `state VARCHAR(2)`
    * `postal_code VARCHAR(15)`

### `claims`

* **PK:** `claim_id UUID`
* **FKs:**

    * `policy_id → policies.policy_id`
    * `claimant_id → claimants.claimant_id`
    * `created_by_user_id → users.user_id`
* Fields:

    * `claim_number VARCHAR(40) UNIQUE NOT NULL`
    * `claim_type VARCHAR(50) NOT NULL`
    * `status VARCHAR(30) NOT NULL` *(SUBMITTED, IN_REVIEW, APPROVED, DENIED, etc.)*
    * `loss_date DATE NOT NULL`
    * `reported_at TIMESTAMPTZ NOT NULL DEFAULT now()`
    * `loss_amount_estimate NUMERIC(12,2)`
    * `description TEXT`
    * `region_code VARCHAR(10)`
    * `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`

### `claim_status_history`

* **PK:** `claim_status_history_id UUID`
* **FKs:** `claim_id → claims.claim_id`, `changed_by_user_id → users.user_id`
* Fields:

    * `from_status VARCHAR(30)`
    * `to_status VARCHAR(30) NOT NULL`
    * `change_reason VARCHAR(200)`
    * `changed_at TIMESTAMPTZ NOT NULL DEFAULT now()`

**Relationships**

* `policies (1) → (M) claims`
* `claimants (1) → (M) claims`
* `claims (1) → (M) claim_status_history`

---

## Document / Evidence Handling

### `claim_documents`

* **PK:** `document_id UUID`
* **FKs:** `claim_id → claims.claim_id`, `uploaded_by_user_id → users.user_id`
* Fields:

    * `document_type VARCHAR(50) NOT NULL` *(POLICE_REPORT, PHOTO, INVOICE…)*
    * `file_name VARCHAR(255) NOT NULL`
    * `content_type VARCHAR(100)`
    * `file_size_bytes BIGINT`
    * `storage_provider VARCHAR(20) NOT NULL` *(S3/GCS/AZURE)*
    * `storage_key VARCHAR(500) NOT NULL` *(object key/path)*
    * `sha256_hash VARCHAR(64)`
    * `uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()`

**Relationships**

* `claims (1) → (M) claim_documents`

---

## Fraud Scoring (Score + Reasons/Signals)

### `fraud_scores`

* **PK:** `fraud_score_id UUID`
* **FKs:** `claim_id → claims.claim_id` *(typically unique per claim per scoring version/run)*
* Fields:

    * `score NUMERIC(5,2) NOT NULL` *(0–100 or 0–1 scaled)*
    * `risk_level VARCHAR(20) NOT NULL` *(LOW/MED/HIGH)*
    * `scoring_version VARCHAR(40) NOT NULL`
    * `scored_at TIMESTAMPTZ NOT NULL DEFAULT now()`
* Constraints:

    * Optional: `UNIQUE (claim_id, scoring_version)` (or allow multiple runs per version)

### `fraud_signals`

* **PK:** `fraud_signal_id UUID`
* **FKs:** `fraud_score_id → fraud_scores.fraud_score_id`
* Fields:

    * `signal_code VARCHAR(60) NOT NULL` *(DUPLICATE_ADDRESS, RAPID_REPEAT_CLAIMS…)*
    * `signal_description VARCHAR(255)`
    * `weight NUMERIC(6,3) NOT NULL`
    * `evidence_value VARCHAR(255)` *(e.g., “3 claims in 30 days”)*
* Index suggestion:

    * `(fraud_score_id)`, `(signal_code)`

**Relationships**

* `claims (1) → (M) fraud_scores`
* `fraud_scores (1) → (M) fraud_signals`

> This design keeps “reasons” normalized as rows (not a JSON blob), making it filterable and auditable.

---

## Investigation Case Management (Queue + Workflow)

### `investigation_cases`

* **PK:** `case_id UUID`
* **FKs:** `claim_id → claims.claim_id`, `created_by_user_id → users.user_id`
* Fields:

    * `case_status VARCHAR(30) NOT NULL` *(OPEN, IN_REVIEW, ESCALATED, CLOSED)*
    * `priority VARCHAR(20) NOT NULL` *(LOW/MED/HIGH)*
    * `queue_name VARCHAR(60) NOT NULL` *(Region/Type based queue)*
    * `sla_due_at TIMESTAMPTZ` *(for SLA tracking)*
    * `opened_at TIMESTAMPTZ NOT NULL DEFAULT now()`
    * `closed_at TIMESTAMPTZ`
* Constraints:

    * Optional: `UNIQUE (claim_id)` *(if you want 1 case per claim)*

### `case_assignments`

* **PK:** `case_assignment_id UUID`
* **FKs:** `case_id → investigation_cases.case_id`, `assignee_user_id → users.user_id`, `assigned_by_user_id → users.user_id`
* Fields:

    * `assigned_at TIMESTAMPTZ NOT NULL DEFAULT now()`
    * `unassigned_at TIMESTAMPTZ`
    * `is_primary BOOLEAN NOT NULL DEFAULT TRUE`
* Note:

    * This supports reassignment history without overwriting.

### `case_notes`

* **PK:** `case_note_id UUID`
* **FKs:** `case_id → investigation_cases.case_id`, `created_by_user_id → users.user_id`
* Fields:

    * `note_type VARCHAR(30) NOT NULL` *(NOTE, DECISION, REQUEST_INFO)*
    * `note_text TEXT NOT NULL`
    * `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

### `case_status_history`

* **PK:** `case_status_history_id UUID`
* **FKs:** `case_id → investigation_cases.case_id`, `changed_by_user_id → users.user_id`
* Fields:

    * `from_status VARCHAR(30)`
    * `to_status VARCHAR(30) NOT NULL`
    * `reason VARCHAR(200)`
    * `changed_at TIMESTAMPTZ NOT NULL DEFAULT now()`

**Relationships**

* `claims (1) → (0..1 or M) investigation_cases`
* `investigation_cases (1) → (M) case_assignments`
* `investigation_cases (1) → (M) case_notes`
* `investigation_cases (1) → (M) case_status_history`

---

## Rules Engine (Optional but strong)

### `fraud_rules`

* **PK:** `rule_id UUID`
* Fields:

    * `rule_code VARCHAR(60) UNIQUE NOT NULL`
    * `rule_name VARCHAR(120) NOT NULL`
    * `is_active BOOLEAN NOT NULL DEFAULT TRUE`
    * `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

### `fraud_rule_versions`

* **PK:** `rule_version_id UUID`
* **FK:** `rule_id → fraud_rules.rule_id`
* Fields:

    * `version INT NOT NULL`
    * `condition_type VARCHAR(40) NOT NULL` *(THRESHOLD, MATCH, COUNT_WINDOW…)*
    * `condition_value VARCHAR(255) NOT NULL` *(e.g., “3 in 30 days”)*
    * `weight NUMERIC(6,3) NOT NULL`
    * `effective_from TIMESTAMPTZ NOT NULL`
    * `effective_to TIMESTAMPTZ`
* Constraint:

    * `UNIQUE (rule_id, version)`

**Relationships**

* `fraud_rules (1) → (M) fraud_rule_versions`

---

## Audit & Compliance (Append-only)

### `audit_events`

* **PK:** `audit_event_id UUID`
* **FKs (nullable where appropriate):**

    * `actor_user_id → users.user_id`
    * `claim_id → claims.claim_id`
    * `case_id → investigation_cases.case_id`
* Fields:

    * `event_type VARCHAR(60) NOT NULL` *(CLAIM_CREATED, CASE_ASSIGNED, DOC_UPLOADED…)*
    * `entity_type VARCHAR(40) NOT NULL` *(CLAIM, CASE, DOCUMENT, RULE…)*
    * `entity_id UUID NOT NULL`
    * `event_time TIMESTAMPTZ NOT NULL DEFAULT now()`
    * `ip_address INET`
    * `user_agent VARCHAR(300)`
    * `metadata JSONB` *(small, non-sensitive extras; keep core fields normalized above)*

**Relationships**

* `users (1) → (M) audit_events`
* `claims (1) → (M) audit_events`
* `investigation_cases (1) → (M) audit_events`

---

## How this schema supports the project features

* **Claim intake + lifecycle:** `claims`, `claim_status_history`
* **Document uploads:** `claim_documents` (stores metadata + object key)
* **Fraud score + explainability:** `fraud_scores` + `fraud_signals` (normalized reasons, filterable)
* **Investigation queue + workflow:** `investigation_cases`, `case_assignments`, `case_notes`, `case_status_history`
* **RBAC:** `users`, `roles`, `user_roles`
* **Compliance + auditing:** `audit_events` (append-only, queryable)
