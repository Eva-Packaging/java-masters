package com.frauddetection.claims.entity;

import com.frauddetection.claims.dto.DocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "claim_documents")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 50, nullable = false)
    private DocumentType documentType;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "storage_provider", length = 20, nullable = false)
    private String storageProvider;

    @Column(name = "storage_key", length = 500, nullable = false)
    private String storageKey;

    @Column(name = "sha256_hash", length = 64)
    private String sha256Hash;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
