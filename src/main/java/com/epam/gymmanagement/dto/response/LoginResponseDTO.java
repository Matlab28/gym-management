package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {
    private String message;
    private String token;
    private String tokenType = "Bearer";
    private String role;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String message) {
        this.message = message;
    }

    public LoginResponseDTO(String message, String token) {
        this.message = message;
        this.token = token;
    }

    public LoginResponseDTO(String message, String token, String role) {
        this.message = message;
        this.token = token;
        this.role = role;
    }
}
