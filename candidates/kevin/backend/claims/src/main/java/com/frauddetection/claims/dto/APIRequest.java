package com.frauddetection.claims.dto;

import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

public class APIRequest {
    private RequestName method;

    @URL
    private String url;

    private DateTimeFormat expiresAt;
}
