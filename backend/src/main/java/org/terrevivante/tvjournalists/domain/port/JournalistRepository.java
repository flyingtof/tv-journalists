package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface JournalistRepository {
    Journalist save(Journalist journalist);
    Optional<Journalist> findById(UUID id);
    PageResult<Journalist> search(JournalistSearchCriteria criteria, PageRequest pageRequest);
}
