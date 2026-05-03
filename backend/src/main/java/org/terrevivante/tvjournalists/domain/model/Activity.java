package org.terrevivante.tvjournalists.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Décrit un contexte d'exercice d'un journaliste dans un média donné.
 *
 * <p>Une activité porte des coordonnées potentiellement spécifiques et les thèmes couverts.
 */
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
