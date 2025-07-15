package com.haotng.jobplatform.controller;


import com.haotng.jobplatform.entity.*;
import com.haotng.jobplatform.respository.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import com.haotng.jobplatform.dto.EmployerRegisterDTO;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/employers")
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JobRepository jobRepository;

    @PostMapping
    public ResponseEntity<Employer> createEmployer(@RequestBody EmployerRegisterDTO dto) {
        User user;

        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } else {
            if (dto.getEmail() == null || dto.getPassword() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lacking email or password");
            }

            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }

            user = User.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .role(Role.EMPLOYER)
                    .build();
            userRepository.save(user);
        }

        Employer employer = Employer.builder()
                .companyName(dto.getCompanyName())
                .industry(dto.getIndustry())
                .website(dto.getWebsite())
                .address(dto.getAddress())
                .user(user)
                .build();


        employerRepository.save(employer);
        return ResponseEntity.status(HttpStatus.CREATED).body(employer);
    }

    @GetMapping
    public List<Employer> getAll(){
        return employerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employer> getById(@PathVariable Long id){
        return employerRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/jobs")
    public List<Job> getJobsByEmployerId(@PathVariable Long id){
        return jobRepository.findByEmployerId(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employer> updateEmployer(@PathVariable Long id, @RequestBody EmployerRegisterDTO dto) {
        Employer existing = employerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
        if (dto.getCompanyName() != null) existing.setCompanyName(dto.getCompanyName());
        if (dto.getIndustry() != null) existing.setIndustry(dto.getIndustry());
        if (dto.getWebsite() != null) existing.setWebsite(dto.getWebsite());
        if (dto.getAddress() != null) existing.setAddress(dto.getAddress());

        employerRepository.save(existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployer(@PathVariable Long id){
        Employer employer = employerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employer not found"));
        employerRepository.delete(employer);
        return ResponseEntity.noContent().build();
    }
}
