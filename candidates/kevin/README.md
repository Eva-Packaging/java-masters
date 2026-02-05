# Claims Fraud Signal & Triage Dashboard (Insurance)

**Interview phrase:**

> “At Country Financial, I was part of a team that built a claims fraud signal and triage dashboard so adjusters could prioritize risky claims faster, using Spring Boot microservices, PostgreSQL, JWT/RBAC, and a React/Angular UI.”

**Project Title & Description**
A claims workflow enhancement that scores incoming claims for fraud risk (rule-based at first), flags anomalies (duplicate claims, suspicious timing, policy/coverage mismatches), and routes them into an investigator queue with audit trails and evidence attachments.

**Why It Fits the Resume**
Directly extends the **claims management + document upload + status tracking + audit logging + security** work already listed, and adds a business-relevant capability insurers care about.

**STAR Talking Points (short, 1–2 sentences each)**

* **Situation:** Adjusters were spending too much time on low-risk claims and fraud indicators were being caught late.
* **Task:** Add a scoring + triage layer without disrupting existing claims APIs and ensure full auditability.
* **Action:** I built Spring Boot endpoints for claim intake/scoring, persisted scores and investigation notes with JPA, enforced RBAC (adjuster vs investigator), and added audit logs for every state change.
* **Result:** Investigators focused on high-risk claims first, reducing manual review time and improving detection speed.
