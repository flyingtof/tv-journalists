package org.terrevivante.tvjournalists.domain.model;

import java.util.UUID;

/**
 * Référence un canal de diffusion (radio, TV, presse, etc.) auquel une activité est rattachée.
 */
public record Media(
    UUID id,
    String name,
    MediaType type,
    String url
) {}
