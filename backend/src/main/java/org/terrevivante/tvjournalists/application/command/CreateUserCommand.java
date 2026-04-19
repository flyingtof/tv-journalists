package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.terrevivante.tvjournalists.domain.model.Role;

import java.util.Set;

public record CreateUserCommand(
    @NotBlank
    String username,
    @NotBlank
    @Size(min = 8)
    String password,
    String firstName,
    String lastName,
    @NotNull
    Boolean enabled,
    @NotEmpty
    Set<Role> roles
) {
}
