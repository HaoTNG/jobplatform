package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.dto.AuthRequest;
import com.haotng.jobplatform.dto.AuthResponse;
import com.haotng.jobplatform.dto.RegisterRequest;
import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.JobSeeker;
import com.haotng.jobplatform.entity.Role;
import com.haotng.jobplatform.entity.User;
import com.haotng.jobplatform.respository.EmployerRepository;
import com.haotng.jobplatform.respository.JobSeekerRepository;
import com.haotng.jobplatform.respository.UserRepository;
import com.haotng.jobplatform.security.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            log.warn("Login failed for email: {}. Reason: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Invalid email or password\"}");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Validated({RegisterRequest.EmployerValidation.class, RegisterRequest.JobSeekerValidation.class})
            @RequestBody RegisterRequest request) {
        log.info("Register attempt for email: {}", request.getEmail());

        // Kiểm tra email trùng lặp
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Email already exists\"}");
        }

        // Tạo User
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .build();
        userRepository.save(user);

        // Tạo profile dựa trên role
        if (user.getRole() == Role.EMPLOYER) {
            if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                log.warn("Registration failed: Company name is required for EMPLOYER");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Company name is required for EMPLOYER\"}");
            }
            Employer employer = Employer.builder()
                    .user(user)
                    .companyName(request.getCompanyName())
                    .industry(request.getIndustry())
                    .website(request.getWebsite())
                    .address(request.getAddress())
                    .build();
            employerRepository.save(employer);
            log.info("Employer profile created for email: {}", request.getEmail());
        } else if (user.getRole() == Role.JOB_SEEKER) {
            if (request.getFullName() == null || request.getFullName().isBlank()) {
                log.warn("Registration failed: Full name is required for JOB_SEEKER");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"Full name is required for JOB_SEEKER\"}");
            }
            JobSeeker jobSeeker = JobSeeker.builder()
                    .user(user)
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .skills(request.getSkills())
                    .experience(request.getExperience())
                    .build();
            jobSeekerRepository.save(jobSeeker);
            log.info("JobSeeker profile created for email: {}", request.getEmail());
        }

        log.info("User registered successfully: {}", request.getEmail());
        return ResponseEntity.ok("{\"message\": \"User registered successfully\"}");
    }
}