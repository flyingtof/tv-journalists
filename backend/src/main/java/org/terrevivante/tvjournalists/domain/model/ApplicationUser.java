package org.terrevivante.tvjournalists.domain.model;

import java.util.Set;
import java.util.UUID;

/**
 * Compte interne pouvant accéder à l'application, avec ses rôles d'autorisation.
 */
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
