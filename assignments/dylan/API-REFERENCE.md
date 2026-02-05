# Common Conventions

## Auth

* **Customer/Admin APIs:** OAuth2/OIDC (JWT access token)

    * Customer role: `ROLE_CUSTOMER`
    * Admin role: `ROLE_COMPLIANCE_ADMIN`
* **Third-Party APIs:** OAuth2 client credentials or authorization code (JWT with `client_id` + scopes)

## Headers

* `Authorization: Bearer <jwt>`
* `X-Request-Id: <uuid>` (optional but recommended for tracing)

## Standard Errors (JSON)

```json
{
  "timestamp": "2026-02-05T19:10:30Z",
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Consent is missing or expired",
  "code": "CONSENT_INVALID",
  "path": "/open-banking/accounts"
}
```

Common error codes:

* `VALIDATION_ERROR` (400)
* `UNAUTHORIZED` (401)
* `FORBIDDEN` (403)
* `NOT_FOUND` (404)
* `CONFLICT` (409)
* `RATE_LIMITED` (429)
* `INTERNAL_ERROR` (500)

---

# Service A — Consent Service

## 1) Create (Grant) Consent

**POST** `/api/v1/consents`

**Body**

```json
{
  "customerId": 10012003,
  "appClientId": "fintech-app-123",
  "scopes": ["accounts.read", "transactions.read"],
  "accountIds": [501, 502],
  "expiresAt": "2026-03-05T00:00:00Z",
  "consentVersion": 1
}
```

**Response 201**

```json
{
  "consentId": 90001,
  "customerId": 10012003,
  "appClientId": "fintech-app-123",
  "status": "ACTIVE",
  "scopes": ["accounts.read", "transactions.read"],
  "accountIds": [501, 502],
  "grantedAt": "2026-02-05T19:10:30Z",
  "expiresAt": "2026-03-05T00:00:00Z",
  "consentVersion": 1
}
```

**Errors**

* 400 `VALIDATION_ERROR` (missing scopes, invalid date)
* 403 `FORBIDDEN` (customer cannot grant for someone else)
* 409 `CONFLICT` (active consent already exists for same app + scope set, depending on business rule)

---

## 2) List Customer Consents

**GET** `/api/v1/customers/{customerId}/consents`

**Path params**

* `customerId` (long)

**Query params**

* `status` (optional: `ACTIVE|REVOKED|EXPIRED`)
* `appClientId` (optional)

**Response 200**

```json
{
  "customerId": 10012003,
  "items": [
    {
      "consentId": 90001,
      "appClientId": "fintech-app-123",
      "status": "ACTIVE",
      "scopes": ["accounts.read"],
      "expiresAt": "2026-03-05T00:00:00Z"
    }
  ]
}
```

**Errors**

* 403 `FORBIDDEN` (accessing another customer without admin role)

---

## 3) Get Consent by ID

**GET** `/api/v1/consents/{consentId}`

**Response 200**

```json
{
  "consentId": 90001,
  "customerId": 10012003,
  "appClientId": "fintech-app-123",
  "status": "ACTIVE",
  "scopes": ["accounts.read", "transactions.read"],
  "accountIds": [501, 502],
  "grantedAt": "2026-02-05T19:10:30Z",
  "expiresAt": "2026-03-05T00:00:00Z",
  "consentVersion": 1
}
```

**Errors**

* 404 `NOT_FOUND`

---

## 4) Revoke Consent

**POST** `/api/v1/consents/{consentId}/revoke`

**Body**

```json
{
  "reason": "Customer requested revocation"
}
```

**Response 200**

```json
{
  "consentId": 90001,
  "status": "REVOKED",
  "revokedAt": "2026-02-05T19:22:11Z"
}
```

**Errors**

* 404 `NOT_FOUND`
* 409 `CONFLICT` (already revoked/expired)
* 403 `FORBIDDEN` (not owner or not admin)

---

## 5) Validate Consent (Used by Access Gateway)

**GET** `/api/v1/consents/validate`

**Query params**

* `customerId` (long, required)
* `appClientId` (string, required)
* `scopes` (repeatable or comma-separated, required)
* `accountIds` (optional; required if account-level enforcement)
* `atTime` (optional ISO timestamp; defaults to now)

**Example request**
`/api/v1/consents/validate?customerId=10012003&appClientId=fintech-app-123&scopes=accounts.read,transactions.read&accountIds=501,502`

**Response 200 (valid)**

```json
{
  "valid": true,
  "consentId": 90001,
  "status": "ACTIVE",
  "expiresAt": "2026-03-05T00:00:00Z",
  "grantedScopes": ["accounts.read", "transactions.read"],
  "grantedAccountIds": [501, 502]
}
```

**Response 200 (invalid)**

```json
{
  "valid": false,
  "reason": "CONSENT_EXPIRED",
  "missingScopes": [],
  "missingAccountIds": []
}
```

**Errors**

* 400 `VALIDATION_ERROR` (missing required params)
* 403 `FORBIDDEN` (if caller isn’t trusted gateway/service identity)

---

# Service B — Access Gateway / Policy Enforcement API

> These endpoints are what third parties call. The gateway enforces consent before returning data.

## 6) Get Accounts (Protected)

**GET** `/open-banking/v1/accounts`

**Query params**

* `customerId` (required) *(or derive from token if you model delegated access)*
* `accountIds` (optional filter)

**Response 200**

```json
{
  "customerId": 10012003,
  "accounts": [
    {
      "accountId": 501,
      "type": "CHECKING",
      "maskedNumber": "****1234",
      "status": "OPEN"
    }
  ]
}
```

**Errors**

* 401 `UNAUTHORIZED` (invalid token)
* 403 `FORBIDDEN` with code `CONSENT_INVALID` / `SCOPE_NOT_GRANTED`
* 429 `RATE_LIMITED`

---

## 7) Get Transactions (Protected)

**GET** `/open-banking/v1/accounts/{accountId}/transactions`

**Path params**

* `accountId` (long)

**Query params**

* `customerId` (required)
* `fromDate` (optional `YYYY-MM-DD`)
* `toDate` (optional `YYYY-MM-DD`)
* `page` (optional)
* `size` (optional)

**Response 200**

```json
{
  "accountId": 501,
  "items": [
    {
      "transactionId": "tx-9001",
      "postedAt": "2026-02-01T00:00:00Z",
      "amount": 59.21,
      "currency": "USD",
      "description": "Grocery Store"
    }
  ],
  "page": 0,
  "size": 25,
  "totalItems": 1
}
```

**Errors**

* 403 `FORBIDDEN` if consent doesn’t include `transactions.read` or the account
* 404 `NOT_FOUND` if account not found (or mask as 404 for security)

---

## 8) Introspection / Decision Trace (Optional, useful for debugging)

**GET** `/open-banking/v1/decision`

**Query params**

* `customerId`, `appClientId`, `scopes`, `accountId` (optional)

**Response 200**

```json
{
  "decision": "ALLOW",
  "consentId": 90001,
  "checkedScopes": ["accounts.read"],
  "requestId": "b52d7a4e-7f61-4cdd-bbd6-acde58caa111"
}
```

**Errors**

* 403 `FORBIDDEN` (only allowed for internal/admin use)

---

# Service C — Audit Service (Compliance/Admin)

## 9) Search Audit Events

**GET** `/api/v1/audit-events`

**Query params**

* `customerId` (optional)
* `appClientId` (optional)
* `eventType` (optional)
* `decision` (optional `ALLOW|DENY`)
* `from` (optional ISO timestamp)
* `to` (optional ISO timestamp)
* `page`, `size` (optional)

**Response 200**

```json
{
  "page": 0,
  "size": 25,
  "totalItems": 2,
  "items": [
    {
      "eventId": 70001,
      "eventType": "ACCESS_DENIED",
      "decision": "DENY",
      "denyReason": "SCOPE_NOT_GRANTED",
      "customerId": 10012003,
      "appClientId": "fintech-app-123",
      "resource": "/open-banking/v1/accounts/501/transactions",
      "eventTime": "2026-02-05T19:12:30Z",
      "requestId": "b52d7a4e-7f61-4cdd-bbd6-acde58caa111"
    }
  ]
}
```

**Errors**

* 403 `FORBIDDEN` (admin-only)

---

## 10) Export Evidence Pack (Async)

**POST** `/api/v1/audit-exports`

**Body**

```json
{
  "filters": {
    "customerId": 10012003,
    "appClientId": "fintech-app-123",
    "from": "2026-02-01T00:00:00Z",
    "to": "2026-02-05T23:59:59Z"
  },
  "format": "CSV"
}
```

**Response 202**

```json
{
  "exportId": "exp-20260205-001",
  "status": "QUEUED"
}
```

**Errors**

* 400 `VALIDATION_ERROR`
* 403 `FORBIDDEN`

---

## 11) Check Export Status / Download Link

**GET** `/api/v1/audit-exports/{exportId}`

**Response 200**

```json
{
  "exportId": "exp-20260205-001",
  "status": "COMPLETED",
  "downloadUrl": "https://signed-url.example.com/exports/exp-20260205-001.csv",
  "expiresAt": "2026-02-05T20:10:30Z"
}
```

**Errors**

* 404 `NOT_FOUND`
* 409 `CONFLICT` (if failed; return `status=FAILED` plus reason)

---

# Main Workflows Covered (how you narrate it)

1. **Customer grants consent**
   `POST /api/v1/consents` → consent stored + audit event

2. **Customer views/revokes consent**
   `GET /api/v1/customers/{id}/consents` → `POST /api/v1/consents/{id}/revoke`

3. **Third party calls protected APIs**
   `GET /open-banking/v1/accounts` (gateway checks consent via `/consents/validate`)
   If valid → returns data; if invalid → 403 with `CONSENT_INVALID`

4. **Compliance investigates / exports evidence**
   `GET /api/v1/audit-events` → `POST /api/v1/audit-exports` → `GET /api/v1/audit-exports/{id}`
