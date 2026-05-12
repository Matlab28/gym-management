package com.epam.gymmanagement.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    ADMIN("Admin"),
    TRAINEE("Trainee"),
    TRAINER("Trainer");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown user role: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
