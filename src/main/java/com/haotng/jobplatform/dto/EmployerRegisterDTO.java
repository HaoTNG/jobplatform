package com.haotng.jobplatform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployerRegisterDTO {
    private Long userId; // Nếu đã có User
    private String email; // Nếu muốn tự tạo User
    private String password;

    private String companyName;
    private String industry;
    private String website;
    private String address;
}
