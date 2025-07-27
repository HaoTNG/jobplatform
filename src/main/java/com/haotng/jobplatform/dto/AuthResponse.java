package com.haotng.jobplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDTO user;

    @Data
    @AllArgsConstructor
    public static class UserDTO {
        private String email;
        private String role;
    }
}