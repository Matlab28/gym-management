package com.epam.gymmanagement.security;

import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.repository.TraineeRepository;
import com.epam.gymmanagement.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRoleResolver {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public UserRole resolve(UserEntity user) {
        if (user.getRole() != null) {
            return user.getRole();
        }

        if (trainerRepository.existsByUserUsername(user.getUsername())) {
            return UserRole.TRAINER;
        }

        if (traineeRepository.existsByUserEntity_Username(user.getUsername())) {
            return UserRole.TRAINEE;
        }

        return UserRole.TRAINEE;
    }
}
