package com.haotng.jobplatform.dto;

import lombok.Data;

@Data
public class JobResponseDTO {
    private Long id;
    private Long employerId;
    private String title;
    private String description;
    private String location;
    private Double salary;
}