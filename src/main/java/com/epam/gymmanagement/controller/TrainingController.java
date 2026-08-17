package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.dto.request.AddTrainingRequestDTO;
import com.epam.gymmanagement.dto.request.TrainingSearchRequestDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.TrainingResponseDTO;
import com.epam.gymmanagement.dto.response.TrainingTypeResponseDTO;
import com.epam.gymmanagement.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
        name = "Gym Management Training Controller",
        description = "Endpoints for managing training sessions, including adding new trainings and retrieving trainings for trainees and trainers"
)
public class TrainingController {
    private final TrainingService trainingService;

    @PostMapping({"", "/add"})
    @Operation(summary = "Add a new training session")
    public ResponseEntity<MessageResponseDTO> addTraining(
            @Valid @RequestBody AddTrainingRequestDTO request
    ) {
        MessageResponseDTO response = trainingService.addTraining(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{trainingId}")
    @Operation(summary = "Cancel a planned training session")
    public ResponseEntity<MessageResponseDTO> deleteTraining(@PathVariable UUID trainingId) {
        return ResponseEntity.ok(trainingService.deleteTraining(trainingId));
    }

    @GetMapping("/trainee/{username}")
    @Operation(summary = "Get training sessions for a trainee based on various filters")
    public ResponseEntity<List<TrainingResponseDTO>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType
    ) {
        TrainingSearchRequestDTO request = TrainingSearchRequestDTO.builder()
                .username(username)
                .periodFrom(periodFrom)
                .periodTo(periodTo)
                .trainerName(trainerName)
                .trainingType(parseTrainingType(trainingType))
                .build();

        return ResponseEntity.ok(trainingService.getTraineeTrainings(request));
    }

    @GetMapping("/trainer/{username}")
    @Operation(summary = "Get training sessions for a trainer based on various filters")
    public ResponseEntity<List<TrainingResponseDTO>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodTo,
            @RequestParam(required = false) String traineeName
    ) {
        TrainingSearchRequestDTO request = TrainingSearchRequestDTO.builder()
                .username(username)
                .periodFrom(periodFrom)
                .periodTo(periodTo)
                .traineeName(traineeName)
                .build();

        return ResponseEntity.ok(trainingService.getTrainerTrainings(request));
    }

    @GetMapping("/types")
    @Operation(summary = "Get training type information based on the provided training type name")
    public ResponseEntity<TrainingTypeResponseDTO> getTrainingType(
            @RequestParam String trainingType
    ) {
        return ResponseEntity.ok(trainingService.getTrainingType(trainingType));
    }

    private TrainingType parseTrainingType(String trainingType) {
        if (trainingType == null || trainingType.isBlank()) {
            return null;
        }

        return TrainingType.fromValue(trainingType);
    }
}
