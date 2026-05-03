package org.terrevivante.tvjournalists.api.dto;

import java.util.List;
import java.util.UUID;

public record JournalistProfileDTO(
    UUID id,
    String firstName,
    String lastName,
    String globalEmail,
    String globalPhone,
    List<JournalistProfileActivityDTO> activities
) {}

