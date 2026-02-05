# Normalized Database Schema (MySQL)

Goal: store **customer consents** (grant/version/expiry/revoke), **third-party apps**, **scopes**, and **audit events** in a way that’s queryable and defensible for compliance.

---

# Core Reference Tables

## 1) `customers`

**PK:** `customer_id` (BIGINT)
**Fields**

* `customer_id` BIGINT PK
* `external_customer_ref` VARCHAR(64) UNIQUE (bank core ID)
* `email` VARCHAR(255) NULL
* `created_at` DATETIME NOT NULL
* `updated_at` DATETIME NOT NULL

**Relationships**

* 1 customer → many `consents`
* 1 customer → many `audit_events`

---

## 2) `third_party_apps`

**PK:** `app_id` (BIGINT)
**Fields**

* `app_id` BIGINT PK
* `client_id` VARCHAR(64) UNIQUE NOT NULL (OAuth client id)
* `app_name` VARCHAR(120) NOT NULL
* `status` ENUM('ACTIVE','SUSPENDED') NOT NULL
* `created_at` DATETIME NOT NULL
* `updated_at` DATETIME NOT NULL

**Relationships**

* 1 app → many `consents`
* 1 app → many `audit_events`
* 1 app → many `app_redirect_uris` (optional)
* many apps ↔ many scopes (via `app_allowed_scopes`)

---

## 3) `scopes`

**PK:** `scope_id` (BIGINT)
**Fields**

* `scope_id` BIGINT PK
* `scope_key` VARCHAR(80) UNIQUE NOT NULL
  (examples: `accounts.read`, `transactions.read`, `balances.read`)
* `description` VARCHAR(255) NULL
* `created_at` DATETIME NOT NULL

**Relationships**

* many apps ↔ many scopes (via `app_allowed_scopes`)
* many consents ↔ many scopes (via `consent_scopes`)

---

## 4) `app_allowed_scopes` (App ↔ Scope mapping)

**PK (composite):** (`app_id`, `scope_id`)
**FKs:** `app_id` → `third_party_apps.app_id`, `scope_id` → `scopes.scope_id`
**Fields**

* `app_id` BIGINT NOT NULL
* `scope_id` BIGINT NOT NULL
* `enabled` TINYINT(1) NOT NULL DEFAULT 1
* `created_at` DATETIME NOT NULL

**Relationship**

* **Many-to-many:** apps ↔ scopes
  Used to prevent an app from requesting scopes it’s not allowed to ever ask for.

---

# Consent Domain Tables

## 5) `consents`

**PK:** `consent_id` (BIGINT)
**FKs:**

* `customer_id` → `customers.customer_id`
* `app_id` → `third_party_apps.app_id`

**Fields**

* `consent_id` BIGINT PK
* `customer_id` BIGINT NOT NULL
* `app_id` BIGINT NOT NULL
* `status` ENUM('ACTIVE','REVOKED','EXPIRED') NOT NULL
* `consent_version` INT NOT NULL DEFAULT 1
* `granted_at` DATETIME NOT NULL
* `expires_at` DATETIME NOT NULL
* `revoked_at` DATETIME NULL
* `revocation_reason` VARCHAR(255) NULL
* `created_at` DATETIME NOT NULL
* `updated_at` DATETIME NOT NULL

**Relationships**

* 1 customer → many consents
* 1 app → many consents
* 1 consent → many `consent_scopes`
* 1 consent → many `consent_accounts` (optional “account-level consent”)
* 1 consent → many `consent_history` (versioning/audit)

**Key Indexes (practical)**

* `(customer_id, app_id, status, expires_at)`
* `(app_id, status, expires_at)`

---

## 6) `consent_scopes` (Consent ↔ Scope mapping)

**PK (composite):** (`consent_id`, `scope_id`)
**FKs:** `consent_id` → `consents.consent_id`, `scope_id` → `scopes.scope_id`
**Fields**

* `consent_id` BIGINT NOT NULL
* `scope_id` BIGINT NOT NULL
* `granted_at` DATETIME NOT NULL

**Relationship**

* **Many-to-many:** consents ↔ scopes
  This is how you answer: “Does this consent allow `transactions.read`?”

---

## 7) `accounts` (bank accounts the customer owns)

**PK:** `account_id` (BIGINT)
**FK:** `customer_id` → `customers.customer_id`
**Fields**

* `account_id` BIGINT PK
* `customer_id` BIGINT NOT NULL
* `account_number_masked` VARCHAR(20) NOT NULL (e.g., `****1234`)
* `account_type` ENUM('CHECKING','SAVINGS','CREDIT','LOAN') NOT NULL
* `status` ENUM('OPEN','CLOSED') NOT NULL
* `created_at` DATETIME NOT NULL

**Relationships**

* 1 customer → many accounts
* many consents ↔ many accounts (via `consent_accounts`)

---

## 8) `consent_accounts` (Consent ↔ Account mapping)

**PK (composite):** (`consent_id`, `account_id`)
**FKs:** `consent_id` → `consents.consent_id`, `account_id` → `accounts.account_id`
**Fields**

* `consent_id` BIGINT NOT NULL
* `account_id` BIGINT NOT NULL

**Relationship**

* **Many-to-many:** consents ↔ accounts
  Supports “consent applies only to these accounts” (common in open banking).

---

## 9) `consent_history` (version snapshots)

**PK:** `history_id` (BIGINT)
**FK:** `consent_id` → `consents.consent_id`
**Fields**

* `history_id` BIGINT PK
* `consent_id` BIGINT NOT NULL
* `version` INT NOT NULL
* `status` ENUM('ACTIVE','REVOKED','EXPIRED') NOT NULL
* `snapshot_json` JSON NOT NULL  *(or normalize further if you prefer)*
* `changed_at` DATETIME NOT NULL
* `changed_by` VARCHAR(80) NOT NULL  *(customer/admin/system)*

**Relationship**

* 1 consent → many history rows
  Supports compliance: “show exactly what user agreed to at version N”.

---

# Audit / Enforcement Tables

## 10) `audit_events`

**PK:** `event_id` (BIGINT)
**FKs (nullable where appropriate):**

* `customer_id` → `customers.customer_id`
* `app_id` → `third_party_apps.app_id`
* `consent_id` → `consents.consent_id` (nullable: some events may occur without consent)

**Fields**

* `event_id` BIGINT PK
* `event_type` ENUM(
  'CONSENT_GRANTED','CONSENT_REVOKED','CONSENT_EXPIRED',
  'ACCESS_ALLOWED','ACCESS_DENIED'
  ) NOT NULL
* `customer_id` BIGINT NULL
* `app_id` BIGINT NULL
* `consent_id` BIGINT NULL
* `requested_scopes` VARCHAR(400) NULL  *(store as delimited, or create join table below)*
* `resource` VARCHAR(120) NOT NULL  *(e.g., /accounts, /transactions)*
* `http_method` VARCHAR(10) NOT NULL
* `decision` ENUM('ALLOW','DENY') NULL
* `deny_reason` VARCHAR(80) NULL  *(CONSENT_MISSING, EXPIRED, SCOPE_NOT_GRANTED, etc.)*
* `request_id` VARCHAR(64) NOT NULL  *(correlation id)*
* `ip_address` VARCHAR(45) NULL
* `user_agent` VARCHAR(255) NULL
* `event_time` DATETIME NOT NULL

**Key Indexes**

* `(event_time)`
* `(customer_id, event_time)`
* `(app_id, event_time)`
* `(request_id)` UNIQUE (optional)

> If you want fully normalized scopes per audit event, add the join table below.

---

## 11) `audit_event_scopes` (optional normalization)

**PK (composite):** (`event_id`, `scope_id`)
**FKs:** `event_id` → `audit_events.event_id`, `scope_id` → `scopes.scope_id`
**Fields**

* `event_id` BIGINT NOT NULL
* `scope_id` BIGINT NOT NULL

**Relationship**

* **Many-to-many:** audit events ↔ scopes
  Used to prove exactly which scopes were requested per access attempt.

---

# Relationship Summary (quick)

* **customers (1) → (N) consents**
* **third_party_apps (1) → (N) consents**
* **consents (N) ↔ (N) scopes** via `consent_scopes`
* **third_party_apps (N) ↔ (N) scopes** via `app_allowed_scopes`
* **customers (1) → (N) accounts**
* **consents (N) ↔ (N) accounts** via `consent_accounts`
* **consents (1) → (N) consent_history**
* **customers/apps/consents (1) → (N) audit_events**
* *(optional)* **audit_events (N) ↔ (N) scopes** via `audit_event_scopes`

---

# How this schema supports the project features

### Feature: “Grant consent for specific scopes and specific accounts”

* `consents` stores the consent lifecycle + expiry/revocation
* `consent_scopes` ties consent to exact scopes
* `consent_accounts` limits consent to selected accounts (optional but impressive)

### Feature: “Validate consent quickly during API calls”

* Query pattern:

    * find ACTIVE consent by `(customer_id, app_id)` where `expires_at > now`
    * confirm requested scopes exist in `consent_scopes`
    * confirm requested accounts exist in `consent_accounts` (if enforced)

### Feature: “Prevent apps from requesting scopes they’re not allowed to”

* `app_allowed_scopes` provides an allow-list at the app level

### Feature: “Audit evidence for regulators”

* `audit_events` records each decision (ALLOW/DENY) + why
* `consent_history` preserves a versioned snapshot of what was agreed to at the time
