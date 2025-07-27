package com.haotng.jobplatform.dto;

import lombok.*;

@Data
@Getter
@Setter
public class UserDTO {
    private String email;
    private String role;
    private String fullName;
    private String companyName;
    private String address;
    private String industry;
    private String website;
    private String phone;
    private String skills;
    private String experience;
}