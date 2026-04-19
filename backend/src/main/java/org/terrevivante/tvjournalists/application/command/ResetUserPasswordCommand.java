package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResetUserPasswordCommand(
    @NotNull
    UUID id,
    @NotBlank
    @Size(min = 8)
    String password
) {
}
