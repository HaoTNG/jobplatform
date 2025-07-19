package com.haotng.jobplatform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {
    @NotNull(message = "Job ID cannot be null")
    private Long jobId;

    @NotNull(message = "JobSeeker ID cannot be null")
    private Long jobSeekerId;

    private String coverLetter;
    private String status;
}