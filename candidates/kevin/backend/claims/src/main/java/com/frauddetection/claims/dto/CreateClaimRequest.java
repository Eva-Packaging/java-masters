package com.frauddetection.claims.dto;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateClaimRequest {
    private UUID claimID;
    private String claimNumber;
    private String policyNumber;
    private LocalDate lossDate;
    private LocalDateTime reportedAt;
    private Double lossAmountEstimate;
    private String description;
    private String regionCode;
    private ClaimantDTO claimant;
    private ClaimStatus status;
    private ClaimType claimType;
}
