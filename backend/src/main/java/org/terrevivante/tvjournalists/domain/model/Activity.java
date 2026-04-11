package org.terrevivante.tvjournalists.domain.model;

import java.util.List;
import java.util.UUID;

public record Activity(
    UUID id,
    UUID journalistId,
    Media media,
    String role,
    String specificEmail,
    String specificPhone,
    List<Theme> themes
) {
    public Activity {
        if (media == null) {
            throw new IllegalArgumentException("media must not be null");
        }
        themes = themes == null ? List.of() : List.copyOf(themes);
    }
}
