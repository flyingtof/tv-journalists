package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.constraints.NotBlank;

public record CreateThemeCommand(
    @NotBlank
    String name
) {
}
