package com.epam.gymmanagement.config;

import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.dto.response.TrainingResponseDTO;
import com.epam.gymmanagement.dto.response.UserProfileResponseDTO;
import com.epam.gymmanagement.dto.response.UserSummaryResponseDTO;
import com.epam.gymmanagement.entity.TraineeEntity;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.TrainingEntity;
import com.epam.gymmanagement.entity.TrainingTypeEntity;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);

        configureTrainerMappings(modelMapper);
        configureTraineeMappings(modelMapper);
        configureTrainingMappings(modelMapper);

        return modelMapper;
    }

    private void configureTrainerMappings(ModelMapper modelMapper) {
        Converter<TrainingTypeEntity, String> trainingTypeName = context -> {
            TrainingTypeEntity trainingType = context.getSource();
            return trainingType == null ? null : trainingType.getTrainingTypeName().getValue();
        };

        modelMapper.createTypeMap(TrainerEntity.class, UserSummaryResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(TrainerEntity::getId, UserSummaryResponseDTO::setId);
                    mapper.map(source -> source.getUserEntity().getUsername(), UserSummaryResponseDTO::setUsername);
                    mapper.map(source -> source.getUserEntity().getFirstName(), UserSummaryResponseDTO::setFirstName);
                    mapper.map(source -> source.getUserEntity().getLastName(), UserSummaryResponseDTO::setLastName);
//                    mapper.map(source -> source.getUserEntity().getIsActive(), UserSummaryResponseDTO::setActive);
                    mapper.using(trainingTypeName)
                            .map(TrainerEntity::getSpecialization, UserSummaryResponseDTO::setSpecialization);
                })
                .setPostConverter(context -> {
                    context.getDestination().setRole(UserRole.TRAINER);
                    return context.getDestination();
                });

        modelMapper.createTypeMap(TrainerEntity.class, UserProfileResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(TrainerEntity::getId, UserProfileResponseDTO::setId);
                    mapper.map(source -> source.getUserEntity().getUsername(), UserProfileResponseDTO::setUsername);
                    mapper.map(source -> source.getUserEntity().getFirstName(), UserProfileResponseDTO::setFirstName);
                    mapper.map(source -> source.getUserEntity().getLastName(), UserProfileResponseDTO::setLastName);
//                    mapper.map(source -> source.getUserEntity().getIsActive(), UserProfileResponseDTO::setIsActive);
                    mapper.using(trainingTypeName)
                            .map(TrainerEntity::getSpecialization, UserProfileResponseDTO::setSpecialization);
                    mapper.skip(UserProfileResponseDTO::setTrainees);
                    mapper.skip(UserProfileResponseDTO::setTrainers);
                });
    }

    private void configureTraineeMappings(ModelMapper modelMapper) {
        modelMapper.createTypeMap(TraineeEntity.class, UserSummaryResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(TraineeEntity::getId, UserSummaryResponseDTO::setId);
                    mapper.map(source -> source.getUserEntity().getUsername(), UserSummaryResponseDTO::setUsername);
                    mapper.map(source -> source.getUserEntity().getFirstName(), UserSummaryResponseDTO::setFirstName);
                    mapper.map(source -> source.getUserEntity().getLastName(), UserSummaryResponseDTO::setLastName);
//                    mapper.map(source -> source.getUserEntity().getIsActive(), UserSummaryResponseDTO::setActive);
                })
                .setPostConverter(context -> {
                    context.getDestination().setRole(UserRole.TRAINEE);
                    return context.getDestination();
                });

        modelMapper.createTypeMap(TraineeEntity.class, UserProfileResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(TraineeEntity::getId, UserProfileResponseDTO::setId);
                    mapper.map(source -> source.getUserEntity().getUsername(), UserProfileResponseDTO::setUsername);
                    mapper.map(source -> source.getUserEntity().getFirstName(), UserProfileResponseDTO::setFirstName);
                    mapper.map(source -> source.getUserEntity().getLastName(), UserProfileResponseDTO::setLastName);
//                    mapper.map(source -> source.getUserEntity().getIsActive(), UserProfileResponseDTO::setIsActive);
                    mapper.skip(UserProfileResponseDTO::setTrainers);
                    mapper.skip(UserProfileResponseDTO::setTrainees);
                });
    }

    private void configureTrainingMappings(ModelMapper modelMapper) {
        Converter<TrainingTypeEntity, String> trainingTypeName = context -> {
            TrainingTypeEntity trainingType = context.getSource();
            return trainingType == null ? null : trainingType.getTrainingTypeName().getValue();
        };

        Converter<TrainerEntity, String> trainerFullName = context -> {
            TrainerEntity trainer = context.getSource();
            if (trainer == null || trainer.getUserEntity() == null) {
                return null;
            }
            return trainer.getUserEntity().getFirstName() + " " + trainer.getUserEntity().getLastName();
        };

        Converter<TraineeEntity, String> traineeFullName = context -> {
            TraineeEntity trainee = context.getSource();
            if (trainee == null || trainee.getUserEntity() == null) {
                return null;
            }
            return trainee.getUserEntity().getFirstName() + " " + trainee.getUserEntity().getLastName();
        };

        modelMapper.createTypeMap(TrainingEntity.class, TrainingResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(TrainingEntity::getId, TrainingResponseDTO::setTrainingId);
                    mapper.map(TrainingEntity::getTrainingName, TrainingResponseDTO::setTrainingName);
                    mapper.map(TrainingEntity::getTrainingDate, TrainingResponseDTO::setTrainingDate);
                    mapper.map(TrainingEntity::getTrainingDuration, TrainingResponseDTO::setTrainingDuration);
                    mapper.using(trainingTypeName)
                            .map(TrainingEntity::getTrainingType, TrainingResponseDTO::setTrainingType);
                    mapper.using(trainerFullName)
                            .map(TrainingEntity::getTrainer, TrainingResponseDTO::setTrainerName);
                    mapper.using(traineeFullName)
                            .map(TrainingEntity::getTrainee, TrainingResponseDTO::setTraineeName);
                });
    }
}
