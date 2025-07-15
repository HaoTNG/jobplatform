package com.haotng.jobplatform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerRegisterDTO {
    private Long userId;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String address;
    private String skills;
    private String experience;
}
