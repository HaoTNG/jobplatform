package com.haotng.jobplatform.controller;


import com.haotng.jobplatform.entity.*;
import com.haotng.jobplatform.respository.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.*;
import org.springframework.web.server.ResponseStatusException;
import com.haotng.jobplatform.dto.JobSeekerRegisterDTO;
import java.util.List;

@RestController
@RequestMapping("/api/jobseekers")
@RequiredArgsConstructor
public class JobSeekerController {
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<JobSeeker> createJobSeeker(@RequestBody JobSeekerRegisterDTO dto) {
        User user;

        if (dto.getUserId() != null) {
            // ✅ Gán user từ DB nếu đã có userId
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } else {
            // ✅ Tự tạo user nếu userId chưa có
            if (dto.getEmail() == null || dto.getPassword() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lacking email or password");
            }

            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }

            user = User.builder()
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .role(Role.JOB_SEEKER)
                    .build();
            userRepository.save(user);
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
        return ResponseEntity.status(HttpStatus.CREATED).body(seeker);
    }

    @GetMapping
    public List<JobSeeker> getAllJobSeeker(){
        return jobSeekerRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobSeeker> getById(@PathVariable Long id){
        return jobSeekerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobSeeker> updateJobSeeker(@PathVariable Long id, @RequestBody JobSeekerRegisterDTO dto){
        JobSeeker existing = jobSeekerRepository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "JobSeeker not found"));

        if (dto.getFullName() != null) existing.setFullName(dto.getFullName());
        if (dto.getPhone() != null) existing.setPhone(dto.getPhone());
        if (dto.getAddress() != null) existing.setAddress(dto.getAddress());
        if (dto.getSkills() != null) existing.setSkills(dto.getSkills());
        if (dto.getExperience() != null) existing.setExperience(dto.getExperience());

        jobSeekerRepository.save(existing);
        return ResponseEntity.ok(existing);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobSeeker(@PathVariable Long id){
        JobSeeker existing = jobSeekerRepository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "JobSeeker not found"));
        jobSeekerRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }
}
