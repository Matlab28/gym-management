package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.entity.TrainingTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingTypeEntity, Long> {
    Optional<TrainingTypeEntity> findByTrainingTypeNameIgnoreCase(TrainingType trainingTypeName);

    boolean existsByTrainingTypeNameIgnoreCase(TrainingType trainingTypeName);
}
