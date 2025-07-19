package com.haotng.jobplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobRequest {
    @NotNull(message = "Employer ID cannot be null")
    private Long employerId;
    private String title;
    private String description;
    private String location;
    private Double salary;
}