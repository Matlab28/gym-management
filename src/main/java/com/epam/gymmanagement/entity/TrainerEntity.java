package com.epam.gymmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trainers")
public class TrainerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingTypeEntity specialization;

    @ManyToMany(mappedBy = "trainers")
    @Builder.Default
    private List<TraineeEntity> trainees = new ArrayList<>();

    @OneToMany(
            mappedBy = "trainer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TrainingEntity> trainings = new ArrayList<>();
}
