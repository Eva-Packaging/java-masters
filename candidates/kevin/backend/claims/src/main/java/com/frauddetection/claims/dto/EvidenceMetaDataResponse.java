package com.frauddetection.claims.dto;

import lombok.Data;
import org.hibernate.validator.constraints.UUID;
import java.util.List;

@Data
public class EvidenceMetaDataResponse {
    @UUID
    private UUID id;
    private List<APIRequest> request;
}
