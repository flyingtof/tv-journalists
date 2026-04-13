package org.terrevivante.tvjournalists.api.dto;

import org.terrevivante.tvjournalists.domain.model.MediaType;

import java.util.UUID;

public record MediaDTO(
        UUID id,
        String name,
        MediaType type,
        String url
) {}
