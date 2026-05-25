package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.TrainerRegistrationRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTrainerProfileRequestDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.RegistrationResponseDTO;
import com.epam.gymmanagement.dto.response.TrainerProfileResponseDTO;
import com.epam.gymmanagement.dto.response.TrainingTypeResponseDTO;
import com.epam.gymmanagement.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trainers")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
        name = "Trainer Controller",
        description = "Endpoints for managing trainer profiles, including registration, profile updates, and activation/deactivation"
)
public class TrainerController {
    private final TrainerService trainerService;

    @PostMapping("/register")
    @Operation(summary = "Register a new trainer")
    public ResponseEntity<RegistrationResponseDTO> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequestDTO request
    ) {
        RegistrationResponseDTO response = trainerService.registerTrainer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("profile/{username}")
    @Operation(summary = "Get a trainer profile by username")
    public ResponseEntity<TrainerProfileResponseDTO> getTrainerProfile(
            @PathVariable String username
    ) {
        TrainerProfileResponseDTO response = trainerService.getTrainerProfile(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/training-types")
    @Operation(summary = "Get all available training types")
    public ResponseEntity<List<TrainingTypeResponseDTO>> getTrainingTypes() {
        return ResponseEntity.ok(trainerService.getTrainingTypes());
    }

    @PutMapping("update/{username}")
    @Operation(summary = "Update a trainer profile by username")
    public ResponseEntity<TrainerProfileResponseDTO> updateTrainerProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateTrainerProfileRequestDTO request
    ) {
        TrainerProfileResponseDTO response = trainerService.updateTrainerProfile(username, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/activate")
    @Operation(summary = "Activate a trainer profile by username")
    public ResponseEntity<MessageResponseDTO> activateTrainerProfile(
            @PathVariable String username
    ) {
        MessageResponseDTO response = trainerService.activateTrainerProfile(username);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/deactivate")
    @Operation(summary = "Deactivate a trainer profile by username")
    public ResponseEntity<MessageResponseDTO> deactivateTrainerProfile(
            @PathVariable String username
    ) {
        MessageResponseDTO response = trainerService.deactivateTrainerProfile(username);
        return ResponseEntity.ok(response);
    }
}
