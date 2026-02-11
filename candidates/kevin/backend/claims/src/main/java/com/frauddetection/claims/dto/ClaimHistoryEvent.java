package com.frauddetection.claims.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class ClaimHistoryEvent {
    ClaimStatus fromStatus;
    ClaimStatus toStatus;

    @Email
    String changedBy;

    private DateTimeFormat changedAt;

    @NotNull
    private String reason;

}
