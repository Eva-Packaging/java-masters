package com.frauddetection.claims.repo;

import com.frauddetection.claims.dto.ClaimStatus;
import com.frauddetection.claims.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClaimRepo extends JpaRepository<Claim, UUID> {

    Page<Claim> findByStatusAndClaimNumberContaining(ClaimStatus status, String claimNumber, Pageable pageable);
    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);
    Page<Claim> findByClaimNumberContaining(String claimNumber, Pageable pageable);
}
