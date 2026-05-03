package org.terrevivante.tvjournalists.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Journalise un échange significatif avec un journaliste pour conserver l'historique relationnel.
 */
public record InteractionLog(
    UUID id,
    UUID journalistId,
    UUID activityId,
    LocalDate date,
    String description,
    UUID createdBy,
    OffsetDateTime createdAt
) {}
