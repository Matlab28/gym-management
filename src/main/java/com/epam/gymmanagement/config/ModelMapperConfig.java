package com.epam.gymmanagement.config;

import com.epam.gymmanagement.dto.response.TraineeProfileResponseDTO;
import com.epam.gymmanagement.dto.response.TraineeShortResponseDTO;
import com.epam.gymmanagement.dto.response.TrainerProfileResponseDTO;
import com.epam.gymmanagement.dto.response.TrainerShortResponseDTO;
import com.epam.gymmanagement.dto.response.TrainingResponseDTO;
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

        modelMapper.createTypeMap(TrainerEntity.class, TrainerShortResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(TrainerEntity::getId, TrainerShortResponseDTO::setTrainerId);
                    mapper.map(source -> source.getUser().getUsername(), TrainerShortResponseDTO::setTrainerUsername);
                    mapper.map(source -> source.getUser().getFirstName(), TrainerShortResponseDTO::setTrainerFirstName);
                    mapper.map(source -> source.getUser().getLastName(), TrainerShortResponseDTO::setTrainerLastName);
                    mapper.using(trainingTypeName)
                            .map(TrainerEntity::getSpecialization, TrainerShortResponseDTO::setTrainerSpecialization);
                });

        modelMapper.createTypeMap(TrainerEntity.class, TrainerProfileResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(TrainerEntity::getId, TrainerProfileResponseDTO::setTrainerId);
                    mapper.map(source -> source.getUser().getUsername(), TrainerProfileResponseDTO::setUsername);
                    mapper.map(source -> source.getUser().getFirstName(), TrainerProfileResponseDTO::setFirstName);
                    mapper.map(source -> source.getUser().getLastName(), TrainerProfileResponseDTO::setLastName);
                    mapper.map(source -> source.getUser().getIsActive(), TrainerProfileResponseDTO::setIsActive);
                    mapper.using(trainingTypeName)
                            .map(TrainerEntity::getSpecialization, TrainerProfileResponseDTO::setSpecialization);
                    mapper.skip(TrainerProfileResponseDTO::setTrainees);
                });
    }

    private void configureTraineeMappings(ModelMapper modelMapper) {
        modelMapper.createTypeMap(TraineeEntity.class, TraineeShortResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(TraineeEntity::getId, TraineeShortResponseDTO::setTraineeId);
                    mapper.map(source -> source.getUserEntity().getUsername(), TraineeShortResponseDTO::setTraineeUsername);
                    mapper.map(source -> source.getUserEntity().getFirstName(), TraineeShortResponseDTO::setTraineeFirstName);
                    mapper.map(source -> source.getUserEntity().getLastName(), TraineeShortResponseDTO::setTraineeLastName);
                });

        modelMapper.createTypeMap(TraineeEntity.class, TraineeProfileResponseDTO.class)
                .addMappings(mapper -> {
                    mapper.map(TraineeEntity::getId, TraineeProfileResponseDTO::setTraineeId);
                    mapper.map(source -> source.getUserEntity().getUsername(), TraineeProfileResponseDTO::setUsername);
                    mapper.map(source -> source.getUserEntity().getFirstName(), TraineeProfileResponseDTO::setFirstName);
                    mapper.map(source -> source.getUserEntity().getLastName(), TraineeProfileResponseDTO::setLastName);
                    mapper.map(source -> source.getUserEntity().getIsActive(), TraineeProfileResponseDTO::setIsActive);
                    mapper.skip(TraineeProfileResponseDTO::setTrainers);
                });
    }

    private void configureTrainingMappings(ModelMapper modelMapper) {
        Converter<TrainingTypeEntity, String> trainingTypeName = context -> {
            TrainingTypeEntity trainingType = context.getSource();
            return trainingType == null ? null : trainingType.getTrainingTypeName().getValue();
        };

        Converter<TrainerEntity, String> trainerFullName = context -> {
            TrainerEntity trainer = context.getSource();
            if (trainer == null || trainer.getUser() == null) {
                return null;
            }
            return trainer.getUser().getFirstName() + " " + trainer.getUser().getLastName();
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
