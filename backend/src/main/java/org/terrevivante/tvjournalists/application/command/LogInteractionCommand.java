package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

public record LogInteractionCommand(
    @NotNull
    UUID journalistId,
    UUID activityId,
    @NotNull
    @PastOrPresent
    LocalDate date,
    @NotBlank
    String description,
    UUID createdBy
) {
}
