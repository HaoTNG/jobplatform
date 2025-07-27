package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.entity.*;
import com.haotng.jobplatform.respository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final ApplicationRepository applicationRepository;


    @GetMapping("/seeker")
    public ResponseEntity<?> getSeekerApplications(Authentication authentication) {
        String email = authentication.getName();
        logger.info("Fetching applications for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", email);
                    return new RuntimeException("User not found");
                });

        if (user.getRole() != Role.JOB_SEEKER) {
            logger.error("Unauthorized: User {} is not a JOB_SEEKER", email);
            return ResponseEntity.status(403).body("Unauthorized: Not a JOB_SEEKER");
        }

        JobSeeker jobSeeker = jobSeekerRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    logger.error("JobSeeker not found for user: {}", email);
                    return new RuntimeException("JobSeeker not found");
                });

        List<Application> applications = applicationRepository.findByJobSeeker(jobSeeker);
        List<ApplicationResponse> response = applications.stream()
                .map(app -> new ApplicationResponse(
                        app.getId(),
                        app.getJob().getId(),
                        app.getJob().getTitle(),
                        app.getStatus(),
                        app.getCoverLetter(),
                        app.getAppliedAt()
                ))
                .collect(Collectors.toList());

        logger.info("Fetched {} applications for email: {}", applications.size(), email);
        return ResponseEntity.ok(response);
    }

    static class ApplicationResponse {
        private final Long id;
        private final Long jobId;
        private final String jobTitle;
        private final String status;
        private final String coverLetter;
        private final LocalDateTime appliedAt;

        public ApplicationResponse(Long id, Long jobId, String jobTitle, String status, String coverLetter, LocalDateTime appliedAt) {
            this.id = id;
            this.jobId = jobId;
            this.jobTitle = jobTitle;
            this.status = status;
            this.coverLetter = coverLetter;
            this.appliedAt = appliedAt;
        }

        public Long getId() { return id; }
        public Long getJobId() { return jobId; }
        public String getJobTitle() { return jobTitle; }
        public String getStatus() { return status; }
        public String getCoverLetter() { return coverLetter; }
        public LocalDateTime getAppliedAt() { return appliedAt; }
    }
}