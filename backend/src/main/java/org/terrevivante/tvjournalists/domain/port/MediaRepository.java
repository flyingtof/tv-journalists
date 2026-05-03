package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.Media;

import java.util.List;

/**
 * Port de lecture du référentiel des médias disponibles.
 */
public interface MediaRepository {
    List<Media> findAll();
}
