package com.frauddetection.claims.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDTO {
    private UUID claimId;
    private UUID policyId;
    private UUID claimantId;
    private String claimNumber;
    private ClaimType claimType;
    private ClaimStatus status;
    private LocalDate lossDate;
    private LocalDateTime reportedAt;
    private BigDecimal lossAmountEstimate;
    private String description;
    private String regionCode;
    private LocalDateTime updatedAt;
}
