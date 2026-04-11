package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.Media;

import java.util.List;

public interface MediaRepository {
    List<Media> findAll();
}
