package com.frauddetection.claims.dto;

import lombok.Data;
import java.util.UUID;
import java.time.Instant;

public record DocumentDownloadUrlResponse(
        UUID documentId,
        String url,
        Instant expiresAt
) { }
