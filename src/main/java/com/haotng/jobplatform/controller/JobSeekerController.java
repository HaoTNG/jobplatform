
package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.dto.JobSeekerRegisterDTO;
import com.haotng.jobplatform.dto.JobSeekerResponseDTO;
import com.haotng.jobplatform.entity.JobSeeker;
import com.haotng.jobplatform.entity.Role;
import com.haotng.jobplatform.respository.JobSeekerRepository;
import com.haotng.jobplatform.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/jobseekers")
@RequiredArgsConstructor
@Slf4j
public class JobSeekerController {

    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<JobSeekerResponseDTO> createJobSeeker(@Valid @RequestBody JobSeekerRegisterDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Creating JobSeeker profile for email: {}", email);

        // Kiểm tra xem user đã có profile JobSeeker chưa
        if (jobSeekerRepository.findByUserEmail(email).isPresent()) {
            log.warn("JobSeeker profile already exists for email: {}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // Lấy user hiện tại
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Kiểm tra role
        if (user.getRole() != Role.JOB_SEEKER) {
            log.warn("User {} does not have JOB_SEEKER role", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        JobSeeker seeker = JobSeeker.builder()
                .fullName(dto.getFullName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .skills(dto.getSkills())
                .experience(dto.getExperience())
                .user(user)
                .build();

        jobSeekerRepository.save(seeker);
        log.info("JobSeeker profile created for email: {}", email);

        JobSeekerResponseDTO response = mapToResponseDTO(seeker);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<JobSeekerResponseDTO>> getAllJobSeekers(Pageable pageable) {
        // Chỉ ADMIN được truy cập danh sách
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != Role.ADMIN) {
            log.warn("Access denied to getAllJobSeekers for email: {}", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Page<JobSeekerResponseDTO> page = jobSeekerRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/me")
    public ResponseEntity<JobSeekerResponseDTO> getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching JobSeeker profile for email: {}", email);
        JobSeeker seeker = jobSeekerRepository.findByUserEmail(email)
                .orElseThrow(() -> {
                    log.warn("JobSeeker not found for email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "JobSeeker not found");
                });
        return ResponseEntity.ok(mapToResponseDTO(seeker));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching JobSeeker with id: {} for email: {}", id, email);
        JobSeeker seeker = jobSeekerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("JobSeeker not found with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "JobSeeker not found");
                });

        if (!seeker.getUser().getEmail().equals(email)) {
            log.warn("Access denied to JobSeeker id: {} for email: {}", id, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"error\": \"Access denied\"}");
        }

        return ResponseEntity.ok(mapToResponseDTO(seeker));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobSeekerResponseDTO> updateJobSeeker(@PathVariable Long id, @Valid @RequestBody JobSeekerRegisterDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Updating JobSeeker with id: {} for email: {}", id, email);
        JobSeeker seeker = jobSeekerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("JobSeeker not found with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "JobSeeker not found");
                });

        if (!seeker.getUser().getEmail().equals(email)) {
            log.warn("Access denied to update JobSeeker id: {} for email: {}", id, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        if (dto.getFullName() != null) seeker.setFullName(dto.getFullName());
        if (dto.getPhone() != null) seeker.setPhone(dto.getPhone());
        if (dto.getAddress() != null) seeker.setAddress(dto.getAddress());
        if (dto.getSkills() != null) seeker.setSkills(dto.getSkills());
        if (dto.getExperience() != null) seeker.setExperience(dto.getExperience());

        jobSeekerRepository.save(seeker);
        log.info("JobSeeker updated successfully: {}", id);
        return ResponseEntity.ok(mapToResponseDTO(seeker));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobSeeker(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Deleting JobSeeker with id: {} for email: {}", id, email);
        JobSeeker seeker = jobSeekerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("JobSeeker not found with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "JobSeeker not found");
                });

        if (!seeker.getUser().getEmail().equals(email)) {
            log.warn("Access denied to delete JobSeeker id: {} for email: {}", id, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        jobSeekerRepository.delete(seeker);
        log.info("JobSeeker deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    private JobSeekerResponseDTO mapToResponseDTO(JobSeeker seeker) {
        JobSeekerResponseDTO dto = new JobSeekerResponseDTO();
        dto.setId(seeker.getId());
        dto.setFullName(seeker.getFullName());
        dto.setPhone(seeker.getPhone());
        dto.setAddress(seeker.getAddress());
        dto.setSkills(seeker.getSkills());
        dto.setExperience(seeker.getExperience());
        dto.setUserEmail(seeker.getUser().getEmail());
        return dto;
    }
}
