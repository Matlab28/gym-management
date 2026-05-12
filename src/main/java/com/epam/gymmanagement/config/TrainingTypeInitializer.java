package com.epam.gymmanagement.config;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.entity.TrainingTypeEntity;
import com.epam.gymmanagement.repository.TrainingTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainingTypeInitializer implements ApplicationRunner {
    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (TrainingType trainingType : TrainingType.values()) {
            if (trainingTypeRepository.existsByTrainingTypeName(trainingType)) {
                continue;
            }

            TrainingTypeEntity entity = new TrainingTypeEntity();
            entity.setTrainingTypeName(trainingType);
            trainingTypeRepository.save(entity);
            log.info("Initialized training type={}", trainingType);
        }
    }
}
