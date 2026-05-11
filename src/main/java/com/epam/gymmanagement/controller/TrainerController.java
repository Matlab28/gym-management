package com.epam.gymmanagement.controller;

import com.epam.gymmanagement.dto.request.TrainerRegistrationRequestDTO;
import com.epam.gymmanagement.dto.request.update.UpdateTrainerProfileRequestDTO;
import com.epam.gymmanagement.dto.response.RegistrationResponseDTO;
import com.epam.gymmanagement.dto.response.TrainerProfileResponseDTO;
import com.epam.gymmanagement.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {
    private final TrainerService trainerService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDTO> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequestDTO request
    ) {
        RegistrationResponseDTO response = trainerService.registerTrainer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDTO> getTrainerProfile(
            @PathVariable String username
    ) {
        TrainerProfileResponseDTO response = trainerService.getTrainerProfile(username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDTO> updateTrainerProfile(
            @PathVariable String username,
            @Valid @RequestBody UpdateTrainerProfileRequestDTO request
    ) {
        TrainerProfileResponseDTO response = trainerService.updateTrainerProfile(username, request);
        return ResponseEntity.ok(response);
    }
}
