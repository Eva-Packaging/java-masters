package com.frauddetection.claims.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvidenceMetaDataUpload {
    //TODO: May add annotation for validation later
    private DocumentType type;
    private String fileName;
    private String contentType;
    private Long fileSizeBytes;
    private String sha256Hash;
}
