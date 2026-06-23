package com.epam.gymmanagement.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProfileStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    PENDING("Pending"),
    DELETED("Deleted");

    private final String value;

    ProfileStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ProfileStatus fromValue(String value) {
        for (ProfileStatus status : ProfileStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown profile status: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
