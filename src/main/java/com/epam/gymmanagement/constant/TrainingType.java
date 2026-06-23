package com.epam.gymmanagement.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TrainingType {
    FITNESS("Fitness"),
    YOGA("Yoga"),
    ZUMBA("Zumba"),
    STRETCHING("Stretching"),
    RESISTANCE("Resistance"),
    SET_LATER("Set Later");

    private final String value;

    TrainingType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TrainingType fromValue(String value) {
        for (TrainingType type : TrainingType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown training type: " + value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
