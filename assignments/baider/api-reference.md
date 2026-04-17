Absolutely. Below is a structured **REST API reference** for **Project 5: Real-Time Customer Engagement & Notification System**. It is designed to cover the main workflows of the platform and stay realistic for a Spring Boot microservices project aligned with your resume background in REST APIs, Spring Boot, security, React, and banking-style systems.

I will present this as an interview-friendly reference, not raw implementation code.

---

# Project 5 — REST API Reference

## 1. API Design Overview

## Base Path

```text
/api/v1
```

## Main Functional Areas

* Authentication / session context
* User notification preferences
* Notification center
* Notification delivery/history
* Audit and admin reporting
* Internal event ingestion APIs
* Health and operational endpoints

## API Style

* RESTful JSON APIs
* JWT-secured endpoints
* Standard HTTP status codes
* Pagination for list endpoints
* Filter and sort support for notifications/history

---

# 2. Common Request/Response Conventions

## Standard Headers

```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
Accept: application/json
```

## Common Success Response Shape

```json
{
  "timestamp": "2026-04-15T10:30:00Z",
  "status": 200,
  "message": "Request successful",
  "data": {}
}
```

## Common Error Response Shape

```json
{
  "timestamp": "2026-04-15T10:31:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "path": "/api/v1/preferences"
}
```

---

# 3. Notification Preference APIs

These endpoints support user configuration of notification behavior.

---

## 3.1 Get User Notification Preferences

### HTTP Method and Path

```http
GET /api/v1/preferences
```

### Purpose

Returns all notification preferences for the authenticated user.

### Query Parameters

| Name        | Type    | Required | Description                         |
| ----------- | ------- | -------- | ----------------------------------- |
| `eventType` | string  | No       | Filter by event type                |
| `channel`   | string  | No       | Filter by channel                   |
| `enabled`   | boolean | No       | Filter enabled/disabled preferences |

### Example Request

```http
GET /api/v1/preferences?channel=EMAIL
```

### Example Response

```json
{
  "timestamp": "2026-04-15T10:35:00Z",
  "status": 200,
  "message": "Preferences retrieved successfully",
  "data": [
    {
      "preferenceId": 101,
      "eventType": "TRANSACTION_POSTED",
      "channel": "EMAIL",
      "enabled": true,
      "thresholdAmount": 500.00,
      "quietHoursStart": null,
      "quietHoursEnd": null
    },
    {
      "preferenceId": 102,
      "eventType": "FRAUD_ALERT",
      "channel": "EMAIL",
      "enabled": true,
      "thresholdAmount": null,
      "quietHoursStart": null,
      "quietHoursEnd": null
    }
  ]
}
```

### Error Handling

* `401 Unauthorized` if JWT is missing or invalid
* `403 Forbidden` if user tries to access another user’s preferences
* `500 Internal Server Error` for unexpected backend failure

---

## 3.2 Create or Enable a Notification Preference

### HTTP Method and Path

```http
POST /api/v1/preferences
```

### Purpose

Creates a new preference record for a specific event type and channel.

### Request Body

```json
{
  "eventType": "LOW_BALANCE",
  "channel": "SMS",
  "enabled": true,
  "thresholdAmount": 100.00,
  "quietHoursStart": "22:00:00",
  "quietHoursEnd": "07:00:00"
}
```

### Example Response

```json
{
  "timestamp": "2026-04-15T10:40:00Z",
  "status": 201,
  "message": "Preference created successfully",
  "data": {
    "preferenceId": 201,
    "eventType": "LOW_BALANCE",
    "channel": "SMS",
    "enabled": true,
    "thresholdAmount": 100.00,
    "quietHoursStart": "22:00:00",
    "quietHoursEnd": "07:00:00"
  }
}
```

### Error Handling

* `400 Bad Request` if channel or event type is invalid
* `409 Conflict` if the preference already exists
* `422 Unprocessable Entity` if threshold is invalid for that event type

---

## 3.3 Update a Notification Preference

### HTTP Method and Path

```http
PUT /api/v1/preferences/{preferenceId}
```

### Path Parameters

| Name           | Type | Required | Description          |
| -------------- | ---- | -------- | -------------------- |
| `preferenceId` | long | Yes      | Preference record ID |

### Request Body

```json
{
  "enabled": false,
  "thresholdAmount": 250.00,
  "quietHoursStart": "21:00:00",
  "quietHoursEnd": "06:00:00"
}
```

### Example Response

```json
{
  "timestamp": "2026-04-15T10:42:00Z",
  "status": 200,
  "message": "Preference updated successfully",
  "data": {
    "preferenceId": 201,
    "eventType": "LOW_BALANCE",
    "channel": "SMS",
    "enabled": false,
    "thresholdAmount": 250.00,
    "quietHoursStart": "21:00:00",
    "quietHoursEnd": "06:00:00"
  }
}
```

### Error Handling

* `404 Not Found` if the preference does not exist
* `403 Forbidden` if the preference does not belong to the authenticated user
* `400 Bad Request` for invalid values

---

## 3.4 Delete a Notification Preference

### HTTP Method and Path

```http
DELETE /api/v1/preferences/{preferenceId}
```

### Example Response

```json
{
  "timestamp": "2026-04-15T10:45:00Z",
  "status": 200,
  "message": "Preference deleted successfully",
  "data": {
    "preferenceId": 201
  }
}
```

### Error Handling

* `404 Not Found`
* `403 Forbidden`

---

# 4. Contact Method APIs

These endpoints manage email and phone destinations used for delivery.

---

## 4.1 Get User Contact Methods

### HTTP Method and Path

```http
GET /api/v1/contact-methods
```

### Example Response

```json
{
  "timestamp": "2026-04-15T10:50:00Z",
  "status": 200,
  "message": "Contact methods retrieved successfully",
  "data": [
    {
      "contactMethodId": 11,
      "channel": "EMAIL",
      "contactValue": "user@example.com",
      "isPrimary": true,
      "isVerified": true,
      "status": "ACTIVE"
    },
    {
      "contactMethodId": 12,
      "channel": "SMS",
      "contactValue": "+12406189462",
      "isPrimary": true,
      "isVerified": true,
      "status": "ACTIVE"
    }
  ]
}
```

---

## 4.2 Add Contact Method

### HTTP Method and Path

```http
POST /api/v1/contact-methods
```

### Request Body

```json
{
  "channel": "EMAIL",
  "contactValue": "alerts@example.com",
  "isPrimary": false
}
```

### Example Response

```json
{
  "timestamp": "2026-04-15T10:52:00Z",
  "status": 201,
  "message": "Contact method added successfully",
  "data": {
    "contactMethodId": 15,
    "channel": "EMAIL",
    "contactValue": "alerts@example.com",
    "isPrimary": false,
    "isVerified": false,
    "status": "ACTIVE"
  }
}
```

### Error Handling

* `400 Bad Request` invalid email/phone
* `409 Conflict` duplicate contact method
* `422 Unprocessable Entity` invalid channel-value combination

---

## 4.3 Update Contact Method

### HTTP Method and Path

```http
PUT /api/v1/contact-methods/{contactMethodId}
```

### Request Body

```json
{
  "isPrimary": true,
  "status": "ACTIVE"
}
```

---

## 4.4 Delete Contact Method

### HTTP Method and Path

```http
DELETE /api/v1/contact-methods/{contactMethodId}
```

### Error Handling

* `404 Not Found`
* `409 Conflict` if trying to delete the only required contact method for an enabled preference

---

# 5. Notification Center APIs

These are the main user-facing APIs for in-app notifications.

---

## 5.1 Get Notifications

### HTTP Method and Path

```http
GET /api/v1/notifications
```

### Query Parameters

| Name        | Type    | Required | Description             |
| ----------- | ------- | -------- | ----------------------- |
| `page`      | int     | No       | Page number             |
| `size`      | int     | No       | Page size               |
| `isRead`    | boolean | No       | Read/unread filter      |
| `eventType` | string  | No       | Filter by event type    |
| `priority`  | string  | No       | LOW, HIGH, CRITICAL     |
| `sort`      | string  | No       | Example: createdAt,desc |

### Example Request

```http
GET /api/v1/notifications?page=0&size=10&isRead=false&sort=createdAt,desc
```

### Example Response

```json
{
  "timestamp": "2026-04-15T11:00:00Z",
  "status": 200,
  "message": "Notifications retrieved successfully",
  "data": {
    "content": [
      {
        "notificationId": 9001,
        "eventType": "TRANSACTION_POSTED",
        "title": "Transaction Alert",
        "messageBody": "A debit transaction of $220.00 was posted to your account.",
        "priority": "MEDIUM",
        "isRead": false,
        "createdAt": "2026-04-15T10:58:00Z"
      },
      {
        "notificationId": 9002,
        "eventType": "FRAUD_ALERT",
        "title": "Suspicious Activity Detected",
        "messageBody": "We detected suspicious login activity on your account.",
        "priority": "CRITICAL",
        "isRead": false,
        "createdAt": "2026-04-15T10:55:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

### Error Handling

* `401 Unauthorized`
* `500 Internal Server Error`

---

## 5.2 Get Notification By ID

### HTTP Method and Path

```http
GET /api/v1/notifications/{notificationId}
```

### Path Parameters

| Name             | Type | Required | Description     |
| ---------------- | ---- | -------- | --------------- |
| `notificationId` | long | Yes      | Notification ID |

### Example Response

```json
{
  "timestamp": "2026-04-15T11:02:00Z",
  "status": 200,
  "message": "Notification retrieved successfully",
  "data": {
    "notificationId": 9002,
    "eventType": "FRAUD_ALERT",
    "title": "Suspicious Activity Detected",
    "messageBody": "We detected suspicious login activity on your account at 10:54 AM from a new device.",
    "priority": "CRITICAL",
    "isRead": false,
    "createdAt": "2026-04-15T10:55:00Z",
    "deliveryChannels": ["IN_APP", "EMAIL", "SMS"]
  }
}
```

### Error Handling

* `404 Not Found`
* `403 Forbidden` if user attempts to access another user's notification

---

## 5.3 Mark Notification as Read

### HTTP Method and Path

```http
PATCH /api/v1/notifications/{notificationId}/read
```

### Example Request

```json
{
  "read": true
}
```

### Example Response

```json
{
  "timestamp": "2026-04-15T11:05:00Z",
  "status": 200,
  "message": "Notification marked as read",
  "data": {
    "notificationId": 9002,
    "isRead": true,
    "readAt": "2026-04-15T11:05:00Z"
  }
}
```

### Error Handling

* `404 Not Found`
* `403 Forbidden`

---

## 5.4 Mark All Notifications as Read

### HTTP Method and Path

```http
PATCH /api/v1/notifications/read-all
```

### Example Request

```json
{
  "eventType": "TRANSACTION_POSTED"
}
```

### Example Response

```json
{
  "timestamp": "2026-04-15T11:07:00Z",
  "status": 200,
  "message": "Notifications marked as read",
  "data": {
    "updatedCount": 14
  }
}
```

---

## 5.5 Get Unread Notification Count

### HTTP Method and Path

```http
GET /api/v1/notifications/unread-count
```

### Example Response

```json
{
  "timestamp": "2026-04-15T11:09:00Z",
  "status": 200,
  "message": "Unread count retrieved successfully",
  "data": {
    "unreadCount": 3
  }
}
```

---

# 6. Notification Delivery APIs

These endpoints support channel-level delivery tracking.

---

## 6.1 Get Delivery Status for a Notification

### HTTP Method and Path

```http
GET /api/v1/notifications/{notificationId}/deliveries
```

### Example Response

```json
{
  "timestamp": "2026-04-15T11:15:00Z",
  "status": 200,
  "message": "Delivery records retrieved successfully",
  "data": [
    {
      "deliveryId": 7001,
      "channel": "IN_APP",
      "deliveryStatus": "SENT",
      "attemptCount": 1,
      "deliveredAt": "2026-04-15T10:55:02Z",
      "failureReason": null
    },
    {
      "deliveryId": 7002,
      "channel": "EMAIL",
      "deliveryStatus": "SENT",
      "attemptCount": 1,
      "deliveredAt": "2026-04-15T10:55:05Z",
      "failureReason": null
    },
    {
      "deliveryId": 7003,
      "channel": "SMS",
      "deliveryStatus": "FAILED",
      "attemptCount": 3,
      "deliveredAt": null,
      "failureReason": "Carrier timeout"
    }
  ]
}
```

### Error Handling

* `404 Not Found`
* `403 Forbidden`

---

## 6.2 Retry Failed Delivery

Usually internal/admin only.

### HTTP Method and Path

```http
POST /api/v1/notifications/{notificationId}/deliveries/{deliveryId}/retry
```

### Example Response

```json
{
  "timestamp": "2026-04-15T11:18:00Z",
  "status": 202,
  "message": "Retry request accepted",
  "data": {
    "deliveryId": 7003,
    "status": "RETRY_QUEUED"
  }
}
```

### Error Handling

* `404 Not Found`
* `409 Conflict` if delivery is already successful
* `403 Forbidden` if non-admin attempts retry

---

# 7. Internal Event Ingestion APIs

These APIs can be used by internal systems if event delivery is not exclusively Kafka-based, or for fallback/manual ingestion.

---

## 7.1 Ingest Transaction Event

### HTTP Method and Path

```http
POST /api/v1/internal/events/transactions
```

### Purpose

Accepts a transaction event for notification generation.

### Request Body

```json
{
  "eventReference": "TXN-20260415-1001",
  "userId": 501,
  "eventType": "TRANSACTION_POSTED",
  "amount": 220.00,
  "currency": "USD",
  "transactionType": "DEBIT",
  "accountLast4": "8842",
  "occurredAt": "2026-04-15T10:58:00Z"
}
```

### Example Response

```json
{
  "timestamp": "2026-04-15T11:20:00Z",
  "status": 202,
  "message": "Transaction event accepted for processing",
  "data": {
    "eventReference": "TXN-20260415-1001",
    "processingStatus": "ACCEPTED"
  }
}
```

### Error Handling

* `400 Bad Request` malformed event
* `409 Conflict` duplicate event reference
* `422 Unprocessable Entity` unknown user or invalid event type

---

## 7.2 Ingest Fraud Alert Event

### HTTP Method and Path

```http
POST /api/v1/internal/events/fraud-alerts
```

### Request Body

```json
{
  "eventReference": "FRAUD-20260415-9001",
  "userId": 501,
  "eventType": "FRAUD_ALERT",
  "severity": "CRITICAL",
  "alertMessage": "Suspicious login detected from a new device",
  "occurredAt": "2026-04-15T10:54:00Z"
}
```

### Example Response

```json
{
  "timestamp": "2026-04-15T11:22:00Z",
  "status": 202,
  "message": "Fraud alert accepted for processing",
  "data": {
    "eventReference": "FRAUD-20260415-9001",
    "processingStatus": "ACCEPTED"
  }
}
```

---

## 7.3 Ingest Security/Login Event

### HTTP Method and Path

```http
POST /api/v1/internal/events/security
```

### Example Request

```json
{
  "eventReference": "SEC-20260415-3001",
  "userId": 501,
  "eventType": "LOGIN_ALERT",
  "deviceType": "Chrome on Windows",
  "location": "Baltimore, MD",
  "occurredAt": "2026-04-15T10:53:00Z"
}
```

---

# 8. Audit and Reporting APIs

These are mainly for support, operations, or admin users.

---

## 8.1 Get Audit Logs

### HTTP Method and Path

```http
GET /api/v1/admin/audit-logs
```

### Query Parameters

| Name         | Type     | Required | Description                 |
| ------------ | -------- | -------- | --------------------------- |
| `userId`     | long     | No       | Filter by user              |
| `actionType` | string   | No       | Example: PREFERENCE_UPDATED |
| `from`       | datetime | No       | Start time                  |
| `to`         | datetime | No       | End time                    |
| `page`       | int      | No       | Page number                 |
| `size`       | int      | No       | Page size                   |

### Example Request

```http
GET /api/v1/admin/audit-logs?userId=501&actionType=NOTIFICATION_READ&page=0&size=20
```

### Example Response

```json
{
  "timestamp": "2026-04-15T11:30:00Z",
  "status": 200,
  "message": "Audit logs retrieved successfully",
  "data": {
    "content": [
      {
        "auditLogId": 3001,
        "userId": 501,
        "notificationId": 9002,
        "actionType": "NOTIFICATION_READ",
        "performedBy": "USER",
        "performedAt": "2026-04-15T11:05:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### Error Handling

* `403 Forbidden` if role is not admin/support
* `400 Bad Request` invalid date range

---

## 8.2 Get Notification Metrics Summary

### HTTP Method and Path

```http
GET /api/v1/admin/metrics/notifications
```

### Query Parameters

| Name        | Type     | Required | Description          |
| ----------- | -------- | -------- | -------------------- |
| `from`      | datetime | No       | Start time           |
| `to`        | datetime | No       | End time             |
| `channel`   | string   | No       | Filter by channel    |
| `eventType` | string   | No       | Filter by event type |

### Example Response

```json
{
  "timestamp": "2026-04-15T11:35:00Z",
  "status": 200,
  "message": "Metrics retrieved successfully",
  "data": {
    "totalNotifications": 15240,
    "deliveredCount": 14890,
    "failedCount": 350,
    "successRate": 97.70,
    "topEventTypes": [
      {
        "eventType": "TRANSACTION_POSTED",
        "count": 9200
      },
      {
        "eventType": "FRAUD_ALERT",
        "count": 640
      }
    ]
  }
}
```

---

# 9. Health and Operational APIs

---

## 9.1 Service Health Check

### HTTP Method and Path

```http
GET /api/v1/health
```

### Example Response

```json
{
  "status": "UP",
  "service": "notification-service",
  "timestamp": "2026-04-15T11:40:00Z"
}
```

---

## 9.2 Readiness Check

### HTTP Method and Path

```http
GET /api/v1/health/readiness
```

### Example Response

```json
{
  "status": "UP",
  "database": "UP",
  "kafka": "UP"
}
```

---

# 10. Main Workflow Coverage

## Workflow 1: User Updates Notification Preferences

1. `GET /api/v1/preferences`
2. `POST /api/v1/preferences` or `PUT /api/v1/preferences/{preferenceId}`
3. system stores updated preference
4. audit log is created

## Workflow 2: User Receives Transaction Notification

1. upstream transaction service sends event through Kafka or internal API
2. notification orchestrator processes event
3. notification record created
4. channel delivery records created
5. user calls `GET /api/v1/notifications`
6. frontend shows unread badge via `GET /api/v1/notifications/unread-count`

## Workflow 3: User Opens Notification Center

1. `GET /api/v1/notifications`
2. `GET /api/v1/notifications/{notificationId}`
3. `PATCH /api/v1/notifications/{notificationId}/read`

## Workflow 4: Operations Team Reviews Delivery Failures

1. `GET /api/v1/notifications/{notificationId}/deliveries`
2. `POST /api/v1/notifications/{notificationId}/deliveries/{deliveryId}/retry`
3. `GET /api/v1/admin/audit-logs`

## Workflow 5: Admin Reviews Delivery and Notification Metrics

1. `GET /api/v1/admin/metrics/notifications`
2. `GET /api/v1/admin/audit-logs`

---

# 11. Suggested Controller Structure

For interview discussion, you can say the API layer would be organized like this:

* `PreferenceController`
* `ContactMethodController`
* `NotificationController`
* `DeliveryController`
* `InternalEventController`
* `AuditController`
* `MetricsController`
* `HealthController`

That maps cleanly to a Spring Boot layered design.

---

# 12. Suggested DTOs / Method Signatures

Without giving raw code, here are realistic shapes you can mention.

## Preference APIs

```text
createPreference(CreatePreferenceRequest request)
updatePreference(Long preferenceId, UpdatePreferenceRequest request)
getPreferences(String eventType, String channel, Boolean enabled)
```

## Notification APIs

```text
getNotifications(NotificationSearchRequest request)
getNotificationById(Long notificationId)
markAsRead(Long notificationId)
markAllAsRead(MarkAllReadRequest request)
getUnreadCount()
```

## Internal Event APIs

```text
ingestTransactionEvent(TransactionEventRequest request)
ingestFraudAlertEvent(FraudAlertEventRequest request)
ingestSecurityEvent(SecurityEventRequest request)
```

---

# 13. Error Handling Strategy

## Common Error Scenarios

* invalid JWT
* access to another user’s notification
* invalid preference combinations
* duplicate event ingestion
* invalid channel/contact value
* delivery retry for already delivered message
* unsupported event type

## Recommended Exception Categories

* `ValidationException`
* `ResourceNotFoundException`
* `DuplicateResourceException`
* `UnauthorizedException`
* `ForbiddenException`
* `NotificationProcessingException`

## Suggested HTTP Mapping

* `400` validation errors
* `401` missing/invalid auth
* `403` permission issue
* `404` missing resource
* `409` duplicate/conflict
* `422` business rule violation
* `500` unexpected server error
