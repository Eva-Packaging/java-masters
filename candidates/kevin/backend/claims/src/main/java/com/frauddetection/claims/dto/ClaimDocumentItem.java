package com.frauddetection.claims.dto;

import java.time.Instant;
import java.util.UUID;

public record ClaimDocumentItem(
        UUID documentId,
        DocumentType documentType,
        String fileName,
        Instant uploadedAt,
        String uploadedBy
) {}
