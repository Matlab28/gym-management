package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.ChangePasswordRequestDTO;
import com.epam.gymmanagement.dto.request.ConfirmRequestDto;
import com.epam.gymmanagement.dto.request.LoginRequestDTO;
import com.epam.gymmanagement.dto.request.RegisterRequestDTO;
import com.epam.gymmanagement.dto.request.ResendConfirmationRequestDTO;
import com.epam.gymmanagement.dto.response.AuthResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
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
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user and send an email confirmation code")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
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
    public ResponseEntity<AuthResponseDTO> currentSession() {
        AuthResponseDTO response = authService.currentSession();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirmation")
    @Operation(summary = "Confirm email with a code and return a JWT token")
    public ResponseEntity<AuthResponseDTO> confirm(@Valid @RequestBody ConfirmRequestDto dto) {
        return ResponseEntity.ok(authService.confirm(dto));
    }

    @PostMapping("/confirmation/resend")
    @Operation(summary = "Resend an email confirmation code")
    public ResponseEntity<AuthResponseDTO> resendConfirmationCode(
            @Valid @RequestBody ResendConfirmationRequestDTO request
    ) {
        return ResponseEntity.ok(authService.resendConfirmationCode(request));
    }
}
