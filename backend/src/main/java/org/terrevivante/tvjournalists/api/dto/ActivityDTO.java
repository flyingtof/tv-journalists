package org.terrevivante.tvjournalists.api.dto;

import java.util.Set;
import java.util.UUID;

public record ActivityDTO(
        UUID id,
        UUID mediaId,
        String mediaName,
        String role,
        String specificEmail,
        String specificPhone,
        Set<ThemeDTO> themes
) {}
