package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.LoginRequestDTO;
import com.epam.gymmanagement.dto.request.update.ChangePasswordRequestDTO;
import com.epam.gymmanagement.dto.response.LoginResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.SessionResponseDTO;
import com.epam.gymmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Auth Controller",
        description = "Endpoints for user authentication and session management"
)
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and return a JWT token")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Change the password of the current user")
    public ResponseEntity<MessageResponseDTO> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request
    ) {
        MessageResponseDTO response = authService.changePassword(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get information about the current authenticated user")
    public ResponseEntity<SessionResponseDTO> currentSession() {
        SessionResponseDTO response = authService.currentSession();
        return ResponseEntity.ok(response);
    }
}
