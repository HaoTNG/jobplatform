package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.dto.JobRequest;
import com.haotng.jobplatform.dto.JobResponseDTO;
import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.Job;
import com.haotng.jobplatform.entity.Role;
import com.haotng.jobplatform.entity.User;
import com.haotng.jobplatform.respository.EmployerRepository;
import com.haotng.jobplatform.respository.JobRepository;
import com.haotng.jobplatform.respository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private static final Logger log = LoggerFactory.getLogger(JobController.class);
    private final JobRepository jobRepository;
    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<JobResponseDTO> createJob(@Valid @RequestBody JobRequest request, Authentication authentication) {
        String email = authentication.getName();
        log.info("Creating job for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getRole().equals(Role.EMPLOYER)) {
            log.error("User {} does not have EMPLOYER role", email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only EMPLOYER can create jobs");
        }

        Employer employer = employerRepository.findById(request.getEmployerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
        if (!employer.getUser().getId().equals(user.getId())) {
            log.error("Employer ID {} does not belong to user {}", request.getEmployerId(), email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to create job for this employer");
        }

        Job job = new Job();
        job.setEmployer(employer);
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalary(request.getSalary());

        Job saved = jobRepository.save(job);
        log.info("Job created: ID={}, EmployerID={}", saved.getId(), employer.getId());

        JobResponseDTO response = new JobResponseDTO();
        response.setId(saved.getId());
        response.setEmployerId(saved.getEmployer().getId());
        response.setTitle(saved.getTitle());
        response.setDescription(saved.getDescription());
        response.setLocation(saved.getLocation());
        response.setSalary(saved.getSalary());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<JobResponseDTO> getAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        log.info("Retrieved {} jobs", jobs.size());
        return jobs.stream().map(job -> {
            JobResponseDTO dto = new JobResponseDTO();
            dto.setId(job.getId());
            dto.setEmployerId(job.getEmployer().getId());
            dto.setTitle(job.getTitle());
            dto.setDescription(job.getDescription());
            dto.setLocation(job.getLocation());
            dto.setSalary(job.getSalary());
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/employer/{id}")
    public List<JobResponseDTO> getByEmployer(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        log.info("Retrieving jobs for EmployerID={} by user: {}", id, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getRole().equals(Role.EMPLOYER)) {
            log.error("User {} does not have EMPLOYER role", email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only EMPLOYER can view their jobs");
        }
        Employer employer = employerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
        if (!employer.getId().equals(id)) {
            log.error("User {} not authorized to view jobs for EmployerID={}", email, id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this employer's jobs");
        }

        List<Job> jobs = jobRepository.findByEmployerId(id);
        log.info("Retrieved {} jobs for EmployerID={}", jobs.size(), id);
        return jobs.stream().map(job -> {
            JobResponseDTO dto = new JobResponseDTO();
            dto.setId(job.getId());
            dto.setEmployerId(job.getEmployer().getId());
            dto.setTitle(job.getTitle());
            dto.setDescription(job.getDescription());
            dto.setLocation(job.getLocation());
            dto.setSalary(job.getSalary());
            return dto;
        }).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponseDTO> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request, Authentication authentication) {
        String email = authentication.getName();
        log.info("Updating job ID={} by user: {}", id, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getRole().equals(Role.EMPLOYER)) {
            log.error("User {} does not have EMPLOYER role", email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only EMPLOYER can update jobs");
        }

        Job existing = jobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        Employer employer = employerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
        if (!existing.getEmployer().getId().equals(employer.getId())) {
            log.error("User {} not authorized to update job ID={}", email, id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this job");
        }

        if (request.getTitle() != null) existing.setTitle(request.getTitle());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getLocation() != null) existing.setLocation(request.getLocation());
        if (request.getSalary() != null) existing.setSalary(request.getSalary());

        jobRepository.save(existing);
        log.info("Job updated: ID={}, EmployerID={}", id, employer.getId());

        JobResponseDTO response = new JobResponseDTO();
        response.setId(existing.getId());
        response.setEmployerId(existing.getEmployer().getId());
        response.setTitle(existing.getTitle());
        response.setDescription(existing.getDescription());
        response.setLocation(existing.getLocation());
        response.setSalary(existing.getSalary());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        log.info("Deleting job ID={} by user: {}", id, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getRole().equals(Role.EMPLOYER)) {
            log.error("User {} does not have EMPLOYER role", email);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only EMPLOYER can delete jobs");
        }

        Job existing = jobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        Employer employer = employerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
        if (!existing.getEmployer().getId().equals(employer.getId())) {
            log.error("User {} not authorized to delete job ID={}", email, id);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this job");
        }

        jobRepository.delete(existing);
        log.info("Job deleted: ID={}", id);
        return ResponseEntity.noContent().build();
    }
}