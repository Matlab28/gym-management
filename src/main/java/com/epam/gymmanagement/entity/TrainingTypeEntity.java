package com.epam.gymmanagement.entity;

import com.epam.gymmanagement.constant.TrainingType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "training_types")
public class TrainingTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type", nullable = false, unique = true)
    private TrainingType trainingTypeName;
}
