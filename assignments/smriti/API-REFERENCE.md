
## Common Conventions

### Auth

**Header (required on secured endpoints)**

* `Authorization: Bearer <JWT>`

### Standard Response Envelope (recommended)

Most endpoints return:

```json
{
  "data": { },
  "meta": { "correlationId": "c-123", "timestamp": "2026-02-04T14:10:00Z" }
}
```

### Pagination (queue endpoints)

* `page` (default 0)
* `size` (default 25)
* `sort` (e.g., `priority,desc`)

### Roles (example)

* `OPS_AGENT`: can assign/resolve/reopen
* `COMPLIANCE`: read-only + exports
* `ADMIN`: manage reference data

---

# 1) Auth & Identity

## 1.1 Login

**POST** `/auth/login`
**Body**

```json
{
  "username": "ops.jane",
  "password": "********"
}
```

**200 Response**

```json
{
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresInSeconds": 900,
    "user": {
      "userId": 101,
      "username": "ops.jane",
      "displayName": "Jane Ops",
      "roles": ["OPS_AGENT"]
    }
  },
  "meta": { "correlationId": "c-9f2" }
}
```

**Errors**

* `401 UNAUTHORIZED` invalid credentials

## 1.2 Refresh token

**POST** `/auth/refresh`
**Body**

```json
{ "refreshToken": "eyJhbGciOi..." }
```

**200 Response**

```json
{
  "data": { "accessToken": "eyJhbGciOi...", "expiresInSeconds": 900 }
}
```

---

# 2) Exception Queue & Search (Main Workbench)

## 2.1 Search queue (filters + paging)

**GET** `/exceptions`
**Query params (optional)**

* `status` (e.g., `NEW|IN_PROGRESS|RESOLVED|REOPENED`)
* `severity` (e.g., `CRITICAL|HIGH|MEDIUM|LOW`)
* `category` (e.g., `SETTLEMENT_MISMATCH`)
* `assignedTo` (userId)
* `isBreached` (`true|false`)
* `productType` (e.g., `FX`)
* `sourceSystem` (e.g., `OMS`)
* `fromCreatedAt`, `toCreatedAt` (ISO timestamps)
* `q` (free text, matches `externalTradeId` or `summary`)
* `page`, `size`, `sort`

**200 Response**

```json
{
  "data": {
    "items": [
      {
        "exceptionId": 50021,
        "externalTradeId": "TRD-883192",
        "sourceSystem": "OMS",
        "productType": "FX",
        "status": "IN_PROGRESS",
        "severity": "HIGH",
        "category": "SETTLEMENT_MISMATCH",
        "priority": 2,
        "currentAssignee": { "userId": 101, "displayName": "Jane Ops" },
        "sla": { "dueAt": "2026-02-04T18:00:00Z", "isBreached": false },
        "createdAt": "2026-02-04T10:12:00Z"
      }
    ],
    "page": 0,
    "size": 25,
    "totalItems": 128,
    "totalPages": 6
  },
  "meta": { "correlationId": "c-12a" }
}
```

**Errors**

* `400 BAD_REQUEST` invalid filter values (unknown status/severity)

---

# 3) Exception Detail

## 3.1 Get exception details

**GET** `/exceptions/{exceptionId}`
**Path**

* `exceptionId` (number)

**200 Response**

```json
{
  "data": {
    "exceptionId": 50021,
    "externalTradeId": "TRD-883192",
    "sourceSystem": "OMS",
    "productType": "FX",
    "tradeDate": "2026-02-03",
    "settlementDate": "2026-02-05",
    "status": "IN_PROGRESS",
    "severity": "HIGH",
    "category": "SETTLEMENT_MISMATCH",
    "priority": 2,
    "summary": "Settlement mismatch vs expected net amount",
    "details": "Net amount differs by $1,250.00. Potential FX rounding issue.",
    "currentAssignee": { "userId": 101, "displayName": "Jane Ops" },
    "sla": { "startAt": "2026-02-04T10:12:00Z", "dueAt": "2026-02-04T18:00:00Z", "isBreached": false },
    "createdBy": { "userId": 77, "displayName": "Trade Validator" },
    "createdAt": "2026-02-04T10:12:00Z",
    "updatedAt": "2026-02-04T12:40:00Z",
    "version": 3
  },
  "meta": { "correlationId": "c-12b" }
}
```

**Errors**

* `404 NOT_FOUND` exceptionId not found
* `403 FORBIDDEN` role not allowed to view (rare; usually compliance can view)

## 3.2 Create exception (manual or upstream feed)

**POST** `/exceptions`
**Role:** `OPS_AGENT` or system integration user
**Body**

```json
{
  "externalTradeId": "TRD-999888",
  "sourceSystem": "SettlementEngine",
  "productType": "EQUITY",
  "tradeDate": "2026-02-04",
  "settlementDate": "2026-02-06",
  "severity": "CRITICAL",
  "category": "REF_DATA_MISSING",
  "priority": 1,
  "summary": "Missing counterparty reference data",
  "details": "Counterparty code CP-3321 not found in reference feed"
}
```

**201 Response**

```json
{
  "data": { "exceptionId": 50055 },
  "meta": { "correlationId": "c-12c" }
}
```

**Errors**

* `400 BAD_REQUEST` missing required fields / invalid enums
* `409 CONFLICT` duplicate exception (same externalTradeId + category + open status)

---

# 4) Assignment Workflow

## 4.1 Assign exception

**POST** `/exceptions/{exceptionId}/assignments`
**Role:** `OPS_AGENT`
**Body**

```json
{
  "assignedToUserId": 101,
  "note": "Taking ownership; will verify settlement figures"
}
```

**200 Response**

```json
{
  "data": {
    "exceptionId": 50021,
    "status": "IN_PROGRESS",
    "currentAssignee": { "userId": 101, "displayName": "Jane Ops" },
    "assignedAt": "2026-02-04T12:45:00Z",
    "version": 4
  },
  "meta": { "correlationId": "c-12d" }
}
```

**Errors**

* `404 NOT_FOUND` exception or user not found
* `409 CONFLICT` version mismatch (optimistic locking) if provided
* `403 FORBIDDEN` not OPS_AGENT

## 4.2 Assignment history

**GET** `/exceptions/{exceptionId}/assignments`

**200 Response**

```json
{
  "data": {
    "items": [
      {
        "assignmentId": 9001,
        "assignedTo": { "userId": 101, "displayName": "Jane Ops" },
        "assignedBy": { "userId": 102, "displayName": "Lead Ops" },
        "assignedAt": "2026-02-04T12:45:00Z",
        "unassignedAt": null,
        "note": "Taking ownership; will verify settlement figures"
      }
    ]
  }
}
```

---

# 5) Comments & Collaboration

## 5.1 Add comment

**POST** `/exceptions/{exceptionId}/comments`
**Role:** `OPS_AGENT` (and optionally `COMPLIANCE` if you allow notes)
**Body**

```json
{
  "commentText": "Found mismatch due to FX rate rounding in upstream feed. Requesting recalculation."
}
```

**201 Response**

```json
{
  "data": {
    "commentId": 30011,
    "author": { "userId": 101, "displayName": "Jane Ops" },
    "createdAt": "2026-02-04T13:05:00Z"
  }
}
```

**Errors**

* `400 BAD_REQUEST` empty commentText
* `404 NOT_FOUND` exception not found

## 5.2 List comments

**GET** `/exceptions/{exceptionId}/comments`

**200 Response**

```json
{
  "data": {
    "items": [
      {
        "commentId": 30011,
        "commentText": "Found mismatch due to FX rate rounding...",
        "author": { "userId": 101, "displayName": "Jane Ops" },
        "createdAt": "2026-02-04T13:05:00Z"
      }
    ]
  }
}
```

---

# 6) Attachments (metadata + storage pointer)

## 6.1 Create upload URL (pre-signed)

**POST** `/exceptions/{exceptionId}/attachments/presign`
**Role:** `OPS_AGENT`
**Body**

```json
{
  "fileName": "settlement_screenshot.png",
  "contentType": "image/png",
  "fileSizeBytes": 245123
}
```

**200 Response**

```json
{
  "data": {
    "uploadUrl": "https://s3-presigned-url...",
    "objectKey": "exceptions/50021/settlement_screenshot.png",
    "expiresAt": "2026-02-04T13:25:00Z"
  }
}
```

## 6.2 Confirm attachment (store metadata)

**POST** `/exceptions/{exceptionId}/attachments`
**Body**

```json
{
  "fileName": "settlement_screenshot.png",
  "contentType": "image/png",
  "fileSizeBytes": 245123,
  "storageUrl": "s3://bucket/exceptions/50021/settlement_screenshot.png"
}
```

**201 Response**

```json
{
  "data": { "attachmentId": 70031 }
}
```

**Errors**

* `413 PAYLOAD_TOO_LARGE` exceeds allowed size
* `400 BAD_REQUEST` invalid contentType

## 6.3 List attachments

**GET** `/exceptions/{exceptionId}/attachments`

---

# 7) Resolve / Reopen (Core Lifecycle)

## 7.1 Resolve exception

**POST** `/exceptions/{exceptionId}/resolve`
**Role:** `OPS_AGENT`
**Body**

```json
{
  "resolutionCode": "FIXED_REF_DATA",
  "resolutionNotes": "Loaded missing counterparty CP-3321; trade validated and resubmitted."
}
```

**200 Response**

```json
{
  "data": {
    "exceptionId": 50021,
    "status": "RESOLVED",
    "resolvedAt": "2026-02-04T13:30:00Z",
    "resolvedBy": { "userId": 101, "displayName": "Jane Ops" },
    "version": 5
  },
  "meta": { "correlationId": "c-12e" }
}
```

**Errors**

* `409 CONFLICT` invalid state transition (already RESOLVED)
* `400 BAD_REQUEST` unknown resolutionCode / missing notes for certain codes
* `403 FORBIDDEN` not OPS_AGENT

## 7.2 Reopen exception

**POST** `/exceptions/{exceptionId}/reopen`
**Role:** `OPS_AGENT`
**Body**

```json
{ "reason": "Settlement mismatch persists after recalculation; need further review." }
```

**200 Response**

```json
{
  "data": { "exceptionId": 50021, "status": "REOPENED", "version": 6 }
}
```

---

# 8) SLA & Metrics

## 8.1 Get SLA details

**GET** `/exceptions/{exceptionId}/sla`

**200 Response**

```json
{
  "data": {
    "slaStartAt": "2026-02-04T10:12:00Z",
    "slaDueAt": "2026-02-04T18:00:00Z",
    "isBreached": false,
    "breachedAt": null
  }
}
```

## 8.2 Summary metrics (dashboard KPIs)

**GET** `/metrics/exceptions/summary`
**Role:** `OPS_AGENT` or `COMPLIANCE`
**Query (optional):** `from`, `to`, `productType`, `sourceSystem`

**200 Response**

```json
{
  "data": {
    "openCount": 83,
    "breachedCount": 7,
    "byStatus": { "NEW": 22, "IN_PROGRESS": 61, "RESOLVED": 0 },
    "bySeverity": { "CRITICAL": 4, "HIGH": 29, "MEDIUM": 38, "LOW": 12 }
  }
}
```

---

# 9) Audit & Compliance

## 9.1 Audit timeline for an exception

**GET** `/exceptions/{exceptionId}/audit-events`
**Role:** `COMPLIANCE` or `OPS_AGENT`

**200 Response**

```json
{
  "data": {
    "items": [
      {
        "auditEventId": 88001,
        "eventType": "EXCEPTION_CREATED",
        "actor": { "userId": 77, "displayName": "Trade Validator" },
        "eventAt": "2026-02-04T10:12:00Z",
        "beforeState": null,
        "afterState": { "status": "NEW", "severity": "HIGH", "category": "SETTLEMENT_MISMATCH" }
      },
      {
        "auditEventId": 88015,
        "eventType": "STATUS_CHANGED",
        "actor": { "userId": 101, "displayName": "Jane Ops" },
        "eventAt": "2026-02-04T13:30:00Z",
        "beforeState": { "status": "IN_PROGRESS" },
        "afterState": { "status": "RESOLVED" }
      }
    ]
  }
}
```

## 9.2 Search audit events (compliance)

**GET** `/audit-events`
**Role:** `COMPLIANCE`
**Query params (optional)**

* `eventType`
* `actorUserId`
* `exceptionId`
* `from`, `to`
* `correlationId`
* `page`, `size`, `sort`

---

# 10) Admin / Reference Data (Controlled by ADMIN)

## 10.1 List reference data

**GET** `/reference/exception-statuses`
**GET** `/reference/severity-levels`
**GET** `/reference/exception-categories`
**GET** `/reference/resolution-codes`

## 10.2 Create / Update reference entries

**POST** `/reference/exception-categories`
**PUT** `/reference/exception-categories/{categoryId}`
**Role:** `ADMIN`

---

# Error Handling (Standardized)

### Error Response Shape

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "severity must be one of [CRITICAL, HIGH, MEDIUM, LOW]",
    "details": [
      { "field": "severity", "issue": "invalid_value" }
    ]
  },
  "meta": { "correlationId": "c-err-19", "timestamp": "2026-02-04T14:12:00Z" }
}
```

### Common HTTP Status Codes

* `400 BAD_REQUEST` validation failures, invalid enums/filters
* `401 UNAUTHORIZED` missing/invalid token
* `403 FORBIDDEN` valid token but insufficient role
* `404 NOT_FOUND` missing resource (exceptionId, userId)
* `409 CONFLICT` invalid state transition, optimistic locking conflict, duplicate exception
* `422 UNPROCESSABLE_ENTITY` business rule violation (e.g., missing required fields for resolution)
* `429 TOO_MANY_REQUESTS` rate-limited at gateway
* `500 INTERNAL_SERVER_ERROR` unexpected failures

---

# Main Workflow Coverage (Quick Map)

1. **Login** → `POST /auth/login`
2. **Queue search** → `GET /exceptions`
3. **View details** → `GET /exceptions/{id}`
4. **Assign** → `POST /exceptions/{id}/assignments`
5. **Comment** → `POST /exceptions/{id}/comments`
6. **Attach evidence** → `POST /exceptions/{id}/attachments/presign` + `POST /exceptions/{id}/attachments`
7. **Resolve/Reopen** → `POST /exceptions/{id}/resolve` / `POST /exceptions/{id}/reopen`
8. **Audit/Compliance** → `GET /exceptions/{id}/audit-events` + `GET /audit-events`
9. **KPIs** → `GET /metrics/exceptions/summary`


