package org.terrevivante.tvjournalists.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InteractionLog(
    UUID id,
    UUID journalistId,
    UUID activityId,
    LocalDate date,
    String description,
    UUID createdBy,
    OffsetDateTime createdAt
) {}
