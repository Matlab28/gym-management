package com.epam.gymmanagement.bdd;

import com.epam.gymmanagement.config.MessagingConfig;
import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.constant.TrainingType;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.constant.WorkloadActionType;
import com.epam.gymmanagement.dto.request.TrainerWorkloadRequestDTO;
import com.epam.gymmanagement.entity.TrainerEntity;
import com.epam.gymmanagement.entity.TrainingEntity;
import com.epam.gymmanagement.entity.TrainingTypeEntity;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.service.TrainerWorkloadIntegrationService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GymMessagingIntegrationSteps {

    private static final String QUEUE = "trainer.workload.events";

    private BrokerService broker;
    private JmsTemplate jmsTemplate;
    private TrainerWorkloadIntegrationService publisher;
    private TrainingEntity training;
    private TrainerWorkloadRequestDTO receivedEvent;
    private RuntimeException failure;

    @Before("@messaging")
    public void startBroker() throws Exception {
        String brokerName = "gym-cucumber-" + UUID.randomUUID();
        broker = new BrokerService();
        broker.setBrokerName(brokerName);
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.start();
        broker.waitUntilStarted();

        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory("vm://" + brokerName + "?create=false");
        jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setMessageConverter(new MessagingConfig().jacksonJmsMessageConverter());
        jmsTemplate.setReceiveTimeout(2_000);
        publisher = new TrainerWorkloadIntegrationService(jmsTemplate);
        ReflectionTestUtils.setField(publisher, "workloadQueue", QUEUE);
        failure = null;
        receivedEvent = null;
    }

    @After("@messaging")
    public void stopBroker() throws Exception {
        if (broker != null && !broker.isStopped()) {
            broker.stop();
            broker.waitUntilStopped();
        }
    }

    @Given("a completed training for trainer {string}")
    public void completedTraining(String username) {
        UserEntity trainerUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .username(username)
                .firstName("Jane")
                .lastName("Trainer")
                .email(username + "@example.com")
                .password("encoded-password")
                .isActive(true)
                .profileStatus(ProfileStatus.ACTIVE)
                .role(UserRole.TRAINER)
                .build();
        TrainingTypeEntity yoga = TrainingTypeEntity.builder()
                .id(UUID.randomUUID())
                .trainingTypeName(TrainingType.YOGA)
                .build();
        TrainerEntity trainer = TrainerEntity.builder()
                .id(UUID.randomUUID())
                .userEntity(trainerUser)
                .specialization(yoga)
                .build();
        training = TrainingEntity.builder()
                .id(UUID.randomUUID())
                .trainer(trainer)
                .trainingName("Messaging BDD")
                .trainingDate(LocalDate.of(2026, 8, 21))
                .trainingDuration(60)
                .trainingType(yoga)
                .build();
    }

    @When("Gym Management publishes an ADD workload event")
    public void publishWorkloadEvent() {
        publisher.updateWorkload(training, WorkloadActionType.ADD);
        receivedEvent = (TrainerWorkloadRequestDTO) jmsTemplate.receiveAndConvert(QUEUE);
    }

    @When("the message broker becomes unavailable before publishing")
    public void brokerBecomesUnavailable() throws Exception {
        broker.stop();
        broker.waitUntilStopped();
        try {
            publisher.updateWorkload(training, WorkloadActionType.ADD);
        } catch (RuntimeException exception) {
            failure = exception;
        }
    }

    @Then("the Trainer Workload queue contains the JSON-compatible workload event")
    public void queueContainsEvent() {
        assertNotNull(receivedEvent);
        assertNotNull(receivedEvent.getEventId());
        assertEquals("trainer.integration", receivedEvent.getTrainerUsername());
        assertEquals(60, receivedEvent.getTrainingDuration());
        assertEquals(WorkloadActionType.ADD, receivedEvent.getActionType());
    }

    @Then("Gym Management reports a messaging failure")
    public void messagingFailureIsReported() {
        assertInstanceOf(JmsException.class, failure);
    }
}
