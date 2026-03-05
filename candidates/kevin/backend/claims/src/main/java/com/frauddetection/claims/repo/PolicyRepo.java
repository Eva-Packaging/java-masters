package com.frauddetection.claims.repo;

import com.frauddetection.claims.entity.Claim;
import com.frauddetection.claims.entity.Policy;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PolicyRepo extends JpaRepository<Policy, UUID> {
    /*
    TODO: add methods as needed
     */
    Optional<Policy> findByPolicyNumber(String policyNumber);
}
