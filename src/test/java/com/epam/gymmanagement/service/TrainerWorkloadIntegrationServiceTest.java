package com.epam.gymmanagement.service;

import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.WorkloadActionType;
import com.epam.gymmanagement.dto.request.TrainerWorkloadRequestDTO;
import com.epam.gymmanagement.entity.TrainingEntity;
import jakarta.jms.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadIntegrationServiceTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private TrainerWorkloadIntegrationService service;

    @Test
    void updateWorkloadMapsTrainingToJsonEvent() throws Exception {
        ReflectionTestUtils.setField(service, "workloadQueue", "trainer.workload.events");
        TrainingEntity training = ServiceTestFixtures.training(
                "Yoga Basics",
                ServiceTestFixtures.trainee("trainee.user", true),
                ServiceTestFixtures.trainer("trainer.user", true, TrainingType.YOGA),
                TrainingType.YOGA
        );

        service.updateWorkload(training, WorkloadActionType.ADD);

        ArgumentCaptor<TrainerWorkloadRequestDTO> captor =
                ArgumentCaptor.forClass(TrainerWorkloadRequestDTO.class);
        ArgumentCaptor<MessagePostProcessor> processorCaptor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(
                eq("trainer.workload.events"),
                captor.capture(),
                processorCaptor.capture()
        );
        TrainerWorkloadRequestDTO request = captor.getValue();
        assertNotNull(request.getEventId());
        assertEquals("trainer.user", request.getTrainerUsername());
        assertEquals(training.getTrainingDate(), request.getTrainingDate());
        assertEquals(training.getTrainingDuration(), request.getTrainingDuration());
        assertEquals(WorkloadActionType.ADD, request.getActionType());

        Message message = org.mockito.Mockito.mock(Message.class);
        assertEquals(message, processorCaptor.getValue().postProcessMessage(message));
        verify(message).setStringProperty("eventType", "TRAINER_WORKLOAD_CHANGED");
    }
}
