package com.haotng.jobplatform.controller;

import com.haotng.jobplatform.dto.AuthRequest;
import com.haotng.jobplatform.dto.AuthResponse;
import com.haotng.jobplatform.entity.User;
import com.haotng.jobplatform.respository.UserRepository;
import com.haotng.jobplatform.security.jwt.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JWTUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            logger.debug("Attempting login for email: {}", authRequest.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userRepository.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> {
                        logger.error("User not found after authentication: {}", authRequest.getEmail());
                        return new RuntimeException("User not found");
                    });
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
            AuthResponse response = new AuthResponse(
                    token,
                    new AuthResponse.UserDTO(user.getEmail(), user.getRole().name())
            );
            logger.info("Login successful for email: {}", authRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            logger.error("Login failed for email: {}. Error: {}", authRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        logger.info("User logged out");
        return ResponseEntity.ok("Logged out successfully");
    }
}