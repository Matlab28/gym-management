package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.entity.TrainingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingEntity, UUID> {
    @Query("""
            SELECT t FROM TrainingEntity t
            WHERE t.trainee.userEntity.username = :username
            AND (:periodFrom IS NULL OR t.trainingDate >= :periodFrom)
            AND (:periodTo IS NULL OR t.trainingDate <= :periodTo)
            AND (:trainerName IS NULL OR
                 LOWER(CONCAT(t.trainer.userEntity.firstName, ' ', t.trainer.userEntity.lastName))
                 LIKE LOWER(CONCAT('%', CAST(:trainerName AS string), '%')))
            AND (:trainingType IS NULL OR t.trainingType.trainingTypeName = :trainingType)
            ORDER BY t.trainingDate DESC
            """)
    List<TrainingEntity> findTraineeTrainings(
            @Param("username") String username,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo,
            @Param("trainerName") String trainerName,
            @Param("trainingType") TrainingType trainingType
    );

    @Query("""
            SELECT t FROM TrainingEntity t
            WHERE t.trainer.userEntity.username = :username
            AND (:periodFrom IS NULL OR t.trainingDate >= :periodFrom)
            AND (:periodTo IS NULL OR t.trainingDate <= :periodTo)
            AND (:traineeName IS NULL OR
                 LOWER(CONCAT(t.trainee.userEntity.firstName, ' ', t.trainee.userEntity.lastName))
                 LIKE LOWER(CONCAT('%', CAST(:traineeName AS string), '%')))
            ORDER BY t.trainingDate DESC
            """)
    List<TrainingEntity> findTrainerTrainings(
            @Param("username") String username,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo,
            @Param("traineeName") String traineeName
    );
}
