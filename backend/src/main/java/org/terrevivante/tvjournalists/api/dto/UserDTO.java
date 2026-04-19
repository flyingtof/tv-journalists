package org.terrevivante.tvjournalists.api.dto;

import java.util.List;
import java.util.UUID;

public record UserDTO(
    UUID id,
    String username,
    String firstName,
    String lastName,
    boolean enabled,
    List<String> roles
) {
}
