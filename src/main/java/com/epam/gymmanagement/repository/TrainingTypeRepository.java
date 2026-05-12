package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.entity.TrainingTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingTypeEntity, UUID> {
    Optional<TrainingTypeEntity> findByTrainingTypeName(TrainingType trainingTypeName);

    Optional<TrainingTypeEntity> findByTrainingTypeNameContainingIgnoreCase(TrainingType trainingTypeName);

    boolean existsByTrainingTypeName(TrainingType trainingTypeName);
}
