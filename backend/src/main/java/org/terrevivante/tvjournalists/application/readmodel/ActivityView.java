package org.terrevivante.tvjournalists.application.readmodel;

import java.util.List;
import java.util.UUID;

public record ActivityView(
    UUID id,
    MediaView media,
    String role,
    String specificEmail,
    String specificPhone,
    List<ThemeView> themes
) {
    public ActivityView {
        themes = themes == null ? List.of() : List.copyOf(themes);
    }
}

