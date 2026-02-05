### Base URL

`/api/v1`

### Auth

* `Authorization: Bearer <JWT>`
* Roles: `ADJUSTER`, `INVESTIGATOR`, `ADMIN`

### Standard error format

```json
{
  "timestamp": "2026-02-05T16:10:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "lossDate is required",
  "path": "/api/v1/claims"
}
```

### Common HTTP codes

* `200 OK`, `201 Created`, `204 No Content`
* `400 Bad Request` (validation)
* `401 Unauthorized` (missing/invalid token)
* `403 Forbidden` (RBAC)
* `404 Not Found`
* `409 Conflict` (duplicate claim number, invalid state transition)
* `422 Unprocessable Entity` (business rule violated)
* `429 Too Many Requests` (rate limit)
* `500 Internal Server Error`

---

# 1) Authentication (Auth Service)

## POST `/auth/login`

**Body**

```json
{ "email": "jane.adjuster@company.com", "password": "••••••••" }
```

**201 Response**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR...",
  "expiresInSeconds": 3600,
  "user": {
    "userId": "c7a2c3a1-1f7f-4ac2-8e3d-9b3b7c9b2b10",
    "email": "jane.adjuster@company.com",
    "fullName": "Jane Adjuster",
    "roles": ["ADJUSTER"]
  }
}
```

**Errors**: `401` invalid credentials, `423` account disabled/locked (optional)

## POST `/auth/logout`

**204** (client deletes token)
**Errors**: `401`

## GET `/auth/me`

**200**

```json
{
  "userId": "c7a2c3a1-1f7f-4ac2-8e3d-9b3b7c9b2b10",
  "email": "jane.adjuster@company.com",
  "fullName": "Jane Adjuster",
  "roles": ["ADJUSTER"]
}
```

---

# 2) Claims (Claim Service)

## POST `/claims`

Create a new claim (**ADJUSTER**)
**Body**

```json
{
  "policyNumber": "POL-10001234",
  "claimant": {
    "firstName": "Chris",
    "lastName": "Miller",
    "dob": "1987-06-12",
    "phone": "+1-312-555-0199",
    "email": "chris.miller@example.com"
  },
  "claimType": "AUTO",
  "lossDate": "2026-01-29",
  "lossAmountEstimate": 4200.50,
  "description": "Rear-end collision at stoplight",
  "regionCode": "IL-CHI"
}
```

**201 Response**

```json
{
  "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
  "claimNumber": "CLM-2026-000983",
  "status": "SUBMITTED",
  "reportedAt": "2026-02-05T16:05:22Z"
}
```

**Errors**: `400`, `401`, `403`, `409` (duplicate policy/claim constraint), `422` (policy expired)

## GET `/claims/{claimId}`

**Path**: `claimId`
**200 Response**

```json
{
  "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
  "claimNumber": "CLM-2026-000983",
  "policyNumber": "POL-10001234",
  "claimType": "AUTO",
  "status": "IN_REVIEW",
  "lossDate": "2026-01-29",
  "reportedAt": "2026-02-05T16:05:22Z",
  "lossAmountEstimate": 4200.50,
  "description": "Rear-end collision at stoplight",
  "regionCode": "IL-CHI",
  "claimant": {
    "claimantId": "40d12551-1a6f-4b44-a8f4-fc0b6f4ec2b9",
    "firstName": "Chris",
    "lastName": "Miller"
  }
}
```

**Errors**: `404`, `401`, `403`

## GET `/claims`

Search/list claims (Adjuster or Investigator)
**Query**

* `status` (optional) e.g. `SUBMITTED`
* `claimNumber` (optional)
* `policyNumber` (optional)
* `fromDate`, `toDate` (optional, ISO date)
* `page` (default 0), `size` (default 20), `sort` (e.g. `reportedAt,desc`)

**200 Response**

```json
{
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "items": [
    {
      "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
      "claimNumber": "CLM-2026-000983",
      "status": "SUBMITTED",
      "reportedAt": "2026-02-05T16:05:22Z",
      "regionCode": "IL-CHI"
    }
  ]
}
```

## PATCH `/claims/{claimId}/status`

Status transition (**ADJUSTER**, sometimes **INVESTIGATOR** depending on policy)
**Body**

```json
{ "toStatus": "IN_REVIEW", "reason": "Initial validation complete" }
```

**200 Response**

```json
{ "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55", "status": "IN_REVIEW" }
```

**Errors**: `409` invalid transition, `403`, `404`

## GET `/claims/{claimId}/history`

**200**

```json
{
  "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
  "events": [
    {
      "fromStatus": null,
      "toStatus": "SUBMITTED",
      "changedBy": "jane.adjuster@company.com",
      "changedAt": "2026-02-05T16:05:22Z",
      "reason": "Claim created"
    }
  ]
}
```

---

# 3) Documents / Evidence (Claim Service)

## POST `/claims/{claimId}/documents`

Upload evidence metadata (file typically goes to object storage via presigned URL flow)
**Body**

```json
{
  "documentType": "POLICE_REPORT",
  "fileName": "report.pdf",
  "contentType": "application/pdf",
  "fileSizeBytes": 882133,
  "sha256Hash": "a3f1...9c2d"
}
```

**201 Response**

```json
{
  "documentId": "f32f5aa9-2f0b-4af3-bb7d-3e3a1f1c5a4d",
  "upload": {
    "method": "PUT",
    "url": "https://storage.example.com/presigned/....",
    "expiresAt": "2026-02-05T16:20:22Z"
  }
}
```

**Errors**: `413` file too large, `415` unsupported type, `404`, `403`

## GET `/claims/{claimId}/documents`

**200**

```json
{
  "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
  "items": [
    {
      "documentId": "f32f5aa9-2f0b-4af3-bb7d-3e3a1f1c5a4d",
      "documentType": "POLICE_REPORT",
      "fileName": "report.pdf",
      "uploadedAt": "2026-02-05T16:10:22Z",
      "uploadedBy": "jane.adjuster@company.com"
    }
  ]
}
```

## GET `/documents/{documentId}/download-url`

Returns a short-lived download URL
**200**

```json
{
  "documentId": "f32f5aa9-2f0b-4af3-bb7d-3e3a1f1c5a4d",
  "url": "https://storage.example.com/presigned-download/....",
  "expiresAt": "2026-02-05T16:25:22Z"
}
```

---

# 4) Fraud Scoring (Fraud Service)

## GET `/fraud/claims/{claimId}/score`

Latest fraud score + reasons (Adjuster can view; Investigator needs full detail)
**200**

```json
{
  "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
  "score": 78.5,
  "riskLevel": "HIGH",
  "scoringVersion": "rules-v3",
  "scoredAt": "2026-02-05T16:06:10Z",
  "signals": [
    {
      "signalCode": "RAPID_REPEAT_CLAIMS",
      "description": "Multiple claims within 30 days",
      "weight": 0.35,
      "evidenceValue": "3 claims in 30 days"
    },
    {
      "signalCode": "DUPLICATE_ADDRESS",
      "description": "Address matches prior flagged claims",
      "weight": 0.20,
      "evidenceValue": "Matched 2 prior cases"
    }
  ]
}
```

**Errors**: `404` (not scored yet), `403`

## POST `/fraud/claims/{claimId}/rescore`

Manual rescore (typically **ADMIN** or **INVESTIGATOR**)
**Body**

```json
{ "reason": "New evidence uploaded" }
```

**202 Response**

```json
{ "status": "QUEUED", "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55" }
```

**Errors**: `403`, `404`

---

# 5) Investigation Cases & Queues (Case Service)

## POST `/cases`

Create a case (normally created automatically when score >= threshold; allow manual creation)
**Body**

```json
{
  "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
  "priority": "HIGH",
  "queueName": "IL-CHI-AUTO",
  "slaDueAt": "2026-02-06T16:05:22Z"
}
```

**201 Response**

```json
{
  "caseId": "1f5c8d3e-8b0a-4a20-91a2-1a2e0b2f6c1b",
  "caseStatus": "OPEN"
}
```

**Errors**: `409` case already exists for claim (if enforced), `403`, `404`

## GET `/cases/{caseId}`

**200**

```json
{
  "caseId": "1f5c8d3e-8b0a-4a20-91a2-1a2e0b2f6c1b",
  "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
  "caseStatus": "IN_REVIEW",
  "priority": "HIGH",
  "queueName": "IL-CHI-AUTO",
  "slaDueAt": "2026-02-06T16:05:22Z",
  "assignments": [
    {
      "assigneeUserId": "b5d6f2a1-6b6b-4d28-b7d5-9f0bcb4b5f8a",
      "assigneeEmail": "ivy.investigator@company.com",
      "assignedAt": "2026-02-05T16:12:00Z",
      "isPrimary": true
    }
  ]
}
```

## GET `/cases`

Queue search (Investigator/Admin)
**Query**

* `queueName` (optional)
* `caseStatus` (optional)
* `minScore` / `maxScore` (optional) *(if Case Service denormalizes latest score or joins via view)*
* `slaBefore` (optional datetime)
* `page`, `size`, `sort` (e.g. `priority,desc`)

**200**

```json
{
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "items": [
    {
      "caseId": "1f5c8d3e-8b0a-4a20-91a2-1a2e0b2f6c1b",
      "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
      "caseStatus": "OPEN",
      "priority": "HIGH",
      "queueName": "IL-CHI-AUTO",
      "slaDueAt": "2026-02-06T16:05:22Z",
      "latestFraudScore": 78.5
    }
  ]
}
```

## POST `/cases/{caseId}/assignments`

Assign investigator (**INVESTIGATOR** lead or **ADMIN**)
**Body**

```json
{
  "assigneeUserId": "b5d6f2a1-6b6b-4d28-b7d5-9f0bcb4b5f8a",
  "isPrimary": true
}
```

**201**

```json
{
  "caseId": "1f5c8d3e-8b0a-4a20-91a2-1a2e0b2f6c1b",
  "assignmentId": "aa4d1f60-87d4-4d2c-8a1e-1b7f5e8d0b66",
  "assignedAt": "2026-02-05T16:12:00Z"
}
```

**Errors**: `404`, `403`, `409` already assigned (if constrained)

## DELETE `/cases/{caseId}/assignments/{assignmentId}`

Unassign (soft-unassign)
**204**
**Errors**: `404`, `403`

## POST `/cases/{caseId}/notes`

Add investigation note
**Body**

```json
{
  "noteType": "NOTE",
  "noteText": "Claimant reported loss date differs from repair invoice date. Request clarification."
}
```

**201**

```json
{
  "caseNoteId": "c1ddc5ad-84f9-4e59-9c37-07a1d4cc8fe4",
  "createdAt": "2026-02-05T16:15:10Z"
}
```

## GET `/cases/{caseId}/notes`

**200**

```json
{
  "caseId": "1f5c8d3e-8b0a-4a20-91a2-1a2e0b2f6c1b",
  "items": [
    {
      "caseNoteId": "c1ddc5ad-84f9-4e59-9c37-07a1d4cc8fe4",
      "noteType": "NOTE",
      "noteText": "Claimant reported loss date differs...",
      "createdBy": "ivy.investigator@company.com",
      "createdAt": "2026-02-05T16:15:10Z"
    }
  ]
}
```

## PATCH `/cases/{caseId}/status`

Update case workflow state
**Body**

```json
{ "toStatus": "ESCALATED", "reason": "High-risk signals confirmed; send to SIU." }
```

**200**

```json
{ "caseId": "1f5c8d3e-8b0a-4a20-91a2-1a2e0b2f6c1b", "caseStatus": "ESCALATED" }
```

**Errors**: `409` invalid transition, `403`, `404`

## POST `/cases/{caseId}/document-requests`

Request documents from claimant/adjuster (creates a trackable request)
**Body**

```json
{
  "requestedItems": [
    { "documentType": "REPAIR_INVOICE", "description": "Provide invoice for repairs" },
    { "documentType": "PHOTOS", "description": "Upload accident photos" }
  ],
  "dueAt": "2026-02-07T17:00:00Z"
}
```

**201**

```json
{
  "requestId": "9b0a6d0b-6b8c-4c66-9a44-1e0de2b7d0f3",
  "status": "OPEN"
}
```

**Errors**: `403`, `404`, `422` due date too soon (optional)

---

# 6) Fraud Rules (Rules Service) — optional but powerful

## GET `/rules`

List rules (**ADMIN**)
**200**

```json
{
  "items": [
    {
      "ruleId": "3f8f1d2a-4c2b-4cf8-8f3e-9d2a6f4a1b2c",
      "ruleCode": "RAPID_REPEAT_CLAIMS",
      "ruleName": "Rapid repeat claims in window",
      "isActive": true
    }
  ]
}
```

## POST `/rules`

Create a rule (**ADMIN**)
**Body**

```json
{
  "ruleCode": "DUPLICATE_ADDRESS",
  "ruleName": "Duplicate address match",
  "isActive": true
}
```

**201**

```json
{ "ruleId": "c8f55c7b-0d9d-4b9f-8d88-9a8bb8d6f5b4" }
```

## POST `/rules/{ruleId}/versions`

Add a version (**ADMIN**)
**Body**

```json
{
  "version": 3,
  "conditionType": "COUNT_WINDOW",
  "conditionValue": ">=3 in 30d",
  "weight": 0.35,
  "effectiveFrom": "2026-01-01T00:00:00Z"
}
```

**201**

```json
{ "ruleVersionId": "1a0b5f6a-62d5-4b7b-a5f4-73f1a3d2b9c0" }
```

---

# 7) Audit & Compliance (Audit Service)

## GET `/audit/events`

Search audit events (**ADMIN**, sometimes Investigator limited)
**Query**

* `entityType` (CLAIM/CASE/DOCUMENT/RULE)
* `entityId`
* `actorUserId`
* `eventType`
* `from`, `to` (datetime)
* `page`, `size`

**200**

```json
{
  "page": 0,
  "size": 50,
  "totalElements": 2,
  "items": [
    {
      "auditEventId": "5b6c7d8e-1111-2222-3333-444455556666",
      "eventType": "CASE_ASSIGNED",
      "entityType": "CASE",
      "entityId": "1f5c8d3e-8b0a-4a20-91a2-1a2e0b2f6c1b",
      "actorUserId": "b5d6f2a1-6b6b-4d28-b7d5-9f0bcb4b5f8a",
      "eventTime": "2026-02-05T16:12:00Z",
      "metadata": { "assigneeUserId": "b5d6f2a1-6b6b-4d28-b7d5-9f0bcb4b5f8a" }
    }
  ]
}
```

---

# Main workflows covered

1. **Adjuster creates claim** → `POST /claims`
2. **System scores claim** → `GET /fraud/claims/{claimId}/score` (or async)
3. **Case created & queued** → `POST /cases` (auto) + `GET /cases` (queue view)
4. **Investigator assigns case** → `POST /cases/{caseId}/assignments`
5. **Investigation notes + status changes** → `POST /cases/{caseId}/notes`, `PATCH /cases/{caseId}/status`
6. **Evidence upload** → `POST /claims/{claimId}/documents` + download URL
7. **Audit reporting** → `GET /audit/events`
8. (Optional) **Admin manages rules** → `/rules` endpoints
