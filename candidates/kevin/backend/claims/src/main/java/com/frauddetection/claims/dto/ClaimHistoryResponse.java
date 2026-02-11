package com.frauddetection.claims.dto;

import lombok.Data;

import java.util.UUID;
import java.util.List;

@Data
public class ClaimHistoryResponse {
    private UUID claimId;
    private List<ClaimHistoryEvent> history;

}
