package com.epam.gymmanagement.health;

import com.epam.gymmanagement.repository.TraineeRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TraineeHealthIndicator implements HealthIndicator {

    private final TraineeRepository traineeRepository;

    public TraineeHealthIndicator(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Override
    public Health health() {
        try {
            long traineeCount = traineeRepository.count();

            return Health.up()
                    .withDetail("traineeService", "Available")
                    .withDetail("traineeCount", traineeCount)
                    .build();

        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("traineeService", "Unavailable")
                    .withDetail("message", "Trainee repository is not available")
                    .build();
        }
    }
}