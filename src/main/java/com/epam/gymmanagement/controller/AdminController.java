package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.AdminRegistrationRequestDTO;
import com.epam.gymmanagement.dto.response.AdminDashboardResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
        name = "Admin Controller",
        description = "Registering new admins and providing admin dashboard data"
)
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new admin")
    public ResponseEntity<MessageResponseDTO> registerAdmin(
            @Valid @RequestBody AdminRegistrationRequestDTO request
    ) {
        MessageResponseDTO response = adminService.registerAdmin(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin dashboard data")
    public ResponseEntity<AdminDashboardResponseDTO> dashboard() {
        AdminDashboardResponseDTO response = adminService.dashboard();
        return ResponseEntity.ok(response);
    }
}
