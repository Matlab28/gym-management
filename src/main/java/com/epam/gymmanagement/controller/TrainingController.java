package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.dto.request.AddTrainingRequestDTO;
import com.epam.gymmanagement.dto.request.TraineeTraineesRequestDTO;
import com.epam.gymmanagement.dto.request.TrainerTrainingsRequestDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.TrainingResponseDTO;
import com.epam.gymmanagement.service.TrainingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class TrainingController {
    private final TrainingService trainingService;

    @PostMapping
    public ResponseEntity<MessageResponseDTO> addTraining(
            @Valid @RequestBody AddTrainingRequestDTO request
    ) {
        MessageResponseDTO response = trainingService.addTraining(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trainee")
    public ResponseEntity<List<TrainingResponseDTO>> getTraineeTrainings(
            @Valid @RequestBody TraineeTraineesRequestDTO dto
    ) {
        return ResponseEntity.ok(trainingService.getTraineeTrainings(dto));
    }

    @GetMapping("/trainee/{username}")
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
        TraineeTraineesRequestDTO dto = new TraineeTraineesRequestDTO();
        dto.setUsername(username);
        dto.setPeriodFrom(periodFrom);
        dto.setPeriodTo(periodTo);
        dto.setTrainerName(trainerName);
        dto.setTrainingType(parseTrainingType(trainingType));

        return ResponseEntity.ok(trainingService.getTraineeTrainings(dto));
    }

    @GetMapping("/trainer/{username}")
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
        TrainerTrainingsRequestDTO dto = new TrainerTrainingsRequestDTO();
        dto.setUsername(username);
        dto.setPeriodFrom(periodFrom);
        dto.setPeriodTo(periodTo);
        dto.setTraineeName(traineeName);

        return ResponseEntity.ok(trainingService.getTrainerTrainings(dto));
    }

    private TrainingType parseTrainingType(String trainingType) {
        if (trainingType == null || trainingType.isBlank()) {
            return null;
        }

        return TrainingType.fromValue(trainingType);
    }
}
