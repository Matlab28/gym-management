package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.entity.TrainingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<TrainingEntity, Long> {
    List<TrainingEntity> findByTrainee_UserEntity_Username(String username);

    List<TrainingEntity> findByTrainerUserUsername(String username);

    @Query("""
            SELECT t FROM TrainingEntity t
            WHERE t.trainee.userEntity.username = :username
            AND (:periodFrom IS NULL OR t.trainingDate >= :periodFrom)
            AND (:periodTo IS NULL OR t.trainingDate <= :periodTo)
            AND (:trainerName IS NULL OR
                 LOWER(CONCAT(t.trainer.user.firstName, ' ', t.trainer.user.lastName))
                 LIKE LOWER(CONCAT('%', :trainerName, '%')))
            AND (:trainingType IS NULL OR
                 LOWER(t.trainingType.trainingTypeName) = LOWER(:trainingType))
            ORDER BY t.trainingDate DESC
            """)
    List<TrainingEntity> findTraineeTrainings(
            @Param("username") String username,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo,
            @Param("trainerName") String trainerName,
            @Param("trainingType") String trainingType
    );

    @Query("""
            SELECT t FROM TrainingEntity t
            WHERE t.trainer.user.username = :username
            AND (:periodFrom IS NULL OR t.trainingDate >= :periodFrom)
            AND (:periodTo IS NULL OR t.trainingDate <= :periodTo)
            AND (:traineeName IS NULL OR
                 LOWER(CONCAT(t.trainee.userEntity.firstName, ' ', t.trainee.userEntity.lastName))
                 LIKE LOWER(CONCAT('%', :traineeName, '%')))
            ORDER BY t.trainingDate DESC
            """)
    List<TrainingEntity> findTrainerTrainings(
            @Param("username") String username,
            @Param("periodFrom") LocalDate periodFrom,
            @Param("periodTo") LocalDate periodTo,
            @Param("traineeName") String traineeName
    );
}
