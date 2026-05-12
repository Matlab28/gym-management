package com.epam.gymmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trainings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "trainee_id", nullable = false)
    private TraineeEntity trainee;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerEntity trainer;

    @Column(nullable = false)
    private String trainingName;

    @ManyToOne
    @JoinColumn(name = "training_type_id", nullable = false)
    private TrainingTypeEntity trainingType;

    @Column(nullable = false)
    private LocalDate trainingDate;

    @Column(nullable = false)
    private Integer trainingDuration;
}
