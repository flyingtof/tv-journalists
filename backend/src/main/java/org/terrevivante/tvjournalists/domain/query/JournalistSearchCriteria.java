package org.terrevivante.tvjournalists.domain.query;

import java.util.List;

public record JournalistSearchCriteria(
    String name,
    List<String> media,
    List<String> themes
) {
    public JournalistSearchCriteria {
        media = media == null ? List.of() : List.copyOf(media);
        themes = themes == null ? List.of() : List.copyOf(themes);
    }
}
