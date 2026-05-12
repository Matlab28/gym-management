package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.LoginRequestDTO;
import com.epam.gymmanagement.dto.request.update.ChangePasswordRequestDTO;
import com.epam.gymmanagement.dto.response.LoginResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.SessionResponseDTO;
import com.epam.gymmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<MessageResponseDTO> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request
    ) {
        MessageResponseDTO response = authService.changePassword(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SessionResponseDTO> currentSession() {
        SessionResponseDTO response = authService.currentSession();
        return ResponseEntity.ok(response);
    }
}
