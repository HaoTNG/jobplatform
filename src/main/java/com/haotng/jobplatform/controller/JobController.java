package com.haotng.jobplatform.controller;


import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.Job;
import com.haotng.jobplatform.respository.EmployerRepository;
import com.haotng.jobplatform.respository.JobRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobRepository jobRepository;
    private final EmployerRepository employerRepository;

    // 📌 POST /api/jobs → Tạo job mới
    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        if (job.getEmployer() == null || job.getEmployer().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employer ID must be provided");
        }

        Long employerId = job.getEmployer().getId();
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));

        job.setEmployer(employer);
        Job saved = jobRepository.save(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    // 📌 GET /api/jobs → Lấy tất cả job
    @GetMapping
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // 📌 GET /api/jobs/employer/{id} → Job của employer
    @GetMapping("/employer/{id}")
    public List<Job> getByEmployer(@PathVariable Long id) {
        return jobRepository.findByEmployerId(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job job) {
        Job existing = jobRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if(job.getTitle() != null) existing.setTitle(job.getTitle());
        if(job.getDescription() != null) existing.setDescription(job.getDescription());
        if(job.getLocation() != null) existing.setLocation(job.getLocation());
        if(job.getSalary() != null) existing.setSalary(job.getSalary());

        jobRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteJob(@RequestBody Job job) {
        Job existing = jobRepository.findById(job.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        jobRepository.delete(existing);
        return ResponseEntity.ok().build();
    }
}