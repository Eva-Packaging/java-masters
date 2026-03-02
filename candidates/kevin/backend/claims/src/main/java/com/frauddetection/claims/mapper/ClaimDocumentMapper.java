package com.frauddetection.claims.mapper;

import com.frauddetection.claims.dto.ClaimDocumentItem;
import com.frauddetection.claims.dto.EvidenceMetaDataResponse;
import com.frauddetection.claims.dto.EvidenceMetaDataUpload;
import com.frauddetection.claims.entity.ClaimDocument;

public interface ClaimDocumentMapper {

    // EvidenceMetaDataUpload -> ClaimDocument entity
    static ClaimDocument toEntity(EvidenceMetaDataUpload upload) {
        // TODO: Fill in all fields
        return null;
    }

    // ClaimDocument entity -> ClaimDocumentItem DTO
    static ClaimDocumentItem toClaimDocumentItem(ClaimDocument document) {
        // TODO: Fill in all fields
        return null;
    }

    // ClaimDocument entity -> EvidenceMetaDataResponse
    static EvidenceMetaDataResponse toEvidenceMetaDataResponse(ClaimDocument document) {
        // TODO: Fill in all fields
        return null;
    }
}
