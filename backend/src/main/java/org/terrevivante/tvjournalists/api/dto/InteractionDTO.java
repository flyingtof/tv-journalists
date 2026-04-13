package org.terrevivante.tvjournalists.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record InteractionDTO(
        UUID id,
        LocalDate date,
        String description,
        UUID activityId
) {}
