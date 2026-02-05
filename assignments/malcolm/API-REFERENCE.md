
> **Base URL (via Gateway):** `/api/v1`
> **Auth:** `Authorization: Bearer <JWT>`
> **Roles (examples):** `AGENT`, `SUPERVISOR`, `AUDITOR`, `ADMIN`

---

## Common Conventions

### Standard Headers

* `Authorization: Bearer <JWT>`
* `X-Correlation-Id: <uuid>` *(optional, echoed back for tracing)*

### Standard Error Response (all endpoints)

```json
{
  "timestamp": "2026-02-04T14:22:11Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "transactionAmount must be > 0",
  "path": "/api/v1/cases",
  "correlationId": "b3c2d7d4-4d67-4c7c-9fb3-3b5e6b7a2f1c"
}
```

### Typical HTTP Status Codes

* **200/201** success
* **400** validation errors / malformed request
* **401** missing/invalid token
* **403** insufficient role/permission
* **404** case/resource not found
* **409** conflict (invalid workflow transition, concurrency/version mismatch)
* **422** business rule violations (optional)
* **500** unexpected server error

---

# 1) Authentication / User Context

### Get current user context

**GET** `/auth/me`
**Roles:** any authenticated

**Response 200**

```json
{
  "userId": 1021,
  "displayName": "Malcolm Lott",
  "email": "malcolm.lott@company.com",
  "roles": ["AGENT"],
  "permissions": ["CASE_READ", "CASE_WRITE", "EVIDENCE_UPLOAD"]
}
```

---

# 2) Reference Data (Reason Codes + Evidence Checklist)

### List dispute reason codes

**GET** `/reference/reason-codes`
**Query params:** `activeOnly=true|false` *(default true)*

**Response 200**

```json
[
  { "code": "FRAUD_CARD_NOT_PRESENT", "description": "Card-not-present fraud" },
  { "code": "DUPLICATE_CHARGE", "description": "Duplicate transaction posted" }
]
```

### Get required evidence checklist for a reason code

**GET** `/reference/reason-codes/{code}/evidence-requirements`

**Response 200**

```json
{
  "reasonCode": "FRAUD_CARD_NOT_PRESENT",
  "requirements": [
    { "requirementId": 11, "name": "Customer affidavit", "mandatory": true },
    { "requirementId": 12, "name": "Police report (if available)", "mandatory": false }
  ]
}
```

---

# 3) Case Intake (Create Dispute Case)

### Create a dispute case

**POST** `/cases`
**Roles:** `AGENT`, `SUPERVISOR`

**Body**

```json
{
  "customerRef": "CUST_TKN_88f2a",
  "maskedCardLast4": "1234",
  "transactionRef": "TXN_9a71b2",
  "merchantName": "ACME ONLINE",
  "transactionAmount": 249.99,
  "transactionCurrency": "USD",
  "transactionDate": "2026-02-01T19:10:00Z",
  "reasonCode": "FRAUD_CARD_NOT_PRESENT",
  "priority": "HIGH",
  "initialNote": "Customer reports unauthorized purchase."
}
```

**Response 201**

```json
{
  "caseId": 9001201,
  "caseNumber": "DISP-2026-000981",
  "currentStatus": "NEW",
  "slaDueAt": "2026-02-03T19:10:00Z",
  "createdAt": "2026-02-04T14:25:00Z"
}
```

**Errors**

* `400 VALIDATION_ERROR` (missing/invalid fields)
* `403 FORBIDDEN` (role not allowed)

---

# 4) Case Queue (Search / Filter)

### Search cases (work queue)

**GET** `/cases`
**Roles:** `AGENT`, `SUPERVISOR`, `AUDITOR` (read-only)

**Query params (all optional)**

* `status=NEW|IN_REVIEW|...`
* `assigneeId=1021` *(or `assignee=ME`)*
* `priority=LOW|MEDIUM|HIGH`
* `reasonCode=FRAUD_CARD_NOT_PRESENT`
* `sla=BREACHING|BREACHED|OK`
* `createdFrom=2026-02-01T00:00:00Z`
* `createdTo=2026-02-04T23:59:59Z`
* Pagination: `page=0&size=20&sort=slaDueAt,asc`

**Response 200**

```json
{
  "page": 0,
  "size": 20,
  "totalElements": 54,
  "items": [
    {
      "caseId": 9001201,
      "caseNumber": "DISP-2026-000981",
      "currentStatus": "NEW",
      "priority": "HIGH",
      "reasonCode": "FRAUD_CARD_NOT_PRESENT",
      "merchantName": "ACME ONLINE",
      "transactionAmount": 249.99,
      "transactionDate": "2026-02-01T19:10:00Z",
      "slaDueAt": "2026-02-03T19:10:00Z",
      "assignee": { "userId": 1021, "displayName": "Malcolm Lott" }
    }
  ]
}
```

**Errors**

* `400 VALIDATION_ERROR` for bad query values

---

# 5) Case Detail (Read)

### Get case by id

**GET** `/cases/{caseId}`
**Roles:** `AGENT`, `SUPERVISOR`, `AUDITOR`

**Response 200**

```json
{
  "caseId": 9001201,
  "caseNumber": "DISP-2026-000981",
  "customerRef": "CUST_TKN_88f2a",
  "maskedCardLast4": "1234",
  "transactionRef": "TXN_9a71b2",
  "merchantName": "ACME ONLINE",
  "transactionAmount": 249.99,
  "transactionCurrency": "USD",
  "transactionDate": "2026-02-01T19:10:00Z",
  "reasonCode": "FRAUD_CARD_NOT_PRESENT",
  "priority": "HIGH",
  "currentStatus": "NEW",
  "slaDueAt": "2026-02-03T19:10:00Z",
  "createdBy": { "userId": 204, "displayName": "Agent A" },
  "assignee": { "userId": 1021, "displayName": "Malcolm Lott" },
  "createdAt": "2026-02-04T14:25:00Z",
  "updatedAt": "2026-02-04T14:30:12Z",
  "version": 3
}
```

**Errors**

* `404 NOT_FOUND` if no case

---

# 6) Assignment (My Queue / Supervisor Routing)

### Assign case to yourself

**POST** `/cases/{caseId}/assignments/self`
**Roles:** `AGENT`, `SUPERVISOR`

**Body (optional)**

```json
{ "note": "Taking ownership from queue." }
```

**Response 200**

```json
{
  "caseId": 9001201,
  "assignee": { "userId": 1021, "displayName": "Malcolm Lott" },
  "assignedAt": "2026-02-04T14:33:00Z"
}
```

**Errors**

* `409 CONFLICT` if case already assigned and policy forbids takeover

### Supervisor reassign

**POST** `/cases/{caseId}/assignments`
**Roles:** `SUPERVISOR`

**Body**

```json
{
  "assigneeId": 1105,
  "assignmentType": "REASSIGN",
  "note": "Balancing workload."
}
```

**Response 200**

```json
{
  "caseId": 9001201,
  "assignee": { "userId": 1105, "displayName": "Agent B" },
  "assignedAt": "2026-02-04T14:35:00Z"
}
```

---

# 7) Workflow Actions (Status Transitions)

> Use **action endpoints** to keep business logic consistent and prevent arbitrary status updates.

### Start review

**POST** `/cases/{caseId}/actions/start-review`
**Roles:** `AGENT`, `SUPERVISOR`

**Body**

```json
{ "note": "Validated transaction details, beginning investigation." , "expectedVersion": 3 }
```

**Response 200**

```json
{
  "caseId": 9001201,
  "fromStatus": "NEW",
  "toStatus": "IN_REVIEW",
  "changedAt": "2026-02-04T14:40:00Z",
  "version": 4
}
```

**Errors**

* `409 CONFLICT` when `expectedVersion` is stale (optimistic locking)
* `409 CONFLICT` when transition is not allowed (e.g., CLOSED → IN_REVIEW)

### Initiate chargeback

**POST** `/cases/{caseId}/actions/initiate-chargeback`
**Roles:** `AGENT`, `SUPERVISOR`

**Body**

```json
{
  "note": "Proceeding with chargeback per policy.",
  "chargebackRef": "CBK-778912",
  "expectedVersion": 4
}
```

**Response 200**

```json
{
  "caseId": 9001201,
  "fromStatus": "IN_REVIEW",
  "toStatus": "CHARGEBACK_INITIATED",
  "chargebackRef": "CBK-778912",
  "changedAt": "2026-02-04T14:45:00Z",
  "version": 5
}
```

### Resolve / Close case

**POST** `/cases/{caseId}/actions/close`
**Roles:** `SUPERVISOR` *(or AGENT with policy)*

**Body**

```json
{
  "resolution": "RESOLVED",
  "resolutionNote": "Chargeback approved and customer credited.",
  "expectedVersion": 5
}
```

**Response 200**

```json
{
  "caseId": 9001201,
  "fromStatus": "CHARGEBACK_INITIATED",
  "toStatus": "CLOSED",
  "resolution": "RESOLVED",
  "closedAt": "2026-02-04T15:02:00Z",
  "version": 6
}
```

---

# 8) Status History (Timeline)

### Get case status history

**GET** `/cases/{caseId}/status-history`
**Roles:** `AGENT`, `SUPERVISOR`, `AUDITOR`

**Response 200**

```json
[
  {
    "fromStatus": null,
    "toStatus": "NEW",
    "changeReason": "Case created",
    "changedBy": { "userId": 204, "displayName": "Agent A" },
    "changedAt": "2026-02-04T14:25:00Z"
  },
  {
    "fromStatus": "NEW",
    "toStatus": "IN_REVIEW",
    "changeReason": "Investigation started",
    "changedBy": { "userId": 1021, "displayName": "Malcolm Lott" },
    "changedAt": "2026-02-04T14:40:00Z"
  }
]
```

---

# 9) Notes

### Add a note to a case

**POST** `/cases/{caseId}/notes`
**Roles:** `AGENT`, `SUPERVISOR`

**Body**

```json
{
  "noteType": "INTERNAL",
  "noteText": "Merchant descriptor differs from prior legitimate purchases."
}
```

**Response 201**

```json
{
  "noteId": 55501,
  "caseId": 9001201,
  "noteType": "INTERNAL",
  "createdBy": { "userId": 1021, "displayName": "Malcolm Lott" },
  "createdAt": "2026-02-04T14:50:00Z"
}
```

### List notes for a case

**GET** `/cases/{caseId}/notes`
**Roles:** `AGENT`, `SUPERVISOR`, `AUDITOR`

---

# 10) Evidence Requests (Checklist-driven)

### Create an evidence request

**POST** `/cases/{caseId}/evidence-requests`
**Roles:** `AGENT`, `SUPERVISOR`

**Body**

```json
{
  "dueAt": "2026-02-10T23:59:59Z",
  "requirementIds": [11, 12],
  "messageToCustomer": "Please upload the signed affidavit and any supporting documentation."
}
```

**Response 201**

```json
{
  "evidenceRequestId": 7001,
  "caseId": 9001201,
  "status": "OPEN",
  "requestedAt": "2026-02-04T14:52:00Z",
  "dueAt": "2026-02-10T23:59:59Z",
  "items": [
    { "requirementId": 11, "name": "Customer affidavit", "status": "PENDING" },
    { "requirementId": 12, "name": "Police report (if available)", "status": "PENDING" }
  ]
}
```

### List evidence requests for a case

**GET** `/cases/{caseId}/evidence-requests`
**Roles:** `AGENT`, `SUPERVISOR`, `AUDITOR`

---

# 11) Evidence Upload (S3 Presigned URL + Metadata)

### Create an upload intent (returns presigned URL)

**POST** `/evidence/upload-intents`
**Roles:** `AGENT`, `SUPERVISOR`

**Body**

```json
{
  "caseId": 9001201,
  "requirementId": 11,
  "fileName": "affidavit.pdf",
  "contentType": "application/pdf",
  "fileSizeBytes": 182001
}
```

**Response 201**

```json
{
  "uploadIntentId": "upl_9b0f1d2a",
  "caseId": 9001201,
  "storageProvider": "S3",
  "bucket": "pnc-dispute-evidence",
  "objectKey": "cases/9001201/upl_9b0f1d2a/affidavit.pdf",
  "presignedUrl": "https://s3.amazonaws.com/...signed...",
  "expiresAt": "2026-02-04T15:05:00Z"
}
```

### Confirm upload + create evidence metadata record

**POST** `/evidence/files`
**Roles:** `AGENT`, `SUPERVISOR`

**Body**

```json
{
  "caseId": 9001201,
  "requirementId": 11,
  "fileName": "affidavit.pdf",
  "contentType": "application/pdf",
  "fileSizeBytes": 182001,
  "sha256": "0f1c2a...9d",
  "bucket": "pnc-dispute-evidence",
  "objectKey": "cases/9001201/upl_9b0f1d2a/affidavit.pdf"
}
```

**Response 201**

```json
{
  "evidenceFileId": 88001,
  "caseId": 9001201,
  "requirementId": 11,
  "fileName": "affidavit.pdf",
  "uploadedBy": { "userId": 1021, "displayName": "Malcolm Lott" },
  "uploadedAt": "2026-02-04T14:56:00Z"
}
```

### List evidence files for a case

**GET** `/cases/{caseId}/evidence/files`
**Roles:** `AGENT`, `SUPERVISOR`, `AUDITOR`

**Response 200**

```json
[
  {
    "evidenceFileId": 88001,
    "requirementId": 11,
    "fileName": "affidavit.pdf",
    "contentType": "application/pdf",
    "fileSizeBytes": 182001,
    "uploadedAt": "2026-02-04T14:56:00Z",
    "uploadedBy": { "userId": 1021, "displayName": "Malcolm Lott" }
  }
]
```

### Download evidence file (returns presigned GET url)

**GET** `/evidence/files/{evidenceFileId}/download-url`
**Roles:** `AGENT`, `SUPERVISOR`, `AUDITOR`

**Response 200**

```json
{
  "evidenceFileId": 88001,
  "downloadUrl": "https://s3.amazonaws.com/...signed...",
  "expiresAt": "2026-02-04T15:10:00Z"
}
```

---

# 12) Audit (Compliance)

### Query audit log entries (global or per-case)

**GET** `/audit`
**Roles:** `AUDITOR`, `SUPERVISOR`

**Query params**

* `caseId=9001201` *(optional)*
* `actionType=STATUS_CHANGED` *(optional)*
* `from=2026-02-01T00:00:00Z`
* `to=2026-02-04T23:59:59Z`
* `page=0&size=50`

**Response 200**

```json
{
  "page": 0,
  "size": 50,
  "totalElements": 3,
  "items": [
    {
      "auditId": 99001,
      "caseId": 9001201,
      "actor": { "userId": 1021, "displayName": "Malcolm Lott" },
      "actionType": "EVIDENCE_UPLOADED",
      "actionSummary": "Uploaded affidavit.pdf",
      "correlationId": "b3c2d7d4-4d67-4c7c-9fb3-3b5e6b7a2f1c",
      "createdAt": "2026-02-04T14:56:01Z"
    }
  ]
}
```

---

# 13) Error Handling Examples (Workflow-specific)

### Invalid status transition

**POST** `/cases/9001201/actions/initiate-chargeback`
If case is already `CLOSED`:

**Response 409**

```json
{
  "timestamp": "2026-02-04T15:12:11Z",
  "status": 409,
  "error": "WORKFLOW_CONFLICT",
  "message": "Cannot transition from CLOSED to CHARGEBACK_INITIATED",
  "path": "/api/v1/cases/9001201/actions/initiate-chargeback",
  "correlationId": "f0a7a1d1-2b6a-4f77-9a67-1b3b7a90c111"
}
```

### Optimistic locking / stale update

If `expectedVersion` doesn’t match current:
**Response 409**

```json
{
  "timestamp": "2026-02-04T15:13:22Z",
  "status": 409,
  "error": "VERSION_CONFLICT",
  "message": "Case version mismatch. Refresh and retry.",
  "path": "/api/v1/cases/9001201/actions/close",
  "correlationId": "a5b1c3e9-9b0a-4f1e-8e9a-2b3a5c1d0e11"
}
```

---

## Main Workflows Covered (Mapping)

1. **Intake:** `POST /cases`
2. **Queue:** `GET /cases`
3. **Case detail + timeline:** `GET /cases/{id}`, `GET /cases/{id}/status-history`
4. **Assignments:** `POST /cases/{id}/assignments/self`, `POST /cases/{id}/assignments`
5. **Workflow transitions:** `/cases/{id}/actions/*`
6. **Evidence request:** `POST /cases/{id}/evidence-requests`
7. **Evidence upload:** `POST /evidence/upload-intents` → upload to S3 → `POST /evidence/files`
8. **Compliance:** `GET /audit`
