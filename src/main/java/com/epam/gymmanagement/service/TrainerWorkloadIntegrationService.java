package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.constant.WorkloadActionType;
import com.epam.gymmanagement.dto.request.TrainerWorkloadRequestDTO;
import com.epam.gymmanagement.entity.TrainingEntity;
import org.slf4j.MDC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadIntegrationService {

    private final JmsTemplate jmsTemplate;

    @Value("${app.messaging.workload-queue}")
    private String workloadQueue;

    public void updateWorkload(TrainingEntity training, WorkloadActionType actionType) {
        var trainerUser = training.getTrainer().getUserEntity();
        TrainerWorkloadRequestDTO request = new TrainerWorkloadRequestDTO(
                UUID.randomUUID(),
                trainerUser.getUsername(),
                trainerUser.getFirstName(),
                trainerUser.getLastName(),
                trainerUser.getProfileStatus() == ProfileStatus.ACTIVE,
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );

        try {
            jmsTemplate.convertAndSend(workloadQueue, request, message -> {
                message.setJMSCorrelationID(MDC.get("transactionId"));
                message.setStringProperty("eventType", "TRAINER_WORKLOAD_CHANGED");
                return message;
            });
            log.info(
                    "Published workload event: eventId={}, trainerUsername={}, actionType={}, trainingDate={}, duration={}",
                    request.getEventId(),
                    trainerUser.getUsername(),
                    actionType,
                    training.getTrainingDate(),
                    training.getTrainingDuration()
            );
        } catch (JmsException exception) {
            log.error(
                    "Failed to publish workload event: eventId={}, trainerUsername={}, actionType={}, errorType={}",
                    request.getEventId(),
                    trainerUser.getUsername(),
                    actionType,
                    exception.getClass().getSimpleName()
            );
            throw exception;
        }
    }
}
