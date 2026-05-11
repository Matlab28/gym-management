package com.epam.gymmanagement.repository;

import com.epam.gymmanagement.entity.TraineeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<TraineeEntity, Long> {
    Optional<TraineeEntity> findByUserEntity_Username(String username);

    boolean existsByUserEntity_Username(String username);
}
