package com.haotng.jobplatform.controller;


import com.haotng.jobplatform.entity.*;
import com.haotng.jobplatform.respository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final JobSeekerRepository jobSeekerRepository;

    @PostMapping
    public ResponseEntity<Application> create(@RequestBody Application application) {
        if (application.getJob() == null || application.getJob().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lacking (job.id) in Application");
        }

        if (application.getJobSeeker() == null || application.getJobSeeker().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lacking (jobSeeker.id) in Application");
        }

        // Tìm job
        Job job = jobRepository.findById(application.getJob().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        // Tìm job seeker
        JobSeeker jobSeeker = jobSeekerRepository.findById(application.getJobSeeker().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "JobSeeker not found"));

        // Gán đầy đủ
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setAppliedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    @GetMapping("/{jobId}")
    public List<Application> getByJob(@PathVariable Long jobId){
        return applicationRepository.findByJobId(jobId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable Long id, @RequestBody Application updated) {
        Application existing = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if (updated.getCoverLetter() != null) existing.setCoverLetter(updated.getCoverLetter());
        applicationRepository.save(existing);

        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        Application existing = applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        applicationRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }

}

