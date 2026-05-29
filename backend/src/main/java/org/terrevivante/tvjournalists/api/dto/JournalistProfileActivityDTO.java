package org.terrevivante.tvjournalists.api.dto;

import java.util.Set;
import java.util.UUID;

public record JournalistProfileActivityDTO(
    UUID id,
    String mediaName,
    String specificEmail,
    String specificPhone,
    Set<ThemeDTO> themes
) {}
