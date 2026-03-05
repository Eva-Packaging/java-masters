package com.frauddetection.claims.mapper;

import com.frauddetection.claims.dto.*;
import com.frauddetection.claims.entity.Claim;
import com.frauddetection.claims.entity.ClaimDocument;
import com.frauddetection.claims.entity.ClaimStatusHistory;

import java.math.BigDecimal;
import java.util.UUID;

public interface ClaimMapper {

    // ========== Claim Entity Mappers ==========

    static Claim toEntity(CreateClaimRequest req) {
        if (req == null) {
            return null;
        }

        BigDecimal lossAmount = req.getLossAmountEstimate() == null
                ? null
                : BigDecimal.valueOf(req.getLossAmountEstimate());

        
        return Claim.builder()
                .claimantId(UUID.randomUUID()) //TODO: add second entity to change 
                .claimNumber(req.getClaimNumber())
                .claimType(req.getClaimType())
                .status(req.getStatus())
                .lossDate(req.getLossDate())
                .reportedAt(req.getReportedAt())
                .lossAmountEstimate(lossAmount)
                .description(req.getDescription())
                .regionCode(req.getRegionCode())
                .createdByUserId(req.getCreatedByUserId())
                .build();

    }

    static ClaimDTO toDTO(Claim claim){
        return ClaimDTO.builder()
                .claimId(claim.getClaimId())
                .policyId(claim.getPolicy().getId())
                .claimantId(claim.getClaimantId())
                .claimNumber(claim.getClaimNumber())
                .claimType(claim.getClaimType())
                .status(claim.getStatus())
                .lossDate(claim.getLossDate())
                .reportedAt(claim.getReportedAt())
                .lossAmountEstimate(claim.getLossAmountEstimate())
                .description(claim.getDescription())
                .regionCode(claim.getRegionCode())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    static CreateClaimResponse toCreateClaimResponse(Claim claim){
        CreateClaimResponse response = new CreateClaimResponse();
        response.setClaimId(claim.getClaimId());
        response.setClaimNumber(claim.getClaimNumber());
        response.setStatus(claim.getStatus());
        response.setReportedAt(claim.getReportedAt());
        return response;
    }

    static void updateClaimStatus(Claim claim, UpdateStatusRequest req) {
        claim.setStatus((req.status()));
    }

    static ClaimDocument toClaimDocumentEntity(EvidenceMetaDataUpload upload, UUID claimId, UUID uploadedByUserId) {
        return ClaimDocument.builder()
                .claimId(claimId)
                .uploadedByUserId(uploadedByUserId)
                .documentType(upload.getType())
                .fileName(upload.getFileName())
                .contentType(upload.getContentType())
                .fileSizeBytes(upload.getFileSizeBytes())
                .sha256Hash(upload.getSha256Hash())
                .storageProvider("S3")  // or make this configurable
                .storageKey("claims/" + claimId + "/" + UUID.randomUUID() + "/" + upload.getFileName())
                .build();
        // uploadedAt is auto-set by @PrePersist
    }

    static ClaimDocumentItem toClaimDocumentItem(ClaimDocument document) {

        // TODO: Fill in all fields
        return null;
    }

    static EvidenceMetaDataResponse toEvidenceMetaDataResponse(ClaimDocument document) {
        // TODO: Fill in all fields
        return null;
    }

    static ClaimHistoryEvent toClaimHistoryEvent(ClaimStatusHistory history) {
        // TODO: Fill in all fields
        return null;
    }

}
