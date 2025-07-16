package com.haotng.jobplatform.dto;

import lombok.Data;

@Data
public class JobSeekerResponseDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String address;
    private String skills;
    private String experience;
    private String userEmail;
}