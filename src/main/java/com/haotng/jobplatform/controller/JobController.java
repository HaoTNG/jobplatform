package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.dto.JobDTO;
import com.haotng.jobplatform.entity.Job;
import com.haotng.jobplatform.respository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    private final JobRepository jobRepository;

    public JobController(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @GetMapping
    public ResponseEntity<List<JobDTO>> getJobs() {
        logger.info("Fetching all jobs");
        List<Job> jobs = jobRepository.findAll();
        List<JobDTO> jobDTOs = jobs.stream().map(job -> {
            JobDTO dto = new JobDTO();
            dto.setId(job.getId());
            dto.setTitle(job.getTitle());
            dto.setDescription(job.getDescription());
            dto.setLocation(job.getLocation());
            dto.setSalary(job.getSalary());
            dto.setEmployerEmail(job.getEmployer().getUser().getEmail());
            dto.setCompanyName(job.getEmployer().getCompanyName());
            return dto;
        }).collect(Collectors.toList());
        logger.debug("Jobs fetched: {}", jobDTOs);
        return ResponseEntity.ok(jobDTOs);
    }

    @GetMapping("/employer/{id}")
    public ResponseEntity<List<Job>> getJobsByEmployer(@PathVariable Long id) {
        List<Job> jobs = jobRepository.findByEmployerId(id);
        return ResponseEntity.ok(jobs);
    }

    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        // Giả sử employer_id được gửi trong body hoặc lấy từ SecurityContext
        Job savedJob = jobRepository.save(job);
        return ResponseEntity.ok(savedJob);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id, @RequestBody Job jobDetails) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setTitle(jobDetails.getTitle());
        job.setDescription(jobDetails.getDescription());
        job.setLocation(jobDetails.getLocation());
        job.setSalary(jobDetails.getSalary());
        Job updatedJob = jobRepository.save(job);
        return ResponseEntity.ok(updatedJob);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        jobRepository.delete(job);
        return ResponseEntity.noContent().build();
    }
}