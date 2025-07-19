package com.haotng.jobplatform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponseDTO {
    private Long id;
    private Long jobId;
    private Long jobSeekerId;
    private String status;
    private String coverLetter;
    private LocalDateTime appliedAt;
}