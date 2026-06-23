package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.TraineeRegistrationRequestDTO;
import com.epam.gymmanagement.dto.request.UpdateTraineeProfileRequestDTO;
import com.epam.gymmanagement.dto.request.UpdateTraineeTrainersRequestDTO;
import com.epam.gymmanagement.dto.response.AuthResponseDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.UserProfileResponseDTO;
import com.epam.gymmanagement.dto.response.UserSummaryResponseDTO;
import com.epam.gymmanagement.service.TraineeService;
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
@RequestMapping("/api/v1/trainees")
@CrossOrigin("https://gym-management-front.vercel.app")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
        name = "Trainee Controller",
        description = "Endpoints for managing trainee profiles and their assigned trainers"
)
public class TraineeController {
    private final TraineeService traineeService;

    @PostMapping("/register")
    @Operation(summary = "Register a new trainee")
    public ResponseEntity<AuthResponseDTO> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequestDTO request
    ) {
        AuthResponseDTO response = traineeService.registerTrainee(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("profile/{username}")
    @Operation(summary = "Get a trainee profile by username")
    public ResponseEntity<UserProfileResponseDTO> getTraineeProfile(
            @PathVariable String username
    ) {
        UserProfileResponseDTO response = traineeService.getTraineeProfile(username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("update/{username}")
    @Operation(summary = "Update a trainee profile by username")
    public ResponseEntity<UserProfileResponseDTO> updateTraineeProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeProfileRequestDTO request
    ) {
        UserProfileResponseDTO response = traineeService.updateTraineeProfile(username, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("delete/{username}")
    @Operation(summary = "Delete a trainee profile by username")
    public ResponseEntity<MessageResponseDTO> deleteTraineeProfile(
            @PathVariable String username
    ) {
        MessageResponseDTO response = traineeService.deleteTraineeProfile(username);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/activate")
    @Operation(summary = "Activate a trainee profile by username")
    public ResponseEntity<MessageResponseDTO> activateTraineeProfile(
            @PathVariable String username
    ) {
        MessageResponseDTO response = traineeService.activateTraineeProfile(username);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/deactivate")
    @Operation(summary = "Deactivate a trainee profile by username")
    public ResponseEntity<MessageResponseDTO> deactivateTraineeProfile(
            @PathVariable String username
    ) {
        MessageResponseDTO response = traineeService.deactivateTraineeProfile(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/not-assigned-trainers")
    @Operation(summary = "Get a list of active trainers not assigned to the trainee")
    public ResponseEntity<List<UserSummaryResponseDTO>> getNotAssignedActiveTrainers(
            @PathVariable String username
    ) {
        List<UserSummaryResponseDTO> response =
                traineeService.getNotAssignedActiveTrainers(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("update/{username}/trainers")
    @Operation(summary = "Update the list of trainers assigned to a trainee")
    public ResponseEntity<List<UserSummaryResponseDTO>> updateTraineeTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequestDTO request
    ) {
        List<UserSummaryResponseDTO> response =
                traineeService.updateTraineeTrainers(username, request);

        return ResponseEntity.ok(response);
    }
}
