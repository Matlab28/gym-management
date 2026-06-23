package com.epam.gymmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {
    private String username;
    private String password;
    private String message;
    private String token;
    private String tokenType;
    private String role;

    private AuthResponseDTO(String username,
                            String password,
                            String message,
                            String token,
                            String tokenType,
                            String role) {
        this.username = username;
        this.password = password;
        this.message = message;
        this.token = token;
        this.tokenType = tokenType;
        this.role = role;
    }

    public static AuthResponseDTO registration(String username, String password) {
        return new AuthResponseDTO(username, password, null, null, null, null);
    }

    public static AuthResponseDTO login(String message, String token, String role) {
        return new AuthResponseDTO(null, null, message, token, "Bearer", role);
    }

    public static AuthResponseDTO session(String username, String role) {
        return new AuthResponseDTO(username, null, null, null, null, role);
    }

    public static AuthResponseDTO register(String message, String token) {
        return new AuthResponseDTO(null, null, message, token, token == null ? null : "Bearer", null);
    }
}
