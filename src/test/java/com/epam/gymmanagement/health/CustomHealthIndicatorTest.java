package com.epam.gymmanagement.health;

import com.epam.gymmanagement.repository.TraineeRepository;
import com.epam.gymmanagement.repository.TrainerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Health;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomHealthIndicatorTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;

    @Test
    void databaseHealthIsUpWhenConnectionIsValid() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);

        Health health = new DatabaseHealthIndicator(dataSource).health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals("Available", health.getDetails().get("database"));
    }

    @Test
    void databaseHealthIsDownWhenConnectionIsInvalid() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(false);

        Health health = new DatabaseHealthIndicator(dataSource).health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Not valid", health.getDetails().get("database"));
    }

    @Test
    void databaseHealthIsDownWhenConnectionFails() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("database unavailable"));

        Health health = new DatabaseHealthIndicator(dataSource).health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Unavailable", health.getDetails().get("database"));
    }

    @Test
    void traineeHealthIncludesRepositoryCount() {
        when(traineeRepository.count()).thenReturn(3L);

        Health health = new TraineeHealthIndicator(traineeRepository).health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals("Available", health.getDetails().get("traineeService"));
        assertEquals(3L, health.getDetails().get("traineeCount"));
    }

    @Test
    void traineeHealthIsDownWhenRepositoryFails() {
        when(traineeRepository.count()).thenThrow(new RuntimeException("repository unavailable"));

        Health health = new TraineeHealthIndicator(traineeRepository).health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Unavailable", health.getDetails().get("traineeService"));
    }

    @Test
    void trainerHealthIncludesRepositoryCount() {
        when(trainerRepository.count()).thenReturn(2L);

        Health health = new TrainerHealthIndicator(trainerRepository).health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals("Available", health.getDetails().get("trainerService"));
        assertEquals(2L, health.getDetails().get("trainerCount"));
    }

    @Test
    void trainerHealthIsDownWhenRepositoryFails() {
        when(trainerRepository.count()).thenThrow(new RuntimeException("repository unavailable"));

        Health health = new TrainerHealthIndicator(trainerRepository).health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Unavailable", health.getDetails().get("trainerService"));
    }
}
