package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.Theme;

import java.util.List;

public interface ThemeRepository {
    List<Theme> findAll();
}
