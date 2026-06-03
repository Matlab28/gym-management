package com.epam.gymmanagement.health;

import com.epam.gymmanagement.repository.TrainerRepository;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TrainerHealthIndicator implements HealthIndicator {

    private final TrainerRepository trainerRepository;

    public TrainerHealthIndicator(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    public Health health() {
        try {
            long trainerCount = trainerRepository.count();

            return Health.up()
                    .withDetail("trainerService", "Available")
                    .withDetail("trainerCount", trainerCount)
                    .build();

        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("trainerService", "Unavailable")
                    .withDetail("message", "Trainer repository is not available")
                    .build();
        }
    }
}
