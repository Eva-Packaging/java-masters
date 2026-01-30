package com.taskflow.taskflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record CreateUserRequest(
        @NotEmpty
        String name,

        @Email
        String email
) {

}
