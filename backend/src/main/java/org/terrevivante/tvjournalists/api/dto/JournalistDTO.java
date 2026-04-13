package org.terrevivante.tvjournalists.api.dto;

import java.util.List;
import java.util.UUID;

public record JournalistDTO(
        UUID id,
        String firstName,
        String lastName,
        String globalEmail,
        String globalPhone,
        List<ActivityDTO> activities
) {}
