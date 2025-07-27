package com.haotng.jobplatform.dto;

import lombok.*;

@Data
@Getter
@Setter
public class EmployerResponseDTO {
    private Long id;
    private String companyName;
    private String industry;
    private String website;
    private String address;
    private String userEmail;
}