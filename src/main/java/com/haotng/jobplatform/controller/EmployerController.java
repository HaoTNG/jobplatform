package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.dto.EmployerRegisterDTO;
import com.haotng.jobplatform.dto.EmployerResponseDTO;
import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.Job;
import com.haotng.jobplatform.entity.Role;
import com.haotng.jobplatform.respository.EmployerRepository;
import com.haotng.jobplatform.respository.JobRepository;
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

import java.util.List;

@RestController
@RequestMapping("/api/employers")
@RequiredArgsConstructor
@Slf4j
public class EmployerController {

    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    @PostMapping
    public ResponseEntity<EmployerResponseDTO> createEmployer(@Valid @RequestBody EmployerRegisterDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Creating Employer profile for email: {}", email);

        // Kiểm tra xem user đã có profile Employer chưa
        if (employerRepository.findByUserEmail(email).isPresent()) {
            log.warn("Employer profile already exists for email: {}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // Lấy user hiện tại
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Kiểm tra role
        if (user.getRole() != Role.EMPLOYER) {
            log.warn("User {} does not have EMPLOYER role", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Employer employer = Employer.builder()
                .companyName(dto.getCompanyName())
                .industry(dto.getIndustry())
                .website(dto.getWebsite())
                .address(dto.getAddress())
                .user(user)
                .build();

        employerRepository.save(employer);
        log.info("Employer profile created for email: {}", email);

        EmployerResponseDTO response = mapToResponseDTO(employer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EmployerResponseDTO>> getAll(Pageable pageable) {
        // Chỉ ADMIN được truy cập danh sách
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != Role.ADMIN) {
            log.warn("Access denied to getAllEmployers for email: {}", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Page<EmployerResponseDTO> page = employerRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching Employer with id: {} for email: {}", id, email);
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Employer not found with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found");
                });

        if (!employer.getUser().getEmail().equals(email)) {
            log.warn("Access denied to Employer id: {} for email: {}", id, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"error\": \"Access denied\"}");
        }

        return ResponseEntity.ok(mapToResponseDTO(employer));
    }

    @GetMapping("/me")
    public ResponseEntity<EmployerResponseDTO> getMe() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching Employer profile for email: {}", email);
        Employer employer = employerRepository.findByUserEmail(email)
                .orElseThrow(() -> {
                    log.warn("Employer not found for email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found");
                });
        return ResponseEntity.ok(mapToResponseDTO(employer));
    }

    @GetMapping("/{id}/jobs")
    public ResponseEntity<List<Job>> getJobsByEmployerId(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching jobs for Employer id: {} by email: {}", id, email);
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Employer not found with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found");
                });

        if (!employer.getUser().getEmail().equals(email) &&
                userRepository.findByEmail(email)
                        .map(user -> user.getRole() != Role.ADMIN)
                        .orElse(true)) {
            log.warn("Access denied to jobs for Employer id: {} by email: {}", id, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<Job> jobs = jobRepository.findByEmployerId(id);
        return ResponseEntity.ok(jobs);
    }

    @PutMapping("/me")
    public ResponseEntity<EmployerResponseDTO> updateEmployer(@Valid @RequestBody EmployerRegisterDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Updating Employer profile for email: {}", email);

        Employer employer = employerRepository.findByUserEmail(email)
                .orElseThrow(() -> {
                    log.warn("Employer not found for email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found");
                });

        if (dto.getCompanyName() != null) employer.setCompanyName(dto.getCompanyName());
        if (dto.getIndustry() != null) employer.setIndustry(dto.getIndustry());
        if (dto.getWebsite() != null) employer.setWebsite(dto.getWebsite());
        if (dto.getAddress() != null) employer.setAddress(dto.getAddress());

        employerRepository.save(employer);
        log.info("Employer updated successfully for email: {}", email);
        return ResponseEntity.ok(mapToResponseDTO(employer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployer(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Deleting Employer with id: {} for email: {}", id, email);
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Employer not found with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found");
                });

        if (!employer.getUser().getEmail().equals(email)) {
            log.warn("Access denied to delete Employer id: {} for email: {}", id, email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        employerRepository.delete(employer);
        log.info("Employer deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }

    private EmployerResponseDTO mapToResponseDTO(Employer employer) {
        EmployerResponseDTO dto = new EmployerResponseDTO();
        dto.setId(employer.getId());
        dto.setCompanyName(employer.getCompanyName());
        dto.setIndustry(employer.getIndustry());
        dto.setWebsite(employer.getWebsite());
        dto.setAddress(employer.getAddress());
        dto.setUserEmail(employer.getUser().getEmail());
        return dto;
    }
}
