package com.haotng.jobplatform.dto;

import lombok.Data;

@Data
public class JobDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String salary;
    private String employerEmail;
    private String companyName;
}