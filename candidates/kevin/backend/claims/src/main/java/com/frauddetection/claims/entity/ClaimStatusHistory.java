package com.frauddetection.claims.entity;

import com.frauddetection.claims.dto.ClaimStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "claim_status_history")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "claim_status_history_id")
    private UUID claimStatusHistoryId;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "changed_by_user_id", nullable = false)
    private UUID changedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private ClaimStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 30, nullable = false)
    private ClaimStatus toStatus;

    @Column(name = "change_reason", length = 200)
    private String changeReason;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
