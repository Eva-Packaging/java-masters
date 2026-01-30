package com.taskflow.taskflow.dto;

import com.taskflow.taskflow.domain.Task;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


public record CreateTaskRequest(
        @NotNull
        String title,

        @NotEmpty
        String description,

        @NotNull
        Task.Priority priority,

        //optional
        LocalDate dueDate,

        @NotNull
        Long assignedUserId
) {
    
}
