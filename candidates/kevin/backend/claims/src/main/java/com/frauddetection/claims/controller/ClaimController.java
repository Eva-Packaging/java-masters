package com.frauddetection.claims.controller;

import com.frauddetection.claims.dto.*;
import com.frauddetection.claims.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    //Claims

    @PostMapping("/claims")
    public ResponseEntity<CreateClaimResponse> createClaim(@RequestBody CreateClaimRequest req) {
        CreateClaimResponse response = claimService.createClaim(req);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/claims/{claimId}")
    public ResponseEntity<ClaimDTO> getClaims(@PathVariable UUID claimId) {
        ClaimDTO claimDTO = claimService.getClaimById(claimId);

        return ResponseEntity.status(200).body(claimDTO);
    }

    @PatchMapping("/claims/{claimId}/status")
    public ResponseEntity<ClaimDTO> updateClaimStatus(@PathVariable UUID claimId, @RequestBody UpdateStatusRequest req) {
        ClaimDTO updated = claimService.updateStatus(claimId, req);
        return ResponseEntity.status(200).body(updated);
    }

    @GetMapping("/claims/{claimId}/history")
    public ResponseEntity<ClaimHistoryResponse> getClaimHistory(@PathVariable UUID claimId) {
        ClaimHistoryResponse claimHistoryResponse = claimService.getClaimHistory(claimId);
        return ResponseEntity.status(200).body(claimHistoryResponse);
    }

    @PostMapping("/claims/{claimId}/documents")
    public ResponseEntity<EvidenceMetaDataResponse> uploadEvidenceMetaData(@RequestBody EvidenceMetaDataUpload evidenceMetaDataUpload,@PathVariable UUID claimId){
        EvidenceMetaDataResponse metaDataResponse = claimService.uploadEvidenceMetaData(evidenceMetaDataUpload, claimId);
        return ResponseEntity.status(201).body(metaDataResponse);
    }

    @GetMapping("/claims/{claimId}/documents")
    public ResponseEntity<ClaimDocumentsResponse> getClaimDocuments(@PathVariable UUID claimId) {
        ClaimDocumentsResponse response = claimService.getClaimDocuments(claimId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/documents/{documentId}/download-url")
    public ResponseEntity<DocumentDownloadUrlResponse> getDownloadUrl(@PathVariable UUID documentId) {
        DocumentDownloadUrlResponse response = claimService.getDownloadUrl(documentId);
        return ResponseEntity.ok(response);
    }

    //Fraud Scoring








}
