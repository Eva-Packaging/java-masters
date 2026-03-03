package com.frauddetection.claims.entity;

import com.frauddetection.claims.dto.ClaimStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "claims")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "claim_id")
    private UUID claimId;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(name = "claimant_id", nullable = false)
    private UUID claimantId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "claim_number", length = 40, unique = true, nullable = false)
    private String claimNumber;

    @Column(name = "claim_type", length = 50, nullable = false)
    private String claimType;

    @Column(name = "status", length = 30, nullable = false)
    private ClaimStatus status;

    @Column(name = "loss_date", nullable = false)
    private LocalDate lossDate;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Column(name = "loss_amount_estimate", precision = 12, scale = 2)
    private BigDecimal lossAmountEstimate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        reportedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
