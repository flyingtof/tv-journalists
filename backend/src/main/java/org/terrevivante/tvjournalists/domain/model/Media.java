package org.terrevivante.tvjournalists.domain.model;

import java.util.UUID;

public record Media(
    UUID id,
    String name,
    MediaType type,
    String url
) {}
