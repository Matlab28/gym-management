package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.dto.response.TrainingResponseDTO;
import com.epam.gymmanagement.dto.response.UserProfileResponseDTO;
import com.epam.gymmanagement.dto.response.UserSummaryResponseDTO;
import com.epam.gymmanagement.entity.TraineeEntity;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.TrainingEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymMapperTest {
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private GymMapper gymMapper;

    @Test
    void toTraineeProfileResponseMapsAssignedTrainersOntoMappedProfile() {
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.YOGA);
        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true, trainer);
        UserSummaryResponseDTO mappedTrainer = new UserSummaryResponseDTO();
        UserProfileResponseDTO mappedProfile = new UserProfileResponseDTO();

        when(modelMapper.map(trainer, UserSummaryResponseDTO.class)).thenReturn(mappedTrainer);
        when(modelMapper.map(trainee, UserProfileResponseDTO.class)).thenReturn(mappedProfile);

        UserProfileResponseDTO response = gymMapper.toTraineeProfileResponse(trainee);

        assertSame(mappedProfile, response);
        assertEquals(List.of(mappedTrainer), response.getTrainers());
    }

    @Test
    void toTrainerProfileResponseMapsAssignedTraineesOntoMappedProfile() {
        TraineeEntity trainee = ServiceTestFixtures.trainee("trainee.user", true);
        TrainerEntity trainer = ServiceTestFixtures.trainer("trainer.user", true, TrainingType.FITNESS);
        trainer.setTrainees(List.of(trainee));
        UserSummaryResponseDTO mappedTrainee = new UserSummaryResponseDTO();
        UserProfileResponseDTO mappedProfile = new UserProfileResponseDTO();

        when(modelMapper.map(trainee, UserSummaryResponseDTO.class)).thenReturn(mappedTrainee);
        when(modelMapper.map(trainer, UserProfileResponseDTO.class)).thenReturn(mappedProfile);

        UserProfileResponseDTO response = gymMapper.toTrainerProfileResponse(trainer);

        assertSame(mappedProfile, response);
        assertEquals(List.of(mappedTrainee), response.getTrainees());
    }

    @Test
    void toTrainingResponseDelegatesToModelMapper() {
        TrainingEntity training = ServiceTestFixtures.training(
                "Yoga Basics",
                ServiceTestFixtures.trainee("trainee.user", true),
                ServiceTestFixtures.trainer("trainer.user", true, TrainingType.YOGA),
                TrainingType.YOGA
        );
        TrainingResponseDTO mapped = new TrainingResponseDTO();

        when(modelMapper.map(training, TrainingResponseDTO.class)).thenReturn(mapped);

        TrainingResponseDTO response = gymMapper.toTrainingResponse(training);

        assertSame(mapped, response);
    }
}
