package com.epam.gymmanagement.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class TraineeMetrics {
    private final Counter traineeCreatedCounter;
    private final Counter traineeUpdatedCounter;
    private final Counter traineeDeletedCounter;
    private final Counter traineeActivatedCounter;
    private final Counter traineeDeactivatedCounter;
    private final Counter traineeTrainerAssignmentUpdatedCounter;

    public TraineeMetrics(MeterRegistry meterRegistry) {
        this.traineeCreatedCounter = Counter.builder("trainees_created_total")
                .description("Total number of created trainees")
                .register(meterRegistry);

        this.traineeUpdatedCounter = Counter.builder("trainees_updated_total")
                .description("Total number of updated trainee profiles")
                .register(meterRegistry);

        this.traineeDeletedCounter = Counter.builder("trainees_deleted_total")
                .description("Total number of deleted trainee profiles")
                .register(meterRegistry);

        this.traineeActivatedCounter = Counter.builder("trainees_activated_total")
                .description("Total number of activated trainee profiles")
                .register(meterRegistry);

        this.traineeDeactivatedCounter = Counter.builder("trainees_deactivated_total")
                .description("Total number of deactivated trainee profiles")
                .register(meterRegistry);

        this.traineeTrainerAssignmentUpdatedCounter = Counter.builder("trainee_trainer_assignments_updated_total")
                .description("Total number of trainee trainer assignment updates")
                .register(meterRegistry);
    }
}
