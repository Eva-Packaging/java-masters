package com.frauddetection.claims.dto;

import jakarta.validation.constraints.NotNull;


public record UpdateStatusRequest(
        @NotNull
        String reason,

        @NotNull
        ClaimStatus status
) {

}
