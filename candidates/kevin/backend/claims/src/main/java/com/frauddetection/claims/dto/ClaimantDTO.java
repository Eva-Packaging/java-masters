package com.frauddetection.claims.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ClaimantDTO {
    private UUID claimantId;
    private String firstName;
    private String lastName;
}
