package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.JobSeeker;
import com.haotng.jobplatform.entity.User;
import com.haotng.jobplatform.respository.EmployerRepository;
import com.haotng.jobplatform.respository.JobSeekerRepository;
import com.haotng.jobplatform.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        return userRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<User> getByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updated) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check trùng email nếu đang đổi email
        if (updated.getEmail() != null && !updated.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(updated.getEmail())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
            }
            existing.setEmail(updated.getEmail());
        }

        if (updated.getPassword() != null) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }

        if (updated.getRole() != null) {
            existing.setRole(updated.getRole());
        }

        userRepository.save(existing);
        return ResponseEntity.ok(existing);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User không tồn tại"));

        // Kiểm tra nếu user đang được gán vào Employer
        Employer employer = employerRepository.findByUserId(id);
        if (employer != null) {
            employerRepository.delete(employer);
        }

        // Nếu có luồng JobSeeker thì xử lý tương tự
        JobSeeker jobSeeker = jobSeekerRepository.findByUserId(id);
        if (jobSeeker != null) {
            jobSeekerRepository.delete(jobSeeker);
        }

        userRepository.delete(user);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
