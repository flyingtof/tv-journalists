package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

public interface SearchJournalistsUseCase {
    PageResult<Journalist> search(JournalistSearchCriteria criteria, PageRequest pageRequest);
}
