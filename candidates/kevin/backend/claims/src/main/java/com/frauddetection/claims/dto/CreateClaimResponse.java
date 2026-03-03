package com.frauddetection.claims.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/*
{
  "claimId": "7a1c0a25-4d65-47a8-9c1a-ef39d0c2fd55",
  "claimNumber": "CLM-2026-000983",
  "status": "SUBMITTED",
  "reportedAt": "2026-02-05T16:05:22Z"
}
 */
public class CreateClaimResponse {
    private UUID claimId;
    private String claimNumber;
    private ClaimStatus status;
    private LocalDateTime reportedAt;

    public UUID getClaimId() {
        return claimId;
    }

    public void setClaimId(UUID claimId) {
        this.claimId = claimId;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

}
