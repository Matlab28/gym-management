package com.epam.gymmanagement.service;

import com.epam.gymmanagement.dto.response.*;
import com.epam.gymmanagement.entity.TraineeEntity;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.TrainingEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GymMapper {
    private final ModelMapper modelMapper;

    public TrainerShortResponseDTO toTrainerShortResponse(TrainerEntity trainer) {
        return modelMapper.map(trainer, TrainerShortResponseDTO.class);
    }

    public TraineeShortResponseDTO toTraineeShortResponse(TraineeEntity trainee) {
        return modelMapper.map(trainee, TraineeShortResponseDTO.class);
    }

    public TraineeProfileResponseDTO toTraineeProfileResponse(TraineeEntity trainee) {
        List<TrainerShortResponseDTO> trainers = trainee.getTrainers()
                .stream()
                .map(this::toTrainerShortResponse)
                .toList();

        TraineeProfileResponseDTO response = modelMapper.map(trainee, TraineeProfileResponseDTO.class);
        response.setTrainers(trainers);
        return response;
    }

    public TrainerProfileResponseDTO toTrainerProfileResponse(TrainerEntity trainer) {
        List<TraineeShortResponseDTO> trainees = trainer.getTrainees()
                .stream()
                .map(this::toTraineeShortResponse)
                .toList();

        TrainerProfileResponseDTO response = modelMapper.map(trainer, TrainerProfileResponseDTO.class);
        response.setTrainees(trainees);
        return response;
    }

    public TrainingResponseDTO toTrainingResponse(TrainingEntity training) {
        return modelMapper.map(training, TrainingResponseDTO.class);
    }
}
