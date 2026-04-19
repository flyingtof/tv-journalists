package org.terrevivante.tvjournalists.domain.model;

import java.util.Set;
import java.util.UUID;

public record ApplicationUser(
    UUID id,
    String username,
    String passwordHash,
    String firstName,
    String lastName,
    boolean enabled,
    Set<Role> roles
) {
    public ApplicationUser {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
