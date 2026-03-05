package com.frauddetection.claims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import java.security.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "policies")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "policy_id")
    private UUID id;
    @Column(name = "policy_number", length = 40, unique = true, nullable = false)
    private String policyNumber;
    
    @Column(name = "policy_type", length = 50, nullable = false)
    private String policyType;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;    

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
