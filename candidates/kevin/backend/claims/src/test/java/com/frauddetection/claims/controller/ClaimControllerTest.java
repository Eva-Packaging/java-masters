package com.frauddetection.claims.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import com.frauddetection.claims.dto.ClaimDTO;
import com.frauddetection.claims.dto.ClaimDocumentItem;
import com.frauddetection.claims.dto.ClaimDocumentsResponse;
import com.frauddetection.claims.dto.ClaimHistoryResponse;
import com.frauddetection.claims.dto.ClaimStatus;
import com.frauddetection.claims.dto.ClaimType;
import com.frauddetection.claims.dto.ClaimantDTO;
import com.frauddetection.claims.dto.CreateClaimRequest;
import com.frauddetection.claims.dto.CreateClaimResponse;
import com.frauddetection.claims.dto.DocumentDownloadUrlResponse;
import com.frauddetection.claims.dto.DocumentType;
import com.frauddetection.claims.dto.EvidenceMetaDataResponse;
import com.frauddetection.claims.dto.EvidenceMetaDataUpload;
import com.frauddetection.claims.dto.UpdateStatusRequest;
import com.frauddetection.claims.exception.GlobalExceptionHandler;
import com.frauddetection.claims.service.ClaimService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;



import tools.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;


@WebMvcTest(ClaimController.class)
@Import(GlobalExceptionHandler.class)
public class ClaimControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    //setup
    private CreateClaimRequest request;
    private CreateClaimResponse response;
    private UUID claimId;
    private LocalDateTime reportedAt;
    private ClaimantDTO claimant;
    private ClaimDTO claimDto;
    private UpdateStatusRequest updateStatusRequest;
    private ClaimDTO updatedClaimDto;
    private ClaimHistoryResponse claimHistoryResponse;
    private EvidenceMetaDataUpload evidenceMetaDataUpload;
    private EvidenceMetaDataResponse evidenceMetaDataResponse;
    private UUID documentId;
    private ClaimDocumentItem claimDocumentItem;
    private ClaimDocumentsResponse claimDocumentResponse;
    private DocumentDownloadUrlResponse downloadUrlResponse;





    @MockitoBean
    private ClaimService claimService;

    @BeforeEach
    void setup(){
        claimant = new ClaimantDTO();
        claimant.setClaimantId(UUID.randomUUID());
        claimant.setFirstName("Kevin");
        claimant.setLastName("DeCara");

        claimId = UUID.randomUUID();
        documentId = UUID.randomUUID();
        reportedAt = LocalDateTime.of(2026, 3, 30, 10, 15);

        claimDto = ClaimDTO.builder()
                .claimId(claimId)
                .policyId(UUID.randomUUID())
                .claimantId(claimant.getClaimantId())
                .claimNumber("CLM-123")
                .claimType(ClaimType.AUTO)
                .status(ClaimStatus.SUBMITTED)
                .lossDate(LocalDate.of(2025, 3, 25))
                .reportedAt(reportedAt)
                .lossAmountEstimate(BigDecimal.valueOf(2500.00))
                .description("Rear-end collision")
                .regionCode("US-NY")
                .updatedAt(reportedAt)
                .build();

        updateStatusRequest = new UpdateStatusRequest("moved claim into review", ClaimStatus.IN_REVIEW);

        claimHistoryResponse = new ClaimHistoryResponse();
        claimHistoryResponse.setClaimId(claimId);
        claimHistoryResponse.setHistory(List.of());


        updatedClaimDto = ClaimDTO.builder()
            .claimId(claimId)
            .policyId(claimDto.getPolicyId())
            .claimantId(claimant.getClaimantId())
            .claimNumber("CLM-123")
            .claimType(ClaimType.AUTO)
            .status(ClaimStatus.IN_REVIEW)
            .lossDate(LocalDate.of(2026, 3, 25))
            .reportedAt(reportedAt)
            .lossAmountEstimate(BigDecimal.valueOf(2500.00))
            .description("Rear-end collision")
            .regionCode("US-NY")
            .updatedAt(reportedAt)
            .build();
       
        //claimDto.equals(claimDto);

        request = new CreateClaimRequest();
        request.setClaimID(claimId);
        request.setClaimNumber("CLM-123");
        request.setPolicyNumber("POL-456");
        request.setLossDate(LocalDate.of(2026, 3, 25));
        request.setLossAmountEstimate(2500.00);
        request.setDescription("Rear-end collision");
        request.setRegionCode("US-NY");
        request.setClaimant(claimant);
        request.setStatus(ClaimStatus.SUBMITTED);
        request.setClaimType(ClaimType.AUTO);
        request.setCreatedByUserId(UUID.randomUUID());
        request.setReportedAt(reportedAt);

        response = new CreateClaimResponse();
        response.setClaimId(claimId);
        response.setClaimNumber("CLM-123");
        response.setStatus(ClaimStatus.SUBMITTED);
        response.setReportedAt(reportedAt);

        evidenceMetaDataUpload = new EvidenceMetaDataUpload();
        evidenceMetaDataUpload.setType(DocumentType.POLICE_REPORT);
        evidenceMetaDataUpload.setFileName("report.pdf");
        evidenceMetaDataUpload.setContentType("application/pdf");
        evidenceMetaDataUpload.setFileSizeBytes(1024L);
        evidenceMetaDataUpload.setSha256Hash("abc123");

        evidenceMetaDataResponse = new EvidenceMetaDataResponse();
        evidenceMetaDataResponse.setRequest(List.of());

        claimDocumentItem = new ClaimDocumentItem(
            documentId,
            DocumentType.POLICE_REPORT,
            "report.pdf",
            Instant.parse("2026-02-05T16:10:22Z"),
        "jane.adjuster@company.com"        
        );

        claimDocumentResponse = new ClaimDocumentsResponse(
            claimId,
            List.of(claimDocumentItem)
        );

        downloadUrlResponse = new DocumentDownloadUrlResponse(
            documentId,
            "https://storage.example.com/documents/" + documentId + "?expires=3600",
            Instant.parse("2026-04-08T18:00:00Z")        
        );

    }

    // Tests should follow the arrange, act, assert pattern
    @Test
    @WithMockUser
    void testCreateClaim() throws Exception {

        when(claimService.createClaim(any(CreateClaimRequest.class))).thenReturn(response);

        mockMvc.perform(
            post("/claims")

            //attack a valid token to state changing request for security
            .with(csrf()) 
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )

        //verify the result after the request succeeds
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.claimId").value(response.getClaimId().toString()))
        .andExpect(jsonPath("$.claimNumber").value(response.getClaimNumber()))
        .andExpect(jsonPath("$.status").value(response.getStatus().name()));

        verify(claimService).createClaim(any(CreateClaimRequest.class));

    }

    //Return 200 plus fields in the DTO for the claim
    @Test
    @WithMockUser
    void testGetClaim() throws Exception {

        when(claimService.getClaimById(claimId)).thenReturn(claimDto); 

        mockMvc.perform(get("/claims/{claimId}", claimId))
                //return 200
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimId").value(claimId.toString()))
                .andExpect(jsonPath("$.claimNumber").value("CLM-123"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

    }

    //assert 200 plus the table changed
    @Test
    @WithMockUser
    void testUpdateClaimStatus() throws Exception {
        when(claimService.updateStatus(claimId, updateStatusRequest))
            .thenReturn(updatedClaimDto);
        
            mockMvc.perform(patch("/claims/{claimId}/status", claimId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateStatusRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.claimId").value(claimId.toString()))
            .andExpect(jsonPath("$.claimNumber").value("CLM-123"))
            .andExpect(jsonPath("$.status").value("IN_REVIEW"));
        
            verify(claimService).updateStatus(claimId, updateStatusRequest);

    }

    //assert 200 plus the history returned
    @Test
    @WithMockUser
    void testGetClaimHistory() throws Exception {
        when(claimService.getClaimHistory(claimId)).thenReturn(claimHistoryResponse);

        mockMvc.perform(get("/claims/{claimId}/history", claimId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.claimId").value(claimId.toString()))
        .andExpect(jsonPath("$.history").isArray());

    }

    @Test
    @WithMockUser
    void testUploadEvidenceMetaData() throws Exception {
        when(claimService.uploadEvidenceMetaData(evidenceMetaDataUpload, claimId))
        .thenReturn(evidenceMetaDataResponse);

        mockMvc.perform(post("/claims/{claimId}/documents", claimId)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(evidenceMetaDataUpload)))
            .andExpect(status().isCreated());

        verify(claimService).uploadEvidenceMetaData(evidenceMetaDataUpload, claimId);

    }

    @Test
    @WithMockUser
    void testGetClaimDocuments() throws Exception {
        when(claimService.getClaimDocuments(claimId)).thenReturn(claimDocumentResponse);

        mockMvc.perform(get("/claims/{claimId}/documents", claimId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.claimId").value(claimId.toString()))
            .andExpect(jsonPath("$.items[0].documentId").value(documentId.toString()))
            .andExpect(jsonPath("$.items[0].documentType").value("POLICE_REPORT"))
            .andExpect(jsonPath("$.items[0].fileName").value("report.pdf"))
            .andExpect(jsonPath("$.items[0].uploadedBy").value("jane.adjuster@company.com"));

        verify(claimService).getClaimDocuments(claimId);

    }

    @Test
    @WithMockUser
    void testGetDownloadUrl() throws Exception {
        when(claimService.getDownloadUrl(documentId)).thenReturn(downloadUrlResponse);

         mockMvc.perform(get("/documents/{documentId}/download-url", documentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documentId").value(documentId.toString()))
            .andExpect(jsonPath("$.url").value("https://storage.example.com/documents/" + documentId + "?expires=3600"))
            .andExpect(jsonPath("$.expiresAt").value("2026-04-08T18:00:00Z"));

        verify(claimService).getDownloadUrl(documentId);
    }

 

}
