package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.AddTrainingRequestDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import com.epam.gymmanagement.dto.response.TrainingResponseDTO;
import com.epam.gymmanagement.service.TrainingService;
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
public class TrainingController {
    private final TrainingService trainingService;

    @PostMapping
    public ResponseEntity<MessageResponseDTO> addTraining(
            @Valid @RequestBody AddTrainingRequestDTO request
    ) {
        MessageResponseDTO response = trainingService.addTraining(request);
        return ResponseEntity.ok(response);
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

            @RequestParam(required = false)
            String trainerName,

            @RequestParam(required = false)
            String trainingType
    ) {
        List<TrainingResponseDTO> response = trainingService.getTraineeTrainings(
                username,
                periodFrom,
                periodTo,
                trainerName,
                trainingType
        );

        return ResponseEntity.ok(response);
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

            @RequestParam(required = false)
            String traineeName
    ) {
        List<TrainingResponseDTO> response = trainingService.getTrainerTrainings(
                username,
                periodFrom,
                periodTo,
                traineeName
        );

        return ResponseEntity.ok(response);
    }
}
