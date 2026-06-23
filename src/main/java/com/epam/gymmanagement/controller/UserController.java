package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.ProfileRequestDTO;
import com.epam.gymmanagement.dto.response.UserProfileResponseDTO;
import com.epam.gymmanagement.dto.response.UserSummaryResponseDTO;
import com.epam.gymmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(
        name = "User Controller",
        description = "Endpoints for managing user profile operations"
)
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new user profile")
    public ResponseEntity<UserSummaryResponseDTO> createUser(
            @Valid @RequestBody ProfileRequestDTO dto
    ) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @GetMapping("/all")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserSummaryResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/profiles")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all user profiles")
    public ResponseEntity<List<UserProfileResponseDTO>> getAllUserProfiles() {
        return ResponseEntity.ok(userService.getAllUserProfiles());
    }

    @GetMapping("/{userId}/profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get user profile by user ID")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(@Valid @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @GetMapping("/summary")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get user by user ID")
    public ResponseEntity<UserSummaryResponseDTO> getUserById(@Valid @RequestParam UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("update/{userId}/profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update user profile by user ID")
    public ResponseEntity<UserProfileResponseDTO> updateUserProfile(
            @Valid @PathVariable UUID userId, @RequestBody UserProfileResponseDTO dto) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, dto));
    }

    @DeleteMapping("/delete/{userId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete user by user ID")
    public ResponseEntity<String> deleteUser(@Valid @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }
}
