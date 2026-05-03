package org.terrevivante.tvjournalists.api.dto;

import java.util.List;
import java.util.UUID;

public record JournalistListItemDTO(
    UUID id,
    String firstName,
    String lastName,
    String globalEmail,
    List<String> mediaNames
) {}

