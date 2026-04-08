package com.frauddetection.claims.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        log.error("An error occurred: {}", ex.getMessage(), ex);
        ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.status(problemDetails.getStatus()).body(problemDetails);
    }

    @ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleClaimNotFoundException(Exception ex){
        log.error("Claim not found: ", ex.getMessage(), ex);
        ProblemDetail problemDetails = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(problemDetails.getStatus()).body(problemDetails);
    }
    
}
