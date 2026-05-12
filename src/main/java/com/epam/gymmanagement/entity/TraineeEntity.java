package com.epam.gymmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trainees")
public class TraineeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate dateOfBirth;
    private String address;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @ManyToMany
    @JoinTable(
            name = "trainee_trainers",
            joinColumns = @JoinColumn(name = "trainee_id"),
            inverseJoinColumns = @JoinColumn(name = "trainer_id")
    )
    @Builder.Default
    private List<TrainerEntity> trainers = new ArrayList<>();

    @OneToMany(
            mappedBy = "trainee",
            cascade = CascadeType.ALL,
            orphanRemoval = true

    )
    @Builder.Default
    private List<TrainingEntity> trainings = new ArrayList<>();
}
