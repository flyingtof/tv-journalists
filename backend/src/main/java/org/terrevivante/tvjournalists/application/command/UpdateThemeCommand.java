package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateThemeCommand(
    @NotNull
    UUID id,
    @NotBlank
    String name
) {
}
