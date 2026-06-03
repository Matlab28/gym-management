package com.epam.gymmanagement.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    private static final String HEALTH_DETAIL_KEY = "database";
    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                return Health.up()
                        .withDetail(HEALTH_DETAIL_KEY, "Available")
                        .build();
            }
            return Health.down()
                    .withDetail(HEALTH_DETAIL_KEY, "Not valid")
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail(HEALTH_DETAIL_KEY, "Unavailable")
                    .build();
        }
    }
}
