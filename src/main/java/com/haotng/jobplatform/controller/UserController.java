package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.dto.UserDTO;
import com.haotng.jobplatform.entity.User;
import com.haotng.jobplatform.entity.Employer;
import com.haotng.jobplatform.entity.JobSeeker;
import com.haotng.jobplatform.entity.Role;
import com.haotng.jobplatform.respository.UserRepository;
import com.haotng.jobplatform.respository.EmployerRepository;
import com.haotng.jobplatform.respository.JobSeekerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDTO dto = new UserDTO();
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setFullName(user.getFullName());
        if (user.getRole().name().equals("EMPLOYER") && user.getEmployer() != null) {
            dto.setCompanyName(user.getEmployer().getCompanyName());
            dto.setAddress(user.getEmployer().getAddress());
            dto.setIndustry(user.getEmployer().getIndustry());
            dto.setWebsite(user.getEmployer().getWebsite());
        } else if (user.getRole().name().equals("JOB_SEEKER") && user.getJobSeeker() != null) {
            dto.setFullName(user.getJobSeeker().getFullName());
            dto.setPhone(user.getJobSeeker().getPhone());
            dto.setAddress(user.getJobSeeker().getAddress());
            dto.setSkills(user.getJobSeeker().getSkills());
            dto.setExperience(user.getJobSeeker().getExperience());
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@RequestBody UserDTO userDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update User fullName
        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
        }

        // Update Employer or JobSeeker
        if (user.getRole().name().equals("EMPLOYER") && user.getEmployer() != null) {
            if (userDTO.getCompanyName() != null) {
                user.getEmployer().setCompanyName(userDTO.getCompanyName());
            }
            if (userDTO.getAddress() != null) {
                user.getEmployer().setAddress(userDTO.getAddress());
            }
            if (userDTO.getIndustry() != null) {
                user.getEmployer().setIndustry(userDTO.getIndustry());
            }
            if (userDTO.getWebsite() != null) {
                user.getEmployer().setWebsite(userDTO.getWebsite());
            }
        } else if (user.getRole().name().equals("JOB_SEEKER") && user.getJobSeeker() != null) {
            if (userDTO.getFullName() != null) {
                user.getJobSeeker().setFullName(userDTO.getFullName()); // Đồng bộ fullName
            }
            if (userDTO.getPhone() != null) {
                user.getJobSeeker().setPhone(userDTO.getPhone());
            }
            if (userDTO.getAddress() != null) {
                user.getJobSeeker().setAddress(userDTO.getAddress());
            }
            if (userDTO.getSkills() != null) {
                user.getJobSeeker().setSkills(userDTO.getSkills());
            }
            if (userDTO.getExperience() != null) {
                user.getJobSeeker().setExperience(userDTO.getExperience());
            }
        }

        userRepository.save(user);

        // Return updated DTO
        UserDTO responseDTO = new UserDTO();
        responseDTO.setEmail(user.getEmail());
        responseDTO.setRole(user.getRole().name());
        responseDTO.setFullName(user.getFullName());
        if (user.getRole().name().equals("EMPLOYER") && user.getEmployer() != null) {
            responseDTO.setCompanyName(user.getEmployer().getCompanyName());
            responseDTO.setAddress(user.getEmployer().getAddress());
            responseDTO.setIndustry(user.getEmployer().getIndustry());
            responseDTO.setWebsite(user.getEmployer().getWebsite());
        } else if (user.getRole().name().equals("JOB_SEEKER") && user.getJobSeeker() != null) {
            responseDTO.setFullName(user.getJobSeeker().getFullName());
            responseDTO.setPhone(user.getJobSeeker().getPhone());
            responseDTO.setAddress(user.getJobSeeker().getAddress());
            responseDTO.setSkills(user.getJobSeeker().getSkills());
            responseDTO.setExperience(user.getJobSeeker().getExperience());
        }
        return ResponseEntity.ok(responseDTO);
    }
}
