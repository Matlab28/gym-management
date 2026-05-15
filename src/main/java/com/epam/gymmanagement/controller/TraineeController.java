package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.TraineeRegistrationRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTraineeProfileRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTraineeTrainersRequestDTO;
import com.epam.gymmanagement.dto.response.*;
import com.epam.gymmanagement.service.TraineeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class TraineeController {
    private final TraineeService traineeService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDTO> registerTrainee(
            @Valid @RequestBody TraineeRegistrationRequestDTO request
    ) {
        RegistrationResponseDTO response = traineeService.registerTrainee(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponseDTO> getTraineeProfile(
            @PathVariable String username
    ) {
        TraineeProfileResponseDTO response = traineeService.getTraineeProfile(username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponseDTO> updateTraineeProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeProfileRequestDTO request
    ) {
        TraineeProfileResponseDTO response = traineeService.updateTraineeProfile(username, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<MessageResponseDTO> deleteTraineeProfile(
            @PathVariable String username
    ) {
        MessageResponseDTO response = traineeService.deleteTraineeProfile(username);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/activate")
    public ResponseEntity<MessageResponseDTO> activateTraineeProfile(
            @PathVariable String username
    ) {
        MessageResponseDTO response = traineeService.activateTraineeProfile(username);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/deactivate")
    public ResponseEntity<MessageResponseDTO> deactivateTraineeProfile(
            @PathVariable String username
    ) {
        MessageResponseDTO response = traineeService.deactivateTraineeProfile(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/not-assigned-trainers")
    public ResponseEntity<List<TrainerShortResponseDTO>> getNotAssignedActiveTrainers(
            @PathVariable String username
    ) {
        List<TrainerShortResponseDTO> response =
                traineeService.getNotAssignedActiveTrainers(username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<TrainerAssignmentResponseDTO> updateTraineeTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequestDTO request
    ) {
        TrainerAssignmentResponseDTO response =
                traineeService.updateTraineeTrainers(username, request);

        return ResponseEntity.ok(response);
    }
}
