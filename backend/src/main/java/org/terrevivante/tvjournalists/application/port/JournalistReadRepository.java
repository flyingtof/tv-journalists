package org.terrevivante.tvjournalists.application.port;

import org.terrevivante.tvjournalists.application.readmodel.JournalistListItemView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistProfileView;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface JournalistReadRepository {
    PageResult<JournalistListItemView> search(JournalistSearchCriteria criteria, PageRequest pageRequest);

    Optional<JournalistProfileView> findProfileById(UUID id);
}

