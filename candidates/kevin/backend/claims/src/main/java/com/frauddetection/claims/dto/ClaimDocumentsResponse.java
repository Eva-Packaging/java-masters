package com.frauddetection.claims.dto;

import java.util.List;
import java.util.UUID;

public record ClaimDocumentsResponse(
        UUID claimId,
        List<ClaimDocumentItem> items
) {}
