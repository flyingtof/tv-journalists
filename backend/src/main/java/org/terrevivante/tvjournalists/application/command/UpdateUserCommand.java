package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.terrevivante.tvjournalists.domain.model.Role;

import java.util.Set;
import java.util.UUID;

public record UpdateUserCommand(
    @NotNull
    UUID id,
    String firstName,
    String lastName,
    @NotNull
    Boolean enabled,
    @NotEmpty
    Set<Role> roles
) {
}
