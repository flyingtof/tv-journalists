package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.Theme;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de gestion du référentiel des thèmes éditoriaux.
 */
public interface ThemeRepository {
    List<Theme> findAll();

    Optional<Theme> findById(UUID id);

    Optional<Theme> findByNameIgnoreCase(String name);

    Theme save(Theme theme);

    void deleteById(UUID id);

    boolean isInUse(UUID id);
}
