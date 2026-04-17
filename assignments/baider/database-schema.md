Absolutely. Below is a **normalized database schema** for **Project 5: Real-Time Customer Engagement & Notification System**. I’m using **MySQL** as the primary relational database because it fits your stated preference and matches the kind of structured banking data model that aligns with your resume background in SQL, MySQL, Oracle, Spring Boot, and secure transaction systems.

---

# Project 5 — Normalized Database Schema

## 1. Design Goals

This schema is designed to support these main features:

* user notification preferences
* real-time transaction alerts
* fraud/security notifications
* in-app notification center
* email/SMS delivery tracking
* auditability and compliance
* read/unread notification management
* configurable templates by notification type and channel

The schema is normalized mainly to **3NF** so that:

* user data is not duplicated across notification records
* channel definitions are reusable
* preferences are configurable by event type and delivery channel
* delivery attempts are tracked separately from the notification itself
* audit history stays structured and queryable

---

# 2. Core Tables Overview

Here is the recommended table set:

1. `users`
2. `notification_event_types`
3. `notification_channels`
4. `user_contact_methods`
5. `user_notification_preferences`
6. `notification_templates`
7. `business_events`
8. `notifications`
9. `notification_deliveries`
10. `notification_status_history`
11. `audit_logs`

---

# 3. Table-by-Table Schema

---

## A. `users`

Stores the customer or account holder receiving notifications.

### Purpose

Central user table referenced by notifications, preferences, contact methods, and audit records.

### Sample Fields

| Field                   | Data Type          | Notes                             |
| ----------------------- | ------------------ | --------------------------------- |
| `user_id`               | BIGINT PK          | Primary key                       |
| `external_customer_ref` | VARCHAR(50) UNIQUE | Banking/customer system reference |
| `first_name`            | VARCHAR(100)       |                                   |
| `last_name`             | VARCHAR(100)       |                                   |
| `email`                 | VARCHAR(255)       | Primary email                     |
| `phone_number`          | VARCHAR(20)        | Primary mobile number             |
| `status`                | VARCHAR(20)        | ACTIVE, LOCKED, DISABLED          |
| `created_at`            | DATETIME           |                                   |
| `updated_at`            | DATETIME           |                                   |

### Primary Key

* `user_id`

### Relationships

* One user → many contact methods
* One user → many preferences
* One user → many notifications
* One user → many audit logs

---

## B. `notification_event_types`

Defines the business event categories that can trigger notifications.

### Purpose

Prevents hardcoding strings like `"TRANSACTION_POSTED"` or `"FRAUD_ALERT"` everywhere.

### Sample Fields

| Field            | Data Type          | Notes                       |
| ---------------- | ------------------ | --------------------------- |
| `event_type_id`  | BIGINT PK          | Primary key                 |
| `event_code`     | VARCHAR(50) UNIQUE | Example: TRANSACTION_POSTED |
| `event_name`     | VARCHAR(100)       | Human-readable name         |
| `description`    | VARCHAR(255)       |                             |
| `priority_level` | VARCHAR(20)        | LOW, MEDIUM, HIGH, CRITICAL |
| `is_active`      | BOOLEAN            |                             |
| `created_at`     | DATETIME           |                             |

### Example Event Types

* TRANSACTION_POSTED
* LOW_BALANCE
* FRAUD_ALERT
* PAYMENT_CONFIRMED
* LOGIN_ALERT
* PASSWORD_CHANGED

### Primary Key

* `event_type_id`

### Relationships

* One event type → many preferences
* One event type → many templates
* One event type → many business events

---

## C. `notification_channels`

Stores supported delivery channels.

### Purpose

Standardizes the available delivery methods.

### Sample Fields

| Field          | Data Type          | Notes              |
| -------------- | ------------------ | ------------------ |
| `channel_id`   | BIGINT PK          | Primary key        |
| `channel_code` | VARCHAR(30) UNIQUE | IN_APP, EMAIL, SMS |
| `channel_name` | VARCHAR(50)        |                    |
| `is_active`    | BOOLEAN            |                    |
| `created_at`   | DATETIME           |                    |

### Primary Key

* `channel_id`

### Relationships

* One channel → many user preferences
* One channel → many templates
* One channel → many notification deliveries

---

## D. `user_contact_methods`

Stores user-specific contact destinations by channel.

### Purpose

Keeps delivery destinations separate from the main `users` table and allows multiple methods if needed.

### Sample Fields

| Field               | Data Type    | Notes                                         |
| ------------------- | ------------ | --------------------------------------------- |
| `contact_method_id` | BIGINT PK    | Primary key                                   |
| `user_id`           | BIGINT FK    | References `users.user_id`                    |
| `channel_id`        | BIGINT FK    | References `notification_channels.channel_id` |
| `contact_value`     | VARCHAR(255) | Email address, phone number, device token     |
| `is_primary`        | BOOLEAN      |                                               |
| `is_verified`       | BOOLEAN      |                                               |
| `status`            | VARCHAR(20)  | ACTIVE, INACTIVE                              |
| `created_at`        | DATETIME     |                                               |
| `updated_at`        | DATETIME     |                                               |

### Primary Key

* `contact_method_id`

### Foreign Keys

* `user_id` → `users.user_id`
* `channel_id` → `notification_channels.channel_id`

### Relationships

* One user → many contact methods
* One channel → many contact methods

### Why This Table Matters

A user may have:

* one email for email alerts
* one phone for SMS
* multiple verified contact methods in future expansion

---

## E. `user_notification_preferences`

Stores per-user notification settings by event type and channel.

### Purpose

This is the core preference table for opt-in/opt-out logic.

### Sample Fields

| Field               | Data Type          | Notes                                               |
| ------------------- | ------------------ | --------------------------------------------------- |
| `preference_id`     | BIGINT PK          | Primary key                                         |
| `user_id`           | BIGINT FK          | References `users.user_id`                          |
| `event_type_id`     | BIGINT FK          | References `notification_event_types.event_type_id` |
| `channel_id`        | BIGINT FK          | References `notification_channels.channel_id`       |
| `is_enabled`        | BOOLEAN            | User wants this notification/channel                |
| `threshold_amount`  | DECIMAL(15,2) NULL | For balance or transaction thresholds               |
| `quiet_hours_start` | TIME NULL          | Optional                                            |
| `quiet_hours_end`   | TIME NULL          | Optional                                            |
| `created_at`        | DATETIME           |                                                     |
| `updated_at`        | DATETIME           |                                                     |

### Primary Key

* `preference_id`

### Foreign Keys

* `user_id` → `users.user_id`
* `event_type_id` → `notification_event_types.event_type_id`
* `channel_id` → `notification_channels.channel_id`

### Unique Constraint

* (`user_id`, `event_type_id`, `channel_id`)

This prevents duplicate preference rows for the same user/event/channel combination.

### Relationships

* Many preferences belong to one user
* Many preferences belong to one event type
* Many preferences belong to one channel

### Why This Table Matters

This supports features like:

* receive fraud alerts by SMS and email
* receive low-balance alerts only in-app
* receive transaction alerts only above `$500.00`

---

## F. `notification_templates`

Stores reusable message templates by event type and channel.

### Purpose

Lets you separate message content from business logic.

### Sample Fields

| Field              | Data Type         | Notes                                               |
| ------------------ | ----------------- | --------------------------------------------------- |
| `template_id`      | BIGINT PK         | Primary key                                         |
| `event_type_id`    | BIGINT FK         | References `notification_event_types.event_type_id` |
| `channel_id`       | BIGINT FK         | References `notification_channels.channel_id`       |
| `template_name`    | VARCHAR(100)      |                                                     |
| `subject_template` | VARCHAR(255) NULL | Used for email                                      |
| `body_template`    | TEXT              | Message body                                        |
| `is_active`        | BOOLEAN           |                                                     |
| `version_no`       | INT               | For template versioning                             |
| `created_at`       | DATETIME          |                                                     |
| `updated_at`       | DATETIME          |                                                     |

### Primary Key

* `template_id`

### Foreign Keys

* `event_type_id` → `notification_event_types.event_type_id`
* `channel_id` → `notification_channels.channel_id`

### Relationships

* One event type → many templates
* One channel → many templates

### Why This Table Matters

A fraud alert message should not be stored inside application code.
Instead:

* Email template can contain full content
* SMS template can contain a shorter version
* In-app template can contain title + detail message

---

## G. `business_events`

Stores the normalized business event received from upstream systems.

### Purpose

Captures the source event that triggered notification creation.

### Sample Fields

| Field                | Data Type           | Notes                                               |
| -------------------- | ------------------- | --------------------------------------------------- |
| `business_event_id`  | BIGINT PK           | Primary key                                         |
| `event_type_id`      | BIGINT FK           | References `notification_event_types.event_type_id` |
| `event_reference`    | VARCHAR(100) UNIQUE | Transaction ID, fraud case ID, login event ID       |
| `user_id`            | BIGINT FK           | References `users.user_id`                          |
| `source_system`      | VARCHAR(100)        | TRANSACTION_SERVICE, FRAUD_SERVICE                  |
| `event_payload_json` | JSON                | Raw event snapshot                                  |
| `event_occurred_at`  | DATETIME            | When event happened                                 |
| `received_at`        | DATETIME            | When notification platform received it              |
| `created_at`         | DATETIME            |                                                     |

### Primary Key

* `business_event_id`

### Foreign Keys

* `event_type_id` → `notification_event_types.event_type_id`
* `user_id` → `users.user_id`

### Relationships

* One user → many business events
* One event type → many business events
* One business event → many notifications

### Why This Table Matters

This supports:

* replay/debugging
* idempotency checks using `event_reference`
* audit tracing back to the original event

---

## H. `notifications`

Stores the logical notification created for a user.

### Purpose

Represents the notification itself, independent of how many channels it was delivered through.

### Sample Fields

| Field               | Data Type     | Notes                                               |
| ------------------- | ------------- | --------------------------------------------------- |
| `notification_id`   | BIGINT PK     | Primary key                                         |
| `business_event_id` | BIGINT FK     | References `business_events.business_event_id`      |
| `user_id`           | BIGINT FK     | References `users.user_id`                          |
| `event_type_id`     | BIGINT FK     | References `notification_event_types.event_type_id` |
| `title`             | VARCHAR(255)  | Notification title                                  |
| `message_body`      | TEXT          | Rendered message content                            |
| `priority_level`    | VARCHAR(20)   | LOW, HIGH, CRITICAL                                 |
| `status`            | VARCHAR(20)   | CREATED, SENT, FAILED, PARTIAL                      |
| `is_read`           | BOOLEAN       | For in-app notification center                      |
| `read_at`           | DATETIME NULL |                                                     |
| `created_at`        | DATETIME      |                                                     |
| `updated_at`        | DATETIME      |                                                     |

### Primary Key

* `notification_id`

### Foreign Keys

* `business_event_id` → `business_events.business_event_id`
* `user_id` → `users.user_id`
* `event_type_id` → `notification_event_types.event_type_id`

### Relationships

* One user → many notifications
* One business event → many notifications
* One notification → many delivery attempts
* One notification → many status history entries

### Why This Table Matters

This drives the in-app notification center:

* show notification list
* show unread counts
* mark as read
* open detail page

---

## I. `notification_deliveries`

Tracks delivery attempts per channel.

### Purpose

A single notification may be sent through multiple channels, so delivery tracking must be separated from `notifications`.

### Sample Fields

| Field                 | Data Type         | Notes                                               |
| --------------------- | ----------------- | --------------------------------------------------- |
| `delivery_id`         | BIGINT PK         | Primary key                                         |
| `notification_id`     | BIGINT FK         | References `notifications.notification_id`          |
| `channel_id`          | BIGINT FK         | References `notification_channels.channel_id`       |
| `contact_method_id`   | BIGINT FK NULL    | References `user_contact_methods.contact_method_id` |
| `delivery_status`     | VARCHAR(20)       | PENDING, SENT, FAILED, RETRYING                     |
| `provider_message_id` | VARCHAR(100) NULL | External provider reference                         |
| `attempt_count`       | INT               |                                                     |
| `last_attempt_at`     | DATETIME NULL     |                                                     |
| `delivered_at`        | DATETIME NULL     |                                                     |
| `failure_reason`      | VARCHAR(255) NULL |                                                     |
| `created_at`          | DATETIME          |                                                     |
| `updated_at`          | DATETIME          |                                                     |

### Primary Key

* `delivery_id`

### Foreign Keys

* `notification_id` → `notifications.notification_id`
* `channel_id` → `notification_channels.channel_id`
* `contact_method_id` → `user_contact_methods.contact_method_id`

### Relationships

* One notification → many delivery records
* One channel → many delivery records
* One contact method → many delivery records

### Why This Table Matters

It supports:

* email + SMS + in-app for one event
* retries
* troubleshooting
* operational dashboards

---

## J. `notification_status_history`

Stores status transitions for a notification.

### Purpose

Maintains a historical timeline of notification state changes.

### Sample Fields

| Field               | Data Type         | Notes                                      |
| ------------------- | ----------------- | ------------------------------------------ |
| `status_history_id` | BIGINT PK         | Primary key                                |
| `notification_id`   | BIGINT FK         | References `notifications.notification_id` |
| `old_status`        | VARCHAR(20)       | Previous status                            |
| `new_status`        | VARCHAR(20)       | New status                                 |
| `changed_by`        | VARCHAR(100)      | SYSTEM, CUSTOMER, SUPPORT_AGENT            |
| `change_reason`     | VARCHAR(255) NULL |                                            |
| `changed_at`        | DATETIME          |                                            |

### Primary Key

* `status_history_id`

### Foreign Keys

* `notification_id` → `notifications.notification_id`

### Relationships

* One notification → many status history rows

### Why This Table Matters

Useful for:

* support troubleshooting
* event traceability
* compliance reviews
* explaining how a notification changed over time

---

## K. `audit_logs`

Stores broader audit activity for compliance and support.

### Purpose

Captures critical system/user actions beyond delivery status.

### Sample Fields

| Field             | Data Type        | Notes                                      |
| ----------------- | ---------------- | ------------------------------------------ |
| `audit_log_id`    | BIGINT PK        | Primary key                                |
| `user_id`         | BIGINT FK NULL   | References `users.user_id`                 |
| `notification_id` | BIGINT FK NULL   | References `notifications.notification_id` |
| `action_type`     | VARCHAR(50)      | PREFERENCE_UPDATED, NOTIFICATION_READ      |
| `action_details`  | JSON             | Structured metadata                        |
| `performed_by`    | VARCHAR(100)     | SYSTEM, USER, ADMIN                        |
| `performed_at`    | DATETIME         |                                            |
| `source_ip`       | VARCHAR(45) NULL | Optional                                   |
| `created_at`      | DATETIME         |                                            |

### Primary Key

* `audit_log_id`

### Foreign Keys

* `user_id` → `users.user_id`
* `notification_id` → `notifications.notification_id`

### Relationships

* One user → many audit logs
* One notification → many audit logs

### Why This Table Matters

Important for banking-style requirements:

* proving preference changes
* showing when a user read an alert
* tracing sensitive security notifications

---

# 4. Relationship Summary

## One-to-Many Relationships

* `users` → `user_contact_methods`

* `users` → `user_notification_preferences`

* `users` → `business_events`

* `users` → `notifications`

* `users` → `audit_logs`

* `notification_event_types` → `user_notification_preferences`

* `notification_event_types` → `notification_templates`

* `notification_event_types` → `business_events`

* `notification_event_types` → `notifications`

* `notification_channels` → `user_contact_methods`

* `notification_channels` → `user_notification_preferences`

* `notification_channels` → `notification_templates`

* `notification_channels` → `notification_deliveries`

* `business_events` → `notifications`

* `notifications` → `notification_deliveries`

* `notifications` → `notification_status_history`

* `notifications` → `audit_logs`

## Many-to-Many Relationships

These are resolved through junction-style tables:

### A. Users ↔ Event Types ↔ Channels

Resolved by `user_notification_preferences`

This allows:

* one user to enable many event types
* one event type to be enabled for many users
* each preference to vary by channel

### B. Users ↔ Channels

Resolved by `user_contact_methods`

This allows:

* one user to have multiple contact methods
* one channel type to apply to many users

---

# 5. Normalization Rationale

## First Normal Form

Each table has atomic values:

* no repeated columns like `email_1`, `email_2`
* no multivalue fields like `preferred_channels = "EMAIL,SMS"`

## Second Normal Form

Non-key fields depend on the full key:

* preference settings belong to the specific user + event + channel combination
* delivery details belong to a specific notification delivery row

## Third Normal Form

No unnecessary transitive dependency:

* channel names are stored in `notification_channels`, not repeated everywhere
* event type definitions are stored in `notification_event_types`
* templates are separated from notifications

---

# 6. How the Schema Supports Project Features

## Feature: Real-Time Transaction Alerts

Supported by:

* `business_events`
* `notifications`
* `notification_deliveries`

Flow:

1. transaction event arrives
2. row inserted into `business_events`
3. notification created in `notifications`
4. delivery attempts tracked in `notification_deliveries`

---

## Feature: User Notification Preferences

Supported by:

* `user_notification_preferences`
* `user_contact_methods`

Examples:

* User enables SMS for fraud alerts
* User disables email for low-balance alerts
* User sets threshold to alert only for transactions above 1000.00

---

## Feature: In-App Notification Center

Supported by:

* `notifications`

Key fields:

* `is_read`
* `read_at`
* `created_at`
* `priority_level`

This enables:

* unread badge count
* recent notification list
* read/unread filtering
* critical alert highlighting

---

## Feature: Email and SMS Delivery Tracking

Supported by:

* `notification_deliveries`
* `user_contact_methods`

This enables:

* multi-channel delivery
* provider tracking
* retries
* failure reason storage

---

## Feature: Audit and Compliance

Supported by:

* `audit_logs`
* `notification_status_history`
* `business_events`

This enables:

* preference change history
* notification status trace
* source-event traceability
* support and compliance investigations

---

## Feature: Template-Based Messaging

Supported by:

* `notification_templates`

This enables:

* different message bodies for email vs SMS vs in-app
* versioning templates safely
* reusing templates across many notifications

---

# 7. Example Interview Explanation

You can explain the schema like this:

“I normalized the schema around users, event types, channels, preferences, business events, notifications, and delivery tracking. The idea was to separate the logical notification from the physical delivery attempts. For example, one fraud alert may generate one notification record, but it can have multiple delivery rows for email, SMS, and in-app. I also separated user preferences by event type and channel so the platform could support flexible opt-in logic without duplicating data.”

That sounds strong in interviews because it shows:

* normalization thinking
* scalability thinking
* traceability thinking
* real-world product design

---

# 8. Suggested Indexes

These are worth mentioning in interviews.

## On `notifications`

* index on (`user_id`, `is_read`, `created_at`)
* index on (`event_type_id`)
* index on (`business_event_id`)

## On `user_notification_preferences`

* unique index on (`user_id`, `event_type_id`, `channel_id`)

## On `business_events`

* unique index on (`event_reference`)
* index on (`user_id`, `event_occurred_at`)

## On `notification_deliveries`

* index on (`notification_id`, `channel_id`)
* index on (`delivery_status`)

These help with:

* unread notification queries
* idempotency checks
* fast preference lookup
* delivery troubleshooting
