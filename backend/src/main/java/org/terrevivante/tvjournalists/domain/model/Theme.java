package org.terrevivante.tvjournalists.domain.model;

import java.util.UUID;

/**
 * Catégorie éditoriale utilisée pour qualifier une activité journalistique.
 */
public record Theme(
    UUID id,
    String name
) {}
