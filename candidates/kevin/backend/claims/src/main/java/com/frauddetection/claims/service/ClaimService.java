package com.frauddetection.claims.service;

import com.frauddetection.claims.dto.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimService {

    public CreateClaimResponse createClaim(CreateClaimRequest req) {
        return new CreateClaimResponse();
    }

    public ClaimDTO getClaimById(UUID claimId) {
        return new ClaimDTO();
    }

    public ClaimDTO updateStatus(UUID claimId, UpdateStatusRequest req) {
        return new ClaimDTO();
    }

    public ClaimHistoryResponse getClaimHistory(UUID claimId){
        ClaimHistoryResponse claimHistoryResponse = new ClaimHistoryResponse();
        claimHistoryResponse.setClaimId(claimId);
        return new ClaimHistoryResponse();
    }

    public EvidenceMetaDataResponse uploadEvidenceMetaData(EvidenceMetaDataUpload evidenceMetaDataUpload, UUID claimId){
        return new EvidenceMetaDataResponse();
    }

    public ClaimDocumentsResponse getClaimDocuments(UUID claimId) {
        ClaimDocumentItem item = new ClaimDocumentItem(
                UUID.fromString("f32f5aa9-2f0b-4af3-bb7d-3e3a1f1c5a4d"),
                DocumentType.POLICE_REPORT,
                "report.pdf",
                Instant.parse("2026-02-05T16:10:22Z"),
                "jane.adjuster@company.com"
        );

        return new ClaimDocumentsResponse(
                claimId,
                List.of(item)
        );
    }

    public DocumentDownloadUrlResponse getDownloadUrl(UUID documentId) {
        String url = generatePresignedUrl(documentId);
        Instant expiresAt = Instant.now().plusSeconds(3600);

        return new DocumentDownloadUrlResponse(documentId, url, expiresAt);
    }

    private String generatePresignedUrl(UUID documentId) {
        // TODO: Replace with actual S3/cloud storage presigned URL generation
        return "https://storage.example.com/documents/" + documentId + "?expires=3600";
    }

}
