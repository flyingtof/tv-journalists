package org.terrevivante.tvjournalists.api.dto;

import java.util.List;

public record CurrentUserDTO(
    String username,
    String firstName,
    String lastName,
    List<String> roles
) {
}
