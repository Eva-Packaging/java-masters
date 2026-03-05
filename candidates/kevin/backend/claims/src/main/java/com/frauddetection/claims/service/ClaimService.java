package com.frauddetection.claims.service;

import com.frauddetection.claims.dto.*;
import com.frauddetection.claims.entity.Claim;
import com.frauddetection.claims.entity.Policy;
import com.frauddetection.claims.mapper.ClaimMapper;
import com.frauddetection.claims.repo.ClaimRepo;
import com.frauddetection.claims.repo.PolicyRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class ClaimService {

    private ClaimRepo claimRepo;

    private PolicyRepo policyRepo;
   
    public ClaimService(ClaimRepo claimRepo, PolicyRepo policyRepo) {
        this.claimRepo = claimRepo;
        this.policyRepo = policyRepo;
    }

    public CreateClaimResponse createClaim(CreateClaimRequest req) {
        // need to query policy from the policyNumber and claimant from the claimant Object
        Policy policy = policyRepo.findByPolicyNumber(req.getPolicyNumber())
                .orElseThrow(() -> new RuntimeException("Policy not found: " + req.getPolicyNumber()));
        Claim claim = ClaimMapper.toEntity(req);
        claim.setPolicy(policy);
        claimRepo.save(claim);
        //TODO: Also need to produce claim event here after we implement Kafka
        return ClaimMapper.toCreateClaimResponse(claim);
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

    public Page<ClaimDTO> searchClaims(ClaimStatus status, String claimNumber, int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "asc";

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<Claim> claimsPage;

        if (status != null && claimNumber != null) {
            claimsPage = claimRepo.findByStatusAndClaimNumberContaining(status, claimNumber, pageable);
        } else if (status != null) {
            claimsPage = claimRepo.findByStatus(status, pageable);
        } else if (claimNumber != null) {
            claimsPage = claimRepo.findByClaimNumberContaining(claimNumber, pageable);
        } else {
            claimsPage = claimRepo.findAll(pageable);
        }

        return claimsPage.map(ClaimMapper::toDTO);
    }
}
