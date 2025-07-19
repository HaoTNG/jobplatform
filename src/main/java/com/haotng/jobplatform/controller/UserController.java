package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.JobSeeker;
import com.haotng.jobplatform.entity.Role;
import com.haotng.jobplatform.entity.User;
import com.haotng.jobplatform.respository.EmployerRepository;
import com.haotng.jobplatform.respository.JobSeekerRepository;
import com.haotng.jobplatform.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerRepository jobSeekerRepository;

    public UserController(UserRepository userRepository, EmployerRepository employerRepository, JobSeekerRepository jobSeekerRepository) {
        this.userRepository = userRepository;
        this.employerRepository = employerRepository;
        this.jobSeekerRepository = jobSeekerRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("EMPLOYER".equals(user.getRole())) {
            Employer employer = employerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Employer not found"));
            return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getRole(), employer.getId()));
        } else if ("JOB_SEEKER".equals(user.getRole())) {
            JobSeeker jobSeeker = jobSeekerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("JobSeeker not found"));
            return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getRole(), jobSeeker.getId()));
        } else {
            throw new RuntimeException("Invalid user role");
        }


    }

    static class UserResponse {
        private final Long userId;
        private final String email;
        private final Role role;
        private final Long profileId;

        public UserResponse(Long userId, String email, Role role, Long profileId) {
            this.userId = userId;
            this.email = email;
            this.role = role;
            this.profileId = profileId;
        }

        public Long getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public Role getRole() {
            return role;
        }

        public Long getProfileId() {
            return profileId;
        }
    }
}    
