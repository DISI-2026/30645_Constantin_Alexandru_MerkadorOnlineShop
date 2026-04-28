package org.example.postservice.infrastructure.entity;

import lombok.Getter;

@Getter
public enum Rating {
    POOR(1, "Poor"),
    FAIR(2, "Fair"),
    GOOD(3, "Good"),
    VERY_GOOD(4, "Very Good"),
    EXCELLENT(5, "Excellent");

    private final int value;
    private final String description;

    Rating(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Rating fromValue(int value) {
        for (Rating rating : Rating.values()) {
            if (rating.value == value) {
                return rating;
            }
        }
        throw new IllegalArgumentException("Invalid rating value: " + value);
    }
}

