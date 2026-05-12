package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.AdminRegistrationRequestDTO;
import com.epam.gymmanagement.dto.response.AdminDashboardResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.service.AdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDTO> registerAdmin(
            @Valid @RequestBody AdminRegistrationRequestDTO request
    ) {
        MessageResponseDTO response = adminService.registerAdmin(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponseDTO> dashboard() {
        AdminDashboardResponseDTO response = adminService.dashboard();
        return ResponseEntity.ok(response);
    }
}
