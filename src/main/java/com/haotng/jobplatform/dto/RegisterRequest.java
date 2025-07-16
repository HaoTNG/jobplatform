package com.haotng.jobplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must be at least 8 characters long and contain at least one letter and one number")
    private String password;

    @NotBlank(message = "Role cannot be empty")
    @Pattern(regexp = "^(EMPLOYER|JOB_SEEKER|ADMIN)$",
            message = "Role must be EMPLOYER, JOB_SEEKER, or ADMIN")
    private String role;

    // Fields for Employer
    @NotBlank(message = "Company name is required for EMPLOYER", groups = EmployerValidation.class)
    private String companyName;

    private String industry;

    private String website;

    private String address;

    // Fields for JobSeeker
    @NotBlank(message = "Full name is required for JOB_SEEKER", groups = JobSeekerValidation.class)
    private String fullName;

    private String phone;

    private String skills;

    private String experience;

    // Validation groups
    public interface EmployerValidation {}
    public interface JobSeekerValidation {}
}