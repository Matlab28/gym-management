package com.epam.gymmanagement.service;

import com.epam.gymmanagement.dto.response.TrainingResponseDTO;
import com.epam.gymmanagement.dto.response.UserProfileResponseDTO;
import com.epam.gymmanagement.dto.response.UserSummaryResponseDTO;
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

    public UserSummaryResponseDTO toTrainerSummaryResponse(TrainerEntity trainer) {
        return modelMapper.map(trainer, UserSummaryResponseDTO.class);
    }

    public UserSummaryResponseDTO toTraineeSummaryResponse(TraineeEntity trainee) {
        return modelMapper.map(trainee, UserSummaryResponseDTO.class);
    }

    public UserProfileResponseDTO toTraineeProfileResponse(TraineeEntity trainee) {
        List<UserSummaryResponseDTO> trainers = trainee.getTrainers()
                .stream()
                .map(this::toTrainerSummaryResponse)
                .toList();

        UserProfileResponseDTO response = modelMapper.map(trainee, UserProfileResponseDTO.class);
        response.setTrainers(trainers);
        return response;
    }

    public UserProfileResponseDTO toTrainerProfileResponse(TrainerEntity trainer) {
        List<UserSummaryResponseDTO> trainees = trainer.getTrainees()
                .stream()
                .map(this::toTraineeSummaryResponse)
                .toList();

        UserProfileResponseDTO response = modelMapper.map(trainer, UserProfileResponseDTO.class);
        response.setTrainees(trainees);
        return response;
    }

    public TrainingResponseDTO toTrainingResponse(TrainingEntity training) {
        return modelMapper.map(training, TrainingResponseDTO.class);
    }
}
