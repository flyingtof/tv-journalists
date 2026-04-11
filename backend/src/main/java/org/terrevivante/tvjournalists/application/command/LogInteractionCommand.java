package org.terrevivante.tvjournalists.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record LogInteractionCommand(
    UUID journalistId,
    UUID activityId,
    LocalDate date,
    String description,
    UUID createdBy
) {
    public LogInteractionCommand {
        if (journalistId == null) {
            throw new IllegalArgumentException("journalistId must not be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("date must not be in the future");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
    }
}
