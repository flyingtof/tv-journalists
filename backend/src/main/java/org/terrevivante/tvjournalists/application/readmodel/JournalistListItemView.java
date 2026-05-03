package org.terrevivante.tvjournalists.application.readmodel;

import java.util.List;
import java.util.UUID;

public record JournalistListItemView(
    UUID id,
    String firstName,
    String lastName,
    String globalEmail,
    List<String> mediaNames
) {
    public JournalistListItemView {
        mediaNames = mediaNames == null ? List.of() : List.copyOf(mediaNames);
    }
}

