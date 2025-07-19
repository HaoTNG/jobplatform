package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.dto.ApplicationRequest;
import com.haotng.jobplatform.dto.ApplicationResponseDTO;
import com.haotng.jobplatform.entity.*;
import com.haotng.jobplatform.respository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class hiApplicationController {
    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;

    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> create(@Valid @RequestBody ApplicationRequest request, Authentication authentication) {
        String email = authentication.getName();
        log.info("Creating application for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getRole().equals(Role.JOB_SEEKER)) {
            log.error("User {} does not have JOB_SEEKER role", email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only JOB_SEEKER can apply");
        }

        JobSeeker jobSeeker = jobSeekerRepository.findById(request.getJobSeekerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "JobSeeker not found"));
        if (!jobSeeker.getUser().getId().equals(user.getId())) {
            log.error("JobSeeker ID {} does not belong to user {}", request.getJobSeekerId(), email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to apply for this JobSeeker");
        }

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        Application application = new Application();
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setAppliedAt(LocalDateTime.now());
        application.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        application.setCoverLetter(request.getCoverLetter());

        Application saved = applicationRepository.save(application);
        log.info("Application created: ID={}, JobID={}, JobSeekerID={}", saved.getId(), job.getId(), jobSeeker.getId());

        ApplicationResponseDTO response = new ApplicationResponseDTO();
        response.setId(saved.getId());
        response.setJobId(saved.getJob().getId());
        response.setJobSeekerId(saved.getJobSeeker().getId());
        response.setStatus(saved.getStatus());
        response.setCoverLetter(saved.getCoverLetter());
        response.setAppliedAt(saved.getAppliedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{jobId}")
    public List<ApplicationResponseDTO> getByJob(@PathVariable Long jobId, Authentication authentication) {
        String email = authentication.getName();
        log.info("Retrieving applications for JobID={} by user: {}", jobId, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getRole().equals(Role.EMPLOYER)) {
            log.error("User {} does not have EMPLOYER role", email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only EMPLOYER can view applications");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        Employer employer = employerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
        if (!job.getEmployer().getId().equals(employer.getId())) {
            log.error("User {} not authorized to view applications for JobID={}", email, jobId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this job's applications");
        }

        List<Application> applications = applicationRepository.findByJobId(jobId);
        log.info("Retrieved {} applications for JobID={}", applications.size(), jobId);

        return applications.stream().map(app -> {
            ApplicationResponseDTO dto = new ApplicationResponseDTO();
            dto.setId(app.getId());
            dto.setJobId(app.getJob().getId());
            dto.setJobSeekerId(app.getJobSeeker().getId());
            dto.setStatus(app.getStatus());
            dto.setCoverLetter(app.getCoverLetter());
            dto.setAppliedAt(app.getAppliedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> updateApplication(@PathVariable Long id, @Valid @RequestBody ApplicationRequest request, Authentication authentication) {
        String email = authentication.getName();
        log.info("Updating application ID={} by user: {}", id, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getRole().equals(Role.EMPLOYER)) {
            log.error("User {} does not have EMPLOYER role", email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only EMPLOYER can update applications");
        }

        Application existing = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        Job job = jobRepository.findById(existing.getJob().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        Employer employer = employerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
        if (!job.getEmployer().getId().equals(employer.getId())) {
            log.error("User {} not authorized to update application ID={}", email, id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this application");
        }

        if (request.getStatus() != null) {
            if (!List.of("PENDING", "ACCEPTED", "REJECTED").contains(request.getStatus())) {
                log.error("Invalid status: {}", request.getStatus());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
            }
            existing.setStatus(request.getStatus());
        }
        if (request.getCoverLetter() != null) {
            existing.setCoverLetter(request.getCoverLetter());
        }
        applicationRepository.save(existing);
        log.info("Application updated: ID={}, Status={}", id, existing.getStatus());

        ApplicationResponseDTO response = new ApplicationResponseDTO();
        response.setId(existing.getId());
        response.setJobId(existing.getJob().getId());
        response.setJobSeekerId(existing.getJobSeeker().getId());
        response.setStatus(existing.getStatus());
        response.setCoverLetter(existing.getCoverLetter());
        response.setAppliedAt(existing.getAppliedAt());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        log.info("Deleting application ID={} by user: {}", id, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Application existing = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (user.getRole().equals(Role.EMPLOYER)) {
            Employer employer = employerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
            if (!existing.getJob().getEmployer().getId().equals(employer.getId())) {
                log.error("User {} not authorized to delete application ID={}", email, id);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this application");
            }
        } else if (user.getRole().equals(Role.JOB_SEEKER)) {
            JobSeeker jobSeeker = jobSeekerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "JobSeeker not found"));
            if (!existing.getJobSeeker().getId().equals(jobSeeker.getId())) {
                log.error("User {} not authorized to delete application ID={}", email, id);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this application");
            }
        } else {
            log.error("User {} does not have EMPLOYER or JOB_SEEKER role", email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only EMPLOYER or JOB_SEEKER can delete applications");
        }

        applicationRepository.delete(existing);
        log.info("Application deleted: ID={}", id);
        return ResponseEntity.noContent().build();
    }
}