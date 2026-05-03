package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.readmodel.JournalistListItemView;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

public interface SearchJournalistListUseCase {
    PageResult<JournalistListItemView> search(JournalistSearchCriteria criteria, PageRequest pageRequest);
}

